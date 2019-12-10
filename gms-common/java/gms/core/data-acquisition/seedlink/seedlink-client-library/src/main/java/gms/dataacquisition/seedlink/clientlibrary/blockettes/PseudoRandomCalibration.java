package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;

public class PseudoRandomCalibration extends Blockette {
	public static final int TYPE = 320;
	public static final int LEN = 64;
	private short next;
	private byte[] time;
	private byte flags;
	private long calibDuration;
	private float peakToPeakStepAmplitude;
	private String channel;
	private long referenceAmplitude;
	private String coupling, rolloff, noiseType;

	public PseudoRandomCalibration(int next, byte[] time, byte flags,
			long calibDur, float p2pAmp, String channel, long refAmp,
			String coupling, String rolloff, String noiseType) {
		super(Type.PSEUDO_RANDOM_CALIBRATION);
		this.next = (short)next;
		this.time = Utils.checkLength("time", time, 10);
		this.flags = flags;
		this.calibDuration = calibDur;
		this.peakToPeakStepAmplitude = p2pAmp;
		this.channel = Utils.checkLength("channel",channel,3);
		this.referenceAmplitude = refAmp;
		this.coupling = Utils.checkLength("coupling", coupling, 12);
		this.rolloff = Utils.checkLength("rolloff", rolloff, 12);
		this.noiseType = Utils.checkLength("noiseType", noiseType, 8);
	}
	
	public PseudoRandomCalibration(){
		this(-1,new byte[10],(byte)0,0,0f,"???",0l,"????????????",
				"????????????","????????");
	}

	public byte[] getTime() { return time; }
	public byte getFlags() { return flags; }
	public long getCalibrationDuration() { return calibDuration; }
	public float getPeakToPeakStepAmplitude() {return peakToPeakStepAmplitude;}
	public String getChannel() { return channel; }
	public long getReferenceAmplitude() { return referenceAmplitude; }
	public String getCoupling() { return coupling; }
	public String getRolloff() { return rolloff; }
	public String getNoiseType() { return noiseType; }

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
	throws IOException, PacketException {
		this.next = d.readShort();
		this.time = Utils.fillBuffer(new byte[10],d);
		d.readByte(); //Reserved, not used
		this.flags = d.readByte();
		this.calibDuration = Integer.toUnsignedLong(d.readInt());
		this.peakToPeakStepAmplitude = d.readFloat();
		this.channel = Utils.ascii(Utils.fillBuffer(new byte[3], d));
		d.readByte(); //Reserved, not used
		this.referenceAmplitude = Integer.toUnsignedLong(d.readInt());
		this.coupling = Utils.ascii(Utils.fillBuffer(new byte[12], d));
		this.rolloff = Utils.ascii(Utils.fillBuffer(new byte[12], d));
		this.noiseType = Utils.ascii(Utils.fillBuffer(new byte[8], d));
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[next=").append(next)
				.append(",time=").append(Utils.ascii(time))
				.append(",flags=").append(flags)
				.append(",calibDuration=").append(calibDuration)
				.append(",peakToPeakStepAmplitude=")
						.append(peakToPeakStepAmplitude)
				.append(",channel=").append(channel)
				.append(",referenceAmplitude=").append(referenceAmplitude)
				.append(",coupling=").append(coupling)
				.append(",rolloff=").append(rolloff)
				.append(",noiseType=").append(noiseType)
				.append("]").toString();
	}
}
