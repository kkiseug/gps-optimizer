package core;

public interface OutlierRemover extends TrackFilter {
    RemoveResult remove(GpsTrack gpsTrack);

    @Override
    default CleaningResult filter(GpsTrack track) {
        RemoveResult result = remove(track);
        return new CleaningResult(result.cleanedTrack(), result.warnings());
    }
}
