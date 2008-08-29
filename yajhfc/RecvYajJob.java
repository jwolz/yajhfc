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

import yajhfc.FormattedFile.FileFormat;
import yajhfc.readstate.PersistentReadState;

public class RecvYajJob extends YajJob {
    protected int fileNameCol;
    protected int errorCol;
    protected int progressCol;
    protected boolean read;
    PersistentReadState readState;
    
    @Override
    public boolean isError() {
        // Also update mainwin.MenuViewListener if this is changed!
        if (errorCol >= 0) {
            String errorDesc = getStringData(errorCol);
            return (errorDesc != null) && (errorDesc.length() > 0);
        } else {
            return false; // Actually undetermined, but we are optimistic ;-)
        }
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        if (read != this.read) {
            this.read = read;
            readState.setRead((String)getIDValue(), read);
        }
    }
    
    public boolean isInProgress() {
        return (Boolean)getData(progressCol);
    }
    
    @Override
    public void setColumns(FmtItemList columns) {
        super.setColumns(columns);
        fileNameCol = columns.getCompleteView().indexOf(utils.recvfmt_FileName);
        errorCol = columns.getCompleteView().indexOf(utils.recvfmt_ErrorDesc);
        progressCol = columns.getCompleteView().indexOf(utils.recvfmt_InProgress);
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
    
    public RecvYajJob(FmtItemList cols, String[] stringData, PersistentReadState readState) {
        super(cols, stringData);
        this.readState = readState;
        this.read = readState.isRead((String)getIDValue());
    }
}
