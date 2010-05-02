package yajhfc.model;
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
import java.util.Collections;
import java.util.List;

import yajhfc.FmtItemList;
import yajhfc.HylaServerFile;
import yajhfc.RecvFormat;
import yajhfc.file.FormattedFile.FileFormat;

public class RecvYajJob extends YajJob<RecvFormat> {
    protected boolean read;
    protected UnReadMyTableModel tableModel;
    
    @Override
    public boolean isError() {
        // Also update MainWin.MenuViewListener if this is changed!
        int errorCol = columns.getCompleteView().indexOf(RecvFormat.e);
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
            tableModel.persistentReadState.setRead((String)getIDValue(), read);
            tableModel.fireReadStateChanged();
        }
    }
    
    public boolean isInProgress() {
        return (Boolean)getData(RecvFormat.z);
    }
    
    @Override
    public void setColumns(FmtItemList<RecvFormat> columns) {
        super.setColumns(columns);
    }
    
    public String getServerTIF() {
        return getStringData(RecvFormat.f);
    }
    
    @Override
    public List<HylaServerFile> getServerFilenames(HylaFAXClient hyfc, List<String> inaccessibleFiles) {
        return Collections.singletonList(new HylaServerFile("recvq/" + getServerTIF(), FileFormat.TIFF));
    }

    @Override
    public void delete(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        hyfc.dele("recvq/" + getServerTIF());
    }
    
    @Override
    public Object getIDValue() {
        return getStringData(RecvFormat.f);
    }
    
    public RecvYajJob(FmtItemList<RecvFormat> cols, String[] stringData, UnReadMyTableModel tableModel) {
        super(cols, stringData);
        this.tableModel = tableModel;
        this.read = tableModel.persistentReadState.isRead((String)getIDValue());
    }
}
