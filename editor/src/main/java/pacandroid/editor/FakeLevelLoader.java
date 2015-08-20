/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pacandroid.editor;

import pacandroid.model.Level;
import pacandroid.model.loader.LevelLoader;

/**
 * @author lachlan
 */
public class FakeLevelLoader extends LevelLoader {

    private final Level level;

    public FakeLevelLoader(Level level) {
        this.level = level;
    }

    @Override
    public Level loadNextLevel() {
        setupLevel(level);
        return level;
    }
}
