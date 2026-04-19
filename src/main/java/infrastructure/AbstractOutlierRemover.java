package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.OutlierRemover;
import core.RemoveResult;
import core.Warning;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOutlierRemover implements OutlierRemover {

    protected final int minimumPoints;

    protected AbstractOutlierRemover(int minimumPoints) {
        this.minimumPoints = minimumPoints;
    }

    @Override
    public RemoveResult remove(GpsTrack gpsTrack) {
        if (gpsTrack.size() < minimumPoints) {
            return new RemoveResult(
                gpsTrack,
                List.of(),
                List.of(new Warning("제거하기 위한 최소 개수 (" + minimumPoints + "개)를 만족하지 않습니다."))
            );
        }

        return removeCoordinates(gpsTrack);
    }

    protected List<Warning> generateWarnings(GpsTrack gpsTrack, List<Coordinate> removed) {
        List<Warning> warnings = new ArrayList<>();
        double removalRate = (double) removed.size() / gpsTrack.size();
        if (removalRate > 0.3) {
            warnings.add(new Warning("제거율이 30%를 초과했습니다. 임계값 재검토 권장"));
        }
        return warnings;
    }

    protected Coordinate getMedianCoordinate(List<Coordinate> others) {
        List<Double> latitudes = others.stream()
            .map(Coordinate::latitude)
            .sorted()
            .toList();

        List<Double> longitudes = others.stream()
            .map(Coordinate::longitude)
            .sorted()
            .toList();

        double medianLat = median(latitudes);
        double medianLon = median(longitudes);

        return new Coordinate(medianLon, medianLat, null);
    }

    protected double median(List<Double> values) {
        int mid = values.size() / 2;

        if (values.size() % 2 == 1) {
            return values.get(mid);
        }
        return (values.get(mid - 1) + values.get(mid)) / 2.0;
    }

    protected long medianLong(List<Long> values) {
        int mid = values.size() / 2;

        if (values.size() % 2 == 1) {
            return values.get(mid);
        }
        return (values.get(mid - 1) + values.get(mid)) / 2;
    }

    protected abstract RemoveResult removeCoordinates(GpsTrack gpsTrack);
}
