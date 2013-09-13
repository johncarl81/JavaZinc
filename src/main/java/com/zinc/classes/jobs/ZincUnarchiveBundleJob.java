package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.*;
import com.zinc.classes.fileutils.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJob extends ZincJob<ZincBundle> {
    private final Future<ZincBundle> mDownloadedBundle;
    private final ZincBundleCloneRequest mBundleCloneRequest;
    private final ZincFutureFactory mFutureFactory;
    private final FileHelper mFileHelper;

    public ZincUnarchiveBundleJob(final Future<ZincBundle> downloadedBundle,
                                  final ZincBundleCloneRequest bundleCloneRequest,
                                  final ZincFutureFactory futureFactory,
                                  final FileHelper fileHelper) {
        mDownloadedBundle = downloadedBundle;
        mBundleCloneRequest = bundleCloneRequest;
        mFutureFactory = futureFactory;
        mFileHelper = fileHelper;
    }

    @Override
    public ZincBundle run() throws Exception {
        final ZincBundle downloadedBundle = mDownloadedBundle.get();

        final int version = downloadedBundle.getVersion();
        final BundleID bundleID = mBundleCloneRequest.getBundleID();

        final File localBundleFolder = new File(mBundleCloneRequest.getRepoFolder().getAbsolutePath() + "/" + SourceURL.getLocalBundlesFolder(bundleID, version, mBundleCloneRequest.getFlavorName()));
        final ZincBundle result = new ZincBundle(localBundleFolder, bundleID, version);

        if (!localBundleFolder.exists()) {
            final ZincManifest manifest = getManifest(version, bundleID);

            logMessage("unarchiving");
            unarchiveBundle(downloadedBundle, result, manifest);

            logMessage("cleaning up archive");
            mFileHelper.removeFile(downloadedBundle);
        } else {
            logMessage("skipping unarchiving - bundle already found");
        }

        return result;
    }

    private ZincManifest getManifest(final int version,
                                     final BundleID bundleID) throws InterruptedException, ExecutionException {
        return mFutureFactory.downloadManifest(
                        mBundleCloneRequest.getSourceURL(),
                        bundleID.getBundleName(),
                        version
                ).get();
    }

    private void unarchiveBundle(final ZincBundle downloadedBundle,
                                 final ZincBundle result,
                                 final ZincManifest manifest) throws IOException {
        final Map<String, ZincManifest.FileInfo> files = manifest.getFilesWithFlavor(mBundleCloneRequest.getFlavorName());

        for (final Map.Entry<String, ZincManifest.FileInfo> entry : files.entrySet()) {
            final ZincManifest.FileInfo fileInfo = entry.getValue();

            final String originFilename = fileInfo.getHashWithExtension();
            final String destinationFilename = entry.getKey();

            if (fileInfo.isGzipped()) {
                mFileHelper.unzipFile(downloadedBundle, originFilename, result, destinationFilename);
            } else {
                mFileHelper.copyFile(downloadedBundle, originFilename, result, destinationFilename);
            }
        }
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mBundleCloneRequest.getBundleID() + ")";
    }
}
