package gms.dataacquisition.seedlink.clientlibrary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Allows for Packet objects to retain the original binary data that they were
 * parsed from (useful for debugging).
 * @author bjlawry
 */
public class CachedInputStream extends InputStream{
	private InputStream impl;
	private ByteArrayOutputStream cachedReads;
	private int cachedBytes;
	
	public CachedInputStream(InputStream is){
		impl = is;
		cachedReads = new ByteArrayOutputStream();
		cachedBytes = 0;
	}
	
	public byte[] getCachedData(){ return cachedReads.toByteArray(); }
	
	public int getCachedBytesCount(){ return cachedBytes; }
	
	public void clearCachedData(){
		try {
			cachedReads.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cachedReads = new ByteArrayOutputStream();
	}
	
	@Override
	public int available() throws IOException{ return impl.available(); }
	
	@Override
	public void close() throws IOException{ impl.close(); }
	
	@Override
	public void mark(int limit){ impl.mark(limit); }
	
	@Override
	public boolean markSupported(){ return impl.markSupported(); }

	@Override
	public int read() throws IOException {
		int read = impl.read();
		cachedReads.write(read);
		cachedBytes++;
		return read;
	}
	
	@Override
	public int read(byte[] b, int offset, int length) throws IOException{
		int read = impl.read(b, offset, length);
		cachedReads.write(b, offset, read);
		cachedBytes += read;
		return read;
	}
	
	@Override
	public int read(byte[] b) throws IOException{ return read(b,0,b.length); }
	
	@Override
	public void reset() throws IOException{ impl.reset(); }
	
	@Override
	public long skip(long s) throws IOException{ return impl.skip(s); }
}
