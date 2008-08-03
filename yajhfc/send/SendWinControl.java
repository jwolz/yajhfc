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
package yajhfc.send;

import java.awt.Window;
import java.io.InputStream;

import yajhfc.HylaServerFile;

/**
 * Control methods for the send dialog
 * @author jonas
 *
 */
public interface SendWinControl {
    public void setVisible(boolean visible);
    public boolean getModalResult();
    public Window getWindow();
    
    public void addServerFile(HylaServerFile serverFile);
    public void addRecipient(String faxNumber, String name, String company, String location, String voiceNumber);
    public void setSubject(String subject);
    public void addInputStream(InputStream inStream);
    public void addLocalFile(String fileName);
}
