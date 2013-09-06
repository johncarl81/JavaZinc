package com.zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincJobCreator;
import com.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincJobFactory implements ZincJobCreator {
    private static final String CATALOG_FILENAME = "catalog.json";

    private final Gson mGson;

    public ZincJobFactory(final Gson gson) {
        mGson = gson;
    }

    @Override
    public ZincJob<ZincCatalog> downloadCatalog(final URL sourceURL) {
        final URL url;
        try {
            url = new URL(sourceURL, CATALOG_FILENAME);
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Invalid URL: " + sourceURL + "/" + CATALOG_FILENAME, e);
        }

        return new ZincDownloadObjectJob<ZincCatalog>(createRequestExecutor(), url, mGson, ZincCatalog.class);
    }

    @Override
    public ZincJob<File> downloadArchive(final URL url, final File root, final String child) {
        return new ZincDownloadArchiveJob(createRequestExecutor(), url, root, child);
    }

    private ZincRequestExecutor createRequestExecutor() {
        return new ZincRequestExecutor() {
            @Override
            public InputStream get(final URL url) throws AbstractZincDownloadJob.DownloadFileError {
                try {
                    return getRequest(url).stream();
                } catch (HttpRequest.HttpRequestException e) {
                    throw new AbstractZincDownloadJob.DownloadFileError("Error downloading file at url '" + url + "'", e);
                }
            }

            private HttpRequest getRequest(final URL url) {
                return HttpRequest.get(url).acceptGzipEncoding().uncompress(true);
            }
        };
    }
}
