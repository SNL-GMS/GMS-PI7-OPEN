package gms.dataacquisition.seedlink.clientlibrary.blockettes;

import gms.dataacquisition.seedlink.clientlibrary.Blockette;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import gms.dataacquisition.seedlink.clientlibrary.data.Format;
import gms.dataacquisition.seedlink.clientlibrary.data.Formats;
import java.io.DataInputStream;
import java.io.IOException;

public class DataOnly extends Blockette{
	public static final int TYPE = 1000;
	public static final int LEN = 8;
	private short next;
	private byte format, wordOrder, dataLength;
	
	public DataOnly(int next, int fmt, int order, int len) {
		super(Type.DATA_ONLY);
		this.next = (short)next;
		format = (byte)fmt;
		wordOrder = (byte)order;
		dataLength = (byte)len;
	}
	
	public DataOnly(){ this(-1,-1,-1,-1); }
	
	@Override
	public void read(DataInputStream d)
	throws IOException,PacketException{
		next = d.readShort();
		format = (byte)d.read();
		wordOrder = (byte)d.read();
		dataLength = (byte)d.read();
		d.readByte();//reserved byte, not used
	}

	@Override
	public short getNextBlocketteOffset() { return next; }
	
	public byte getFormatCode(){ return format; }
	
	public byte getWordOrder(){ return wordOrder; }
	
	public byte getDataLength(){ return dataLength; }
	
	public Format getFormat(){ return Formats.getFormat(getFormatCode()); }
	
	public boolean isBigEndian(){ return getWordOrder() > 0; }

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName())
				.append("[type=").append(getType())
				.append(",next=").append(next)
				.append(",format=").append(Formats.name((int)format))
				.append(",wordOrder=").append(wordOrder)
				.append(",dataLength=").append(dataLength)
				.append("]").toString();
	}
}
