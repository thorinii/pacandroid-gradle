package me.lachlanap.pacandroid.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.lachlanap.pacandroid.model.Entity;

public interface EntityRenderer {

    public void renderEntity(Entity entity, SpriteBatch batch,
            DefaultLevelRenderer renderer);
}
