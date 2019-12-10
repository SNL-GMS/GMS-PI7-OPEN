package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import gms.dataacquisition.seedlink.clientlibrary.data.Format;
import gms.dataacquisition.seedlink.clientlibrary.data.Format.Uncompressed.Sp;
import java.io.DataInput;

public class IEEEFloat extends Sp {
	@Override
	public int code() { return 4; }

	@Override
	public int sampleLength() { return 4; }

	@Override
	public String name() { return "IEEE_FLOAT"; }

	@Override
	public float[] decode(byte[] data, int samples, boolean bigEndian)
			throws Exception {
		DataInput in = Format.toDataInput(data, bigEndian);
		float[] s = new float[samples];
		for(int i = 0; i < s.length; i++) s[i++] = in.readFloat();
		return s;
	}
}
