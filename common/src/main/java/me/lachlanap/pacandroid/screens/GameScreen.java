/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import me.lachlanap.pacandroid.PacAndroidGame;
import me.lachlanap.pacandroid.controller.LevelController;
import me.lachlanap.pacandroid.controller.SteeringController;
import me.lachlanap.pacandroid.model.Level;
import me.lachlanap.pacandroid.recorder.GameRecorder;
import me.lachlanap.pacandroid.stats.HeatMap;
import me.lachlanap.pacandroid.util.AppLog;
import me.lachlanap.pacandroid.view.DefaultLevelRenderer;
import me.lachlanap.pacandroid.view.LevelRenderer;
import me.lachlanap.pacandroid.view.fonts.FontRenderer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * @author lachlan
 */
public class GameScreen extends AbstractScreen {

    /**
     * The size of 1 grid square (or 2 units): 32px
     */
    public static final int GRID_UNIT = 55;
    public static final float FRAME_DELTA = 1f / 30;
    private final Level level;
    private LevelController controller;
    private LevelRenderer[] renderers;
    private SteeringController steeringController;
    private final FontRenderer fontRenderer;
    private GameRecorder gameRecorder;
    //
    private float lastSmallDelta;

    public GameScreen(PacAndroidGame game, Level level, FontRenderer fontRenderer) {
        super(game);
        this.level = level;
        this.fontRenderer = fontRenderer;
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        boolean shouldUpdate = delta >= FRAME_DELTA || lastSmallDelta >= FRAME_DELTA;
        if (shouldUpdate) {
            updateLevel(FRAME_DELTA);
            lastSmallDelta = 0;
        } else {
            lastSmallDelta += delta;
        }

        renderLevel(FRAME_DELTA);

        if (shouldUpdate) {
            gameRecorder.takeSnapshot();
        }
    }

    private void updateLevel(float delta) {
        try {
            level.removeDead();

            if (!level.isGameOver())
                level.update(delta);
            controller.update(delta);
        } catch (Exception e) {
            AppLog.l("Error: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void renderLevel(float delta) {
        for (LevelRenderer renderer : renderers)
            renderer.render(delta);
    }

    @Override
    public void show() {
        super.show();

        controller = new LevelController(level);

        steeringController = new SteeringController(controller, getScreenSize());
        steeringController.setRoot(new Vector2(150, 150));

        DefaultLevelRenderer renderer = new DefaultLevelRenderer(
                (int) getScreenSize().x, (int) getScreenSize().y,
                level, fontRenderer);
        renderer.setSteeringController(steeringController);

        renderers = new LevelRenderer[]{
                renderer, //new DebugWorldRenderer(false, level)
        };

        Gdx.input.setInputProcessor(new InputHandler(controller, steeringController));

        this.gameRecorder = new GameRecorder(controller);
    }

    private void writeHeatmap() {
        writeHeatmapToHTTP();

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            try {
                writeHeatmapToFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void writeHeatmapToHTTP() {
        final String type = Gdx.app.getType().name().toLowerCase();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL dest = new URL("http://www.terrifictales.net/pa/stat-heatmap.php"
                                               + "?kqwu=aSD8dh2s09d2"
                                               + "&level=" + level.getName()
                                               + "&client=" + type);
                    HttpURLConnection connection = (HttpURLConnection) dest.openConnection();

                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content/Type", "application/octet-stream");
                    connection.setRequestMethod("POST");

                    writeHeatmap(connection.getOutputStream(), level.getHeatMap());

                    InputStream is = connection.getInputStream();
                    byte[] buf = new byte[1024];
                    System.out.println("Reading (" + connection.getResponseCode() + ")");
                    while (is.read(buf) != -1) ;
                    is.close();


                    dest = new URL("http://www.terrifictales.net/pa/stat-deathmap.php"
                                           + "?akeu=d83hs7uJsjeSufdk"
                                           + "&level=" + level.getName()
                                           + "&client=" + type);
                    connection = (HttpURLConnection) dest.openConnection();

                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content/Type", "application/octet-stream");
                    connection.setRequestMethod("POST");

                    writeHeatmap(connection.getOutputStream(), level.getDeathMap());

                    is = connection.getInputStream();
                    System.out.println("Reading (" + connection.getResponseCode() + ")");
                    while (is.read(buf) != -1) ;
                    is.close();

                    System.out.println("Uploaded data");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }).start();
    }

    private void writeHeatmapToFile() throws IOException {
        if (!Gdx.files.isExternalStorageAvailable())
            return;
        FileHandle fh = Gdx.files.external(".me.lachlanap.pacandroid/stats/");
        if (!fh.exists())
            fh.mkdirs();

        String uuid = UUID.randomUUID().toString();
        try {
            writeHeatmap(
                    fh.child("heatmap-" + uuid + ".dat").write(false),
                    level.getHeatMap());
            writeHeatmap(
                    fh.child("heatmap-" + uuid + ".dat").write(false),
                    level.getDeathMap());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void writeHeatmap(OutputStream heatmap, HeatMap map) throws IOException {
        DataOutputStream dos = new DataOutputStream(heatmap);
        try {
            map.writeOut(dos, level.getGrid());
            dos.flush();
        } finally {
            dos.close();
        }
    }
}
