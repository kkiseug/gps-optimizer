package core;

import core.outlier.Threshold;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class ThresholdTest {

    @Test
    @DisplayName("시속(km/h)이 초속(m/s)으로 정확히 변환되어야 한다")
    void kmhToMpsConversion() {
        // 36 km/h = 10 m/s
        Threshold threshold = Threshold.ofKmPerHour(36.0);
        
        assertThat(threshold.type()).isEqualTo(Threshold.Type.VELOCITY_MPS);
        assertThat(threshold.value()).isCloseTo(10.0, offset(0.0001));
    }

    @Test
    @DisplayName("거리(m) 임계값은 값이 그대로 유지되어야 한다")
    void metersStaysSame() {
        Threshold threshold = Threshold.ofMeters(150.5);
        
        assertThat(threshold.type()).isEqualTo(Threshold.Type.DISTANCE_METERS);
        assertThat(threshold.value()).isEqualTo(150.5);
    }
}
