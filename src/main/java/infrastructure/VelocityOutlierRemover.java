package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.RemoveResult;
import core.Warning;
import exception.CannotCalculateVelocityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class VelocityOutlierRemover extends AbstractOutlierRemover {

    private final double velocityThreshold;
    private final int windowSize;
    private final static double DISTANCE_THRESHOLD_FOR_ZERO_DT = 100.0;

    public VelocityOutlierRemover(double velocityThreshold, int windowSize) {
        super(10);
        this.velocityThreshold = velocityThreshold;
        this.windowSize = windowSize;
    }

    @Override
    protected RemoveResult removeCoordinates(GpsTrack gpsTrack) {
        List<Coordinate> cleaned = new ArrayList<>();
        List<Coordinate> removed = new ArrayList<>();

        for (int idx = 0; idx < gpsTrack.size(); idx++) {
            List<Coordinate> window = gpsTrack.window(idx, windowSize);
            Coordinate target = gpsTrack.get(idx);

            Coordinate medianCoordinate = getMedianCoordinate(window);

            Instant medianTimestamp = Instant.ofEpochMilli(medianLong(
                window.stream()
                    .map(c -> c.timestamp().toEpochMilli())
                    .sorted()
                    .toList()
            ));

            try {
                double velocity = target.velocityFrom(
                    new Coordinate(medianCoordinate.longitude(), medianCoordinate.latitude(), medianTimestamp)
                );

                if (velocity > velocityThreshold) {
                    removed.add(target);
                } else {
                    cleaned.add(target);
                }
            } catch (CannotCalculateVelocityException e) {
                double distance = target.distanceFrom(medianCoordinate);
                if (distance > DISTANCE_THRESHOLD_FOR_ZERO_DT) {
                    removed.add(target);
                } else {
                    cleaned.add(target);
                }
            }
        }

        if (cleaned.size() < GpsTrack.MINIMUM_COORD_SIZE) {
            return new RemoveResult(gpsTrack, List.of(), List.of(new Warning("제거 후 남은 좌표가 너무 적어 제거를 수행하지 않았습니다.")));
        }

        List<Warning> warnings = generateWarnings(gpsTrack, removed);

        return new RemoveResult(
            new GpsTrack(cleaned),
            removed,
            warnings
        );
    }
}
