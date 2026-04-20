package infrastructure.filter;

import core.common.CleaningResult;
import core.common.Coordinate;
import core.common.GpsTrack;
import core.common.StepReport;
import core.common.Warning;
import core.filter.FilterState;
import core.filter.TrackFilter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class KalmanFilter implements TrackFilter {

    private final double q;
    private final double r;

    public KalmanFilter(double q, double r) {
        this.q = q;
        this.r = r;
    }

    @Override
    public CleaningResult filter(GpsTrack track) {
        List<Coordinate> coords = track.getCoordinates();
        if (coords.size() < 3) {
            return new CleaningResult(track, List.of(), List.of(new StepReport("KalmanFilter", track.size(), track.size(), 0)));
        }

        FilterState latFilter = new FilterState(coords.getFirst().latitude(), q, r);
        FilterState lonFilter = new FilterState(coords.getFirst().longitude(), q, r);

        List<Coordinate> result = new ArrayList<>();
        result.add(coords.getFirst()); // 시작점 유지

        for (int i = 1; i < coords.size() - 1; i++) {
            Coordinate current = coords.get(i);
            Coordinate prev = coords.get(i - 1);
            double dt = 0.5;
            if (track.hasTimestamps()) {
                dt = Duration.between(prev.timestamp(), current.timestamp()).toMillis() / 1000.0;
            }

            double smoothedLat = latFilter.update(current.latitude(), dt);
            double smoothedLon = lonFilter.update(current.longitude(), dt);

            result.add(new Coordinate(smoothedLon, smoothedLat, current.timestamp()));
        }

        result.add(coords.getLast()); // 끝점 유지

        StepReport report = StepReport.ofModification("KalmanFilter", track.size(), result.size() - 2);
        List<Warning> warnings = new ArrayList<>();
        if (!track.hasTimestamps()) {
            warnings.add(new Warning("Timestamp가 존재하지 않아 KalmanFilter 정확도가 떨어질 수 있습니다."));
        }
        return new CleaningResult(new GpsTrack(result), warnings, List.of(report));
    }
}
