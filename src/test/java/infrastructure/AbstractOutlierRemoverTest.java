package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.RemoveResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AbstractOutlierRemoverTest {

    // 최소 포인트, 제거율 경고는 공통 로직이라 여기서 테스트
    private final VelocityOutlierRemover remover = new VelocityOutlierRemover(8.3, 5);

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

    @Nested
    @DisplayName("최소 포인트 수 검증")
    class MinimumPoints {

        @Test
        @DisplayName("포인트 수가 10개 미만이면 원본 트랙을 반환한다")
        void insufficientPoints() {
            GpsTrack track = new GpsTrack(normalTrack(5));

            RemoveResult result = remover.remove(track);

            assertThat(result.cleanedTrack()).isEqualTo(track);
            assertThat(result.removedPoints()).isEmpty();
        }

        @Test
        @DisplayName("포인트 수가 10개 미만이면 Warning이 발생한다")
        void insufficientPointsWarning() {
            GpsTrack track = new GpsTrack(normalTrack(5));

            RemoveResult result = remover.remove(track);

            assertThat(result.warnings())
                .anyMatch(w -> w.message().contains("최소 개수"));
        }

        @Test
        @DisplayName("포인트 수가 10개이면 정상 처리한다")
        void exactMinimumPoints() {
            GpsTrack track = new GpsTrack(normalTrack(10));

            assertThatCode(() -> remover.remove(track))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("제거율 경고")
    class RemovalRateWarning {

        @Test
        @DisplayName("제거율이 30% 이하이면 Warning이 없다")
        void noWarningWhenLowRemovalRate() {
            GpsTrack track = new GpsTrack(normalTrack(10));

            RemoveResult result = remover.remove(track);

            assertThat(result.warnings())
                .noneMatch(w -> w.message().contains("30%"));
        }

        @Test
        @DisplayName("제거율이 30% 초과이면 Warning이 발생한다")
        void warningWhenHighRemovalRate() {
            List<Coordinate> coords = normalTrack(10);
            coords.set(2, coord(127.1276, 37.4979, 10L));
            coords.set(4, coord(127.2276, 37.4979, 20L));
            coords.set(6, coord(127.3276, 37.4979, 30L));
            coords.set(8, coord(127.4276, 37.4979, 40L));
            GpsTrack track = new GpsTrack(coords);

            // 10개 중 4개가 튐 -> 제거율 40%
            RemoveResult result = remover.remove(track);

            assertThat(result.warnings())
                .anyMatch(w -> w.message().contains("30%"));
        }
    }
}
