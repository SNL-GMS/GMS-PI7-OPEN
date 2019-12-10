package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;

public class SineCalibration extends Blockette{
	public static final int TYPE = 310;
	public static final int LENGTH = 60;
	private short next;
	private byte[] time;
	private byte flags;
	private long calibDuration;
	private float period, amplitude;
	private String channel;
	private long referenceAmplitude;
	private String coupling, rolloff;
	
	public SineCalibration(int next, byte[] time, byte flags, long calibDur,
			double period, double amp, String chan, long refAmp,
			String coupling, String rolloff){
		super(Type.SINE_CALIBRATION);
		this.next = (short)next;
		this.time = Utils.checkLength("time", time, 10);
		this.flags = flags;
		this.calibDuration = calibDur;
		this.period = (float)period;
		this.amplitude = (float)amp;
		this.channel = Utils.checkLength("chan", chan, 3);
		this.referenceAmplitude = refAmp;
		this.coupling = Utils.checkLength("coupling", coupling, 12);
		this.rolloff = Utils.checkLength("rolloff", rolloff, 12);;
	}
	
	public SineCalibration(){
		this(-1,new byte[10],(byte)0,-1,0.0,0.0,"???",-1,"????????????",
				"????????????");
	}

	public byte[] getTime() { return time; }
	public byte getFlags() { return flags; }
	public long getCalibrationDuration() { return calibDuration; }
	public float getPeriod() { return period; }
	public float getAmplitude() { return amplitude; }
	public String getChannel() { return channel; }
	public long getReferenceAmplitude() { return referenceAmplitude; }
	public String getCoupling() { return coupling; }
	public String getRolloff() { return rolloff; }
	
	//TODO add boolean getters for individual flag values

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
		this.period = d.readFloat();
		this.amplitude = d.readFloat();
		this.channel = Utils.ascii(Utils.fillBuffer(new byte[3], d));
		d.readByte(); //Reserved, not used
		this.referenceAmplitude = Integer.toUnsignedLong(d.readInt());
		this.coupling = Utils.ascii(Utils.fillBuffer(new byte[12], d));
		this.rolloff = Utils.ascii(Utils.fillBuffer(new byte[12], d));
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[next=").append(next)
				.append(",time=").append(Utils.ascii(time))
				.append(",flags=").append(flags)
				.append(",calibDuration=").append(calibDuration)
				.append(",period=").append(period)
				.append(",amplitude=").append(amplitude)
				.append(",channel=").append(channel)
				.append(",referenceAmplitude=").append(referenceAmplitude)
				.append(",coupling=").append(coupling)
				.append(",rolloff=").append(rolloff)
				.append("]").toString();
	}
}
