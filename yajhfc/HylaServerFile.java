package yajhfc;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

public class HylaServerFile {
    protected String path;
    protected String type;
    
    public String getPath() {
        return path;
    }
    
    public String getType() {
        return type;
    }
    
    public void download(HylaFAXClient hyfc, File target) 
        throws IOException, FileNotFoundException, ServerResponseException {
        FileOutputStream out = new FileOutputStream(target);
        
        hyfc.type(gnu.inet.ftp.FtpClientProtocol.TYPE_IMAGE);
        hyfc.get(path, out);
        out.close();
    }
    
    public void view(HylaFAXClient hyfc, FaxOptions opts)
        throws IOException, FileNotFoundException, ServerResponseException, UnknownFormatException {
        
        File tmptif = File.createTempFile("fax", "." + type);
        tmptif.deleteOnExit();
        
        download(hyfc, tmptif);        
        
        String execCmd;
        if (type.equalsIgnoreCase("tif")) 
            execCmd = opts.faxViewer;
        else if (type.equalsIgnoreCase("ps") || type.equalsIgnoreCase("pdf"))
            execCmd = opts.psViewer;
        else
            throw new UnknownFormatException(MessageFormat.format("File format {1} not supported.", type));
        
        if (execCmd.indexOf("%s") >= 0)
            execCmd = execCmd.replace("%s", tmptif.getPath());
        else
            execCmd += " " + tmptif.getPath();
        
        Runtime.getRuntime().exec(execCmd);
    }
    
    @Override
    public String toString() {
        return path;
    }
    
    public HylaServerFile(String path, String type) {
        this.path = path;
        this.type = type;
    }
}

