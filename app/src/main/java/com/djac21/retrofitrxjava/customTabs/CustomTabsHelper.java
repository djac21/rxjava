package com.djac21.retrofitrxjava.customTabs;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CustomTabsHelper {
    private static final String TAG = "CustomTabsHelper";
    private static final String STABLE_PACKAGE = "com.android.chrome";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";

    private static String packageNameToUse;

    private CustomTabsHelper() {

    }

    public static String getPackageNameToUse(Context context) {
        if (packageNameToUse != null)
            return packageNameToUse;

        PackageManager pm = context.getPackageManager();
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null)
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;

        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null)
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
        }


        if (packagesSupportingCustomTabs.isEmpty())
            packageNameToUse = null;
        else if (packagesSupportingCustomTabs.size() == 1)
            packageNameToUse = packagesSupportingCustomTabs.get(0);
        else if (!TextUtils.isEmpty(defaultViewHandlerPackageName) && !hasSpecializedHandlerIntents(context, activityIntent) && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName))
            packageNameToUse = defaultViewHandlerPackageName;
        else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE))
            packageNameToUse = STABLE_PACKAGE;
        else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE))
            packageNameToUse = BETA_PACKAGE;
        else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE))
            packageNameToUse = DEV_PACKAGE;
        else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE))
            packageNameToUse = LOCAL_PACKAGE;

        return packageNameToUse;
    }

    private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
        try {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> handlers = packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
            if (handlers == null || handlers.size() == 0)
                return false;

            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;
                if (filter == null)
                    continue;
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0)
                    continue;
                if (resolveInfo.activityInfo == null)
                    continue;
                return true;
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Runtime exception while getting specialized handlers");
        }
        return false;
    }
}