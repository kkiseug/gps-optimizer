package exception;

public class InvalidTimestampOrderException extends RuntimeException {
    public InvalidTimestampOrderException(String message) {
        super(message);
    }
}
