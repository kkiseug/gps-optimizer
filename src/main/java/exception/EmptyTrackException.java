package exception;

public class EmptyTrackException extends RuntimeException {
    public EmptyTrackException(String message) {
        super(message);
    }
}
