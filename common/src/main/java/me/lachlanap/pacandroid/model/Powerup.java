/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.model;

/**
 * @author lachlan
 */
public enum Powerup {

    Null(-1, null), Edible(7000, "Edible"), LevelStartFreeze(3000, null, true);

    Powerup(int buffMillis, String name) {
        this.buffMillis = buffMillis;
        this.freeze = false;
        this.name = name;
    }

    Powerup(int buffMillis, String name, boolean freeze) {
        this.buffMillis = buffMillis;
        this.freeze = freeze;
        this.name = name;
    }

    public boolean isHuman() {
        return name != null;
    }

    public final int buffMillis;
    public final String name;
    public final boolean freeze;
}
