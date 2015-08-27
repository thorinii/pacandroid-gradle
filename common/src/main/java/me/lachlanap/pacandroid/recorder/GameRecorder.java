package me.lachlanap.pacandroid.recorder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import me.lachlanap.pacandroid.controller.LevelController;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lachlan on 25/08/2015.
 */
public class GameRecorder {
    private final ScreenRecorder screenRecorder;
    private final LevelController levelController;
    private final DataOutputStream out;
    private int tick;

    public GameRecorder(LevelController levelController) {
        this.levelController = levelController;

        FileHandle outDir = Gdx.files.absolute("/tmp").child("pa_" + Gdx.app.hashCode());
        if (outDir.exists()) outDir.deleteDirectory();
        outDir.mkdirs();

        this.screenRecorder = new ScreenRecorder(outDir);

        try {
            this.out = new DataOutputStream(new FileOutputStream(outDir.child("player_input.bin").file()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to open player_input.bin", e);
        }
    }

    public void takeSnapshot() {
        screenRecorder.takeScreenshot(tick);

        byte inputState = levelController.getPackedInputState();
        try {
            out.writeInt(tick);
            out.writeByte(inputState);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tick++;
    }
}
