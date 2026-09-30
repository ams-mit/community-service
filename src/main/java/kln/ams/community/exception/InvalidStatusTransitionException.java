package kln.ams.community.exception;

public class InvalidStatusTransitionException extends RuntimeException {
    private final String errorCode;

    public InvalidStatusTransitionException(String message) {
        super(message);
        this.errorCode = "INVALID_STATUS_TRANSITION";
    }

    public InvalidStatusTransitionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
