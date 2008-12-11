/**
 * 
 */
package yajhfc.model.archive;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import yajhfc.HylaServerFile;
import yajhfc.file.FormattedFile.FileFormat;

/**
 * @author jonas
 *
 */
public class ArchiveHylaServerFile extends HylaServerFile {
    protected HylaDirAccessor hyda;
    
    /**
     * @param path
     * @param type
     */
    public ArchiveHylaServerFile(HylaDirAccessor hyda, String path, FileFormat type) {
        super(path, type);
        this.hyda = hyda;
    }

    @Override
    public void download(HylaFAXClient hyfc, File target) throws IOException,
            FileNotFoundException, ServerResponseException {

        FileOutputStream outStream =  null;
        try {
            outStream =new FileOutputStream(target);
            hyda.copyFile(path, outStream);
        } finally {
            if (outStream != null)
                outStream.close();
        }
    }
}
