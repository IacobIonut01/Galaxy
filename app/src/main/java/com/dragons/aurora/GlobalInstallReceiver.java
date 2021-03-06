package com.dragons.aurora;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import com.dragons.aurora.activities.DetailsActivity;
import com.dragons.aurora.downloader.DownloadState;
import com.dragons.aurora.fragment.DetailsFragment;
import com.dragons.aurora.fragment.PreferenceFragment;
import com.dragons.aurora.model.App;

public class GlobalInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!expectedAction(action) || null == intent.getData()) {
            return;
        }
        String packageName = intent.getData().getSchemeSpecificPart();
        Log.i(getClass().getSimpleName(), "Finished installation of " + packageName);
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        BlackWhiteListManager manager = new BlackWhiteListManager(context);
        if (actionIsInstall(intent) && wasInstalled(context, packageName) && needToAutoWhitelist(context) && !manager.isBlack()) {
            Log.i(getClass().getSimpleName(), "Whitelisting " + packageName);
            manager.add(packageName);
        }
        if (null != DetailsFragment.app && packageName.equals(DetailsFragment.app.getPackageName())) {
            updateDetails(actionIsInstall(intent));
        }
        ((AuroraApplication) context.getApplicationContext()).removePendingUpdate(packageName, actionIsInstall(intent));
        if (needToRemoveApk(context) && actionIsInstall(intent)) {
            App app = getApp(context, packageName);
            File apkPath = Paths.getApkPath(context, app.getPackageName(), app.getVersionCode());
            boolean deleted = apkPath.delete();
            Log.i(getClass().getSimpleName(), "Removed " + apkPath + " successfully: " + deleted);
        }
    }

    static public void updateDetails(boolean installed) {
        if (installed) {
            DetailsFragment.app.getPackageInfo().versionCode = DetailsFragment.app.getVersionCode();
            DetailsFragment.app.setInstalled(true);
        } else {
            DetailsFragment.app.getPackageInfo().versionCode = 0;
            DetailsFragment.app.setInstalled(false);
        }
    }

    static public boolean actionIsInstall(Intent intent) {
        return !TextUtils.isEmpty(intent.getAction())
                && (intent.getAction().equals(Intent.ACTION_PACKAGE_INSTALL)
                || intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)
                || intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)
                || intent.getAction().equals(DetailsInstallReceiver.ACTION_PACKAGE_REPLACED_NON_SYSTEM)
        )
                ;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static private boolean expectedAction(String action) {
        return action.equals(Intent.ACTION_PACKAGE_INSTALL)
                || action.equals(Intent.ACTION_PACKAGE_ADDED)
                || action.equals(Intent.ACTION_PACKAGE_REPLACED)
                || action.equals(Intent.ACTION_PACKAGE_REMOVED)
                || action.equals(Intent.ACTION_PACKAGE_FULLY_REMOVED)
                || action.equals(DetailsInstallReceiver.ACTION_PACKAGE_REPLACED_NON_SYSTEM)
                ;
    }

    static private boolean needToRemoveApk(Context context) {
        return PreferenceFragment.getBoolean(context, PreferenceFragment.PREFERENCE_DELETE_APK_AFTER_INSTALL);
    }

    static private boolean needToAutoWhitelist(Context context) {
        return PreferenceFragment.getBoolean(context, PreferenceFragment.PREFERENCE_AUTO_WHITELIST);
    }

    static private App getApp(Context context, String packageName) {
        App app = new App();
        PackageManager pm = context.getPackageManager();
        try {
            app = new App(pm.getPackageInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(GlobalInstallReceiver.class.getSimpleName(), "Install broadcast received, but package " + packageName + " not found");
        }
        return app;
    }

    static private boolean wasInstalled(Context context, String packageName) {
        return InstallationState.isInstalled(packageName)
                || (PreferenceFragment.getString(context, PreferenceFragment.INSTALLATION_METHOD_DEFAULT).equals(PreferenceFragment.INSTALLATION_METHOD_DEFAULT)
                && DownloadState.get(packageName).isEverythingFinished()
        )
                ;
    }
}
