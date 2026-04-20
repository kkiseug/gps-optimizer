package core;

import java.util.List;

public interface Simplifier extends TrackFilter {

    SimplifyResult simplify(GpsTrack gpsTrack);

    @Override
    default CleaningResult filter(GpsTrack track) {
        SimplifyResult result = simplify(track);
        StepReport report = StepReport.ofRemoval(
            this.getClass().getSimpleName(),
            track.size(),
            result.simplified().size()
        );
        return new CleaningResult(result.simplified(), List.of(), List.of(report));
    }
}
