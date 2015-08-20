package me.lachlanap.pacandroid.android;

import android.util.Log;
import me.lachlanap.pacandroid.AppLog;

public class AndroidLog implements AppLog.Log {

    @Override
    public void log(String text) {
        Log.i("log", text);
    }
}
