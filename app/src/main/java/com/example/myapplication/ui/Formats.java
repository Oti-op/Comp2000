package com.example.myapplication.ui;

import android.content.Context;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Keeps dates, times and people counts worded the same way on every screen. */
public final class Formats {

    private Formats() {
    }

    public static String day(long time) {
        return new SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(new Date(time));
    }

    public static String time(long time) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(time));
    }

    /** For example "Wed 4 Nov, 14:00–15:00". */
    public static String range(Context c, long start, long end) {
        return c.getString(R.string.time_range, day(start), time(start), time(end));
    }

    /** For example "4 people". */
    public static String people(Context c, int n) {
        return c.getResources().getQuantityString(R.plurals.people_count, n, n);
    }
}
