package core;

import infrastructure.DouglasPeuckerSimplifier;
import infrastructure.LengthOutlierRemover;
import infrastructure.VelocityOutlierRemover;
import infrastructure.KalmanFilter;
import java.util.ArrayList;
import java.util.List;

public class TrackCleaner {

    private final GpsTrack rawTrack;
    private final List<TrackFilter> filters = new ArrayList<>();
    private static final int DEFAULT_WINDOW_SIZE = 5;

    private TrackCleaner(GpsTrack rawTrack) {
        this.rawTrack = rawTrack;
    }

    public static TrackCleaner of(GpsTrack rawTrack) {
        return new TrackCleaner(rawTrack);
    }

    public TrackCleaner removeOutliers(Threshold threshold) {
        return removeOutliers(threshold, DEFAULT_WINDOW_SIZE);
    }

    public TrackCleaner removeOutliers(Threshold threshold, int windowSize) {
        if (threshold.type() == Threshold.Type.VELOCITY_MPS) {
            filters.add(new VelocityOutlierRemover(threshold.value(), windowSize));
        } else if (threshold.type() == Threshold.Type.DISTANCE_METERS) {
            filters.add(new LengthOutlierRemover(threshold.value(), windowSize));
        }
        return this;
    }

    /**
     * 기본 설정으로 칼만 필터 적용 (Q=0.00001, R=0.001)
     */
    public TrackCleaner smooth(Algorithm algorithm) {
        if (algorithm == Algorithm.KALMAN) {
            return smooth(algorithm, 0.00001, 0.001);
        }
        return this;
    }

    /**
     * 상세 설정으로 칼만 필터 적용
     * @param q 프로세스 노이즈 (작을수록 더 부드러워지지만 반응이 늦어짐)
     * @param r 측정 노이즈 (클수록 GPS 신호를 덜 믿고 부드러워짐)
     */
    public TrackCleaner smooth(Algorithm algorithm, double q, double r) {
        if (algorithm == Algorithm.KALMAN) {
            filters.add(new KalmanFilter(q, r));
        }
        return this;
    }

    public TrackCleaner simplify(Tolerance tolerance) {
        filters.add(new DouglasPeuckerSimplifier(tolerance));
        return this;
    }

    public CleaningResult clean() {
        GpsTrack currentTrack = rawTrack;
        List<Warning> allWarnings = new ArrayList<>();
        List<StepReport> allReports = new ArrayList<>();

        for (TrackFilter filter : filters) {
            CleaningResult result = filter.filter(currentTrack);
            currentTrack = result.cleanedTrack();
            allWarnings.addAll(result.warnings());
            allReports.addAll(result.stepReports());
        }

        return new CleaningResult(currentTrack, allWarnings, allReports);
    }
}
