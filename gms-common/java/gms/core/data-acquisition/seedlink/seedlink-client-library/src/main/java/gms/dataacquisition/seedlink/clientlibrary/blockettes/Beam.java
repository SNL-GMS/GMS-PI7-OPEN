package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import java.io.DataInputStream;
import java.io.IOException;

public class Beam extends Blockette{
	public static final int TYPE = 400;
	public static final int LEN = 16;
	private short next;
	private float azimuth, slowness;
	private short configuration;
	
	public Beam(int next, double az, double slow, int config) {
		super(Type.BEAM);
		this.next = (short)next;
		this.azimuth = (float)az;
		this.slowness = (float)slow;
		this.configuration = (short)config;
	}
	
	public Beam(){ this(-1,Double.NaN,Double.NaN,-1); }
	
	public float getAzimuth(){ return azimuth; }
	public float getSlowness(){ return slowness; }
	public short getConfiguration(){ return configuration; }

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
			throws IOException, PacketException {
		this.next = d.readShort();
		this.azimuth = d.readFloat();
		this.slowness = d.readFloat();
		this.configuration = d.readShort();
		//next two bytes are reserved and not used:
		d.readByte(); d.readByte();
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[next=").append(next)
				.append(",azimuth=").append(azimuth)
				.append(",slowness=").append(slowness)
				.append(",configuration=").append(configuration)
				.append("]").toString();
	}
}
