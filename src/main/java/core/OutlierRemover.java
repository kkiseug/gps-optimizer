package core;

import java.util.List;

public interface OutlierRemover extends TrackFilter {
    RemoveResult remove(GpsTrack gpsTrack);

    @Override
    default CleaningResult filter(GpsTrack track) {
        RemoveResult result = remove(track);
        StepReport report = new StepReport(
            this.getClass().getSimpleName(),
            track.size(),
            result.cleanedTrack().size(),
            0
        );
        return new CleaningResult(result.cleanedTrack(), result.warnings(), List.of(report));
    }
}
