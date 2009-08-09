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

import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JFrame;

import yajhfc.HylaClientManager;
import yajhfc.Utils;
import yajhfc.MainWin.SendReadyState;

/**
 * @author jonas
 *
 */
public class DummyFrame extends JFrame implements MainApplicationFrame {

    protected HylaClientManager clientManager;
    
    public DummyFrame() {
        super(Utils.AppShortName);
        clientManager = new HylaClientManager(Utils.getFaxOptions());
        setIconImage(Toolkit.getDefaultToolkit().getImage(DummyFrame.class.getResource("/yajhfc/icon.png")));
    }
    
    /* (non-Javadoc)
     * @see yajhfc.launch.MainApplicationFrame#bringToFront()
     */
    public void bringToFront() {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see yajhfc.launch.MainApplicationFrame#getClientManager()
     */
    public HylaClientManager getClientManager() {
        return clientManager;
    }

    /* (non-Javadoc)
     * @see yajhfc.launch.MainApplicationFrame#getFrame()
     */
    public Frame getFrame() {
        return this;
    }

    /* (non-Javadoc)
     * @see yajhfc.launch.MainApplicationFrame#getSendReadyState()
     */
    public SendReadyState getSendReadyState() {
        return SendReadyState.Ready;
    }

}
