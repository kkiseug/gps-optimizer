package core.common;

import exception.EmptyTrackException;
import exception.InsufficientPointsException;
import exception.InvalidTimestampException;
import exception.InvalidTimestampOrderException;
import exception.InvalidWindowSizeException;
import java.util.List;

public record GpsTrack(
    List<Coordinate> coordinates
) {

    public static final int MINIMUM_COORD_SIZE = 2;

    public GpsTrack {
        validateCoordinateNotEmpty(coordinates);

        coordinates = List.copyOf(coordinates);

        validateCoordinateOverMinimumSize(coordinates);
        validateAllCoordinatesHasTimeStamp(coordinates);
        validateTimestampsOrder(coordinates);
    }

    public List<Coordinate> window(int index, int size) {
        if (size % 2 == 0) {
            throw new InvalidWindowSizeException("윈도우 크기는 홀수만 가능합니다.");
        }

        int half = size / 2;
        if (index - half < 0) {
            return coordinates.subList(0, index + half + 1);
        }

        if (index + half >= coordinates.size()) {
            return coordinates.subList(index - half, coordinates.size());
        }

        return coordinates.subList(index - half, index + half + 1);
    }

    public double length() {
        double length = 0;

        for (int idx = 0; idx < coordinates.size() - 1; idx++) {
            Coordinate coord1 = coordinates.get(idx);
            Coordinate coord2 = coordinates.get(idx + 1);
            length += (coord1.distanceFrom(coord2));
        }

        return length;
    }

    public int size() {
        return coordinates.size();
    }

    public boolean hasTimestamps() {
        Coordinate first = coordinates.getFirst();
        return first.hasTimestamp();
    }

    public Coordinate get(int index) {
        return coordinates.get(index);
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    private static void validateCoordinateNotEmpty(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new EmptyTrackException("좌표는 비어있을 수 없습니다.");
        }
    }

    private static void validateCoordinateOverMinimumSize(List<Coordinate> coordinates) {
        if (coordinates.size() < MINIMUM_COORD_SIZE) {
            throw new InsufficientPointsException("좌표 수는 최소 2개여야 합니다.");
        }
    }

    private static void validateAllCoordinatesHasTimeStamp(List<Coordinate> coordinates) {
        boolean hasTimestamp = coordinates.getFirst().hasTimestamp();
        for (Coordinate coordinate : coordinates) {
            if (hasTimestamp != coordinate.hasTimestamp()) {
                throw new InvalidTimestampException("타임스탬프는 아예 없거나, 모두 존재해야 합니다.");
            }
        }
    }

    private static void validateTimestampsOrder(List<Coordinate> coordinates) {
        boolean hasTimestamp = coordinates.getFirst().hasTimestamp();
        if (!hasTimestamp) {
            return;
        }

        for (int i = 1; i < coordinates.size(); i++) {
            if (coordinates.get(i).timestamp().isBefore(coordinates.get(i - 1).timestamp())) {
                throw new InvalidTimestampOrderException("타임스탬프 순서가 잘못되었습니다.");
            }
        }
    }
}
