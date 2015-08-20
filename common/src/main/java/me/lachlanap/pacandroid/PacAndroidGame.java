/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid;

import com.badlogic.gdx.Game;
import me.lachlanap.pacandroid.model.Level;
import me.lachlanap.pacandroid.model.loader.LevelLoader;
import me.lachlanap.pacandroid.screens.GameScreen;
import me.lachlanap.pacandroid.screens.MainMenuScreen;
import me.lachlanap.pacandroid.view.fonts.FontRenderer;

/**
 * @author lachlan
 */
public class PacAndroidGame extends Game {

    private LevelLoader loader;
    private FontRenderer fontRenderer;

    public PacAndroidGame(LevelLoader levelLoader) {
        this.loader = levelLoader;
    }

    public PacAndroidGame() {
    }

    @Override
    public void create() {
        if (this.loader == null)
            this.loader = new LevelLoader();

        fontRenderer = new FontRenderer();
        fontRenderer.setFont("BenderSolid");

        //mainMenu();
        play();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void play() {
        Level level = loader.loadNextLevel();
        setScreen(new GameScreen(this, level, fontRenderer));
    }

    public void mainMenu() {
        setScreen(new MainMenuScreen(this));
    }
}
