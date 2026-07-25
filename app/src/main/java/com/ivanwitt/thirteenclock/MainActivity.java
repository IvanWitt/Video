package com.ivanwitt.thirteenclock;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.ZonedDateTime;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(24);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("13×20 Clock");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView description = new TextView(this);
        description.setText("Аналоговые сутки: 13 часов × 20 минут\nЦолькин + Хааб\nДлинный счёт · GMT 584283");
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        description.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, -2);
        descParams.topMargin = dp(20);
        root.addView(description, descParams);

        ZonedDateTime now = ZonedDateTime.now();
        MayaCalendar.Result maya = MayaCalendar.forDate(now.toLocalDate());
        ThirteenTime.Result alt = ThirteenTime.from(now);

        TextView current = new TextView(this);
        current.setText("Сейчас: " + alt.digital() + "\n" + maya.calendarRoundLine() + "\n" + maya.longCount);
        current.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        current.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams currentParams = new LinearLayout.LayoutParams(-1, -2);
        currentParams.topMargin = dp(28);
        root.addView(current, currentParams);

        Button add = new Button(this);
        add.setText("Добавить виджет на рабочий стол");
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, -2);
        buttonParams.topMargin = dp(32);
        root.addView(add, buttonParams);
        add.setOnClickListener(v -> pinWidget());

        TextView note = new TextView(this);
        note.setText("Если ваш лаунчер не поддерживает автоматическое добавление, откройте стандартное меню «Виджеты» и выберите 13×20 Clock.");
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, -2);
        noteParams.topMargin = dp(16);
        root.addView(note, noteParams);

        setContentView(root);
    }

    private void pinWidget() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        if (manager.isRequestPinAppWidgetSupported()) {
            ComponentName provider = new ComponentName(this, ThirteenClockWidget.class);
            PendingIntent success = PendingIntent.getActivity(
                    this,
                    13,
                    getIntent(),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            manager.requestPinAppWidget(provider, null, success);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
