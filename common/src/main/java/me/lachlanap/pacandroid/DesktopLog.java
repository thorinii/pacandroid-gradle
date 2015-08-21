/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.lachlanap.pacandroid;

import me.lachlanap.pacandroid.util.AppLog;

/**
 *
 * @author lachlan
 */
public class DesktopLog implements AppLog.Log {

    public void log(String text) {
        System.out.println(text);
    }
}
