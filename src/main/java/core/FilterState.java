package core;

public class FilterState {

    private double x;
    private double v;
    private double p;

    private final double q = 0.00001;
    private final double r = 0.001;

    public FilterState(double initialValue) {
        this.x = initialValue;
        this.v = 0;
        this.p = 1.0;
    }

    public double update(double z, double dt) {
        if (dt <= 0) dt = 0.1; // 최소 시간 간격 보장

        // 예측
        double x_pred = x + v * dt;
        double p_pred = p + q;

        // 보정
        double k = p_pred / (p_pred + r);
        x = x_pred + k * (z - x_pred);

        // 업데이트
        v = (x - x_pred) / dt;
        p = (1 - k) * p_pred;

        return x;
    }
}
