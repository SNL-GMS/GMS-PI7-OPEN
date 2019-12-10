package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;

public class CalibrationAbort extends Blockette{
	public static final int TYPE = 395;
	public static final int LEN = 16;
	private short next;
	private byte[] time;

	public CalibrationAbort(int next, byte[] time) {
		super(Type.GENERIC_CALIBRATION);
		this.next = (short)next;
		this.time = Utils.checkLength("time", time, 10);
	}
	
	public CalibrationAbort(){ this(-1,new byte[10]); }
	
	public byte[] getTime(){ return time; }

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
	throws IOException, PacketException {
		this.next = d.readShort();
		this.time = Utils.fillBuffer(new byte[10], d);
		
		//two reserved, unused bytes remain:
		d.readByte(); d.readByte();
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[next=").append(next)
				.append(",time=").append(Utils.ascii(time))
				.append("]").toString();
	}
}
