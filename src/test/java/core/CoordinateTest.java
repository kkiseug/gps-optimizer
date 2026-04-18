package core;

import exception.CannotCalculateVelocityException;
import exception.InvalidCoordinateException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CoordinateTest {

    @Nested
    @DisplayName("생성")
    class Create {

        @Test
        @DisplayName("위도, 경도, 타임스탬프로 생성할 수 있다")
        void createWithTimestamp() {
            Instant now = Instant.now();

            assertThatCode(() -> new Coordinate(127.0, 37.0, now))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("타임스탬프 없이 생성할 수 있다")
        void createWithoutTimestamp() {
            assertThatCode(() -> new Coordinate(127.0, 37.0, null))
                .doesNotThrowAnyException();
        }

        @Nested
        @DisplayName("경도 유효성 검증")
        class LongitudeValidation {

            @Test
            @DisplayName("경도가 -180 미만이면 예외가 발생한다")
            void longitudeBelowMin() {
                assertThatThrownBy(() -> new Coordinate(-181.0, 37.0, null))
                    .isInstanceOf(InvalidCoordinateException.class);
            }

            @Test
            @DisplayName("경도가 180 초과이면 예외가 발생한다")
            void longitudeAboveMax() {
                assertThatThrownBy(() -> new Coordinate(181.0, 37.0, null))
                    .isInstanceOf(InvalidCoordinateException.class);
            }

            @Test
            @DisplayName("경도 경계값 -180은 유효하다")
            void longitudeMinBoundary() {
                assertThatCode(() -> new Coordinate(-180.0, 37.0, null))
                    .doesNotThrowAnyException();
            }

            @Test
            @DisplayName("경도 경계값 180은 유효하다")
            void longitudeMaxBoundary() {
                assertThatCode(() -> new Coordinate(180.0, 37.0, null))
                    .doesNotThrowAnyException();
            }
        }

        @Nested
        @DisplayName("위도 유효성 검증")
        class LatitudeValidation {

            @Test
            @DisplayName("위도가 -90 미만이면 예외가 발생한다")
            void latitudeBelowMin() {
                assertThatThrownBy(() -> new Coordinate(127.0, -91.0, null))
                    .isInstanceOf(InvalidCoordinateException.class);
            }

            @Test
            @DisplayName("위도가 90 초과이면 예외가 발생한다")
            void latitudeAboveMax() {
                assertThatThrownBy(() -> new Coordinate(127.0, 91.0, null))
                    .isInstanceOf(InvalidCoordinateException.class);
            }

            @Test
            @DisplayName("위도 경계값 -90은 유효하다")
            void latitudeMinBoundary() {
                assertThatCode(() -> new Coordinate(127.0, -90.0, null))
                    .doesNotThrowAnyException();
            }

            @Test
            @DisplayName("위도 경계값 90은 유효하다")
            void latitudeMaxBoundary() {
                assertThatCode(() -> new Coordinate(127.0, 90.0, null))
                    .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("거리 계산")
    class DistanceCalculation {

        @Test
        @DisplayName("같은 좌표 간 거리는 0이다")
        void sameCoordinate() {
            Coordinate a = new Coordinate(127.0, 37.0, null);
            Coordinate b = new Coordinate(127.0, 37.0, null);

            assertThat(a.distanceFrom(b)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("강남역 - 잠실역 간 거리는 약 6.6km이다")
        void gangnamToJamsil() {
            Coordinate gangnam = new Coordinate(127.0276, 37.4979, null);
            Coordinate jamsil = new Coordinate(127.1000, 37.5133, null);

            double distance = gangnam.distanceFrom(jamsil);

            assertThat(distance).isBetween(6_500.0, 6_700.0);
        }

        @Test
        @DisplayName("거리 계산은 대칭이다 (A→B == B→A)")
        void distanceIsSymmetric() {
            Coordinate a = new Coordinate(127.0276, 37.4979, null);
            Coordinate b = new Coordinate(127.1000, 37.5133, null);

            assertThat(a.distanceFrom(b)).isEqualTo(b.distanceFrom(a));
        }
    }

    @Nested
    @DisplayName("속도 계산")
    class VelocityCalculation {

        @Test
        @DisplayName("795m를 100초에 이동하면 속도는 약 7.9 m/s이다")
        void normalVelocity() {
            Coordinate a = new Coordinate(126.9784, 37.5665, Instant.ofEpochSecond(0));
            Coordinate b = new Coordinate(126.9874, 37.5665, Instant.ofEpochSecond(100));

            double velocity = a.velocityFrom(b);

            assertThat(velocity).isBetween(7.5, 8.5);
        }

        @Test
        @DisplayName("타임스탬프 순서가 역전돼도 속도는 양수이다")
        void reversedTimestamp() {
            Coordinate a = new Coordinate(126.9784, 37.5665, Instant.ofEpochSecond(100));
            Coordinate b = new Coordinate(126.9874, 37.5665, Instant.ofEpochSecond(0));

            double velocity = a.velocityFrom(b);

            assertThat(velocity).isPositive();
        }

        @Test
        @DisplayName("500ms 차이나는 두 좌표의 속도를 계산할 수 있다")
        void subSecondTimestamp() {
            Coordinate a = new Coordinate(127.0, 37.0, Instant.ofEpochMilli(0));
            Coordinate b = new Coordinate(127.0001, 37.0, Instant.ofEpochMilli(500));

            assertThatCode(() -> a.velocityFrom(b))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("타임스탬프가 없으면 속도 계산 시 예외가 발생한다")
        void noTimestamp() {
            Coordinate a = new Coordinate(127.0, 37.0, null);
            Coordinate b = new Coordinate(127.1, 37.1, Instant.now());

            assertThatThrownBy(() -> a.velocityFrom(b))
                .isInstanceOf(CannotCalculateVelocityException.class);
        }

        @Test
        @DisplayName("타임스탬프가 동일하면 속도 계산 시 예외가 발생한다")
        void sameTimestamp() {
            Instant now = Instant.now();
            Coordinate a = new Coordinate(127.0, 37.0, now);
            Coordinate b = new Coordinate(127.1, 37.1, now);

            assertThatThrownBy(() -> a.velocityFrom(b))
                .isInstanceOf(CannotCalculateVelocityException.class);
        }
    }

    @Nested
    @DisplayName("타임스탬프 보유 여부")
    class HasTimestamp {

        @Test
        @DisplayName("타임스탬프가 있으면 true를 반환한다")
        void hasTimestamp() {
            Coordinate coordinate = new Coordinate(127.0, 37.0, Instant.now());

            assertThat(coordinate.hasTimestamp()).isTrue();
        }

        @Test
        @DisplayName("타임스탬프가 없으면 false를 반환한다")
        void noTimestamp() {
            Coordinate coordinate = new Coordinate(127.0, 37.0, null);

            assertThat(coordinate.hasTimestamp()).isFalse();
        }
    }
}
