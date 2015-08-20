/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid.model.loader;

/**
 *
 * @author lachlan
 */
public class LevelLoaderException extends RuntimeException {

    public LevelLoaderException(String msg) {
        super(msg);
    }

    public LevelLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
