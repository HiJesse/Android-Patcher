package cn.jesse.patcher.build.util;

/**
 * Created by jesse on 18/12/2016.
 */

public class PatchException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PatchException() {
    }

    public PatchException(String message) {
        super(message);
    }

    public PatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatchException(Throwable cause) {
        super(cause);
    }
}
