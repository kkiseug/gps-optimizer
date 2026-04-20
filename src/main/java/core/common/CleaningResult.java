package core.common;

import java.util.List;

public record CleaningResult(
    GpsTrack cleanedTrack,
    List<Warning> warnings,
    List<StepReport> stepReports
) {
    public int totalRemovedCount() {
        return stepReports.stream()
            .mapToInt(StepReport::removedCount)
            .sum();
    }
}
