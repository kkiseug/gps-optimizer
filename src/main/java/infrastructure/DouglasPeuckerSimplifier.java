package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.Simplifier;
import core.SimplifyResult;
import core.Tolerance;
import java.util.ArrayList;
import java.util.List;

public class DouglasPeuckerSimplifier implements Simplifier {

    private final Tolerance tolerance;

    public DouglasPeuckerSimplifier(Tolerance tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public SimplifyResult simplify(GpsTrack gpsTrack) {
        if (gpsTrack.size() <= 2) {
            return new SimplifyResult(gpsTrack);
        }

        boolean[] kept = new boolean[gpsTrack.size()];
        kept[0] = true;
        kept[gpsTrack.size() - 1] = true;

        simplifyRecursive(gpsTrack, 0, gpsTrack.size() - 1, tolerance.meters(), kept);

        List<Coordinate> simplified = new ArrayList<>();
        for (int i = 0; i < gpsTrack.size(); i++) {
            if (kept[i]) {
                simplified.add(gpsTrack.get(i));
            }
        }

        return new SimplifyResult(new GpsTrack(simplified));
    }

    private void simplifyRecursive(GpsTrack gpsTrack, int first, int last, double tolerance, boolean[] kept) {
        if (first + 1 >= last) {
            return;
        }

        double maxDistance = -1.0;
        int maxIndex = -1;

        Coordinate start = gpsTrack.get(first);
        Coordinate end = gpsTrack.get(last);

        for (int i = first + 1; i < last; i++) {
            Coordinate target = gpsTrack.get(i);
            Coordinate closest = closestCoordinateFrom(start, end, target);
            double distance = closest.distanceFrom(target);

            if (distance > maxDistance) {
                maxDistance = distance;
                maxIndex = i;
            }
        }

        if (maxDistance > tolerance) {
            kept[maxIndex] = true;
            simplifyRecursive(gpsTrack, first, maxIndex, tolerance, kept);
            simplifyRecursive(gpsTrack, maxIndex, last, tolerance, kept);
        }
    }

    public Coordinate closestCoordinateFrom(Coordinate start, Coordinate end, Coordinate target) {
        double projectionRatio = projectionRatioBetween(start, end, target);
        if (projectionRatio <= 0) return start;
        if (projectionRatio >= 1) return end;

        double lon = start.longitude() + (end.longitude() - start.longitude()) * projectionRatio;
        double lat = start.latitude() + (end.latitude() - start.latitude()) * projectionRatio;

        return new Coordinate(lon, lat, start.timestamp());
    }

    public double projectionRatioBetween(Coordinate start, Coordinate end, Coordinate target) {
        double dx = end.longitude() - start.longitude();
        double dy = end.latitude() - start.latitude();
        if (dx == 0 && dy == 0) return 0;

        return ((target.longitude() - start.longitude()) * dx + (target.latitude() - start.latitude()) * dy)
            / (dx * dx + dy * dy);
    }
}
