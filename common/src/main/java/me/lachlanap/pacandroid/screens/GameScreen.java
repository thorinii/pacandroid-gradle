/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import me.lachlanap.pacandroid.PacAndroidGame;
import me.lachlanap.pacandroid.controller.LevelController;
import me.lachlanap.pacandroid.controller.SteeringController;
import me.lachlanap.pacandroid.model.Level;
import me.lachlanap.pacandroid.recorder.GameRecorder;
import me.lachlanap.pacandroid.util.AppLog;
import me.lachlanap.pacandroid.view.DefaultLevelRenderer;
import me.lachlanap.pacandroid.view.LevelRenderer;
import me.lachlanap.pacandroid.view.fonts.FontRenderer;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATPopulation;
import org.encog.persist.EncogDirectoryPersistence;

import java.nio.file.Paths;

/**
 * @author lachlan
 */
public class GameScreen extends AbstractScreen {

    /**
     * The size of 1 grid square (or 2 units): 32px
     */
    public static final int GRID_UNIT = 55;
    public static final float FRAME_DELTA = 1f / 30;
    private final PacAndroidGame.Mode mode;
    private final Level level;
    private LevelController controller;
    private LevelRenderer[] renderers;
    private SteeringController steeringController;
    private final FontRenderer fontRenderer;
    private GameRecorder gameRecorder;
    //
    private float lastSmallDelta;

    public GameScreen(PacAndroidGame.Mode mode, PacAndroidGame game, Level level, FontRenderer fontRenderer) {
        super(game);
        this.mode = mode;
        this.level = level;
        this.fontRenderer = fontRenderer;
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        boolean shouldUpdate = delta >= FRAME_DELTA || lastSmallDelta >= FRAME_DELTA;
        if (shouldUpdate) {
            System.out.println(controller.toString());
            updateLevel(FRAME_DELTA);
            lastSmallDelta = 0;
        } else {
            lastSmallDelta += delta;
        }

        renderLevel(FRAME_DELTA);

        if (shouldUpdate && gameRecorder != null) {
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

        if (mode != PacAndroidGame.Mode.Playing)
            this.gameRecorder = new GameRecorder(controller,
                                                 mode == PacAndroidGame.Mode.PlayingAndRecording);

        if (mode == PacAndroidGame.Mode.PlayingWithNeuralNetwork)
            gameRecorder.setListener(new GameRecorder.Listener() {
                NEATPopulation network = (NEATPopulation) EncogDirectoryPersistence.loadObject(
                        Paths.get("network.eg").toAbsolutePath().toFile());

                @Override
                public void onSnapshotTaken(int tick, double[] gameState, double[] inputState) {
                    if (tick % 3 != 0) return;
                    MLData in = new BasicMLData(gameState);
                    MLData data = network.compute(in);

                    if (data.getData(0) > Math.random()) controller.leftPressed();
                    else controller.leftReleased();
                    if (data.getData(1) > Math.random()) controller.rightPressed();
                    else controller.rightReleased();
                    if (data.getData(2) > Math.random()) controller.upPressed();
                    else controller.upReleased();
                    if (data.getData(3) > Math.random()) controller.downPressed();
                    else controller.downReleased();
                }
            });
    }
}
