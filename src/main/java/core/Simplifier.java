package core;

import java.util.List;

public interface Simplifier extends TrackFilter {

    SimplifyResult simplify(GpsTrack gpsTrack);

    @Override
    default CleaningResult filter(GpsTrack track) {
        SimplifyResult result = simplify(track);
        StepReport report = new StepReport(
            this.getClass().getSimpleName(),
            track.size(),
            result.simplified().size(),
            0
        );
        return new CleaningResult(result.simplified(), List.of(), List.of(report));
    }
}
