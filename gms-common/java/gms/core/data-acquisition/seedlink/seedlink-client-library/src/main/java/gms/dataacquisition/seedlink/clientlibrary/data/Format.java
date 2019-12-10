package gms.dataacquisition.seedlink.clientlibrary.data;

import com.google.common.io.LittleEndianDataInputStream;
import gms.dataacquisition.seedlink.clientlibrary.Packet;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;

public interface Format {
	int code();
	
	int sampleLength();
	
	String name();
	
	boolean isCompressed();
	
	Number[] samples(byte[] data, int numSamp, boolean bigEndian)
			throws Exception;
	
	default Number[] samples(Packet p) throws Exception{
		return samples(p.getData(),p.getSampleCount(),p.isDataBigEndian());
	}
	
	abstract class Compressed implements Format{
		@Override
		public boolean isCompressed(){ return true; }
		
		@Override
		public int sampleLength(){ return -1; }
		
		public static abstract class Int extends Compressed{
			public abstract int[] decode(byte[] data, int samples,
					boolean bigEndian) throws Exception;
			
			public int[] decode(Packet p) throws Exception{
				return decode(p.getData(),p.getSampleCount(),
						p.isDataBigEndian());
			}
			
			@Override
			public Number[] samples(byte[] data, int numSamp,boolean bigEndian)
					throws Exception{
				int[] s = decode(data,numSamp,bigEndian);
				Number[] n = new Number[s.length];
				for(int i = 0; i < n.length; i++) n[i] = s[i];
				return n;
			}
		}
	}
	
	abstract class Uncompressed implements Format{
		@Override
		public boolean isCompressed(){ return false; }
		
		public static abstract class Int extends Uncompressed{
			public abstract int[] decode(byte[] data, int samples,
					boolean bigEndian) throws Exception;
			
			public int[] decode(Packet p) throws Exception{
				return decode(p.getData(),p.getSampleCount(),
						p.isDataBigEndian());
			}
			
			@Override
			public Number[] samples(byte[] data, int numSamp,boolean bigEndian)
					throws Exception{
				int[] s = decode(data,numSamp,bigEndian);
				Number[] n = new Number[s.length];
				for(int i = 0; i < n.length; i++) n[i] = s[i];
				return n;
			}
		}
		
		public static abstract class Sp extends Uncompressed{
			public abstract float[] decode(byte[] data, int samples,
					boolean bigEndian) throws Exception;
			
			public float[] decode(Packet p) throws Exception{
				return decode(p.getData(),p.getSampleCount(),
						p.isDataBigEndian());
			}
			
			@Override
			public Number[] samples(byte[] data, int numSamp,boolean bigEndian)
					throws Exception{
				float[] s = decode(data,numSamp,bigEndian);
				Number[] n = new Number[s.length];
				for(int i = 0; i < n.length; i++) n[i] = s[i];
				return n;
			}
		}
		
		public static abstract class Dp extends Uncompressed{
			public abstract double[] decode(byte[] data, int samples,
					boolean bigEndian) throws Exception;
			
			public double[] decode(Packet p) throws Exception{
				return decode(p.getData(),p.getSampleCount(),
						p.isDataBigEndian());
			}
			
			@Override
			public Number[] samples(byte[] data, int numSamp,boolean bigEndian)
					throws Exception{
				double[] s = decode(data,numSamp,bigEndian);
				Number[] n = new Number[s.length];
				for(int i = 0; i < n.length; i++) n[i] = s[i];
				return n;
			}
		}
	}
	
	static DataInput toDataInput(byte[] data, boolean bigEndian){
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		return bigEndian ? new DataInputStream(in) :
			new LittleEndianDataInputStream(in);
	}
}