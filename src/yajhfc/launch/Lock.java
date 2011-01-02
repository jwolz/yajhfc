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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class Lock implements SubmitProtocol {
    private static final Logger log = Logger.getLogger(Lock.class.getName());
    
    final static int CODE_SUBMIT_STREAM = 1;
    final static int CODE_SUBMIT = 3;
    final static int CODE_ADD_FILES = 5;
    final static int CODE_BRING_TO_FRONT = 6;
    final static int CODE_ADD_RECIPIENTS = 7;
    final static int CODE_USE_COVER = 8;
    final static int CODE_SET_SUBJECT = 9;
    final static int CODE_SET_COMMENT = 10;
    final static int CODE_SET_MODEM = 11;
    
    public final static int RESPONSE_OK = 0;
    public final static int RESPONSE_NOT_CONNECTED = 10;
    public final static int RESPONSE_GENERAL_ERROR = 1;
    public final static int RESPONSE_GOT_EXCEPTION = 2;
    public final static int RESPONSE_UNKNOWN_OPCODE = 255;
    
    protected final Socket sock;
    protected DataOutputStream outStream;
    protected DataInputStream inStream;
    
    protected InputStream inStreamToSubmit = null;
    
    protected Lock(Socket sock) throws IOException {
        super();
        this.sock = sock;
        outStream = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
        inStream = new DataInputStream(sock.getInputStream());
    }
    
    private void checkResponse() throws IOException {
        int response = inStream.read();
        String responseMsg = null;
        if (response >= 0) {
            responseMsg = inStream.readUTF();   
        }
        if (Utils.debugMode) {
            log.info("Got response : " + response + "; msg: " + responseMsg);
        }
        if (response != 0) {
            if (response > 0) {
                throw new ResponseException("Error " + response + " received from old instance: " + responseMsg, response);
            } else {
                throw new ResponseException("Could not read response from old instance", -1);
            }
        }
    }
    

    public void addFiles(Collection<String> fileNames) throws IOException {
        if (Utils.debugMode) {
            log.finer("addFiles: " + fileNames);
        }
        outStream.write(CODE_ADD_FILES);
        outStream.writeInt(fileNames.size());
        for (String sFile : fileNames) {
            outStream.writeUTF(new File(sFile).getAbsolutePath());
        }
        outStream.flush();
        checkResponse();
    }

    public void addRecipients(Collection<String> recipients) throws IOException {
        if (Utils.debugMode) {
            log.finer("addRecipients: " + recipients);
        }
        outStream.write(CODE_ADD_RECIPIENTS);
        outStream.writeInt(recipients.size());
        for (String number : recipients) {
            outStream.writeUTF(number);
        }
        outStream.flush();
        checkResponse();
    }

    public void setCloseAfterSubmit(boolean closeAfterSumbit) {
        throw new UnsupportedOperationException("CloseAfterSubmit not supported.");
    }

    public void setComments(String comments) throws IOException {
        if (Utils.debugMode) {
            log.finer("setComments: " + comments);
        }
        outStream.write(CODE_SET_COMMENT);
        outStream.writeUTF(comments);
        outStream.flush();
        checkResponse(); 
    }

    public void setCover(boolean useCover) throws IOException {
        if (Utils.debugMode) {
            log.finer("setCover: " + useCover);
        }
        outStream.write(CODE_USE_COVER);
        outStream.writeBoolean(useCover);
        outStream.flush();
        checkResponse();
    }

    public void setInputStream(InputStream stream, String streamSource) {
        if (Utils.debugMode) {
            log.finer("setInputStream: " + stream);
        }
        this.inStreamToSubmit = stream;
    }

    public void setSubject(String subject) throws IOException {
        if (Utils.debugMode) {
            log.finer("setSubject: " + subject);
        }
        outStream.write(CODE_SET_SUBJECT);
        outStream.writeUTF(subject);
        outStream.flush();
        checkResponse();
    }

    public void setModem(String modem) throws IOException {
        if (Utils.debugMode) {
            log.finer("setModem: " + modem);
        }
        outStream.write(CODE_SET_MODEM);
        outStream.writeUTF(modem);
        outStream.flush();
        checkResponse();
    }
    
    /**
     * Bring the old instance to front
     * @throws IOException 
     */
    public void bringToFront() throws IOException {
        if (Utils.debugMode) {
            log.finer("bringToFront");
        }
        outStream.write(CODE_BRING_TO_FRONT);
        outStream.flush();
        checkResponse();
    }
    
    public long[] submit(boolean wait) throws IOException {
        if (inStreamToSubmit == null) {
            if (Utils.debugMode) {
                log.finer("submit: no stream, wait=" + wait);
            }
            outStream.write(CODE_SUBMIT);
            outStream.writeBoolean(wait);
            outStream.flush();
        } else {
            if (Utils.debugMode) {
                log.finer("submit: have stream, wait=" + wait);
            }
            outStream.write(CODE_SUBMIT_STREAM);
            outStream.writeBoolean(wait);
            Utils.copyStream(inStreamToSubmit, outStream);
            outStream.flush();
            sock.shutdownOutput();
        }
        checkResponse();
        int size = inStream.readInt();
        long[] rv = new long[size];
        for (int i=0; i<size; i++) {
            rv[i] = inStream.readLong();
        }
        return rv;
    }
    
    /**
     * Close connection to the old instance
     */
    public void close() {
        if (!sock.isClosed()) {
            try {
                outStream.close();
            } catch (IOException e) {
                log.log(Level.WARNING, "Error closing outStream:", e);
            }
            try {
                inStream.close();
            } catch (IOException e) {
                log.log(Level.WARNING, "Error closing inStream:", e);
            }
            try {
                sock.close();
            } catch (IOException e) {
                log.log(Level.WARNING, "Error closing sock:", e);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // STATIC METHODS:
    /////////////////////////////////////////////////////////////////////////////////
    public static LockThread lockThread;
    
    private static InetAddress getLocalhost() 
    throws UnknownHostException {
        final byte[] addr = {127, 0, 0, 1};
        return InetAddress.getByAddress(addr);
    }

    private static File getLockFile() {
        return new File(Utils.getConfigDir(), "lock");
    }

    /**
     * Checks if a valid lock exists.
     * @return A socket to connect to the old instance or null if none exists.
     */
    public static Lock checkLock() {
        File lock = getLockFile();
        if (lock.exists()) {
            try {
                BufferedReader filin = new BufferedReader(new FileReader(lock));
                String strport = filin.readLine();
                filin.close();

                int port = Integer.parseInt(strport);
                Socket cli = new Socket(getLocalhost(), port);
                return new Lock(cli);
            } catch (Exception e) {
                // do nothing
            }
        } 

        return null;
    }
    
    public static void createLock() {
        final int portStart = 64007;
        final int portEnd = 65269;
        int port;
        ServerSocket sockBlock = null;
        File lock = getLockFile();
        
        try {
            for (port = portStart; port <= portEnd; port++) {
                try {
                    sockBlock = new ServerSocket(port, 50, getLocalhost());
                    break;
                } catch (Exception e) {
                    // do nothing, try next port
                }
            }
            if (sockBlock != null) {
                Writer filout = new OutputStreamWriter(new FileOutputStream(lock));
                filout.write("" + port + "\n");
                filout.close();
                lock.deleteOnExit();
                //isLocking = true;
                lockThread = new LockThread(sockBlock);
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not create lock: ", e);
        }
    }
    
    public static void startLockThread() {
        if (lockThread != null) {
            lockThread.start();
        }
    }
    
    public static void releaseLock() {
        if (lockThread == null)
            return;
        
        try {
            lockThread.releaseLock();
            
            if (lockThread.socket != null) {
                lockThread.socket.close();
            }
            
            lockThread = null;
            
            getLockFile().delete();
        } catch (IOException e) {
            // do nothing
        }
    }
}
