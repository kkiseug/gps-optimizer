package core;

import infrastructure.LengthOutlierRemover;
import infrastructure.VelocityOutlierRemover;
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

    public TrackCleaner smooth(Algorithm algorithm) {
        // TODO: Implement smoothing algorithms
        filters.add(track -> new CleaningResult(track, List.of(new Warning(algorithm + " smoothing is not implemented yet."))));
        return this;
    }

    public TrackCleaner simplify(Tolerance tolerance) {
        // TODO: Implement simplification algorithms
        filters.add(track -> new CleaningResult(track, List.of(new Warning("Simplification with tolerance " + tolerance.meters() + "m is not implemented yet."))));
        return this;
    }

    public CleaningResult clean() {
        GpsTrack currentTrack = rawTrack;
        List<Warning> allWarnings = new ArrayList<>();

        for (TrackFilter filter : filters) {
            CleaningResult result = filter.filter(currentTrack);
            currentTrack = result.cleanedTrack();
            allWarnings.addAll(result.warnings());
        }

        return new CleaningResult(currentTrack, allWarnings);
    }
}
