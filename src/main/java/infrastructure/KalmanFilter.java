package infrastructure;

import core.CleaningResult;
import core.Coordinate;
import core.FilterState;
import core.GpsTrack;
import core.StepReport;
import core.TrackFilter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class KalmanFilter implements TrackFilter {

    // Q: 프로세스 노이즈 (사람의 움직임이 얼마나 변칙적인가? 작을수록 경로가 직선에 가까워짐)
    private final double q = 0.00001;
    // R: 측정 노이즈 (GPS 오차가 어느 정도인가? 클수록 GPS 신호를 덜 믿고 부드러워짐)
    private final double r = 0.001;

    @Override
    public CleaningResult filter(GpsTrack track) {
        List<Coordinate> coords = track.getCoordinates();

        FilterState latFilter = new FilterState(coords.getFirst().latitude());
        FilterState lonFilter = new FilterState(coords.getFirst().longitude());

        List<Coordinate> result = new ArrayList<>();
        result.add(coords.getFirst());

        int smoothed = 0;
        for (int i = 1; i < coords.size() - 1; i++) {
            Coordinate current = coords.get(i);
            Coordinate prev = result.get(i - 1);
            double dt = Duration.between(prev.timestamp(), current.timestamp()).toMillis() / 1000.0;

            double smoothedLat = latFilter.update(current.latitude(), dt);
            double smoothedLon = lonFilter.update(current.longitude(), dt);

            Coordinate smoothedCoord = new Coordinate(smoothedLon, smoothedLat, current.timestamp());
            if (smoothedCoord.equals(current)) smoothed++;
            result.add(smoothedCoord);
        }

        result.add(coords.getLast());

        int modifiedCount = result.size() - 2;
        return new CleaningResult(
            new GpsTrack(result),
            List.of(),
            List.of(new StepReport("KalmanFilter", track.size(), result.size(), modifiedCount))
        );
    }
}
