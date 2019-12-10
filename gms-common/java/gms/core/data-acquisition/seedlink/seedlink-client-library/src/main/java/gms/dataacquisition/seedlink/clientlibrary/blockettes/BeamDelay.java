package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.Utils;
import java.io.DataInputStream;
import java.io.IOException;

public class BeamDelay extends Blockette{
	public static final int TYPE = 405;
	public static final int LEN = 6;
	private short next;
	private byte[] delayValues;
	
	public BeamDelay(int next, byte[] vals){
		super(Type.BEAM_DELAY);
		this.next = (short)next;
		this.delayValues = Utils.checkLength("vals", vals, 2);
	}
	
	public BeamDelay(){ this(-1, new byte[2]); }
	
	public byte[] getDelayValues(){ return delayValues; }

	@Override
	public short getNextBlocketteOffset() { return next; }

	@Override
	public void read(DataInputStream d)
			throws IOException, PacketException {
		this.next = d.readShort();
		this.delayValues = Utils.fillBuffer(new byte[2], d);
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[next=").append(next)
				.append(",delayValues=").append(Utils.ascii(delayValues))
				.append("]").toString();
	}
}
