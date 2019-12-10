package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;

public class Timing extends Blockette{
	public static final int TYPE = 500;
	public static final int LEN = 200;
	private short next;
	private float vcoCorrection;
	private byte[] time;
	private byte microSec, receptionQuality;
	private long exceptionCount;
	private String exceptionType, clockModel, clockStatus;
	
	public Timing(int next, double vco, byte[] time, byte micros, byte recepQ,
			long exceptions, String excType, String clkMod, String clkStat){
		super(Type.TIMING);
		this.next = (short)next;
		this.vcoCorrection = (float)vco;
		this.time = Utils.checkLength("time", time, 10);
		this.microSec = micros;
		this.receptionQuality = recepQ;
		this.exceptionCount = exceptions;
		this.exceptionType = Utils.checkLength("excType", excType, 16);
		this.clockModel = Utils.checkLength("clkMod", clkMod, 32);
		this.clockStatus = Utils.checkLength("clkStat", clkStat, 128);
	}
	
	public Timing(){
		this(-1,Double.NaN,new byte[10],(byte)-1,(byte)-1,-1l,
				Utils.repeatChar('?',16),Utils.repeatChar('?',32),
				Utils.repeatChar('?',128));
	}

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
	throws IOException, PacketException {
		this.next = d.readShort();
		this.vcoCorrection = d.readFloat();
		this.time = Utils.fillBuffer(time, d);
		this.microSec = d.readByte();
		this.receptionQuality = d.readByte();
		this.exceptionCount = Integer.toUnsignedLong(d.readInt());
		this.exceptionType = Utils.ascii(Utils.fillBuffer(new byte[16], d));
		this.clockModel = Utils.ascii(Utils.fillBuffer(new byte[32], d));
		this.clockStatus = Utils.ascii(Utils.fillBuffer(new byte[128], d));
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[next=").append(next)
				.append(",vcoCorrection=").append(vcoCorrection)
				.append(",time=").append(Utils.ascii(time))
				.append(",microSec=").append(microSec)
				.append(",receptionQuality=").append(receptionQuality)
				.append(",exceptionCount=").append(exceptionCount)
				.append(",exceptionType=").append(exceptionType)
				.append(",clockModel=").append(clockModel)
				.append(",clockStatus=").append(clockStatus)
				.append("]").toString();
	}
}
