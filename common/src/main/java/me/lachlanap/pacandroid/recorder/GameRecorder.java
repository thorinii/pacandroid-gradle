package me.lachlanap.pacandroid.recorder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import me.lachlanap.pacandroid.controller.LevelController;
import me.lachlanap.pacandroid.model.Apple;
import me.lachlanap.pacandroid.model.Grid;
import me.lachlanap.pacandroid.model.Level;
import me.lachlanap.pacandroid.model.Powerup;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Records player input, critical game state, and the screen to disk.
 */
public class GameRecorder {
    private final ScreenRecorder screenRecorder;
    private final LevelController levelController;
    private final DataOutputStream out;
    private Listener listener;
    private int tick;

    public GameRecorder(LevelController levelController, boolean writingToDisk) {
        this.levelController = levelController;

        FileHandle outDir = Gdx.files.local("training").child("pa_" + Math.round(Math.random() * 10000000));
        if (writingToDisk && outDir.exists()) outDir.deleteDirectory();
        if (writingToDisk) outDir.mkdirs();

        this.screenRecorder = new ScreenRecorder(outDir);

        if (writingToDisk) {
            try {
                this.out = new DataOutputStream(new FileOutputStream(outDir.child("player_input.bin").file()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to open player_input.bin", e);
            }
        } else
            this.out = null;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void takeSnapshot() {
//        screenRecorder.takeScreenshot(tick);


        Level level = levelController.getLevel();
        Grid grid = level.getGrid();
        Vector2 gridLockedAndyPosition = grid.pointToGrid(level.getAndyAndroid().getPosition());

        int SIGHT = 5;

        double[] wallSightGrid = new double[SIGHT * SIGHT];
        double[] rewardSightGrid = new double[SIGHT * SIGHT];
        for (int x = 0; x < SIGHT; x++) {
            for (int y = 0; y < SIGHT; y++) {
                int space = grid.get((int) gridLockedAndyPosition.x + x - SIGHT / 2,
                                     (int) gridLockedAndyPosition.y + y - SIGHT / 2, Grid.GRID_WALL);
                boolean isWall = Grid.isWall(space);
                boolean isReward = space == Grid.GRID_JELLYBEAN || space == Grid.GRID_POWERUP;

                wallSightGrid[x * SIGHT + y] = isWall ? 1.0 : 0.0;
                rewardSightGrid[x * SIGHT + y] = isReward ? 1.0 : 0.0;
            }
        }


        double[] enemySightGrid = new double[SIGHT * SIGHT];
        for (Apple apple : level.getEntitiesByType(Apple.class)) {
            Vector2 applePosition = grid.pointToGrid(apple.getPosition());
            applePosition.x = applePosition.x - gridLockedAndyPosition.x + SIGHT / 2;
            applePosition.y = applePosition.y - gridLockedAndyPosition.y + SIGHT / 2;

            if (applePosition.x >= 0 && applePosition.x < SIGHT
                    && applePosition.y >= 0 && applePosition.y < SIGHT) {
                enemySightGrid[((int) (applePosition.x * SIGHT + applePosition.y))] = 1.0;
            }
        }

        double[] inputState = new double[]{
                levelController.isLeft() ? 1.0 : 0.0,
                levelController.isRight() ? 1.0 : 0.0,
                levelController.isUp() ? 1.0 : 0.0,
                levelController.isDown() ? 1.0 : 0.0
        };
        double[] gameState = concatenate(wallSightGrid, rewardSightGrid, enemySightGrid, new double[]{
                Math.max(0.0, Math.min(1, level.getLives() / 3.0)),
                level.getCurrentPowerup() == Powerup.Edible ? 1.0 : 0.0
        });

        if (out != null) {
            try {
                for (double d : inputState)
                    out.writeDouble(d);
                for (double d : gameState)
                    out.writeDouble(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (listener != null)
            listener.onSnapshotTaken(tick, gameState, inputState);

        tick++;
    }

    private double[] concatenate(double[]... arrays) {
        int totalSize = 0;
        for (double[] array : arrays)
            totalSize += array.length;
        double[] tmp = new double[totalSize];
        int offset = 0;
        for (double[] array : arrays) {
            System.arraycopy(array, 0, tmp, offset, array.length);
            offset += array.length;
        }
        return tmp;
    }

    public interface Listener {
        void onSnapshotTaken(int tick, double[] gameState, double[] inputState);
    }
}
