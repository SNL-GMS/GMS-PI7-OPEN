package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import gms.dataacquisition.seedlink.clientlibrary.data.Format;
import gms.dataacquisition.seedlink.clientlibrary.data.Format.Uncompressed.Int;
import java.io.DataInput;

public class Int32 extends Int {
	@Override
	public int code() { return 3; }

	@Override
	public int sampleLength() { return 4; }

	@Override
	public String name() { return "INT_32"; }

	@Override
	public int[] decode(byte[] data, int samples, boolean bigEndian)
			throws Exception {
		DataInput in = Format.toDataInput(data, bigEndian);
		int[] s = new int[samples];
		for(int i = 0; i < s.length; i++) s[i++] = in.readInt();
		return s;
	}
}
