package core.outlier;

public record Threshold(
    Type type,
    double value
) {
    public enum Type {
        VELOCITY_MPS,
        DISTANCE_METERS
    }

    public static Threshold ofKmPerHour(double kmh) {
        return new Threshold(Type.VELOCITY_MPS, kmh / 3.6);
    }

    public static Threshold ofMeters(double meters) {
        return new Threshold(Type.DISTANCE_METERS, meters);
    }
}
