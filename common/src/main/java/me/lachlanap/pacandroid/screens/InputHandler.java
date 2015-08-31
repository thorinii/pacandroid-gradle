package me.lachlanap.pacandroid.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import me.lachlanap.pacandroid.controller.LevelController;
import me.lachlanap.pacandroid.controller.SteeringController;

public class InputHandler implements InputProcessor {
    private final LevelController controller;
    private final SteeringController steeringController;

    public InputHandler(LevelController controller, SteeringController steeringController) {
        this.controller = controller;
        this.steeringController = steeringController;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
                controller.leftPressed();
                break;
            case Input.Keys.RIGHT:
                controller.rightPressed();
                break;
            case Input.Keys.UP:
                controller.upPressed();
                break;
            case Input.Keys.DOWN:
                controller.downPressed();
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
                controller.leftReleased();
                break;
            case Input.Keys.RIGHT:
                controller.rightReleased();
                break;
            case Input.Keys.UP:
                controller.upReleased();
                break;
            case Input.Keys.DOWN:
                controller.downReleased();
                break;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer,
                             int button) {
        steeringController.touchDown(screenX, screenY);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        steeringController.touchUp(screenX, screenY);

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        steeringController.touchDragged(screenX, screenY);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return true;
    }
}
