package com.ivanwitt.thirteenclock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class ThirteenClockWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
        WidgetScheduler.scheduleNext(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        WidgetScheduler.scheduleNext(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WidgetScheduler.cancel(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (WidgetScheduler.ACTION_TICK.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            updateAll(context);
            WidgetScheduler.scheduleNext(context);
        }
    }

    private static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, ThirteenClockWidget.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        ZonedDateTime now = ZonedDateTime.now();
        LocalDate date = now.toLocalDate();
        MayaCalendar.Result maya = MayaCalendar.forDate(date);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_thirteen_clock);
        views.setImageViewBitmap(R.id.clock_face, WidgetRenderer.render(now));
        views.setTextViewText(R.id.maya_date, maya.calendarRoundLine());
        views.setTextViewText(R.id.long_count, maya.longCount);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(
                context,
                0,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, pending);

        manager.updateAppWidget(appWidgetId, views);
    }
}
