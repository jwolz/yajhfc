/**
 * 
 */
package yajhfc.printerport.win32;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.printerport.FIFO;
import yajhfc.util.pipe.win32.Win32NamedPipeInputStream;

/**
 * Windows named pipe implementation (created using Win32 API with JNA)
 * 
 * @author jonas
 *
 */
public class Win32FIFO extends FIFO {

	/**
	 * @param fifoName
	 */
	public Win32FIFO(String fifoName) throws IOException {
		super(translateFIFOName(fifoName));

	}
	
	private static String translateFIFOName(String fifoName) throws IOException {
		if (!Utils.IS_WINDOWS)
			throw new IOException("This class only supports Windows!");
		
		if (fifoName.startsWith(Win32NamedPipeInputStream.LOCAL_PIPE_PREFIX)) {
			return fifoName;
		} else {
			if ((fifoName.indexOf('/') >= 0 || fifoName.indexOf('\\') >=0)) {
				throw new IOException("Invalid FIFO name " + fifoName + ". It must either be just a file name without path or start with " + Win32NamedPipeInputStream.LOCAL_PIPE_PREFIX);
			} else {
				return Win32NamedPipeInputStream.LOCAL_PIPE_PREFIX + fifoName;
			}
		}
	}

	/* (non-Javadoc)
	 * @see yajhfc.printerport.FIFO#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		return new Win32NamedPipeInputStream(fifoName);
	}

	/* (non-Javadoc)
	 * @see yajhfc.printerport.FIFO#close()
	 */
	@Override
	public void close() {
		// Do nothing
	}
	
	/**
	 * Checks if we have Windows and JNA available
	 * @return
	 */
	public static boolean isAvailable() {
		if (!Utils.IS_WINDOWS)
			return false;
		try {
			// Check if both the base JNA class and the win32 platform classes are available
			Class.forName("com.sun.jna.Native");
			Class.forName("com.sun.jna.platform.win32.WinNT");
			return true;
		} catch (ClassNotFoundException e) {
			Logger.getLogger(Win32FIFO.class.getName()).log(Level.INFO, "JNA not found", e);
			return false;
		}
	}

}
