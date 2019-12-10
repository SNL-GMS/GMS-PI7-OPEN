package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import gms.dataacquisition.seedlink.clientlibrary.data.Format.Uncompressed.Int;

public class Int24 extends Int {
	@Override
	public int code() { return 2; }

	@Override
	public int sampleLength() { return 3; }

	@Override
	public String name() { return "INT_24"; }

	@Override
	public int[] decode(byte[] d, int samples, boolean bigEndian)
			throws Exception {
		int[] s = new int[samples];
		
		if(bigEndian){
			for(int i = 0; i < samples; i++){
				int b = 3*i;
				s[i] = (d[b+2]&0xFF)<<16 | (d[b+1]&0xFF)<<8 | (d[b+0]&0xFF);
			}
		}
		else{
			for(int i = 0; i < samples; i++){
				int b = 3*i;
				s[i] = (d[b+0]&0xFF)<<16 | (d[b+1]&0xFF)<<8 | (d[b+2]&0xFF);
			}
		}
		
		return s;
	}
}
