/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package yajhfc.ui;

import java.awt.HeadlessException;
import java.awt.Window;

import yajhfc.util.ProgressWorker.ProgressUI;

/**
 * Implements methods that can be used for user interaction.
 * 
 * These can be implemented either using Swing/AWT (default) or using the console.
 * For the Swing implementation it is required that the methods automatically handle the dispatching to the event dispatch thread.
 * 
 * @author jonas
 *
 */
public abstract class YajOptionPane {
    
    /**
     * Shows an exception dialog
     * @param title
     * @param message
     * @param exc
     */
    public abstract void showExceptionDialog(String title, String message, Exception exc);
    
    /**
     * Shows an exception dialog
     * @param message
     * @param exc
     */
    public abstract void showExceptionDialog(String message, Exception exc);
    
    /**
     * Shows the password dialog and returns a tuple (username, password)
     * or null if the user selected cancel.
     * @param owner
     * @param title
     * @param prompt
     * @param userName
     * @param editableUsername
     * @return
     */
    public abstract String[] showPasswordDialog(String title, String prompt, String userName, boolean editableUsername);
    
    /**
     * Shows the password dialog and returns a tuple (username, password)
     * or null if the user selected cancel.
     * @param owner
     * @param title
     * @param prompt
     * @param userName
     * @param editableUsername
     * @return
     */
    public abstract String[] showPasswordDialog(String title, String prompt, String userName, boolean editableUsername, boolean allowEmptyPassword);
    
    /**
     * Brings up a dialog that displays a message using a default
     * icon determined by the <code>messageType</code> parameter.
     *
     * @param message   the <code>Object</code> to display
     * @param title     the title string for the dialog
     * @param messageType the type of message to be displayed:
     *                  <code>ERROR_MESSAGE</code>,
     *          <code>INFORMATION_MESSAGE</code>,
     *          <code>WARNING_MESSAGE</code>,
     *                  <code>QUESTION_MESSAGE</code>,
     *          or <code>PLAIN_MESSAGE</code>
     * @exception HeadlessException if
     *   <code>GraphicsEnvironment.isHeadless</code> returns
     *   <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public abstract void showMessageDialog(String message, String title, int messageType);
    
    /**
     * Brings up a dialog where the number of choices is determined
     * by the <code>optionType</code> parameter, where the
     * <code>messageType</code>
     * parameter determines the icon to display.
     * The <code>messageType</code> parameter is primarily used to supply
     * a default icon from the Look and Feel.
     *
     * @param message   the <code>Object</code> to display
     * @param title     the title string for the dialog
     * @param optionType an integer designating the options available
     *          on the dialog: <code>YES_NO_OPTION</code>,
     *          or <code>YES_NO_CANCEL_OPTION</code>
     * @param messageType an integer designating the kind of message this is; 
     *                  primarily used to determine the icon from the pluggable
     *                  Look and Feel: <code>ERROR_MESSAGE</code>,
     *          <code>INFORMATION_MESSAGE</code>, 
     *                  <code>WARNING_MESSAGE</code>,
     *                  <code>QUESTION_MESSAGE</code>,
     *          or <code>PLAIN_MESSAGE</code>
     * @return an integer indicating the option selected by the user
     * @exception HeadlessException if
     *   <code>GraphicsEnvironment.isHeadless</code> returns
     *   <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public abstract int showConfirmDialog(String message, String title, int optionType, int messageType);
 
    /**
     * Runs the given runnable in the correct thread for UI updates (if there is no restriction for the thread, this method
     * may simply call toRun.run() synchronously).
     * @param toRun
     */
    public abstract void invokeLater(Runnable toRun);
    
    /**
     * Creates and returns a default progress monitor
     * @return
     */
    public abstract ProgressUI createDefaultProgressMonitor(String message, String note, int min, int max);
    
    /**
     * Returns the Window parent for the dialogs or null if no parent exists.
     * @return
     */
    public Window getParent() {
        return null;
    }
}
