package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MurdockEventDetection extends Blockette{
	public static final int TYPE = 201;
	public static final int LEN = 60;
	private short next;
	private float amplitude, period, backgroundEstimate;
	private byte flags;
	private byte[] time, snr;
	private byte lookBack, pickAlgorithm;
	private String detectorName;
	
	public MurdockEventDetection(int next, double amp, double per, double bg,
			byte flags, byte[] time, byte[] snr, byte lb,byte pick,String det){
		super(Type.MURDOCK_EVENT_DETECTION);
		this.next = (short)next;
		this.amplitude = (float)amp;
		this.period = (float)per;
		this.backgroundEstimate = (float)bg;
		this.flags = flags;
		this.time = Utils.checkLength("time",time,10);
		this.snr = Utils.checkLength("snr",snr,6);
		this.lookBack = lb;
		this.pickAlgorithm = pick;
		this.detectorName = Utils.checkLength("det",det,24);
	}
	
	public MurdockEventDetection(){
		this(1,0f,0f,0f,(byte)0,new byte[10],new byte[6],(byte)0,(byte)0,
				"????????????????????????");
	}
	
	public float getAmplitude(){ return amplitude; }
	
	public float getPeriod(){ return period; }
	
	public float getBackgroundEstimate(){ return backgroundEstimate; }
	
	public byte getFlags(){ return flags; }
	
	public byte[] getSignalOnsetTime(){ return time; }
	
	public byte[] getSnr(){ return snr; }
	
	public byte getLookBack(){ return lookBack; }
	
	public byte getPickAlgorithm(){ return pickAlgorithm; }
	
	public String getDetectorName(){ return detectorName; }

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d) 
	throws IOException, PacketException {
		this.next = d.readShort();
		this.amplitude = d.readFloat();
		this.period = d.readFloat();
		this.backgroundEstimate = d.readFloat();
		this.flags = d.readByte();
		d.readByte(); //Reserved, not used
		Utils.fillBuffer(this.time, d);
		Utils.fillBuffer(this.snr, d);
		this.lookBack = d.readByte();
		this.pickAlgorithm = d.readByte();
		this.detectorName =Utils.nextString(d,new byte[24],24,"detector name");
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[type=").append(getType())
				.append(",next=").append(next)
				.append(",amplitude=").append(amplitude)
				.append(",period=").append(period)
				.append(",backgroundEstimate=").append(backgroundEstimate)
				.append(",flags=").append(flags)
				.append(",time=").append(
						new String(time,StandardCharsets.US_ASCII))
				.append(",snr=").append(snr)
				.append(",lookBack=").append(lookBack)
				.append(",pickAlgorithm=").append(pickAlgorithm)
				.append(",detectorName=").append(detectorName)
				.append("]").toString();
	}
}
