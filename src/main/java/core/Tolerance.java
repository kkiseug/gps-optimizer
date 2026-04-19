package core;

public record Tolerance(
    double meters
) {
    public static Tolerance ofMeters(double meters) {
        return new Tolerance(meters);
    }
}
