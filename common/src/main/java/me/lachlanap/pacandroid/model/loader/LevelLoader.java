package me.lachlanap.pacandroid.model.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import me.lachlanap.pacandroid.model.AndyAndroid;
import me.lachlanap.pacandroid.model.Grid;
import me.lachlanap.pacandroid.model.Level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LevelLoader {

    private static final String LEVELS_FILE = "builtin-levels.txt";
    private final LevelFileReader reader = new LevelFileReader();
    private final String[] files;

    public LevelLoader() {
        files = loadBuiltinLevels();
    }

    private String[] loadBuiltinLevels() {
        try {
            FileHandle levelsFile = Gdx.files.classpath(LEVELS_FILE);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            levelsFile.read()));
            try {
                List<String> lines = new ArrayList<String>();
                String line;

                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }

                return lines.toArray(new String[lines.size()]);
            } finally {
                br.close();
            }
        } catch (IOException ioe) {
            throw new LevelLoaderException("Couldn't read levels file", ioe);
        }
    }

    public Level loadNextLevel() {
        Level l = loadLevel(files[(int) (Math.random() * files.length)]);
        return l;
    }

    private Level loadLevel(String levelFile) {
        FileHandle handle;

        if (System.getProperty("level-file") != null) {
            levelFile = System.getProperty("level-file");
            handle = Gdx.files.absolute(levelFile);
        } else {
            handle = Gdx.files.classpath(levelFile);
        }

        if (!handle.exists()) {
            handle = Gdx.files.absolute(levelFile);

            if (!handle.exists()) {
                handle = Gdx.files.absolute("test-data/" + levelFile);
            }
        }

        if (!handle.exists())
            throw new LevelLoaderException("Can not find level: " + levelFile);

        try {
            InputStream in = handle.read();
            try {
                Level level = reader.readLevel(in, levelFile);
                setupLevel(level);

                return level;
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new LevelLoaderException("Unable to read level file: " + levelFile, e);
        }
    }

    protected void setupLevel(Level level) {
        spawnAndroid(level);
        randomisePowerups(level);
    }

    private void spawnAndroid(Level l) {
        Grid g = l.getGrid();

        AndyAndroid entity = new AndyAndroid(g, l);
        int x = 11;
        int y = 5;
        for (int i = 0; i < 10; i++) {
            if (g.isEmpty(x + i, y, Grid.GRID_WALL)) {
                x += i;
                break;
            } else if (g.isEmpty(x - i, y, Grid.GRID_WALL)) {
                x -= i;
                break;
            } else if (g.isEmpty(x, y + i, Grid.GRID_WALL)) {
                y += i;
                break;
            } else if (g.isEmpty(x, y - i, Grid.GRID_WALL)) {
                y -= i;
                break;
            }
        }

        entity.setPosition(new Vector2(
                x * Level.GRID_UNIT_SIZE,
                y * Level.GRID_UNIT_SIZE));

        l.spawnEntity(entity);
    }

    private void randomisePowerups(Level level) {
        Grid g = level.getGrid();
        int numberOfPowerups = 0;
        for (int i = 0; i < g.getWidth(); i++)
            for (int j = 0; j < g.getHeight(); j++)
                if (g.get(i, j) == Grid.GRID_POWERUP)
                    numberOfPowerups++;

        int powerupsToKeep = 3;
        int powerupsToRemove = numberOfPowerups - powerupsToKeep;

        while (powerupsToRemove > 0) {
            for (int i = 0; i < g.getWidth(); i++) {
                for (int j = 0; j < g.getHeight(); j++) {
                    if (g.get(i, j) == Grid.GRID_POWERUP) {
                        if (Math.random() > 0.8) {
                            g.set(i, j, Grid.GRID_JELLYBEAN);
                            powerupsToRemove--;
                        }
                    }
                }
            }
        }
    }
}
