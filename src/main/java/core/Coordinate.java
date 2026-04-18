package core;

import exception.CannotCalculateVelocityException;
import exception.InvalidCoordinateException;
import java.time.Duration;
import java.time.Instant;

public record Coordinate(
    double longitude,
    double latitude,
    Instant timestamp
) {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    public Coordinate {
        if (longitude < -180 || longitude > 180) {
            throw new InvalidCoordinateException("경도는 -180 ~ 180 사이의 실수 값이어야 합니다.");
        }
        if (latitude < -90 || latitude > 90) {
            throw new InvalidCoordinateException("위도는 -90 ~ 90 사이의 실수 값이어야 합니다.");
        }
    }

    public double distanceFrom(Coordinate other) {
        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(other.latitude);
        double lon2 = Math.toRadians(other.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2)
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    public double velocityFrom(Coordinate other) {
        if (!hasTimestamp() || !other.hasTimestamp()) {
            throw new CannotCalculateVelocityException("타임스탬프가 없습니다.");
        }

        double meterDistance = distanceFrom(other);
        Duration between = Duration.between(this.timestamp, other.timestamp).abs();

        validateBetweenTimestampIsNotZero(between);

        double seconds = between.toMillis() / 1000.0;
        return meterDistance / seconds; // m/s
    }

    private void validateBetweenTimestampIsNotZero(Duration between) {
        if (between.isZero()) {
            throw new CannotCalculateVelocityException("두 좌표의 타임스탬프가 동일합니다.");
        }
    }

    public boolean hasTimestamp() {
        return timestamp != null;
    }
}
