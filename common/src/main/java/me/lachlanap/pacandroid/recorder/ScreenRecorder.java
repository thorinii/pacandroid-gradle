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
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Records the rendered screen.
 */
public class ScreenRecorder {
    public static final int BACKLOG_TO_DROP_FRAMES = 5;
    public static final int DOWNSAMPLE_FACTOR = 16;
    private final ThreadPoolExecutor tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
    private final ExecutorService executor = tpe;
    private int counter;

    public void takeScreenshot() {
        int frameNumber = counter;
        counter++;

        if (frameNumber % 10 != 0)
            return;
        if (tpe.getQueue().size() > BACKLOG_TO_DROP_FRAMES) {
            System.out.println("Dropping a frame");
            return;
        }

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);

        scrapeScreen(width, height, pixmap);
        savePixmapAsync(frameNumber, pixmap);
    }

    private void scrapeScreen(int width, int height, Pixmap pixmap) {
        ByteBuffer pixels = pixmap.getPixels();
        Gdx.gl.glReadPixels(0, 0, width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
    }

    private void savePixmapAsync(final int frameNumber, final Pixmap pixmap) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Pixmap downsampled = downsample(pixmap);
                savePixmap(frameNumber, downsampled);
            }
        });
    }

    private Pixmap downsample(Pixmap in) {
        Pixmap out = new Pixmap(in.getWidth() / DOWNSAMPLE_FACTOR, in.getHeight() / DOWNSAMPLE_FACTOR, in.getFormat());
        out.drawPixmap(in, 0, 0, in.getWidth(), in.getHeight(), 0, 0, out.getWidth(), out.getHeight());
        return out;
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
