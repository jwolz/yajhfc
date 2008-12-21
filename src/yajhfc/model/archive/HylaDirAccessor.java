/**
 * 
 */
package yajhfc.model.archive;

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
}
