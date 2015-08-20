/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.gui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * @author lachlan
 */
public abstract class GUIComponent extends Actor {

    public void setCentre(float x, float y) {
        super.setPosition(x - getWidth() / 2, y - getHeight() / 2);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        draw((SpriteBatch) batch, parentAlpha);
    }

    public abstract void draw(SpriteBatch batch, float parentAlpha);
}
