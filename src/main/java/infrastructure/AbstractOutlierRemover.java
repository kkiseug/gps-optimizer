package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.OutlierRemover;
import core.RemoveResult;
import core.Warning;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

    protected Coordinate getMedianCoordinate(List<Coordinate> coordinates) {
        int size = coordinates.size();
        double[] lats = new double[size];
        double[] lons = new double[size];

        for (int i = 0; i < size; i++) {
            Coordinate c = coordinates.get(i);
            lats[i] = c.latitude();
            lons[i] = c.longitude();
        }

        Arrays.sort(lats);
        Arrays.sort(lons);

        double medianLat = median(lats);
        double medianLon = median(lons);

        return new Coordinate(medianLon, medianLat, null);
    }

    protected Instant getMedianTimestamp(List<Coordinate> coordinates) {
        int size = coordinates.size();
        long[] timestamps = new long[size];

        for (int i = 0; i < size; i++) {
            timestamps[i] = coordinates.get(i).timestamp().toEpochMilli();
        }

        Arrays.sort(timestamps);
        return Instant.ofEpochMilli(medianLong(timestamps));
    }

    protected double median(double[] values) {
        int size = values.length;
        int mid = size / 2;

        if (size % 2 == 1) {
            return values[mid];
        }
        return (values[mid - 1] + values[mid]) / 2.0;
    }

    protected long medianLong(long[] values) {
        int size = values.length;
        int mid = size / 2;

        if (size % 2 == 1) {
            return values[mid];
        }
        return (values[mid - 1] + values[mid]) / 2;
    }

    protected abstract RemoveResult removeCoordinates(GpsTrack gpsTrack);
}
