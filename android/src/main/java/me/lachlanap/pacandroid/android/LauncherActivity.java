package me.lachlanap.pacandroid.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import me.lachlanap.pacandroid.AppLog;
import me.lachlanap.pacandroid.PacAndroidGame;

public class LauncherActivity extends AndroidApplication {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useWakelock = true;

        AppLog.init(new AndroidLog());
        initialize(new PacAndroidGame(), config);
    }
}
