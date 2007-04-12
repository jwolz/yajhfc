package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import yajhfc.FormattedFile.FileFormat;

public class RecvYajJob extends YajJob {
    protected int fileNameCol;
    protected boolean read = false;
    
    private boolean haveError;
    
    @Override
    public boolean isError() {
        // Also update mainwin.MenuViewListener if this is changed!
        return haveError; 
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    @Override
    public void setColumns(Vector<FmtItem> columns) {
        super.setColumns(columns);
        fileNameCol = columns.indexOf(utils.recvfmt_FileName);
    }
    
    public String getServerTIF() {
        return stringData[fileNameCol];
    }
    
    @Override
    public List<HylaServerFile> getServerFilenames(HylaFAXClient hyfc) {
        HylaServerFile[] result = new HylaServerFile[1];
        result[0] = new HylaServerFile("recvq/" + getServerTIF(), FileFormat.TIFF);
        return Arrays.asList(result);
    }

    @Override
    public void delete(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        hyfc.dele("recvq/" + getServerTIF());
    }
    
    @Override
    public Object getIDValue() {
        return stringData[fileNameCol];
    }
    
    public RecvYajJob(Vector<FmtItem> cols, String[] stringData) {
        super(cols, stringData);
        
        // Also update mainwin.MenuViewListener if this is changed!
        int idx = columns.indexOf(utils.recvfmt_ErrorDesc);
        if (idx >= 0) {
            String errorDesc = getStringData(idx);
            haveError = (errorDesc != null) && (errorDesc.length() > 0);
        } else {
            haveError = false; // Actually undetermined, but we are optimistic ;-)
        }
    }
}
