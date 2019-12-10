package gms.dataacquisition.seedlink.clientlibrary.control;


import static gms.dataacquisition.seedlink.clientlibrary.Utils.nextByte;
import static gms.dataacquisition.seedlink.clientlibrary.Utils.nextBytes;
import static gms.dataacquisition.seedlink.clientlibrary.Utils.nextInt;
import static gms.dataacquisition.seedlink.clientlibrary.Utils.nextShort;
import static gms.dataacquisition.seedlink.clientlibrary.Utils.nextString;

import gms.dataacquisition.seedlink.clientlibrary.Packet;
import gms.dataacquisition.seedlink.clientlibrary.PacketException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class DataHeader {
	public static final int HEADER_LEN = 48;
	public static final int SEQ_NUM_LEN = Packet.SEQ_FMT.length();
	public static final int Q_LEN = 1;
	public static final int STA_ID_LEN = 5;
	public static final int LOC_ID_LEN = 2;
	public static final int CHAN_ID_LEN = 3;
	public static final int NET_ID_LEN = 2;
	public static final int RECORD_TIME_LEN = 10;
	private String seqNum, quality, staId, locId, chanId, netId;
	private byte[] startTime;
	private short numSamp, sampRateFactor, sampRateMultiplier;
	private byte activityFlags, ioFlags, qFlags, blockettes;
	private int timeCorrection;
	private short beginningOfData, firstBlockette;
	
	private DataHeader(){
		
	}
	
	/**
	 * Taken from page 110 of the SEED 2.4 Manual.
	 * @return sample rate computed from sampRateFactor and sampRateMultiplier
	 */
	public double getSampleRate(){
		if(getSampRateFactor() > 0 && getSampRateMultiplier() > 0)
			return getSampRateFactor()*getSampRateMultiplier();
		
		if(getSampRateFactor() > 0 && getSampRateMultiplier() < 0)
			return -1.0*getSampRateFactor()/getSampRateMultiplier();
		
		if(getSampRateFactor() < 0 && getSampRateMultiplier() > 0)
			return -1.0*getSampRateMultiplier()/getSampRateFactor();
		
		return 1/(getSampRateFactor()*getSampRateMultiplier());
	}
	
	public long getStartTimeMillis(){
		try {
			DataInputStream d = new DataInputStream(new ByteArrayInputStream(
					getStartTime()));
			
			ZonedDateTime z = ZonedDateTime.now(ZoneOffset.UTC)
					.withYear(d.readUnsignedShort())
					.withDayOfYear(d.readUnsignedShort())
					.withHour(d.readUnsignedByte())
					.withMinute(d.readUnsignedByte())
					.withSecond(d.readUnsignedByte());
			
			d.readByte(); //unused
			
			return z.withNano(100000*d.readUnsignedShort()).toInstant()
					.toEpochMilli();
		} catch (IOException e) {
			//this will never happen
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public String getSeqNum() { return seqNum; }
	public String getQuality() { return quality; }
	public String getStationId() { return staId; }
	public String getLocationId() { return locId; }
	public String getChannelId() { return chanId; }
	public String getNetworkId() { return netId; }
	public byte[] getStartTime() { return startTime; }
	public short getNumSamp() { return numSamp; }
	public short getSampRateFactor() { return sampRateFactor; }
	public short getSampRateMultiplier() { return sampRateMultiplier; }
	public byte getActivityFlags() { return activityFlags; }
	public byte getIoFlags() { return ioFlags; }
	public byte getQualityFlags() { return qFlags; }
	public byte getNumBlockettes() { return blockettes; }
	public int getTimeCorrection() { return timeCorrection; }
	public short getBeginningOfData() { return beginningOfData; }
	public short getFirstBlockette() { return firstBlockette; }
	
	public byte[] toBytes() {
		try(ByteArrayOutputStream o = new ByteArrayOutputStream();
				DataOutputStream d = new DataOutputStream(o)){
			d.write(seqNum.getBytes(StandardCharsets.US_ASCII));
			d.write(quality.getBytes(StandardCharsets.US_ASCII));
			d.write(" ".getBytes(StandardCharsets.US_ASCII));
			d.write(staId.getBytes(StandardCharsets.US_ASCII));
			d.write(locId.getBytes(StandardCharsets.US_ASCII));
			d.write(chanId.getBytes(StandardCharsets.US_ASCII));
			d.write(netId.getBytes(StandardCharsets.US_ASCII));
			d.write(startTime);
			d.writeShort(numSamp);
			d.writeShort(sampRateFactor);
			d.writeShort(sampRateMultiplier);
			d.write(activityFlags);
			d.write(ioFlags);
			d.write(qFlags);
			d.write(blockettes);
			d.writeInt(timeCorrection);
			d.writeShort(beginningOfData);
			d.writeShort(firstBlockette);
			
			byte[] bytes = o.toByteArray();
			if(bytes.length != HEADER_LEN){
				System.err.println("header length must be "+HEADER_LEN+
					" but was "+bytes.length+
					", truncating/padding as necessary");
				return Arrays.copyOf(bytes, HEADER_LEN);
			}
			return bytes;
		} catch (IOException e){
			//This will never happen:
			e.printStackTrace();
			return new byte[HEADER_LEN];
		}
	}
	
	@Override
	public String toString(){ 
		return new StringBuilder(getClass().getName())
				.append("[seqNum=").append(seqNum)
				.append(",quality=").append(quality)
				.append(",staId=").append(staId)
				.append(",locId=").append(locId)
				.append(",chanId=").append(chanId)
				.append(",netId=").append(netId)
				.append(",startTime=").append(startTime)
				.append(",numSamp=").append(numSamp)
				.append(",sampRateFactor=").append(sampRateFactor)
				.append(",sampRateMultiplier=").append(sampRateMultiplier)
				.append(",activityFlags=").append(activityFlags)
				.append(",ioFlags=").append(ioFlags)
				.append(",qFlags=").append(qFlags)
				.append(",blockettes=").append(blockettes)
				.append(",timeCorrection=").append(timeCorrection)
				.append(",beginningOfData=").append(beginningOfData)
				.append(",firstBlockette=").append(firstBlockette)
				.append("]").toString();
	}
	
	public static DataHeader read(InputStream is)
	throws IOException, PacketException {
		byte[] buf = new byte[RECORD_TIME_LEN];
		DataInputStream d = (is instanceof DataInputStream) ?
				(DataInputStream)is : new DataInputStream(is);
		
		DataHeader header = new DataHeader();
		header.seqNum = nextString(d,buf,SEQ_NUM_LEN,"sequence number");
		header.quality = nextString(d,buf,Q_LEN,"quality indicator");
		nextString(is,buf,1,"reserved byte");
		header.staId = nextString(d,buf,STA_ID_LEN,"station id");
		header.locId = nextString(d,buf,LOC_ID_LEN,"location id");
		header.chanId = nextString(d,buf,CHAN_ID_LEN,"channel id");
		header.netId = nextString(d,buf,NET_ID_LEN,"network id");
		header.startTime =nextBytes(d,buf,RECORD_TIME_LEN,"record start time");
		header.numSamp = nextShort(d,"number of samples");
		header.sampRateFactor = nextShort(d,"sample rate factor");
		header.sampRateMultiplier = nextShort(d,"sample rate multiplier");
		header.activityFlags = nextByte(d,"activity flags");
		header.ioFlags = nextByte(d,"io/clock flags");
		header.qFlags = nextByte(d,"quality flags");
		header.blockettes = nextByte(d,"number of blockettes");
		header.timeCorrection = nextInt(d,"time correction");
		header.beginningOfData = nextShort(d,"beginning of data");
		header.firstBlockette = nextShort(d,"first blockette");
		
		return header;
	}
	
	public static DataHeader readAscii(String s) throws PacketException{
		if(s.length() < HEADER_LEN) throw new PacketException(
				"SEED Data Headers must be 48 characters but found "+
						s.length());
		
		try {
			return read(new ByteArrayInputStream(s.getBytes(
					StandardCharsets.US_ASCII)));
		} catch (IOException e) {
			//This will never happen because BAIS doesn't throw exceptions:
			throw new PacketException(e);
		}
	}
}
