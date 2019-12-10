package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;

public class StepCalibration extends Blockette{
	public static final int TYPE = 300;
	public static final int LENGTH = 60;
	private short next;
	private byte[] time;
	private byte stepCount, flags;
	private long stepDuration, intervalDuration;
	private float calibrationAmplitude;
	private String channel;
	private long referenceAmplitude;
	private String coupling, rolloff;
	
	public StepCalibration(int next, byte[] time, int stepCt, byte flags, 
			long stepDur, long intDur, long calibAmp, String chan, long refAmp,
			String coupling, String rolloff) {
		super(Type.STEP_CALIBRATION);
		this.next = (short)next;
		this.time = Utils.checkLength("time", time, 10);
		this.stepCount = (byte)stepCt;
		this.flags = flags;
		this.stepDuration = stepDur;
		this.intervalDuration = intDur;
		this.calibrationAmplitude = calibAmp;
		this.channel = Utils.checkLength("chan", chan, 3);
		this.referenceAmplitude = refAmp;
		this.coupling = Utils.checkLength("coupling", coupling, 12);
		this.rolloff = Utils.checkLength("rolloff", rolloff, 12);
	}
	
	public StepCalibration(){
		this(-1,new byte[10],-1,(byte)-1,-1,-1,-1,"???",-1,"????????????",
				"????????????");
	}

	public short getNext() { return next; }
	public byte[] getCalibrationBeginTime() { return time; }
	public byte getStepCount() { return stepCount; }
	public byte getFlags() { return flags; }
	public long getStepDuration() { return stepDuration; }
	public long getIntervalDuration() { return intervalDuration; }
	public float getCalibrationAmplitude() { return calibrationAmplitude; }
	public String getChannel() { return channel; }
	public long getReferenceAmplitude() { return referenceAmplitude; }
	public String getCoupling() { return coupling; }
	public String getRolloff() { return rolloff; }
	
	//TODO add boolean getters for individual flag values

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
	throws IOException,PacketException{
		this.next = d.readShort();
		Utils.fillBuffer(this.time,d);
		this.stepCount = d.readByte();
		this.flags = d.readByte();
		this.stepDuration = Integer.toUnsignedLong(d.readInt());
		this.intervalDuration = Integer.toUnsignedLong(d.readInt());
		this.calibrationAmplitude = d.readFloat();
		this.channel = Utils.ascii(Utils.fillBuffer(new byte[3],d));
		d.readByte(); //Reserved, not used
		this.referenceAmplitude = Integer.toUnsignedLong(d.readInt());
		this.coupling = Utils.ascii(Utils.fillBuffer(new byte[12],d));
		this.rolloff = Utils.ascii(Utils.fillBuffer(new byte[12],d));
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[type=").append(getType())
				.append(",next=").append(next)
				.append(",time=").append(Utils.ascii(time))
				.append(",stepCount=").append(stepCount)
				.append(",flags=").append(flags)
				.append(",stepDuration=").append(stepDuration)
				.append(",intervalDuration=").append(intervalDuration)
				.append(",calibrationAmplitude=").append(calibrationAmplitude)
				.append(",channel=").append(channel)
				.append(",referenceAmplitude=").append(referenceAmplitude)
				.append(",coupling=").append(coupling)
				.append(",rolloff=").append(rolloff)
				.append("]").toString();
	}
}
