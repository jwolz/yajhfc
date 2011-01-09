/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.logging.Logger;

import javax.swing.JFrame;

import yajhfc.MainWin.SendReadyState;
import yajhfc.launch.CommandLineOpts;
import yajhfc.launch.Launcher2;
import yajhfc.launch.MainApplicationFrame;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.send.LocalFileTFLItem;
import yajhfc.send.SendController;
import yajhfc.send.SendControllerListener;
import yajhfc.send.StreamTFLItem;
import yajhfc.server.Server;
import yajhfc.server.ServerManager;
import yajhfc.server.ServerOptions;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.ProgressContentPane;

/**
 * @author jonas
 *
 */
public class NoGUISender extends JFrame implements MainApplicationFrame {

    ProgressContentPane progressPanel;
    
    /**
     * @throws HeadlessException
     */
    public NoGUISender() throws HeadlessException {
        super(Utils.AppShortName);
        

        progressPanel = new ProgressContentPane();
        setContentPane(progressPanel);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setIconImage(Toolkit.getDefaultToolkit().getImage(NoGUISender.class.getResource("/yajhfc/icon.png")));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public void bringToFront() {
        toFront();
    }
    
    public Frame getFrame() {
        return this;
    }
    
    public SendReadyState getSendReadyState() {
        return SendReadyState.Ready;
    }
    
    
    public void saveWindowSettings() {
        // Do nothing
    }
    
    public static void startUpWithoutUI(CommandLineOpts opts) {
        if (opts.recipients.size() == 0) {
            System.err.println("In no GUI mode you have to specify at least one recipient.");
            System.exit(1);
        }
        if (opts.fileNames.size() == 0 && !opts.useStdin) {
            System.err.println("In no GUI mode you have to specify at least one file to send or --stdin.");
            System.exit(1);
        }
                
        NoGUISender progressFrame = new NoGUISender();
        Launcher2.application = progressFrame;
        progressFrame.progressPanel.showIndeterminateProgress(Utils._("Logging in..."), null);
        progressFrame.setVisible(true);
        
        try {
            Server server;
            if (opts.serverToUse == null) {
                server = ServerManager.getDefault().getCurrent(); 
            } else {
                ServerOptions so = IDAndNameOptions.getItemFromCommandLineCoding(Utils.getFaxOptions().servers, opts.serverToUse);
                if (so != null) {
                    server = ServerManager.getDefault().getServerByID(so.id);
                } else {
                    Logger.getAnonymousLogger().warning("Server not found, using default instead: " + opts.serverToUse);
                    server = ServerManager.getDefault().getCurrent(); 
                }
            }
            
            SendController sendController = new SendController(server, progressFrame, false, progressFrame.progressPanel);
            sendController.addSendControllerListener(new SendControllerListener() {
               public void sendOperationComplete(boolean success) {
                   System.exit(success ? 0 : 1);
               } 
            });

            if (opts.identityToUse != null) {
                SenderIdentity identity = IDAndNameOptions.getItemFromCommandLineCoding(Utils.getFaxOptions().identities, opts.identityToUse);
                if (identity != null) {
                    sendController.setFromIdentity(identity);
                } else {
                    Logger.getAnonymousLogger().warning("Identity not found, using default instead: " + opts.identityToUse);
                    sendController.setFromIdentity(server.getDefaultIdentity());
                }
            } else {
                sendController.setFromIdentity(server.getDefaultIdentity());
            }
            
            DefaultPBEntryFieldContainer.parseCmdLineStrings(sendController.getNumbers(), opts.recipients);
            
            sendController.setUseCover(opts.useCover != null ? opts.useCover : false);
            if (opts.subject != null)
                sendController.setSubject(opts.subject);
            if (opts.comment != null)
                sendController.setComments(opts.comment);
            if (opts.useStdin) {
                sendController.getFiles().add(new StreamTFLItem(System.in, null));
            }
            for (String file : opts.fileNames) {
                sendController.getFiles().add(new LocalFileTFLItem(file));
            }
            
            if (sendController.validateEntries()) {
                sendController.sendFax();
            }
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(progressFrame, Utils._("Error sending the fax:"), ex);
            System.exit(2);
        }
    }
}
