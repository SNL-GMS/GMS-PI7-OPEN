package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;

public class GenericCalibration extends Blockette{
	public static final int TYPE = 390;
	public static final int LEN = 28;
	private short next;
	private byte[] time;
	private byte flags;
	private long calibDuration;
	private float calibAmplitude;
	private String channel;

	public GenericCalibration(int next, byte[] time, byte flags,
			long calibDur, double calibAmp, String channel) {
		super(Type.GENERIC_CALIBRATION);
		this.next = (short)next;
		this.time = Utils.checkLength("time", time, 10);
		this.flags = flags;
		this.calibDuration = calibDur;
		this.calibAmplitude = (float)calibAmp;
		this.channel = Utils.checkLength("channel", channel, 3);
	}
	
	public GenericCalibration(){ this(-1,new byte[10],(byte)0,-1,0.0,"???"); }

	public byte[] getTime() { return time; }
	public byte getFlags() { return flags; }
	public long getCalibrationDuration() { return calibDuration; }
	public float getCalibrationAmplitude() { return calibAmplitude; }
	public String getChannel() { return channel; }

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
	throws IOException, PacketException {
		this.next = d.readShort();
		this.time = Utils.fillBuffer(new byte[10], d);
		d.readByte(); //Reserved, not used
		this.flags = d.readByte();
		this.calibDuration = Integer.toUnsignedLong(d.readInt());
		this.calibAmplitude = d.readFloat();
		this.channel = Utils.ascii(Utils.fillBuffer(new byte[3], d));
		d.readByte(); //Reserved, not used
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[next=").append(next)
				.append(",time=").append(Utils.ascii(time))
				.append(",flags=").append(flags)
				.append(",calibDuration=").append(calibDuration)
				.append(",calibAmplitude=").append(calibAmplitude)
				.append(",channel=").append(channel)
				.append("]").toString();
	}
}
