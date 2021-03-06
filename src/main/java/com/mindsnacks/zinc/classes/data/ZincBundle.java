package com.mindsnacks.zinc.classes.data;

import java.io.File;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincBundle extends File {
    final private BundleID mBundleID;
    final private int mVersion;

    public ZincBundle(final String root, final BundleID bundleID, final int version) {
        super(root, bundleID.toString());

        mBundleID = bundleID;
        mVersion = version;
    }

    public ZincBundle(final File file, final BundleID bundleID, final int version) {
        super(file.getPath());

        mBundleID = bundleID;
        mVersion = version;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final ZincBundle that = (ZincBundle)o;

        return (mVersion == that.mVersion &&
                mBundleID.equals(that.mBundleID));
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mBundleID.hashCode();
        result = 31 * result + mVersion;
        return result;
    }

    public BundleID getBundleID() {
        return mBundleID;
    }

    public int getVersion() {
        return mVersion;
    }
}
