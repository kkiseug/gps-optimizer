package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.RemoveResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LengthOutlierRemoverTest {

    private final LengthOutlierRemover remover = new LengthOutlierRemover();

    private Coordinate coord(double lon, double lat) {
        return new Coordinate(lon, lat, null);
    }

    // 정상 트랙 - 약 10m 간격
    private List<Coordinate> normalTrack(int size) {
        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            coords.add(coord(127.0276 + i * 0.0001, 37.4979));
        }
        return coords;
    }

    @Nested
    @DisplayName("정상 케이스")
    class Normal {

        @Test
        @DisplayName("정상 거리의 좌표는 제거되지 않는다")
        void normalDistanceNotRemoved() {
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
        @DisplayName("임계값(100m)을 초과하는 좌표는 제거된다")
        void outlierRemoved() {
            List<Coordinate> coords = normalTrack(10);
            // 500m 이상 튀는 좌표
            coords.set(5, coord(127.0276 + 0.005, 37.4979));
            GpsTrack track = new GpsTrack(coords);

            RemoveResult result = remover.remove(track);

            assertThat(result.removedPoints()).hasSize(1);
        }

        @Test
        @DisplayName("제거된 좌표가 removedPoints에 포함된다")
        void removedPointsContainsOutlier() {
            List<Coordinate> coords = normalTrack(10);
            Coordinate outlier = coord(127.0276 + 0.005, 37.4979);
            coords.set(5, outlier);
            GpsTrack track = new GpsTrack(coords);

            RemoveResult result = remover.remove(track);

            assertThat(result.removedPoints()).contains(outlier);
        }
    }
}
