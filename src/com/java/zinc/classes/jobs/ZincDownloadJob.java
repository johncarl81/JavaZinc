package zinc.classes.jobs;

import zinc.exceptions.ZincRuntimeException;

import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class ZincDownloadJob<V> extends ZincJob<V> {
    protected final ZincRequestExecutor mRequestExecutor;
    protected final URL mUrl;
    protected final Class<V> mClass;

    public ZincDownloadJob(final ZincRequestExecutor requestExecutor, final URL url, final Class<V> theClass) {
        mRequestExecutor = requestExecutor;
        mUrl = url;
        mClass = theClass;
    }

    protected static class DownloadFileError extends ZincRuntimeException {
        public DownloadFileError(final String message) {
            super(message);
        }

        public DownloadFileError(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}