package yajhfc.model.jobq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.server.ServerOptions;

public class FileHylaDirAccessor implements HylaDirAccessor {
    static final Logger log = Logger.getLogger(FileHylaDirAccessor.class.getName());
    
    protected File baseDir;
    protected Map<String,SoftReference<File>> fileCache = new HashMap<String, SoftReference<File>>();
    protected String encoding;
    
    public FileHylaDirAccessor(File baseDir, ServerOptions options) {
        if (Utils.debugMode)
            log.fine("Created new dir accessor for " + baseDir);
        this.baseDir = baseDir;
        encoding = options.hylaFAXCharacterEncoding;
    }

    public void deleteFile(String fileName) {
        if (Utils.debugMode)
            log.fine("Delete " + fileName);
        getFile(fileName).delete();
    }

    /**
     * Return a new file object implementing some caching
     * @param fileName
     * @return
     */
    public File getFile(String fileName) {
        SoftReference<File> ref = fileCache.get(fileName);
        File file;
        if (ref != null)
            file = ref.get();
        else
            file = null;
        
        if (file == null) {
            file = new File(baseDir, fileName);
            fileCache.put(fileName, new SoftReference<File>(file));
        }
        return file;
    }

    public String[] listDirectory() throws IOException {
        if (Utils.debugMode)
            log.fine("List base dir");
        return baseDir.list();
    }
    
    public String[] listDirectory(String dir) {
        if (Utils.debugMode)
            log.fine("List directory " + dir);
        return getFile(dir).list();
    }

    public void deleteTree(String dirName) throws IOException {
        if (Utils.debugMode)
            log.fine("Delete tree " + dirName);
        delTree(getFile(dirName));
    }

    private void delTree(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File entry : dir.listFiles()) {
                delTree(entry);
            }
        }
        if (!dir.delete()) {
            throw new IOException("Could not delete the file " + dir);
        }
    }

    public void copyFile(String fileName, OutputStream target) throws IOException {
        if (Utils.debugMode)
            log.fine("Copy file " + fileName);
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(getFile(fileName));
            Utils.copyStream(inStream, target);
        } finally {
            try {
                if (inStream != null)
                    inStream.close();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
    

    public Reader getInputReader(String fileName) throws IOException {
        if (Utils.debugMode)
            log.fine("Get input reader " + fileName);
        return new InputStreamReader(new FileInputStream(getFile(fileName)), encoding);
    }

    public long getLastModified() throws IOException {
        if (Utils.debugMode)
            log.fine("Get base dir last modified ");
        return baseDir.lastModified();
    }

    public long getLastModified(String fileName) throws IOException {
        if (Utils.debugMode)
            log.fine("Get last modified " + fileName);
        return getFile(fileName).lastModified();
    }
    
    public long getSize(String fileName) throws IOException {
        if (Utils.debugMode)
            log.fine("Get size " + fileName);
        return getFile(fileName).length();
    }
    
    public int getProtection(String fileName) throws IOException {
        if (Utils.debugMode)
            log.fine("Get protection " + fileName);
        File f = getFile(fileName);
        int result = 0;
        // Bad emulation of stat call...
        if (f.canRead()) 
            result |= 0444;
        if (f.canWrite()) 
            result |= 0222;
        return result;
    }

    public String getBasePath() {
        return baseDir.getPath();
    }
}
