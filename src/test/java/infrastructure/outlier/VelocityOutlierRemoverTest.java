package infrastructure.outlier;

import core.common.Coordinate;
import core.common.GpsTrack;
import core.outlier.RemoveResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class VelocityOutlierRemoverTest {

    private final VelocityOutlierRemover remover = new VelocityOutlierRemover(8.3, 5);

    private Coordinate coord(double lon, double lat, long epochSecond) {
        return new Coordinate(lon, lat, Instant.ofEpochSecond(epochSecond));
    }

    // 정상 트랙 - 5초 간격, 약 10m 이동 → 2 m/s (정상)
    private List<Coordinate> normalTrack(int size) {
        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            coords.add(coord(127.0276 + i * 0.0001, 37.4979, i * 5L));
        }
        return coords;
    }

    @Nested
    @DisplayName("정상 케이스")
    class Normal {

        @Test
        @DisplayName("정상 속도의 좌표는 제거되지 않는다")
        void normalVelocityNotRemoved() {
            GpsTrack track = new GpsTrack(normalTrack(10));

            RemoveResult result = remover.remove(track);

            assertThat(result.removedPoints()).isEmpty();
            assertThat(result.cleanedTrack().size()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("이상치 제거")
    class OutlierRemoval {

        @Test
        @DisplayName("임계값(8.3 m/s)을 초과하는 좌표는 제거된다")
        void outlierRemoved() {
            List<Coordinate> coords = normalTrack(10);
            // 5초 만에 10km 이동 → 2000 m/s
            coords.set(5, coord(127.1276, 37.4979, 25L));
            GpsTrack track = new GpsTrack(coords);

            RemoveResult result = remover.remove(track);

            assertThat(result.removedPoints()).hasSize(1);
            assertThat(result.cleanedTrack().size()).isEqualTo(9);
        }

        @Test
        @DisplayName("제거된 좌표가 removedPoints에 포함된다")
        void removedPointsContainsOutlier() {
            List<Coordinate> coords = normalTrack(10);
            Coordinate outlier = coord(127.1276, 37.4979, 25L);
            coords.set(5, outlier);
            GpsTrack track = new GpsTrack(coords);

            RemoveResult result = remover.remove(track);

            assertThat(result.removedPoints()).contains(outlier);
        }

        @Test
        @DisplayName("여러 이상치가 있으면 모두 제거된다")
        void multipleOutliersRemoved() {
            List<Coordinate> coords = normalTrack(10);
            coords.set(3, coord(127.1276, 37.4979, 15L));
            coords.set(7, coord(127.2276, 37.4979, 35L));
            GpsTrack track = new GpsTrack(coords);

            RemoveResult result = remover.remove(track);

            assertThat(result.removedPoints()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("제거율 계산")
    class RemovalRate {

        @Test
        @DisplayName("이상치가 없으면 제거율은 0이다")
        void zeroRemovalRate() {
            GpsTrack track = new GpsTrack(normalTrack(10));

            RemoveResult result = remover.remove(track);

            assertThat(result.removalRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("10개 중 2개 제거되면 제거율은 0.2이다")
        void removalRateCalculation() {
            List<Coordinate> coords = normalTrack(10);
            coords.set(3, coord(127.1276, 37.4979, 15L));
            coords.set(7, coord(127.2276, 37.4979, 35L));
            GpsTrack track = new GpsTrack(coords);

            RemoveResult result = remover.remove(track);

            assertThat(result.removalRate()).isEqualTo(0.2);
        }
    }
}
