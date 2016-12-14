package cn.jesse.patcher.loader;

/**
 * Created by jesse on 15/11/2016.
 */
public class PatcherRuntimeException extends RuntimeException {
    private static final String PATCHER_RUNTIME_EXCEPTION_PREFIX = Constants.LOADER_TAG + "Exception:";

    public PatcherRuntimeException(String detailMessage) {
        super(PATCHER_RUNTIME_EXCEPTION_PREFIX + detailMessage);
    }

    public PatcherRuntimeException(String detailMessage, Throwable throwable) {
        super(PATCHER_RUNTIME_EXCEPTION_PREFIX + detailMessage, throwable);
    }

}
