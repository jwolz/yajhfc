/**
 * 
 */
package yajhfc.model.jobq;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

/**
 * An abstract interface used to access the archive directory.
 * The directory separator is "/".
 * 
 * @author jonas
 *
 */
public interface HylaDirAccessor {
    /**
     * Lists the root directory
     * @return
     * @throws IOException
     */
    public String[] listDirectory() throws IOException;
    
    /**
     * Lists the given directory
     * @param dir
     * @return
     * @throws IOException
     */
    public String[] listDirectory(String dir) throws IOException;
    
    /**
     * Returns an Reader for the given file name
     * @param fileName
     * @return
     * @throws IOException
     */
    public Reader getInputReader(String fileName) throws IOException;
    
    /**
     * Copies the given file's content to the target stream
     * @param fileName
     * @param target
     * @throws IOException
     */
    public void copyFile(String fileName, OutputStream target) throws IOException;
    
    
    /**
     * Returns a (possibly temporary) File containing the data for the specified file.
     * @param fileName
     * @throws IOException
     */
    public File getFile(String fileName) throws IOException;
    
    /**
     * Deletes the given file
     * @param fileName
     * @throws IOException
     */
    public void deleteFile(String fileName) throws IOException;
    
    /**
     * Deletes the given directory including its content
     * @param fileName
     * @throws IOException
     */
    public void deleteTree(String dirName) throws IOException;
    
    /**
     * Gets the last modification time of the root directory.
     * Note: This may be a logical time, too
     * @return
     * @throws IOException
     */
    public long getLastModified() throws IOException;
    
    /**
     * Gets the last modification time of the given file or directory.
     * Note: This may be a logical time, too
     * @return
     * @throws IOException
     */
    public long getLastModified(String fileName) throws IOException;
    
    /**
     * Returns the given file's size
     * @param fileName
     * @return
     * @throws IOException
     */
    public long getSize(String fileName) throws IOException;
    
    /**
     * Returns the given file's protection as bit mask similar to the 
     * st_mode field from the stat(2) UNIX API, i.e. this is the same value as you would give to chmod
     * @param fileName
     * @return
     * @throws IOException
     */
    public int getProtection(String fileName) throws IOException;
    
    /**
     * Returns the base path used in this dir accessor
     * @return
     */
    public String getBasePath();
}
