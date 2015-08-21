package me.lachlanap.pacandroid.recorder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Records the rendered screen.
 */
public class ScreenRecorder {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private int counter;

    public void takeScreenshot() {
        int frameNumber = counter;
        counter++;

        if (frameNumber % 60 != 0)
            return;

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);

        scrapScreen(width, height, pixmap);
        savePixmapAsync(frameNumber, pixmap);
    }

    private void scrapScreen(int width, int height, Pixmap pixmap) {
        ByteBuffer pixels = pixmap.getPixels();
        Gdx.gl.glReadPixels(0, 0, width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
    }

    private void savePixmapAsync(final int frameNumber, final Pixmap pixmap) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                savePixmap(frameNumber, pixmap);
            }
        });
    }

    private void savePixmap(int frameNumber, Pixmap pixmap) {
        try {
            FileHandle out = Gdx.files.absolute(String.format("/tmp/screenshot.%05d.png", frameNumber));

            PixmapIO.PNG writer = new PixmapIO.PNG((int) (pixmap.getWidth() * pixmap.getHeight() * 1.5f));
            try {
                writer.setFlipY(true);
                writer.write(out, pixmap);
            } finally {
                writer.dispose();
                pixmap.dispose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
