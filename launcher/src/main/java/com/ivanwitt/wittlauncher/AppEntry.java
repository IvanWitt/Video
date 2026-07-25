package com.ivanwitt.wittlauncher;

import android.content.ComponentName;

final class AppEntry {
    final String label;
    final ComponentName component;

    AppEntry(String label, ComponentName component) {
        this.label = label;
        this.component = component;
    }

    String key() {
        return component.flattenToString();
    }
}
