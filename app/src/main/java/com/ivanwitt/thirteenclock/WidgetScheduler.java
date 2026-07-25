package com.ivanwitt.thirteenclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public final class WidgetScheduler {
    public static final String ACTION_TICK = "com.ivanwitt.thirteenclock.ACTION_TICK";
    private static final int REQUEST_CODE = 1320;

    private WidgetScheduler() {}

    public static void scheduleNext(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long now = System.currentTimeMillis();
        long nextMinute = ((now / 60_000L) + 1L) * 60_000L + 250L;
        PendingIntent pendingIntent = tickIntent(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, nextMinute, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, nextMinute, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, nextMinute, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC, nextMinute, pendingIntent);
        }
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(tickIntent(context));
        }
    }

    private static PendingIntent tickIntent(Context context) {
        Intent intent = new Intent(context, ThirteenClockWidget.class);
        intent.setAction(ACTION_TICK);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
