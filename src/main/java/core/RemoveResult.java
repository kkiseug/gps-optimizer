package core;

import java.util.List;

public record RemoveResult(
    GpsTrack cleanedTrack,
    List<Coordinate> removedPoints,
    List<Warning> warnings
) {

    public double removalRate() {
        return (double) removedPoints.size() / (cleanedTrack.size() + removedPoints.size());
    }
}
