package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import gms.dataacquisition.seedlink.clientlibrary.data.Format;
import gms.dataacquisition.seedlink.clientlibrary.data.Format.Uncompressed.Dp;
import java.io.DataInput;

public class IEEEDouble extends Dp {
	@Override
	public int code() { return 5; }

	@Override
	public int sampleLength() { return 8; }

	@Override
	public String name() { return "IEEE_DOUBLE"; }

	@Override
	public double[] decode(byte[] data, int samples, boolean bigEndian)
			throws Exception {
		DataInput in = Format.toDataInput(data,bigEndian);
		
		double[] s = new double[samples];
		for(int i = 0; i < s.length; i++) s[i++] = in.readDouble();
		return s;
	}
}
