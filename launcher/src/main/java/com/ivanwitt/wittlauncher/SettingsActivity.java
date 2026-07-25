package com.ivanwitt.wittlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {
    private LinearLayout content;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui.prepareWindow(this);
        build();
    }

    @Override protected void onResume() {
        super.onResume();
        build();
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(Ui.dp(this, 26), Ui.dp(this, 54), Ui.dp(this, 26), Ui.dp(this, 42));
        scroll.removeAllViews();
        scroll.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        TextView title = Ui.text(this, "Witt louncher", 42f);
        title.setPadding(0, 0, 0, Ui.dp(this, 18));
        content.addView(title);

        addRow("Изменить лаунчер по умолчанию", "", v -> openHomeSettings());
        addRow("О Witt louncher", "ⓘ", v -> showAbout());

        addSection("Главный экран");
        addRow("Количество приложений на главном экране",
                String.valueOf(Ui.prefs(this).getInt("home_count", 8)), v -> chooseHomeCount());
        addRow("Показывать дату и время", onOff(Ui.prefs(this).getBoolean("show_datetime", true)),
                v -> toggle("show_datetime", true));
        addRow("Расположение приложений", alignmentName(), v -> chooseAlignment());
        addRow("Приложения главного экрана", "Настроить", v -> chooseHomeSlot());

        addSection("Поиск");
        addRow("Автопоказ клавиатуры", onOff(Ui.prefs(this).getBoolean("auto_keyboard", true)),
                v -> toggle("auto_keyboard", true));
        addRow("Автооткрытие единственного совпадения", onOff(Ui.prefs(this).getBoolean("auto_unique", true)),
                v -> toggle("auto_unique", true));

        addSection("Оформление");
        addRow("Панель уведомлений", onOff(Ui.prefs(this).getBoolean("notification_panel", false)),
                v -> toggle("notification_panel", false));
        addRow("Тема", Ui.prefs(this).getBoolean("dark_theme", true) ? "Темная" : "Светлая",
                v -> toggle("dark_theme", true));
        addRow("Размер текста", String.format(java.util.Locale.US, "%.1f", Ui.scale(this)),
                v -> chooseTextScale());

        addSection("Жесты");
        addRow("Свайп влево", gestureName("swipe_left"), v -> chooseGestureApp("swipe_left", "Свайп влево"));
        addRow("Свайп вправо", gestureName("swipe_right"), v -> chooseGestureApp("swipe_right", "Свайп вправо"));

        addSection("Приложения");
        addRow("Скрытые приложения", String.valueOf(hiddenCount()), v -> showHiddenApps());

        TextView footer = Ui.text(this,
                "Долгое нажатие на свободное место главного экрана — настройки.\n" +
                "Свайп вверх — список приложений. В поиске поставьте пробел первым символом, чтобы временно отключить автооткрытие.", 16f);
        footer.setPadding(0, Ui.dp(this, 24), 0, 0);
        content.addView(footer);
    }

    private void addSection(String text) {
        TextView header = Ui.text(this, text, 34f);
        header.setPadding(0, Ui.dp(this, 24), 0, Ui.dp(this, 12));
        content.addView(header);
    }

    private void addRow(String label, String value, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, Ui.dp(this, 9), 0, Ui.dp(this, 9));
        row.setMinimumHeight(Ui.dp(this, 58));

        TextView left = Ui.text(this, label, 21f);
        TextView right = Ui.text(this, value, 21f);
        right.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(right, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setOnClickListener(listener);
        content.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private String onOff(boolean value) {
        return value ? "Вкл" : "Выкл";
    }

    private void toggle(String key, boolean defaultValue) {
        boolean current = Ui.prefs(this).getBoolean(key, defaultValue);
        Ui.prefs(this).edit().putBoolean(key, !current).apply();
        build();
    }

    private void chooseHomeCount() {
        String[] choices = {"4", "5", "6", "7", "8", "9", "10", "11", "12"};
        int current = Ui.prefs(this).getInt("home_count", 8);
        int checked = Math.max(0, Math.min(choices.length - 1, current - 4));
        new AlertDialog.Builder(this)
                .setTitle("Количество приложений")
                .setSingleChoiceItems(choices, checked, (dialog, which) -> {
                    Ui.prefs(this).edit().putInt("home_count", which + 4).apply();
                    dialog.dismiss();
                    build();
                })
                .show();
    }

    private void chooseAlignment() {
        String[] values = {"Слева", "По центру", "Справа"};
        String current = Ui.prefs(this).getString("alignment", "left");
        int checked = "center".equals(current) ? 1 : ("right".equals(current) ? 2 : 0);
        new AlertDialog.Builder(this)
                .setTitle("Расположение приложений")
                .setSingleChoiceItems(values, checked, (dialog, which) -> {
                    String value = which == 1 ? "center" : (which == 2 ? "right" : "left");
                    Ui.prefs(this).edit().putString("alignment", value).apply();
                    dialog.dismiss();
                    build();
                })
                .show();
    }

    private String alignmentName() {
        String value = Ui.prefs(this).getString("alignment", "left");
        if ("center".equals(value)) return "По центру";
        if ("right".equals(value)) return "Справа";
        return "Слева";
    }

    private void chooseTextScale() {
        String[] labels = {"0.8", "0.9", "1.0", "1.1", "1.2", "1.3", "1.4"};
        float current = Ui.scale(this);
        int checked = Math.round((current - 0.8f) * 10f);
        checked = Math.max(0, Math.min(labels.length - 1, checked));
        new AlertDialog.Builder(this)
                .setTitle("Размер текста")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    Ui.prefs(this).edit().putFloat("text_scale", 0.8f + which * 0.1f).apply();
                    dialog.dismiss();
                    build();
                })
                .show();
    }

    private void chooseHomeSlot() {
        int count = Ui.prefs(this).getInt("home_count", 8);
        String[] slots = new String[count];
        for (int i = 0; i < count; i++) {
            AppEntry app = AppRepository.findByKey(this,
                    Ui.prefs(this).getString("home_slot_" + i, ""), true);
            slots[i] = (i + 1) + ". " + (app == null ? "Не назначено" : app.label);
        }
        new AlertDialog.Builder(this)
                .setTitle("Главный экран")
                .setItems(slots, (dialog, which) -> chooseAppForSlot(which))
                .show();
    }

    private void chooseAppForSlot(int slot) {
        List<AppEntry> apps = AppRepository.loadApps(this, false);
        String[] labels = new String[apps.size() + 1];
        labels[0] = "— Пусто —";
        for (int i = 0; i < apps.size(); i++) labels[i + 1] = apps.get(i).label;
        new AlertDialog.Builder(this)
                .setTitle("Приложение " + (slot + 1))
                .setItems(labels, (dialog, which) -> {
                    String key = which == 0 ? "" : apps.get(which - 1).key();
                    Ui.prefs(this).edit().putString("home_slot_" + slot, key).apply();
                    build();
                })
                .show();
    }

    private void chooseGestureApp(String prefKey, String title) {
        List<AppEntry> apps = AppRepository.loadApps(this, false);
        String[] labels = new String[apps.size() + 1];
        labels[0] = "Выкл";
        for (int i = 0; i < apps.size(); i++) labels[i + 1] = apps.get(i).label;
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels, (dialog, which) -> {
                    String key = which == 0 ? "" : apps.get(which - 1).key();
                    Ui.prefs(this).edit().putString(prefKey, key).apply();
                    build();
                })
                .show();
    }

    private String gestureName(String key) {
        AppEntry app = AppRepository.findByKey(this, Ui.prefs(this).getString(key, ""), true);
        return app == null ? "Выкл" : app.label;
    }

    private int hiddenCount() {
        int count = 0;
        for (AppEntry app : AppRepository.loadApps(this, true)) {
            if (Ui.prefs(this).getBoolean("hidden:" + app.key(), false)) count++;
        }
        return count;
    }

    private void showHiddenApps() {
        List<AppEntry> hidden = new ArrayList<>();
        for (AppEntry app : AppRepository.loadApps(this, true)) {
            if (Ui.prefs(this).getBoolean("hidden:" + app.key(), false)) hidden.add(app);
        }
        if (hidden.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Скрытые приложения")
                    .setMessage("Скрытых приложений нет.").setPositiveButton("ОК", null).show();
            return;
        }
        String[] labels = new String[hidden.size()];
        for (int i = 0; i < hidden.size(); i++) labels[i] = hidden.get(i).label;
        new AlertDialog.Builder(this)
                .setTitle("Нажмите, чтобы вернуть")
                .setItems(labels, (dialog, which) -> {
                    Ui.prefs(this).edit().putBoolean("hidden:" + hidden.get(which).key(), false).apply();
                    build();
                })
                .show();
    }

    private void openHomeSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle("Witt louncher")
                .setMessage("Автор: Ivan Witt\nРабочая почта: svc.witt@gmail.com\n\n" +
                        "Минималистичный Android-лаунчер с текстовым главным экраном и быстрым поиском приложений. " +
                        "Функциональная концепция вдохновлена открытым проектом Olauncher.")
                .setPositiveButton("ОК", null)
                .setNeutralButton("Написать", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:svc.witt@gmail.com")));
                    } catch (Exception ignored) {}
                })
                .show();
    }
}
