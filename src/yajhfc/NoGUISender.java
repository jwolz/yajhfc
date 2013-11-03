/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */
package yajhfc;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import yajhfc.MainWin.SendReadyState;
import yajhfc.launch.CommandLineOpts;
import yajhfc.launch.Launcher2;
import yajhfc.launch.MainApplicationFrame;
import yajhfc.launch.SendControllerSubmitProtocol;
import yajhfc.launch.SendWinSubmitProtocol;
import yajhfc.launch.SubmitProtocol;
import yajhfc.ui.YajOptionPane;
import yajhfc.ui.swing.SwingYajOptionPane;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.ProgressContentPane;

/**
 * @author jonas
 *
 */
public class NoGUISender implements MainApplicationFrame {

    protected ProgressContentPane progressPanel;
    protected YajOptionPane dialogUI;
    protected JFrame frame;
    
    protected final SubmitProtocol submitProtocol;
    
    /**
     * @throws HeadlessException
     * @throws InvocationTargetException 
     * @throws InterruptedException 
     */
    public NoGUISender(SubmitProtocol submitProtocol) throws HeadlessException, InterruptedException, InvocationTargetException {
        this.submitProtocol = submitProtocol;

        if (SwingUtilities.isEventDispatchThread()) {
            createUI();
        } else {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createUI();
                }
            });
        }
    }
    
    protected void createUI() {
        frame = new JFrame(VersionInfo.AppShortName);
        dialogUI = new SwingYajOptionPane(frame);        

        progressPanel = new ProgressContentPane();
        frame.setContentPane(progressPanel);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Utils.setDefaultIcons(frame);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        
        progressPanel.showIndeterminateProgress(Utils._("Initializing..."), null);
        frame.setVisible(true);
    }

    public void bringToFront() {
        frame.setVisible(true);
        frame.toFront();
    }
    
    public Frame getFrame() {
        return frame;
    }
    
    public YajOptionPane getDialogUI() {
        return dialogUI;
    }
    
    public SendReadyState getSendReadyState() {
        return SendReadyState.Ready;
    }
    
    public ProgressContentPane getProgressPanel() {
        return progressPanel;
    }
    
    public void saveWindowSettings() {
        // Do nothing
    }
    
    public static void submitWithoutUI(final CommandLineOpts opts, final SendControllerSubmitProtocol submitProto) {
        final NoGUISender sender;
        try {
            sender = new NoGUISender(submitProto);
        } catch (Exception e) {
            Logger.getLogger(NoGUISender.class.getName()).log(Level.SEVERE, "Could not initialize main frame", e);
            System.exit(1);
            return;
        }
        try {
            Launcher2.application = sender;

            opts.fillSubmitProtocol(submitProto);
            submitProto.setCloseAfterSubmit(true);
            
            if (submitProto instanceof SendWinSubmitProtocol) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        sender.getProgressPanel().showIndeterminateProgress(Utils._("Opening send dialog..."), null);
                    }
                });
            }
            
            submitProto.submit(true);

            System.exit(submitProto.waitReady() ? 0 : 1);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(sender.getFrame(), Utils._("Error sending the fax:"), ex);
            System.exit(2);
        }
    }

    public void dispose() {
        frame.dispose();
    }
}
