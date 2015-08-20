/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.editor;

import me.lachlanap.pacandroid.model.Level;
import me.lachlanap.pacandroid.model.loader.LevelLoader;

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
