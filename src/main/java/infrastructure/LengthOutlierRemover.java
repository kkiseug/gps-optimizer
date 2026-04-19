package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.RemoveResult;
import core.Warning;
import java.util.ArrayList;
import java.util.List;

public class LengthOutlierRemover extends AbstractOutlierRemover {

    private final double distanceThreshold;
    private final int windowSize;

    public LengthOutlierRemover(double distanceThreshold, int windowSize) {
        super(10);
        this.distanceThreshold = distanceThreshold;
        this.windowSize = windowSize;
    }

    @Override
    protected RemoveResult removeCoordinates(GpsTrack gpsTrack) {
        List<Coordinate> cleaned = new ArrayList<>();
        List<Coordinate> removed = new ArrayList<>();

        for (int idx = 0; idx < gpsTrack.size(); idx++) {
            List<Coordinate> window = gpsTrack.window(idx, windowSize);
            Coordinate target = gpsTrack.get(idx);

            // 윈도우 전체(target 포함)를 사용하여 중앙값 계산 (Velocity와 동일하게 일관성 유지)
            Coordinate medianCoordinate = getMedianCoordinate(window);

            double distance = target.distanceFrom(medianCoordinate);

            if (distance > distanceThreshold) {
                removed.add(target);
            } else {
                cleaned.add(target);
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
