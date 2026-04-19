package core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrackCleanerTest {

    private Coordinate coord(double lon, double lat, long epochSecond) {
        return new Coordinate(lon, lat, Instant.ofEpochSecond(epochSecond));
    }

    private List<Coordinate> normalTrack(int size) {
        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            coords.add(coord(127.0276 + i * 0.0001, 37.4979, i * 5L));
        }
        return coords;
    }

    @Test
    @DisplayName("Fluent API를 통해 여러 필터를 적용할 수 있다")
    void fluentApiTest() {
        List<Coordinate> coords = normalTrack(10);
        // 속도 이상치 추가 (index 3)
        coords.set(3, coord(127.1276, 37.4979, 15L));
        // 거리 이상치 추가 (index 7)
        coords.set(7, coord(127.5276, 37.4979, 35L));
        
        GpsTrack rawTrack = new GpsTrack(coords);

        CleaningResult result = TrackCleaner.of(rawTrack)
            .removeOutliers(Threshold.ofKmPerHour(30)) // 속도 기반 (기본 윈도우 5)
            .removeOutliers(Threshold.ofMeters(100), 5) // 거리 기반
            .smooth(Algorithm.KALMAN)
            .simplify(Tolerance.ofMeters(5))
            .clean();

        // 10개 중 2개 제거되어야 함
        assertThat(result.cleanedTrack().size()).isEqualTo(8);
        // 미구현 알고리즘에 대한 경고가 포함되어야 함
        assertThat(result.warnings()).anyMatch(w -> w.message().contains("KALMAN smoothing is not implemented yet."));
        assertThat(result.warnings()).anyMatch(w -> w.message().contains("Simplification with tolerance 5.0m is not implemented yet."));
    }
}
