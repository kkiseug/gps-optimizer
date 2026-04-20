package infrastructure;

import core.Coordinate;
import core.GpsTrack;
import core.CleaningResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class KalmanFilterTest {

    private final KalmanFilter filter = new KalmanFilter(0.00001, 0.001);

    private Coordinate coord(double lon, double lat, int seconds) {
        return new Coordinate(lon, lat, Instant.parse("2026-04-19T10:00:00Z").plusSeconds(seconds));
    }

    @Test
    @DisplayName("칼만 필터를 적용해도 시작점과 끝점은 변하지 않아야 한다")
    void keepStartAndEndPoint() {
        List<Coordinate> coords = new ArrayList<>();
        coords.add(coord(127.0, 37.0, 0));
        coords.add(coord(127.0001, 37.0005, 5)); // 노이즈 섞인 중간점
        coords.add(coord(127.0002, 37.0010, 10));
        
        GpsTrack track = new GpsTrack(coords);
        CleaningResult result = filter.filter(track);

        List<Coordinate> smoothed = result.cleanedTrack().getCoordinates();
        
        // 시작점 검증
        assertThat(smoothed.get(0).latitude()).isEqualTo(37.0);
        assertThat(smoothed.get(0).longitude()).isEqualTo(127.0);
        
        // 끝점 검증
        assertThat(smoothed.get(2).latitude()).isEqualTo(37.0010);
        assertThat(smoothed.get(2).longitude()).isEqualTo(127.0002);
    }

    @Test
    @DisplayName("지글지글한 노이즈가 섞인 경로를 부드럽게 보정한다")
    void smoothNoisyTrack() {
        List<Coordinate> coords = new ArrayList<>();
        // 직선 경로에 위아래로 튀는 노이즈 추가
        coords.add(coord(127.0, 37.0, 0));
        coords.add(coord(127.0001, 37.0005, 5)); // 위로 튐
        coords.add(coord(127.0002, 37.0, 10));    // 다시 아래로
        coords.add(coord(127.0003, 37.0005, 15)); // 다시 위로
        coords.add(coord(127.0004, 37.0, 20));
        
        GpsTrack track = new GpsTrack(coords);
        CleaningResult result = filter.filter(track);

        List<Coordinate> smoothed = result.cleanedTrack().getCoordinates();
        
        // 중간 점들(index 1, 2, 3)의 위도가 원본 노이즈보다 중앙쪽으로 수렴했는지 확인
        // 원본 위도는 37.0005와 37.0을 왔다갔다 함
        assertThat(smoothed.get(1).latitude()).isLessThan(37.0005);
        assertThat(smoothed.get(2).latitude()).isGreaterThan(37.0);
    }

    @Test
    @DisplayName("필터링된 결과에 대한 리포트를 정확히 생성한다")
    void generateCorrectReport() {
        List<Coordinate> coords = new ArrayList<>();
        for(int i=0; i<10; i++) coords.add(coord(127.0 + i*0.0001, 37.0, i*5));
        
        CleaningResult result = filter.filter(new GpsTrack(coords));

        assertThat(result.stepReports()).hasSize(1);
        assertThat(result.stepReports().get(0).filterName()).isEqualTo("KalmanFilter");
        assertThat(result.stepReports().get(0).modifiedCount()).isEqualTo(8); // 시작, 끝 제외 8개
    }
}
