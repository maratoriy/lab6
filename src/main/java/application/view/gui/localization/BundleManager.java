package application.view.gui.localization;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BundleManager {
    private static final List<BundleManager> myBundles = new ArrayList<>();

    private ResourceBundle bundle;
    private ApplicationLocale applicationLocale;

    public static BundleManager getBundle(String baseName) {
        for (BundleManager mb : myBundles) {
            if (mb.getBaseName().equals(baseName)) {
                return mb;
            }
        }
        BundleManager mb = new BundleManager(baseName);
        myBundles.add(mb);
        return mb;
    }

    public static BundleManager getBundle(String baseName, ApplicationLocale applicationLocale) {
        BundleManager bundleManager = getBundle(baseName);
        bundleManager.setMyLocale(applicationLocale);
        return bundleManager;
    }

    public String getBaseName() {
        return bundle.getBaseBundleName();
    }

    public ApplicationLocale getMyLocale() {
        return applicationLocale;
    }

    private BundleManager(String baseName) {
        this.bundle = ResourceBundle.getBundle(baseName);
        this.applicationLocale = ApplicationLocale.getMyLocale(bundle.getLocale());
    }

    public void setMyLocale(ApplicationLocale applicationLocale) {
        this.applicationLocale = applicationLocale;
        String baseName = bundle.getBaseBundleName();
        bundle = ResourceBundle.getBundle(baseName, this.applicationLocale.getLocale());
    }

    @NotNull
    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
