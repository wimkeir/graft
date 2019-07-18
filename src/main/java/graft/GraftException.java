package graft;

public class GraftException extends Exception {

    private String message;
    private Throwable cause;

    public GraftException(String message) {
        this.message = message;
        this.cause = null;
    }

    public GraftException(String message, Throwable cause) {
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
