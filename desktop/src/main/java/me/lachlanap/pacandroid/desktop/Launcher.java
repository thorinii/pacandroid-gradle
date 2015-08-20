/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import me.lachlanap.pacandroid.AppLog;
import me.lachlanap.pacandroid.DesktopLog;
import me.lachlanap.pacandroid.PacAndroidGame;

/**
 * @author lachlan
 */
public class Launcher {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 1)
            System.setProperty("level-file", args[0]);

        AppLog.init(new DesktopLog());

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "PacAndroid";
        config.width = 1280;
        config.height = 800;

        PacAndroidGame game = new PacAndroidGame();
        LwjglApplication app = new LwjglApplication(game, config);
    }
}
