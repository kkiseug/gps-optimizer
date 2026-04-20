package infrastructure.simplifier;

import core.common.Coordinate;
import core.common.GpsTrack;
import core.simplifier.SimplifyResult;
import core.simplifier.Tolerance;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DouglasPeuckerSimplifierTest {

    private Coordinate coord(double lon, double lat) {
        return new Coordinate(lon, lat, Instant.now());
    }

    @Test
    @DisplayName("직선상의 중복된 좌표들을 제거한다")
    void simplifyStraightLine() {
        List<Coordinate> coords = new ArrayList<>();
        // (0,0)에서 (0, 0.001)까지 직선
        coords.add(coord(127.0, 37.0));
        coords.add(coord(127.0, 37.0001)); // 직선상
        coords.add(coord(127.0, 37.0002)); // 직선상
        coords.add(coord(127.0, 37.0003)); // 직선상
        coords.add(coord(127.0, 37.0010));
        
        GpsTrack track = new GpsTrack(coords);
        Tolerance tolerance = Tolerance.ofMeters(1); // 1미터 허용오차
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(tolerance);

        SimplifyResult result = simplifier.simplify(track);

        // 양 끝점만 남아야 함 (중간 3개 제거)
        assertThat(result.simplified().size()).isEqualTo(2);
        assertThat(result.simplified().getCoordinates()).containsExactly(
            coords.get(0),
            coords.get(4)
        );
    }

    @Test
    @DisplayName("허용 오차를 벗어나는 꺾임점은 보존한다")
    void keepSignificantPoints() {
        List<Coordinate> coords = new ArrayList<>();
        coords.add(coord(127.0, 37.0));
        coords.add(coord(127.005, 37.005)); // 툭 튀어나온 점 (약 700m 거리)
        coords.add(coord(127.010, 37.0));
        
        GpsTrack track = new GpsTrack(coords);
        Tolerance tolerance = Tolerance.ofMeters(100);
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(tolerance);

        SimplifyResult result = simplifier.simplify(track);

        // 꺾임이 크므로 모든 점이 보존되어야 함
        assertThat(result.simplified().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("좌표가 2개인 경우 그대로 반환한다")
    void handleSmallTrack() {
        List<Coordinate> coords = List.of(
            coord(127.0, 37.0),
            coord(127.010, 37.0)
        );
        GpsTrack track = new GpsTrack(coords);
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(Tolerance.ofMeters(10));
        
        SimplifyResult result = simplifier.simplify(track);

        assertThat(result.simplified().size()).isEqualTo(2);
    }
}
