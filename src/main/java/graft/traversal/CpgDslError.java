package graft.traversal;

public class CpgDslError extends Error {

    private String message;

    public CpgDslError(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
