package core;

import java.util.List;

public interface Simplifier extends TrackFilter {

    SimplifyResult simplify(GpsTrack gpsTrack);

    @Override
    default CleaningResult filter(GpsTrack track) {
        SimplifyResult result = simplify(track);
        return new CleaningResult(result.simplified(), List.of());
    }
}
