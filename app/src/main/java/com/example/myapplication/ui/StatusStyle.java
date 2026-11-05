package com.example.myapplication.ui;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

/** Colours a status pill. The pill's words and icon are set by the caller. */
final class StatusStyle {

    private StatusStyle() {
    }

    static void apply(TextView pill, @ColorRes int background, @ColorRes int foreground) {
        int bg = ContextCompat.getColor(pill.getContext(), background);
        int fg = ContextCompat.getColor(pill.getContext(), foreground);
        pill.setBackgroundTintList(ColorStateList.valueOf(bg));
        pill.setTextColor(fg);
        TextViewCompat.setCompoundDrawableTintList(pill, ColorStateList.valueOf(fg));
    }
}
