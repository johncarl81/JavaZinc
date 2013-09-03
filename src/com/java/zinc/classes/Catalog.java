package zinc.classes;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class Catalog {
    @SerializedName("id")
    private final String mIdentifier;

    @SerializedName("bundles")
    private final Map<String, Info> mBundles;

    public Catalog(final String identifier, final Map<String, Info> bundles) {
        mIdentifier = identifier;
        mBundles = bundles;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public int getVersionForBundleID(final String bundleID, final String distribution) {
        return mBundles.get(bundleID).getVersionForDistribution(distribution);
    }

    private class Info {
        @SerializedName("distributions")
        private final Map<String, Integer> mDistributions;

        public Info(final Map<String, Integer> distributions) {
            mDistributions = distributions;
        }

        public int getVersionForDistribution(String distribution) {
            return mDistributions.get(distribution);
        }
    }
}