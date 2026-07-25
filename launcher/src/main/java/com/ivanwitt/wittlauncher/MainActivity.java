package com.ivanwitt.wittlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView timeView;
    private TextView dateView;
    private final Handler clockHandler = new Handler(Looper.getMainLooper());

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            updateClock();
            clockHandler.postDelayed(this, 1000);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui.prepareWindow(this);
        buildHome();
    }

    @Override protected void onResume() {
        super.onResume();
        buildHome();
        clockHandler.removeCallbacks(clockTick);
        clockHandler.post(clockTick);
    }

    @Override protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockTick);
    }

    private void buildHome() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 28), Ui.dp(this, 54), Ui.dp(this, 28), Ui.dp(this, 42));
        root.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        root.setGravity(Ui.alignmentGravity(this));
        setContentView(root);

        if (Ui.prefs(this).getBoolean("show_datetime", true)) {
            timeView = Ui.text(this, "", 61f);
            dateView = Ui.text(this, "", 20f);
            timeView.setGravity(Ui.alignmentGravity(this));
            dateView.setGravity(Ui.alignmentGravity(this));
            root.addView(timeView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            root.addView(dateView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            updateClock();
        } else {
            timeView = null;
            dateView = null;
        }

        Space spacer = new Space(this);
        root.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1f));

        List<AppEntry> apps = AppRepository.loadApps(this, false);
        int count = Ui.prefs(this).getInt("home_count", 8);
        for (int i = 0; i < count; i++) {
            final int slot = i;
            String savedKey = Ui.prefs(this).getString("home_slot_" + i, "");
            AppEntry selected = AppRepository.findByKey(this, savedKey, false);
            if (selected == null && i < apps.size()) selected = apps.get(i);

            TextView row = Ui.text(this, selected == null ? "—" : selected.label, 31f);
            row.setGravity(Ui.alignmentGravity(this) | Gravity.CENTER_VERTICAL);
            row.setMinHeight(Ui.dp(this, 53));
            final AppEntry app = selected;
            row.setOnClickListener(v -> AppRepository.launch(this, app));
            row.setOnLongClickListener(v -> {
                chooseHomeApp(slot);
                return true;
            });
            root.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        GestureDetector gestures = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }

            @Override public void onLongPress(MotionEvent e) {
                openSettings();
            }

            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > Ui.dp(MainActivity.this, 80)) {
                    if (dy < 0) {
                        openDrawer();
                    } else if (Ui.prefs(MainActivity.this).getBoolean("notification_panel", false)) {
                        expandNotifications();
                    }
                    return true;
                }
                if (Math.abs(dx) > Ui.dp(MainActivity.this, 80)) {
                    String key = Ui.prefs(MainActivity.this)
                            .getString(dx < 0 ? "swipe_left" : "swipe_right", "");
                    AppRepository.launch(MainActivity.this,
                            AppRepository.findByKey(MainActivity.this, key, true));
                    return true;
                }
                return false;
            }
        });
        root.setOnTouchListener((v, event) -> gestures.onTouchEvent(event));
    }

    private void updateClock() {
        if (timeView == null || dateView == null) return;
        Date now = new Date();
        timeView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        String date = new SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now);
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int battery = bm == null ? -1 : bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        dateView.setText(battery >= 0 ? date + ", " + battery + "%" : date);
    }

    private void chooseHomeApp(int slot) {
        List<AppEntry> apps = AppRepository.loadApps(this, false);
        String[] labels = new String[apps.size() + 1];
        labels[0] = "— Пусто —";
        for (int i = 0; i < apps.size(); i++) labels[i + 1] = apps.get(i).label;
        new AlertDialog.Builder(this)
                .setTitle("Приложение " + (slot + 1))
                .setItems(labels, (dialog, which) -> {
                    String key = which == 0 ? "" : apps.get(which - 1).key();
                    Ui.prefs(this).edit().putString("home_slot_" + slot, key).apply();
                    buildHome();
                })
                .show();
    }

    private void openDrawer() {
        startActivity(new android.content.Intent(this, AppDrawerActivity.class));
        overridePendingTransition(0, 0);
    }

    private void openSettings() {
        startActivity(new android.content.Intent(this, SettingsActivity.class));
        overridePendingTransition(0, 0);
    }

    private void expandNotifications() {
        try {
            Object service = getSystemService("statusbar");
            Class<?> manager = Class.forName("android.app.StatusBarManager");
            manager.getMethod("expandNotificationsPanel").invoke(service);
        } catch (Exception ignored) {
        }
    }

    @Override public void onBackPressed() {
        // A launcher is the end of the back stack.
    }
}
