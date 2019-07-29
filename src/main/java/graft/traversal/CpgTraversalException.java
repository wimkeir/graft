package graft.traversal;

public class CpgTraversalException extends RuntimeException {

    private String message;

    public CpgTraversalException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
