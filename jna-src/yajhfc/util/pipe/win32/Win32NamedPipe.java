package yajhfc.util.pipe.win32;

import java.io.IOException;

import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT.HANDLE;

/**
 * Common functions for inbound and outbound pipes
 * @author jonas
 *
 */
class Win32NamedPipe {
	private static final int TIMEOUT = 0;
	private static final int BUFSIZE = 4096;
	
	protected HANDLE hPipe;
	protected boolean connected = false;
	
	/**
	 * Creates and opens a pipe with the specified name and open mode
	 * @param fileName
	 * @param openMode One of the PIPE_ACCESS_* constants
	 * @throws IOException
	 */
	protected Win32NamedPipe(String fileName, int openMode) throws IOException {
		openPipe(fileName, openMode);
	}
	
	private void openPipe(String fileName, int openMode) throws IOException {
		hPipe = MyKernel32.INSTANCE.CreateNamedPipe(fileName, 
				openMode, 
				MyKernel32.PIPE_TYPE_BYTE|MyKernel32.PIPE_READMODE_BYTE|MyKernel32.PIPE_WAIT,
				MyKernel32.PIPE_UNLIMITED_INSTANCES, BUFSIZE, BUFSIZE, TIMEOUT, null);
		if (WinBase.INVALID_HANDLE_VALUE.equals(hPipe)) {
			hPipe = null;
			throwIOException("CreateNamedPipe failed");
		}
	}
	
	/**
	 * Ensures the pipe is connected
	 * @return true if the pipe was connected, false if it was already connected
	 * @throws IOException
	 */
	public boolean ensureConnected() throws IOException {
		if (connected)
			return false;
		checkOpen();
		
		if (MyKernel32.INSTANCE.ConnectNamedPipe(hPipe, null)) {
			connected = true;
			return true;
		} else {
			int lastError = MyKernel32.INSTANCE.GetLastError();
			if (lastError == MyKernel32.ERROR_PIPE_CONNECTED) {
				connected = true;
				return false;
			}
			MyKernel32.INSTANCE.CloseHandle(hPipe);
			hPipe = null;
			throwIOException("ConnectNamedPipe failed", lastError);
			return false; // Should never be reached
		}
	}
	
	/**
	 * Check if the pipe is open and throw an exception if not
	 * @throws IOException
	 */
	protected void checkOpen() throws IOException {
		if (hPipe == null)
			throw new IOException("The pipe has already been closed");
	}
	
	/**
	 * Close the pipe
	 */
	public void close() {
		if (hPipe == null)
			return;
		
		if (connected) {
			MyKernel32.INSTANCE.DisconnectNamedPipe(hPipe);
			connected = false;
		}
		MyKernel32.INSTANCE.CloseHandle(hPipe);
		hPipe = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}
	
	/**
	 * Throws an IOException with the given prefix for the error returned by GetLastError()
	 * @param prefix
	 * @throws IOException
	 */
	static void throwIOException(String prefix) throws IOException {
		throwIOException(prefix, MyKernel32.INSTANCE.GetLastError());
	}
	
	/**
	 * Throws an IOException with the given prefix for the error given in lastError (this number is assumed to come from GetLastError())
	 * @param prefix
	 * @throws IOException
	 */
	static void throwIOException(String prefix, int lastError) throws IOException {
		char[] buf = new char[512];
		int numChar = MyKernel32.INSTANCE.FormatMessage(MyKernel32.FORMAT_MESSAGE_FROM_SYSTEM, null, lastError, 0, buf, buf.length, null);
		String msg;
		if (numChar > 0) {
			msg = new String(buf, 0, numChar).trim();
		} else {
			msg = "<Unknown>";
		}
		
		throw new IOException(prefix + " (GetLastError=" + lastError + ": " + msg + ")");
	}
}
