package core;

import java.util.List;

public interface OutlierRemover extends TrackFilter {
    RemoveResult remove(GpsTrack gpsTrack);

    @Override
    default CleaningResult filter(GpsTrack track) {
        RemoveResult result = remove(track);
        StepReport report = StepReport.ofRemoval(
            this.getClass().getSimpleName(),
            track.size(),
            result.cleanedTrack().size()
        );
        return new CleaningResult(result.cleanedTrack(), result.warnings(), List.of(report));
    }
}
