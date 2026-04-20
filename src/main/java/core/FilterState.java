package core;

public class FilterState {

    private double x;
    private double v;
    private double p;

    private final double q; // 프로세스 노이즈
    private final double r; // 측정 노이즈

    public FilterState(double initialValue, double q, double r) {
        this.x = initialValue;
        this.v = 0;
        this.p = 1.0;
        this.q = q;
        this.r = r;
    }

    public double update(double z, double dt) {
        if (dt <= 0) dt = 0.1;

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
