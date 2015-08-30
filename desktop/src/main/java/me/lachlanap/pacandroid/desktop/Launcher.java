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
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.MLResettable;
import org.encog.ml.MethodFactory;
import org.encog.ml.genetic.MLMethodGeneticAlgorithm;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.TrainingError;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.Format;
import org.encog.util.obj.ObjectCloner;

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
        final BasicNetwork network;
        if(localNetwork.exists()) {
            network = (BasicNetwork) EncogDirectoryPersistence.loadObject(localNetwork);
        } else {network = new BasicNetwork();
            network.addLayer(new BasicLayer(null, true, 77));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 30));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 10));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 8));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 4));
            network.getStructure().finalizeStructure();
            network.reset();
        }

        CalculateScore fitnessFunction = new PacAndroidFitnessFunction();

        MLMethodGeneticAlgorithm trainer = new MLMethodGeneticAlgorithm(new MethodFactory() {
            @Override
            public MLMethod factor() {
                MLMethod result = (MLMethod) ObjectCloner.deepCopy(network);
                ((MLResettable) result).reset();
                return result;
            }
        }, fitnessFunction, 150);


        final long start = System.currentTimeMillis();
        do {
            trainer.iteration();

            final long current = System.currentTimeMillis();
            final long elapsedSeconds = (current - start) / 1000;

            int iteration = trainer.getIteration();

            System.out.println("Iteration #" + Format.formatInteger(iteration)
                                       + " Best:" + Format.formatDouble(trainer.getGenetic().getBestGenome().getScore(), 0)
                                       + " elapsed time = " + Format.formatTimeSpan((int) elapsedSeconds));

            if(iteration % 10 == 0) {
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
            final BasicNetwork network = (BasicNetwork) method;
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
                    network.compute(gameState, inputState);

                    if (inputState[0] > Math.random()) controller.leftPressed();
                    else controller.leftReleased();
                    if (inputState[1] > Math.random()) controller.rightPressed();
                    else controller.rightReleased();
                    if (inputState[2] > Math.random()) controller.upPressed();
                    else controller.upReleased();
                    if (inputState[3] > Math.random()) controller.downPressed();
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
            return frame + level.getScore().getScore()*10;
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
