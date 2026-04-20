package core.filter;

import core.common.CleaningResult;
import core.common.GpsTrack;

public interface TrackFilter {
    CleaningResult filter(GpsTrack track);
}
