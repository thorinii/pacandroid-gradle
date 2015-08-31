package me.lachlanap.pacandroid.controller;

import com.badlogic.gdx.math.Vector2;
import me.lachlanap.pacandroid.exceptions.GameException;
import me.lachlanap.pacandroid.model.*;
import me.lachlanap.pacandroid.util.Timers;

import java.util.ArrayList;
import java.util.List;

public class LevelController {

    private final Level level;
    private boolean left, right, up, down;
    private Vector2 touchControl;
    private int ticks;

    public LevelController(Level level) {
        this.level = level;

        this.ticks = 0;
    }

    public Level getLevel() {
        return level;
    }

    public void leftPressed() {
        left = true;
    }

    public void rightPressed() {
        right = true;
    }

    public void upPressed() {
        up = true;
    }

    public void downPressed() {
        down = true;
    }

    public void leftReleased() {
        left = false;
    }

    public void rightReleased() {
        right = false;
    }

    public void upReleased() {
        up = false;
    }

    public void downReleased() {
        down = false;
    }

    public void setTouchControl(Vector2 touchControl) {
        this.touchControl = touchControl;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isDown() {
        return down;
    }

    @Override
    public String toString() {
        return
                (left ? "L" : "_") +
                        (right ? "R" : "_") +
                        (up ? "U" : "_") +
                        (down ? "D" : "_");
    }

    public void update(float delta) throws GameException {
        ticks++;

        processInput();

        spawnAndroid(level);
        spawnApple(level);

        detectCollisions();

        Timers.update(delta);
    }

    private void spawnAndroid(Level l) {
        Grid g = l.getGrid();

        if (l.getAndyAndroid().isMarkedForKill()) {
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
    }

    private void spawnApple(Level l) {
        Grid g = l.getGrid();

        if (l.getEntitiesByType(Apple.class).size() < l.getMaxEnemies()) {
            int x = 12;
            int y = 8;
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

            Apple entity = new Apple(g);
            entity.setLevel(level);
            entity.setPosition(new Vector2(
                    x * Level.GRID_UNIT_SIZE,
                    y * Level.GRID_UNIT_SIZE));

            l.spawnEntity(entity);
        }
    }

    private boolean randomTime(int averageTicks) {
        return ticks % (int) (Math.random() * averageTicks + averageTicks / 2) == 0;
    }

    private int[] findNearestEmpty(Level l, int x, int y) {
        Grid g = l.getGrid();
        int[] xy = new int[2];
        xy[0] = xy[1] = -1;

        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                int space;

                space = g.get(x + i, y + j, Grid.GRID_WALL);
                if (space == Grid.GRID_EMPTY || space == Grid.GRID_JELLYBEAN) {
                    xy[0] = x + i;
                    xy[1] = y + j;
                    return xy;
                }


                space = g.get(x - i, y + j, Grid.GRID_WALL);
                if (space == Grid.GRID_EMPTY || space == Grid.GRID_JELLYBEAN) {
                    xy[0] = x - i;
                    xy[1] = y + j;
                    return xy;
                }


                space = g.get(x + i, y - j, Grid.GRID_WALL);
                if (space == Grid.GRID_EMPTY || space == Grid.GRID_JELLYBEAN) {
                    xy[0] = x + i;
                    xy[1] = y - j;
                    return xy;
                }


                space = g.get(x - i, y - j, Grid.GRID_WALL);
                if (space == Grid.GRID_EMPTY || space == Grid.GRID_JELLYBEAN) {
                    xy[0] = x - i;
                    xy[1] = y - j;
                    return xy;
                }
            }
        }

        return xy;
    }

    private void processInput() {
        if (left || right || up || down) {
            if (left) {
                level.getAndyAndroid()
                     .setVelocity(new Vector2(-AndyAndroid.REGULAR_SPEED, 0));
            } else if (right) {
                level.getAndyAndroid()
                     .setVelocity(new Vector2(AndyAndroid.REGULAR_SPEED, 0));
            }

            if (up) {
                level.getAndyAndroid()
                     .setVelocity(new Vector2(0, AndyAndroid.REGULAR_SPEED));
            } else if (down) {
                level.getAndyAndroid()
                     .setVelocity(new Vector2(0, -AndyAndroid.REGULAR_SPEED));
            }
        } else if (touchControl != null && touchControl.len() > 0) {
            level.getAndyAndroid()
                 .setVelocity(touchControl.cpy()
                                          .scl(AndyAndroid.REGULAR_SPEED));
        }
    }

    private void detectCollisions() throws GameException {
        List<Entity> entities = new ArrayList<Entity>(level.getEntities());

        for (Entity e : entities) {
            Vector2 epos = e.getPosition();
            Vector2 ebounds = e.getBounds();

            for (Entity o : entities) {
                if (o == e)
                    continue;

                Vector2 opos = o.getPosition();
                Vector2 obounds = o.getBounds();

                // If they intersect
                if (epos.x < opos.x + obounds.x
                        && epos.x + ebounds.x > opos.x
                        && epos.y < opos.y + obounds.y
                        && epos.y + ebounds.y > opos.y)
                    e.collideWith(o);
            }
        }
    }
}
