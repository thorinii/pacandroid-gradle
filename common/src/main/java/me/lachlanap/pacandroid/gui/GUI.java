/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import me.lachlanap.pacandroid.view.fonts.FontRenderer;

/**
 * @author lachlan
 */
public class GUI {

    private final Stage stage;
    private Texture background;
    private final Vector2 size;

    public GUI(int width, int height) {
        size = new Vector2(width, height);
        stage = new Stage(new StretchViewport(width, height));
    }

    public void setFontRenderer(FontRenderer renderer) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof UsesFonts) {
                ((UsesFonts) actor).setFont(renderer);
            }
        }
    }

    public void resize(int width, int height) {
        stage.setViewport(new ExtendViewport(size.x, size.y));
        stage.getCamera().translate(-stage.getWidth(), -stage.getHeight(), 0);
    }

    public void enable() {
        Gdx.input.setInputProcessor(stage);
    }

    public void disable() {
        Gdx.input.setInputProcessor(null);
    }

    public void dispose() {
        stage.dispose();
    }

    public void setBackground(Texture background) {
        this.background = background;
    }

    public void draw() {
        if (background != null) {
            SpriteBatch batch = (SpriteBatch) stage.getBatch();
            batch.begin();
            batch.draw(background, 0, 0);
            batch.end();
        }

        stage.draw();
    }

    public void add(GUIComponent guiComponent) {
        stage.addActor(guiComponent);
    }
}
