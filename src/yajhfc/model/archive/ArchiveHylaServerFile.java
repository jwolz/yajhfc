/**
 * 
 */
package yajhfc.model.archive;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.io.OutputStream;

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
    public void downloadToStream(HylaFAXClient hyfc, OutputStream target)
            throws IOException, ServerResponseException {
        hyda.copyFile(path, target);
    }
    
}
