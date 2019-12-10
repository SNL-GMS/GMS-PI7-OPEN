package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import gms.dataacquisition.seedlink.clientlibrary.data.Format.Uncompressed;

public class Ascii extends Uncompressed {
	@Override
	public int code() { return 0; }

	@Override
	public int sampleLength() { return 1; }

	@Override
	public String name() { return "ASCII"; }

	@Override
	public Number[] samples(byte[] data, int numSamp, boolean bigEndian)
			throws Exception {
		throw new UnsupportedOperationException();
	}
}
