package core;

import java.util.List;

public record CleaningResult(
    GpsTrack cleanedTrack,
    List<Warning> warnings
) {
}
