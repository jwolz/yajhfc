package yajhfc.model.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import yajhfc.Utils;

public class FileHylaDirAccessor implements HylaDirAccessor {
    protected File baseDir;
    
    public FileHylaDirAccessor(File baseDir) {
        this.baseDir = baseDir;
    }

    public void deleteFile(String fileName) {
        new File(baseDir, fileName).delete();
    }

    public String[] listDirectory() throws IOException {
        return baseDir.list();
    }
    
    public String[] listDirectory(String dir) {
        return new File(baseDir, dir).list();
    }

    public void deleteTree(String dirName) throws IOException {
        delTree(new File(baseDir, dirName));
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
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(new File(baseDir, fileName));
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
        return new FileReader(new File(baseDir, fileName));
    }

    public long getLastModified() throws IOException {
        return baseDir.lastModified();
    }

    public long getLastModified(String fileName) throws IOException {
        return new File(baseDir, fileName).lastModified();
    }

}
