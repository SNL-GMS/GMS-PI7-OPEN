package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import java.io.DataInputStream;
import java.io.IOException;

public class DataExtension extends Blockette{
	public static final int TYPE = 1001;
	public static final int LEN = 8;
	private short next;
	private byte timingQuality, microSeconds, frameCount;

	public DataExtension(int next, int timing, int microSec, int frames) {
		super(Type.DATA_EXTENSION);
		this.next = (short)next;
		this.timingQuality = (byte)timing;
		this.microSeconds = (byte)microSec;
		this.frameCount = (byte)frames;
	}
	
	public DataExtension(){ this(-1,-1,-1,-1); }
	
	public byte getTimingQuality(){ return timingQuality; }
	
	public byte getMicros(){ return microSeconds; }
	
	public byte getFrameCount(){ return frameCount; }

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
			throws IOException, PacketException {
		this.next = d.readShort();
		this.timingQuality = d.readByte();
		this.microSeconds = d.readByte();
		d.readByte(); //reserved byte, not used
		this.frameCount = d.readByte();
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[type=").append(getType())
				.append(",next=").append(next)
				.append(",timingQuality=").append(timingQuality)
				.append(",microSeconds=").append(microSeconds)
				.append(",frameCount=").append(frameCount)
				.append("]").toString();
	}
}
