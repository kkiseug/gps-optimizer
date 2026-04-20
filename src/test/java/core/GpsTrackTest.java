package core;

import core.common.Coordinate;
import core.common.GpsTrack;
import exception.EmptyTrackException;
import exception.InsufficientPointsException;
import exception.InvalidTimestampException;
import exception.InvalidTimestampOrderException;
import exception.InvalidWindowSizeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GpsTrackTest {

    // 타임스탬프 없는 좌표 픽스처
    private static final Coordinate GANGNAM = new Coordinate(127.0276, 37.4979, null);
    private static final Coordinate YEOKSAM = new Coordinate(127.0350, 37.5007, null);
    private static final Coordinate SEOLLEUNG = new Coordinate(127.0488, 37.5045, null);
    private static final Coordinate SAMSUNG = new Coordinate(127.0632, 37.5088, null);
    private static final Coordinate JAMSIL = new Coordinate(127.1000, 37.5133, null);

    // 타임스탬프 있는 좌표 픽스처
    private static Coordinate withTime(Coordinate c, long epochSecond) {
        return new Coordinate(c.longitude(), c.latitude(), Instant.ofEpochSecond(epochSecond));
    }

    @Nested
    @DisplayName("생성")
    class Create {

        @Test
        @DisplayName("좌표 리스트로 생성할 수 있다")
        void createWithCoordinates() {
            assertThatCode(() -> new GpsTrack(List.of(GANGNAM, YEOKSAM)))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("생성 후 외부 리스트를 변경해도 트랙이 변경되지 않는다")
        void immutable() {
            List<Coordinate> list = new ArrayList<>(List.of(GANGNAM, YEOKSAM, SEOLLEUNG));
            GpsTrack track = new GpsTrack(list);

            list.add(SAMSUNG);

            assertThat(track.size()).isEqualTo(3);
        }

        @Nested
        @DisplayName("빈 리스트 검증")
        class EmptyValidation {

            @Test
            @DisplayName("null 입력 시 예외가 발생한다")
            void nullCoordinates() {
                assertThatThrownBy(() -> new GpsTrack(null))
                    .isInstanceOf(EmptyTrackException.class);
            }

            @Test
            @DisplayName("빈 리스트 입력 시 예외가 발생한다")
            void emptyCoordinates() {
                assertThatThrownBy(() -> new GpsTrack(List.of()))
                    .isInstanceOf(EmptyTrackException.class);
            }
        }

        @Nested
        @DisplayName("최소 좌표 수 검증")
        class MinimumSizeValidation {

            @Test
            @DisplayName("좌표가 1개이면 예외가 발생한다")
            void oneCoordinate() {
                assertThatThrownBy(() -> new GpsTrack(List.of(GANGNAM)))
                    .isInstanceOf(InsufficientPointsException.class);
            }

            @Test
            @DisplayName("좌표가 2개이면 생성할 수 있다")
            void twoCoordinates() {
                assertThatCode(() -> new GpsTrack(List.of(GANGNAM, YEOKSAM)))
                    .doesNotThrowAnyException();
            }
        }

        @Nested
        @DisplayName("타임스탬프 일관성 검증")
        class TimestampConsistencyValidation {

            @Test
            @DisplayName("타임스탬프가 모두 있으면 생성할 수 있다")
            void allHasTimestamp() {
                List<Coordinate> coordinates = List.of(
                    withTime(GANGNAM, 0),
                    withTime(YEOKSAM, 10),
                    withTime(SEOLLEUNG, 20)
                );

                assertThatCode(() -> new GpsTrack(coordinates))
                    .doesNotThrowAnyException();
            }

            @Test
            @DisplayName("타임스탬프가 모두 없으면 생성할 수 있다")
            void noneHasTimestamp() {
                assertThatCode(() -> new GpsTrack(List.of(GANGNAM, YEOKSAM, SEOLLEUNG)))
                    .doesNotThrowAnyException();
            }

            @Test
            @DisplayName("타임스탬프가 일부만 있으면 예외가 발생한다")
            void partialTimestamp() {
                List<Coordinate> coordinates = List.of(
                    withTime(GANGNAM, 0),
                    YEOKSAM  // 타임스탬프 없음
                );

                assertThatThrownBy(() -> new GpsTrack(coordinates))
                    .isInstanceOf(InvalidTimestampException.class);
            }
        }

        @Nested
        @DisplayName("타임스탬프 순서 검증")
        class TimestampOrderValidation {

            @Test
            @DisplayName("타임스탬프가 역전되면 예외가 발생한다")
            void reversedTimestamp() {
                List<Coordinate> coordinates = List.of(
                    withTime(GANGNAM, 20),
                    withTime(YEOKSAM, 10),  // 역전
                    withTime(SEOLLEUNG, 30)
                );

                assertThatThrownBy(() -> new GpsTrack(coordinates))
                    .isInstanceOf(InvalidTimestampOrderException.class);
            }

            @Test
            @DisplayName("타임스탬프가 없으면 순서 검증을 하지 않는다")
            void noTimestampSkipsOrderValidation() {
                assertThatCode(() -> new GpsTrack(List.of(GANGNAM, YEOKSAM, SEOLLEUNG)))
                    .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("경로 길이 계산")
    class LengthCalculation {

        @Test
        @DisplayName("두 좌표의 경로 길이는 두 좌표 간 거리와 같다")
        void twoCoordinatesLength() {
            GpsTrack track = new GpsTrack(List.of(GANGNAM, JAMSIL));

            double length = track.length();
            double directDistance = GANGNAM.distanceFrom(JAMSIL);

            assertThat(length).isEqualTo(directDistance);
        }

        @Test
        @DisplayName("경로 길이는 각 구간 거리의 합이다")
        void multipleCoordinatesLength() {
            GpsTrack track = new GpsTrack(List.of(GANGNAM, YEOKSAM, SEOLLEUNG));

            double expected = GANGNAM.distanceFrom(YEOKSAM)
                + YEOKSAM.distanceFrom(SEOLLEUNG);

            assertThat(track.length()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("윈도우 반환")
    class Window {

        GpsTrack track = new GpsTrack(List.of(GANGNAM, YEOKSAM, SEOLLEUNG, SAMSUNG, JAMSIL));

        @Test
        @DisplayName("윈도우 크기가 짝수이면 예외가 발생한다")
        void evenWindowSize() {
            assertThatThrownBy(() -> track.window(2, 4))
                .isInstanceOf(InvalidWindowSizeException.class);
        }

        @Test
        @DisplayName("중간 인덱스는 완전한 윈도우를 반환한다")
        void middleIndex() {
            // index=2 (SEOLLEUNG), size=5 → [GANGNAM, YEOKSAM, SEOLLEUNG, SAMSUNG, JAMSIL]
            List<Coordinate> window = track.window(2, 5);

            assertThat(window).containsExactly(GANGNAM, YEOKSAM, SEOLLEUNG, SAMSUNG, JAMSIL);
        }

        @Test
        @DisplayName("앞쪽 경계에서는 가용한 좌표만으로 윈도우를 구성한다")
        void frontBoundary() {
            // index=0 (GANGNAM), size=5 → [GANGNAM, YEOKSAM, SEOLLEUNG]
            List<Coordinate> window = track.window(0, 5);

            assertThat(window).containsExactly(GANGNAM, YEOKSAM, SEOLLEUNG);
        }

        @Test
        @DisplayName("뒤쪽 경계에서는 가용한 좌표만으로 윈도우를 구성한다")
        void backBoundary() {
            // index=4 (JAMSIL), size=5 → [SEOLLEUNG, SAMSUNG, JAMSIL]
            List<Coordinate> window = track.window(4, 5);

            assertThat(window).containsExactly(SEOLLEUNG, SAMSUNG, JAMSIL);
        }
    }

    @Nested
    @DisplayName("타임스탬프 보유 여부")
    class HasTimestamps {

        @Test
        @DisplayName("타임스탬프가 있으면 true를 반환한다")
        void hasTimestamps() {
            GpsTrack track = new GpsTrack(List.of(
                withTime(GANGNAM, 0),
                withTime(YEOKSAM, 10)
            ));

            assertThat(track.hasTimestamps()).isTrue();
        }

        @Test
        @DisplayName("타임스탬프가 없으면 false를 반환한다")
        void noTimestamps() {
            GpsTrack track = new GpsTrack(List.of(GANGNAM, YEOKSAM));

            assertThat(track.hasTimestamps()).isFalse();
        }
    }
}
