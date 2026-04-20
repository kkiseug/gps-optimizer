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
            .removeOutliers(Threshold.ofKmPerHour(30)) // 속도 기반
            .removeOutliers(Threshold.ofMeters(100))    // 거리 기반
            .simplify(Tolerance.ofMeters(1))           // 단순화 (직선 구간 제거)
            .clean();

        // 1. 이상치 제거 확인: 10개 -> 8개
        // 2. 단순화 확인: 직선 구간이 하나로 합쳐져서 8개보다 더 줄어듦
        assertThat(result.cleanedTrack().size()).isLessThan(8);
        assertThat(result.stepReports()).hasSize(3);
    }

    @Test
    @DisplayName("이상치 제거 후 단순화가 순차적으로 적용된다")
    void sequentialProcessingTest() {
        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            coords.add(coord(127.0 + i * 0.0001, 37.0, i * 10L));
        }
        // 인덱스 5번에 강한 이상치 삽입
        coords.set(5, coord(127.1, 37.1, 50L));

        GpsTrack rawTrack = new GpsTrack(coords);

        CleaningResult result = TrackCleaner.of(rawTrack)
            .removeOutliers(Threshold.ofMeters(500)) // 이상치 제거 (5번 제거됨)
            .simplify(Tolerance.ofMeters(1))        // 단순화 (남은 직선들 제거)
            .clean();

        // 이상치(5번) 제거 후 남은 9개는 직선상이므로 양 끝점 2개만 남아야 함
        assertThat(result.cleanedTrack().size()).isEqualTo(2);
        assertThat(result.stepReports().get(0).removedCount()).isEqualTo(1);
    }
}
