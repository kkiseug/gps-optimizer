package core.outlier;

import core.common.CleaningResult;
import core.common.GpsTrack;
import core.common.StepReport;
import core.filter.TrackFilter;
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
