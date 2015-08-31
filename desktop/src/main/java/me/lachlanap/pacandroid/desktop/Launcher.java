/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import me.lachlanap.pacandroid.DesktopLog;
import me.lachlanap.pacandroid.PacAndroidGame;
import me.lachlanap.pacandroid.controller.LevelController;
import me.lachlanap.pacandroid.exceptions.GameException;
import me.lachlanap.pacandroid.model.Level;
import me.lachlanap.pacandroid.model.loader.LevelLoader;
import me.lachlanap.pacandroid.recorder.GameRecorder;
import me.lachlanap.pacandroid.util.AppLog;
import org.encog.Encog;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.networks.training.TrainingError;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.Format;

import java.io.File;

/**
 * @author lachlan
 */
public class Launcher {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String mode;
        if (args.length == 1)
            mode = args[0];
        else if (System.getProperty("pa.mode") != null)
            mode = System.getProperty("pa.mode");
        else
            mode = "";

        PacAndroidGame.Mode parsedMode = parseMode(mode);
        System.out.println("PacAndroid in mode " + parsedMode + " (" + mode + ")");

        AppLog.init(new DesktopLog());

        if (parsedMode == PacAndroidGame.Mode.TrainingNeuralNetworkHeadless) {
            trainHeadless();
        } else {

            LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
            config.title = "PacAndroid";
            config.width = 1280;
            config.height = 800;

            PacAndroidGame game = new PacAndroidGame(parsedMode);
            LwjglApplication app = new LwjglApplication(game, config);
        }
    }

    private static PacAndroidGame.Mode parseMode(String mode) {
        switch (mode) {
            case "":
            case "playing":
                return PacAndroidGame.Mode.Playing;
            case "recording":
                return PacAndroidGame.Mode.PlayingAndRecording;
            case "ai":
                return PacAndroidGame.Mode.PlayingWithNeuralNetwork;
            case "headtraining":
                return PacAndroidGame.Mode.TrainingNeuralNetworkHeaded;
            case "headlesstraining":
                return PacAndroidGame.Mode.TrainingNeuralNetworkHeadless;
            default:
                System.out.println("Mode must be one of: playing, recording, ai, headtraining, headlesstraining");
                throw new IllegalArgumentException("Illegal mode: " + mode);
        }
    }

    private static void trainHeadless() {
        Gdx.files = new LwjglFiles();

        File localNetwork = new File("network.eg");
        final NEATPopulation population;
        if(localNetwork.exists()) {
            population = (NEATPopulation) EncogDirectoryPersistence.loadObject(localNetwork);
        } else {
            population = new NEATPopulation(77, 4, 1000);
            population.reset();
        }

        CalculateScore fitnessFunction = new PacAndroidFitnessFunction();

        TrainEA trainer = NEATUtil.constructNEATTrainer(population, fitnessFunction);

        final long start = System.currentTimeMillis();
        do {
            trainer.iteration();

            final long current = System.currentTimeMillis();
            final long elapsedSeconds = (current - start) / 1000;

            int iteration = trainer.getIteration();

            System.out.println("Iteration #" + Format.formatInteger(iteration)
                                       + " Best:" + Format.formatDouble(trainer.getBestGenome().getScore(), 0)
                                       + " elapsed time = " + Format.formatTimeSpan((int) elapsedSeconds));

            if (iteration % 10 == 0) {
                EncogDirectoryPersistence.saveObject(new File("network.eg"), trainer.getMethod());
            }
        } while (start > -1);
        trainer.finishTraining();

        EncogDirectoryPersistence.saveObject(new File("network.eg"), trainer.getMethod());
        Encog.getInstance().shutdown();
    }

    private static class PacAndroidFitnessFunction implements CalculateScore {
        @Override
        public double calculateScore(final MLMethod method) {
            final NEATNetwork network = (NEATNetwork) method;
            LevelLoader loader = new LevelLoader();
            Level level = loader.loadNextLevel();

            final int MAX_FRAMES = 20000;
            final float FRAME_DELTA = 1 / 30f;

            LevelController controller = new LevelController(level);
            GameRecorder gameRecorder = new GameRecorder(controller, false);
            gameRecorder.setListener(new GameRecorder.Listener() {
                @Override
                public void onSnapshotTaken(int tick, double[] gameState, double[] inputState) {
                    if (tick % 3 != 0) return;
//                if (true) return;
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

            int frame;
            for (frame = 0; frame < MAX_FRAMES; frame++) {
                updateLevel(level, controller, FRAME_DELTA);
                gameRecorder.takeSnapshot();

                if (level.isGameOver())
                    break;
            }

//            return level.getScore().getScore() + level.getLives() * 100;
//            return level.getLives() * 100;
            return frame / 10.0 + level.getScore().getScore() * 100.0;
        }

        private void updateLevel(Level level, LevelController controller, float delta) {
            try {
                level.removeDead();

                if (!level.isGameOver())
                    level.update(delta);
                controller.update(delta);
            } catch (GameException e) {
                throw new TrainingError(e);
            }
        }

        @Override
        public boolean shouldMinimize() {
            return false;
        }

        @Override
        public boolean requireSingleThreaded() {
            return false;
        }
    }
}
