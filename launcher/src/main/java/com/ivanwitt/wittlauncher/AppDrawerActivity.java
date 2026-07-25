package com.ivanwitt.wittlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppDrawerActivity extends Activity {
    private LinearLayout list;
    private EditText search;
    private List<AppEntry> allApps = new ArrayList<>();
    private List<AppEntry> visibleApps = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingLaunch;
    private String lastAutoQuery = "";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui.prepareWindow(this);
        build();
    }

    @Override protected void onResume() {
        super.onResume();
        allApps = AppRepository.loadApps(this, false);
        filter(search == null ? "" : search.getText().toString());
    }

    private void build() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 26), Ui.dp(this, 42), Ui.dp(this, 24), Ui.dp(this, 12));
        root.setBackgroundColor(Color.TRANSPARENT);
        setContentView(root);

        search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Поиск приложений");
        search.setHintTextColor((Ui.textColor(this) & 0x00FFFFFF) | 0x88000000);
        search.setTextColor(Ui.textColor(this));
        search.setTextSize(20f * Ui.scale(this));
        search.setBackgroundColor(Color.TRANSPARENT);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 8));
        root.addView(search, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        allApps = AppRepository.loadApps(this, false);
        filter("");

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        search.setOnEditorActionListener((v, actionId, event) -> {
            boolean searchAction = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            if (!searchAction) return false;
            if (!visibleApps.isEmpty()) {
                AppRepository.launch(this, visibleApps.get(0));
                finish();
            } else {
                String query = search.getText().toString().trim();
                if (!query.isEmpty()) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))));
                    } catch (Exception ignored) {}
                }
            }
            return true;
        });

        scroll.setOnTouchListener(new View.OnTouchListener() {
            float downY;
            @Override public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) downY = event.getY();
                if (event.getAction() == MotionEvent.ACTION_MOVE) hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_UP && event.getY() - downY > Ui.dp(AppDrawerActivity.this, 130)) {
                    finish();
                    overridePendingTransition(0, 0);
                }
                return false;
            }
        });

        if (Ui.prefs(this).getBoolean("auto_keyboard", true)) {
            search.requestFocus();
            search.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
            }, 120);
        }
    }

    private void filter(String raw) {
        if (allApps == null) return;
        boolean disableAuto = raw.startsWith(" ");
        String query = disableAuto ? raw.substring(1).trim() : raw.trim();
        String needle = query.toLowerCase(Locale.getDefault());
        visibleApps = new ArrayList<>();
        for (AppEntry app : allApps) {
            if (needle.isEmpty() || app.label.toLowerCase(Locale.getDefault()).contains(needle)) {
                visibleApps.add(app);
            }
        }
        renderList();

        if (pendingLaunch != null) handler.removeCallbacks(pendingLaunch);
        if (!disableAuto && !needle.isEmpty() && visibleApps.size() == 1 && !needle.equals(lastAutoQuery)) {
            AppEntry only = visibleApps.get(0);
            lastAutoQuery = needle;
            pendingLaunch = () -> {
                if (!isFinishing()) {
                    AppRepository.launch(this, only);
                    finish();
                    overridePendingTransition(0, 0);
                }
            };
            handler.postDelayed(pendingLaunch, 120);
        } else if (visibleApps.size() != 1) {
            lastAutoQuery = "";
        }
    }

    private void renderList() {
        list.removeAllViews();
        int gravity = Ui.alignmentGravity(this);
        for (AppEntry app : visibleApps) {
            TextView row = Ui.text(this, app.label, 28f);
            row.setGravity(gravity | Gravity.CENTER_VERTICAL);
            row.setMinHeight(Ui.dp(this, 55));
            row.setOnClickListener(v -> {
                AppRepository.launch(this, app);
                finish();
                overridePendingTransition(0, 0);
            });
            row.setOnLongClickListener(v -> {
                showAppMenu(row, app);
                return true;
            });
            list.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private void showAppMenu(View anchor, AppEntry app) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Переименовать");
        menu.getMenu().add("Скрыть");
        menu.setOnMenuItemClickListener(item -> {
            if ("Переименовать".contentEquals(item.getTitle())) rename(app);
            if ("Скрыть".contentEquals(item.getTitle())) {
                Ui.prefs(this).edit().putBoolean("hidden:" + app.key(), true).apply();
                allApps = AppRepository.loadApps(this, false);
                filter(search.getText().toString());
            }
            return true;
        });
        menu.show();
    }

    private void rename(AppEntry app) {
        EditText input = new EditText(this);
        input.setText(app.label);
        input.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this)
                .setTitle("Переименовать")
                .setView(input)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String value = input.getText().toString().trim();
                    Ui.prefs(this).edit().putString("alias:" + app.key(), value).apply();
                    allApps = AppRepository.loadApps(this, false);
                    filter(search.getText().toString());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
    }
}
