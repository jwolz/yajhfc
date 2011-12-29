package yajhfc.util.pipe.win32;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.sun.jna.ptr.IntByReference;

/**
 * Input stream to create and read from an inbound Win32 named pipe.
 * 
 * After creating the pipe the first read operation blocks until a client connects to the pipe.
 * This class is not thread-safe.
 * 
 * @author jonas
 *
 */
public class Win32NamedPipeInputStream extends InputStream {

	/**
	 * The prefix of the pseudo file system (including terminating backslash) where named pipes on the local system reside.
	 */
	public static final String LOCAL_PIPE_PREFIX = "\\\\.\\pipe\\";
	
	protected final Win32NamedPipe pipe;
	protected ByteBuffer singleByteBuf;
	protected final IntByReference bytesRead = new IntByReference();
	
	/**
	 * Opens a named pipe at the specified location.
	 * Usually this will be something like "\\\\.\\pipe\\mypipe"
	 * @param fileName
	 * @throws IOException
	 */
	public Win32NamedPipeInputStream(String fileName) throws IOException {
		pipe = new Win32NamedPipe(fileName, MyKernel32.PIPE_ACCESS_INBOUND);
	}
	
	@Override
	public int read() throws IOException {
		if (singleByteBuf == null) {
			singleByteBuf = ByteBuffer.allocate(1);
		}
		int numRead = read(singleByteBuf);
		if (numRead < 0) {
			return -1;
		} else {
			return ((int)singleByteBuf.get(0) & 0xff);
		}
	}
	
	private byte[] lastB = null;
	private ByteBuffer lastBuf = null;
	private ByteBuffer getBuffer(byte[] b, int off, int len) {
		if (b == lastB) { // Optimization for the common case of repeatedly reading into the same array
			lastBuf.position(off);
			lastBuf.limit(off+len);
			return lastBuf;
		} else {
			lastB = b;
			return lastBuf = ByteBuffer.wrap(b, off, len);
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return read(getBuffer(b, off, len));
	}
	
	/**
	 * Reads bytes into the given buffer.
	 * The bytes are written at buf.position() and a maximum of buf.limit() - buf.position() bytes are read
	 * @param buf
	 * @return the number of bytes read or -1 if EOF was reached
	 * @throws IOException
	 */
	public int read(ByteBuffer buf) throws IOException {
		pipe.ensureConnected();
		boolean success = MyKernel32.INSTANCE.ReadFile(pipe.hPipe, buf, buf.limit()-buf.position(), bytesRead, null);		
		
		if (success) {
			int rv = bytesRead.getValue();
			if (rv == 0) { // EOF
				return -1;
			} else {
				return rv;
			}
		} else {
			int lastError = MyKernel32.INSTANCE.GetLastError();
			if (lastError == MyKernel32.ERROR_BROKEN_PIPE) {
				return -1; // Interpret broken pipe as EOF
			} else {
				Win32NamedPipe.throwIOException("ReadFile failed", lastError);
				return -2; // Never reached
			}
		}
	}
	
	
	@Override
	public void close() throws IOException {
		pipe.close();
		lastBuf = null;
		lastB = null;
	}

}
