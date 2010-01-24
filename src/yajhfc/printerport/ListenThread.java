/*
 * YajHFC - Yet another Java Hylafax client
 * Copyright (C) 2009 Jonas Wolz
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
package yajhfc.printerport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.launch.SendWinSubmitProtocol;
import yajhfc.launch.SubmitProtocol;

public class ListenThread extends Thread {
    private static final Logger log = Logger.getLogger(ListenThread.class.getName());
    private final ServerSocket printerSock;

    public ListenThread(String listenAddress, int listenPort) throws UnknownHostException, IOException {
        super("PrinterPort-" + listenAddress);
        this.printerSock = new ServerSocket(listenPort, 0,
                (listenAddress == null || listenAddress.length() == 0) ? null : InetAddress.getByName(listenAddress));
        this.setDaemon(true);
    }

    public void close() {
        try {
            printerSock.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "Error closing socket:", e);
        }
    }
    
    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Socket sock = printerSock.accept();
                try {
                    SubmitProtocol sp = new SendWinSubmitProtocol();
                    sp.setInputStream(sock.getInputStream(), "[" + printerSock.getInetAddress().toString() + ':' + printerSock.getLocalPort() + ']');
                    sp.submit(true);
                    sock.close();
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error accepting a connection:", e);
                }
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Error accepting a connection:", e);
        } finally {
            close();
        }
    }
}