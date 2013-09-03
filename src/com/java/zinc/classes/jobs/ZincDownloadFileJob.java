package zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincDownloadFileJob<V> extends ZincJob<V> {
    private final RequestFactory mRequestFactory;
    private final URL mUrl;
    private final Gson mGson;
    private final Class<V> mClass;

    public ZincDownloadFileJob(final RequestFactory requestFactory, final URL url, final Gson gson, final Class<V> theClass) {
        mRequestFactory = requestFactory;
        mUrl = url;
        mGson = gson;
        mClass = theClass;
    }

    @Override
    public V call() throws Exception {
        final InputStreamReader reader = mRequestFactory.get(mUrl);

        return mGson.fromJson(reader, mClass);
    }

    public interface RequestFactory {
        InputStreamReader get(URL url);
    }

    public class ZincDownloadFileJobFactory<V> {
        ZincDownloadFileJob createJob(final URL url, final Gson gson, final Class<V> theClass) {
              return new ZincDownloadFileJob<V>(new RequestFactory() {
                  @Override
                  public InputStreamReader get(final URL url) {
                    return HttpRequest.get(mUrl).acceptGzipEncoding().uncompress(true).reader();
                  }
              }, url, gson, theClass);
        }
    }
}
