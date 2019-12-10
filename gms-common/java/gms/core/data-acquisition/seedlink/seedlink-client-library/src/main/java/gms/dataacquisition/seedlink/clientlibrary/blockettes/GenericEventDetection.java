package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GenericEventDetection extends Blockette{
	public static final int TYPE = 200;
	public static final int LEN = 52;
	private short next;
	private float amplitude, period, backgroundEstimate;
	private byte flags;
	private byte[] time;
	private String detectorName;

	public GenericEventDetection(int next, double amp, double per, double bg,
			byte flags, byte[] time, String det) {
		super(Type.GENERIC_EVENT_DETECTION);
		this.next = (short)next;
		this.amplitude = (float)amp;
		this.period = (float)per;
		this.backgroundEstimate = (float)bg;
		this.flags = flags;
		this.time = Utils.checkLength("time", time, 10);
		this.detectorName = Utils.checkLength("det", det, 24);
	}
	
	public GenericEventDetection(){this(-1,0f,0f,0f,(byte)0,new byte[10],
			"????????????????????????");}
	
	public float getAmplitude(){ return amplitude; }
	
	public float getPeriod(){ return period; }
	
	public float getBackgroundEstimate(){ return backgroundEstimate; }
	
	public byte getFlags(){ return flags; }
	
	public byte[] getTime(){ return time; }
	
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
		d.readByte(); //reserved, not used
		Utils.fillBuffer(time,0,10,d);
		String dn = Utils.nextString(d, new byte[24], 24,
				"detector name");
		
		if(dn.indexOf(0) != -1) detectorName = dn.substring(0,
				dn.indexOf(0));
		else detectorName = dn;
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
				.append(",detectorName=").append(detectorName)
				.append("]").toString();
	}
}
