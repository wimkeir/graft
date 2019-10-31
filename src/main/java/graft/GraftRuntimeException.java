package graft;

public class GraftRuntimeException extends RuntimeException {

    private String message;
    private Throwable cause;

    public GraftRuntimeException(String message) {
        this.message = message;
        this.cause = null;
    }

    public GraftRuntimeException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
