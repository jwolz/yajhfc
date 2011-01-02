/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc.launch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class LockThread extends Thread {
    private final static Logger log = Logger.getLogger(LockThread.class.getName());
    
    public final ServerSocket socket;
    private boolean isLocking = true;
    
    public LockThread(ServerSocket socket) {
        super();
        this.socket = socket;
    }
    
    private int waitSubmitOK() throws InterruptedException  {
        while (true) {
            if (Launcher2.application != null) {
                switch (Launcher2.application.getSendReadyState()) {
                case Ready:
                    return Lock.RESPONSE_OK;
                case NotReady:
                    return Lock.RESPONSE_NOT_CONNECTED;
                case NeedToWait:
                    //NOP
                }
            }
            Thread.sleep(100);
        }
    }
    
    @Override
    public void run() {
        while (isLocking) {
            Socket srv = null;
            SubmitProtocol submitProto;
            DataInputStream strIn = null;
            DataOutputStream strOut = null;
            List<String> tempList = new ArrayList<String>();
            int size;
            
            try {
                srv = socket.accept();
                if (Utils.debugMode) {
                    log.info("Got connection from new instance...");
                }
                strIn = new DataInputStream(srv.getInputStream());
                strOut = new DataOutputStream(srv.getOutputStream());
                submitProto = new SendWinSubmitProtocol();
                
                int response;
                int opcode;
                String responseMsg;
                boolean wait;

sessionLoop:    do {
                    try {
                        response = Lock.RESPONSE_OK;
                        responseMsg = "OK";
                        switch (opcode = strIn.read()) {
                        case Lock.CODE_ADD_FILES:
                            log.fine("Got CODE_ADD_FILES");
                            tempList.clear();
                            size = strIn.readInt();
                            for (int i = 0; i < size; i++) {
                                tempList.add(strIn.readUTF());
                            }
                            submitProto.addFiles(tempList);
                            break;
                        case Lock.CODE_ADD_RECIPIENTS:
                            log.fine("Got CODE_ADD_RECIPIENTS");
                            tempList.clear();
                            size = strIn.readInt();
                            for (int i = 0; i < size; i++) {
                                tempList.add(strIn.readUTF());
                            }
                            submitProto.addRecipients(tempList);
                            break;
                        case Lock.CODE_BRING_TO_FRONT:
                            log.fine("Got CODE_BRING_TO_FRONT");
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    Launcher2.application.bringToFront();
                                };
                            });
                            break;
                        case Lock.CODE_SET_COMMENT:
                            log.fine("Got CODE_SET_COMMENT");
                            submitProto.setComments(strIn.readUTF());
                            break;
                        case Lock.CODE_SET_SUBJECT:
                            log.fine("Got CODE_SET_SUBJECT");
                            submitProto.setSubject(strIn.readUTF());
                            break;
                        case Lock.CODE_USE_COVER:
                            log.fine("Got CODE_USE_COVER");
                            submitProto.setCover(strIn.readBoolean());
                            break;
                        case Lock.CODE_SET_MODEM:
                            log.fine("Got CODE_SET_MODEM");
                            submitProto.setModem(strIn.readUTF());
                            break;
                        case Lock.CODE_SUBMIT:
                        case Lock.CODE_SUBMIT_STREAM:
                            wait = strIn.readBoolean();
                            if (opcode == Lock.CODE_SUBMIT_STREAM) {
                                log.fine("Got CODE_SUBMIT_STREAM");
                                submitProto.setInputStream(strIn, null);
                            } else {
                                log.fine("Got CODE_SUBMIT");
                            }
                            response = waitSubmitOK();
                            
                            if (response == Lock.RESPONSE_OK) {
                                long[] ids = submitProto.submit(wait);
                                // Special extra data for submit, so copy the write response code
                                strOut.write(response);
                                strOut.writeUTF(responseMsg);
                                if (ids == null) {
                                    // Write the length 0
                                    strOut.writeInt(0);
                                } else {
                                    // Write the job ids for the submitted jobs 
                                    // in the format (int length, long id0, long id1, ...)
                                    strOut.writeInt(ids.length);
                                    for (long id : ids) {
                                        strOut.writeLong(id);
                                    }
                                }
                                strOut.flush();
                                continue sessionLoop;
                            }
                            break;
                        case -1: // Stream closed...
                            log.fine("Stream closed.");
                            break sessionLoop;
                        default:
                            log.fine("Got unkown opcode: " + opcode);
                            response = Lock.RESPONSE_UNKNOWN_OPCODE;
                            responseMsg = "Unknown opcode " + opcode;
                            break;
                        }
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Got exception: ", e);
                        response = Lock.RESPONSE_GOT_EXCEPTION;
                        responseMsg = e.toString();
                    }
                    if (Utils.debugMode) {
                        log.fine("Write response " + response + "; msg: " + responseMsg);
                    }
                    strOut.write(response);
                    strOut.writeUTF(responseMsg);
                    strOut.flush();
                } while (!srv.isClosed());
            } catch (Exception e) {
                log.log(Level.INFO, "Maybe error waiting for connection", e);
            } finally {
                try {
                    strIn.close();
                    strOut.close();
                } catch(Exception e) {
                    // NOP
                }
                try {
                    if (srv != null && !srv.isClosed()) {                        
                        srv.close();
                    }
                } catch(Exception e) {
                    // NOP
                }
                srv = null;
                strIn = null;
                strOut = null;
            }
            
        }
    }
    
    public void releaseLock() {
        isLocking = false;
    }
    
}
