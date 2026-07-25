package com.ivanwitt.wittlauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class AppRepository {
    private AppRepository() {}

    static List<AppEntry> loadApps(Context context, boolean includeHidden) {
        PackageManager pm = context.getPackageManager();
        SharedPreferences prefs = context.getSharedPreferences("witt", Context.MODE_PRIVATE);
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        List<AppEntry> apps = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (ResolveInfo info : infos) {
            if (info.activityInfo == null) continue;
            if (context.getPackageName().equals(info.activityInfo.packageName)) continue;
            ComponentName component = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
            String key = component.flattenToString();
            if (!seen.add(key)) continue;
            if (!includeHidden && prefs.getBoolean("hidden:" + key, false)) continue;

            String original = String.valueOf(info.loadLabel(pm));
            String alias = prefs.getString("alias:" + key, "");
            String label = alias == null || alias.trim().isEmpty() ? original : alias.trim();
            apps.add(new AppEntry(label, component));
        }

        Collections.sort(apps, Comparator.comparing(a -> a.label.toLowerCase(Locale.getDefault())));
        return apps;
    }

    static AppEntry findByKey(Context context, String key, boolean includeHidden) {
        if (key == null || key.isEmpty()) return null;
        for (AppEntry app : loadApps(context, includeHidden)) {
            if (app.key().equals(key)) return app;
        }
        return null;
    }

    static void launch(Context context, AppEntry app) {
        if (app == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(app.component);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }
}
