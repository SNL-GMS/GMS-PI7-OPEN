package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import java.io.DataInputStream;
import java.io.IOException;

public class SampleRate extends Blockette{
	public static final int TYPE = 100;
	public static final int LEN = 12;
	private short next;
	private float sampRate;
	private byte flags;

	public SampleRate(int next, double sampRate, byte flags) {
		super(Type.SAMPLE_RATE);
		this.next = (short)next;
		this.sampRate = (float)sampRate;
		this.flags = flags;
	}
	
	public SampleRate(){ this(-1,0.0,(byte)0); }
	
	public float getSampleRate(){ return sampRate; }
	
	public byte getFlags(){ return flags; }

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
	throws IOException, PacketException {
		next = d.readShort();
		sampRate = d.readFloat();
		flags = d.readByte();
		for(int i = 0; i < 3; i++) d.readByte(); //reserved, not used
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[type=").append(getType())
				.append(",next=").append(next)
				.append(",sampRate=").append(sampRate)
				.append(",flags=").append(flags)
				.append("]").toString();
	}
}
