package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.RemoveResult;
import core.Warning;
import java.util.ArrayList;
import java.util.List;

public class LengthOutlierRemover extends AbstractOutlierRemover {

    private final static int DISTANCE_THRESHOLD = 100;

    @Override
    protected RemoveResult removeCoordinates(GpsTrack gpsTrack) {
        List<Coordinate> cleaned = new ArrayList<>();
        List<Coordinate> removed = new ArrayList<>();

        for (int idx = 0; idx < gpsTrack.size(); idx++) {
            List<Coordinate> window = gpsTrack.window(idx, 5);
            Coordinate target = gpsTrack.get(idx);

            List<Coordinate> others = window.stream()
                .filter(c -> !c.equals(target))  // 판단 대상 제외
                .toList();

            Coordinate medianCoordinate = getMedianCoordinate(others);

            double distance = target.distanceFrom(medianCoordinate);

            if (distance > DISTANCE_THRESHOLD) {
                removed.add(target);
            } else {
                cleaned.add(target);
            }
        }

        List<Warning> warnings = generateWarnings(gpsTrack, removed);

        return new RemoveResult(
            new GpsTrack(cleaned),
            removed,
            warnings
        );
    }
}
