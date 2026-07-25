package com.ivanwitt.wittlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

final class Ui {
    private Ui() {}

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("witt", Context.MODE_PRIVATE);
    }

    static int textColor(Context context) {
        return Ui.prefs(context).getBoolean("dark_theme", true) ? Color.WHITE : Color.BLACK;
    }

    static float scale(Context context) {
        return Ui.prefs(context).getFloat("text_scale", 1.0f);
    }

    static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static void prepareWindow(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    static TextView text(Context context, String value, float sp) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(textColor(context));
        view.setTextSize(sp * scale(context));
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(0, dp(context, 7), 0, dp(context, 7));
        view.setBackgroundColor(Color.TRANSPARENT);
        return view;
    }

    static int alignmentGravity(Context context) {
        String alignment = prefs(context).getString("alignment", "left");
        if ("center".equals(alignment)) return Gravity.CENTER_HORIZONTAL;
        if ("right".equals(alignment)) return Gravity.END;
        return Gravity.START;
    }
}
