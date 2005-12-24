package yajhfc;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class RecvYajJob extends YajJob {
    protected int fileNameCol;
    protected boolean read = false;
    
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
        result[0] = new HylaServerFile("recvq/" + getServerTIF(), "tif");
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
    }
}
