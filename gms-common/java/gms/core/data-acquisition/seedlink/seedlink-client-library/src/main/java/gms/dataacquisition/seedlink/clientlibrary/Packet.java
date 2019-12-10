package gms.dataacquisition.seedlink.clientlibrary;

import static gms.dataacquisition.seedlink.clientlibrary.Utils.fillBuffer;

import gms.dataacquisition.seedlink.clientlibrary.Blockette.Type;
import gms.dataacquisition.seedlink.clientlibrary.blockettes.DataOnly;
import gms.dataacquisition.seedlink.clientlibrary.control.DataHeader;
import gms.dataacquisition.seedlink.clientlibrary.data.Format;
import gms.dataacquisition.seedlink.clientlibrary.data.Formats;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.Steim1;
import gms.dataacquisition.seedlink.clientlibrary.data.formats.Steim2;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Packet {
	public static final int PACKET_LENGTH = 520;
	public static final String PREFIX = "SL";
	public static final String SEQ_FMT = "000000";
	private String packetSeqNum = null;
	private PacketType packetType = null;
	private DataHeader dataHeader = null;
	private Map<Blockette.Type,Blockette> blockettes = new LinkedHashMap<>();
	private Format dataFormat;
	private int dataFormatCode;
	private byte[] data, raw;
	
	private Packet(){}
	
	public String getSeqNum(){ return packetSeqNum; }
	
	public PacketType getPacketType(){ return packetType; }
	
	public DataHeader getDataHeader(){ return dataHeader; }
	
	public Map<Blockette.Type,Blockette> getBlockettes(){ return blockettes; }
	
	/**
	 * @return the actual data payload of this packet as a byte[] (this does
	 * not include any header information - if you need the metadata bytes, use
	 * <code>getOriginalStreamBytes()</code> instead).
	 */
	public byte[] getData(){ return data; }
	
	public Format getFormat(){ return dataFormat; }
	
	public int getFormatCode(){ return dataFormatCode; }
	
	public String getFormatName(){
		Format f = Formats.getFormat(getFormatCode());
		return f != null ? f.name() : "UNKNOWN(code="+getFormatCode()+")";
	}
	
	public byte[] getOriginalStreamBytes(){ return raw; }
	
	public int getSampleCount(){ return dataHeader.getNumSamp(); }
	
	/**
	 * Checks the value returned by DataOnly.getWordOrder(), but also does some
	 * crazy things for special cases - e.g. the ones outlined here:
	 * <p>
	 * <a href="http://seiscode.iris.washington.edu/svn/sacdump/libmseed/README.byteorder">
	 * Steim1/2 ByteOrder Kludge
	 * </a>
	 * @return
	 */
	public boolean isDataBigEndian(){
		Blockette b = blockettes.get(Type.DATA_ONLY);
		if(b == null || !(b instanceof DataOnly)) return true;
		
		Format f = getFormat();
		if(f instanceof Steim1 || f instanceof Steim2){
			int nowYear = ZonedDateTime.now().getYear();
			ZonedDateTime t = ZonedDateTime.ofInstant(
					Instant.ofEpochMilli(getDataHeader().getStartTimeMillis()),
					ZoneId.of("UTC"));
			if(nowYear+5 < t.getYear() || nowYear-95 > t.getYear()) 
				return false;
		}
		
		return ((DataOnly)b).isBigEndian();
	}
	
	/**
	 * Attempts to decode the samples represented by this packet as Numbers.
	 * @return the samples contained within this packet
	 * @throws Exception thrown if an error occurs during decoding (can be
	 * caused by corrupt or garbled data)
	 */
	public Number[] getSamples() throws Exception{
		/*if(dataFormat == null){
			//throw new SampleFormatNotSupportedException(getDataHeader().)
		}*/
		return dataFormat.samples(getData(), getSampleCount(),
				isDataBigEndian());
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("packet [\n");
		sb.append("  seqNum : ").append(packetSeqNum).append("\n")
				.append("  type : ").append(packetType).append("\n")
				.append("  header : ").append(dataHeader).append("\n")
				.append("  blockettes (").append(blockettes.size())
				.append("): [\n");
		for(Blockette b : blockettes.values())
			sb.append("    ").append(b).append("\n");
		sb.append("  ]\n]");
		return sb.toString();
	}
	
	private static String readPrefix(InputStream is)
			throws IOException, PacketException{
		byte[] pre = new byte[]{(byte)is.read(),(byte)is.read()};
		return new String(pre,StandardCharsets.US_ASCII);
	}
	
	private static String readPacketSeqNum(InputStream is) throws IOException{
		byte[] seq = new byte[SEQ_FMT.length()];
		Utils.fillBuffer(seq, is);
		return new String(seq,StandardCharsets.US_ASCII);
	}
	
	private static String toAsciiCodes(String input){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < input.length(); i++){
			if(i > 0) sb.append(' ');
			sb.append((int)input.charAt(i));
		}
		return sb.toString();
	}
	
	private static int bytesRemaining(CachedInputStream cis, DataOnly d){
		return (PREFIX.length()+SEQ_FMT.length()+(int)Math.pow(
				2,d.getDataLength()))-cis.getCachedBytesCount();
	}
	
	private static Packet read(InputStream is, boolean isRecoveryAttempt)
	throws IOException, PacketException{
		CachedInputStream cis = new CachedInputStream(is);
		DataInputStream dis = new DataInputStream(cis);
		
		//Check packet prefix:
		String pre = readPrefix(dis);
		if(pre.startsWith(new String(new byte[]{-1},StandardCharsets.US_ASCII)))
			return null;
		
		if(!PREFIX.equals(pre)) throw new PacketException(
				"Packet must begin with \""+PREFIX+"\" but found \""+pre+
				"\" (ascii: ["+toAsciiCodes(pre)+"])");
		
		//Read packet sequence number:
		Packet p = new Packet();
		p.packetSeqNum = readPacketSeqNum(dis);
		
		//Parse type:
		if(p.packetSeqNum.startsWith("INFO ")){
			if(p.packetSeqNum.endsWith("*")) p.packetType = PacketType.INFO;
			else p.packetType = PacketType.INFO_TERM;
		}
		else p.packetType = PacketType.DATA;
		
		//Parse header:
		p.dataHeader = DataHeader.read(dis);
		
		//Parse Blockettes, if any:
		if(p.dataHeader.getNumBlockettes() > 0){
			Blockette b = null;
			do{
				try{
					b = Blockette.Type.valueOf(dis.readShort()).create();
					b.read(dis);
					p.blockettes.put(b.getType(),b);
				} catch (PacketException e){
					if(isRecoveryAttempt){
						System.err.println("recovery failed.");
						throw e;
					}
					
					int toDiscard = PACKET_LENGTH - cis.getCachedBytesCount();
					
					System.err.println("failed to parse packet, bytes read "+
							"("+cis.getCachedBytesCount()+")\n");
					System.err.println(new String(cis.getCachedData(),
							StandardCharsets.US_ASCII)+"\n");
					System.err.println("discarding "+toDiscard+" bytes ...");
					
					for(int i = 0; i < toDiscard; i++) cis.read();
					
					System.err.println("attempting to recover stream ...");
					p = read(is,true);
					System.err.println("stream recovered successfully.");
					
					return p;
				}
			} while(b != null && b.getNextBlocketteOffset() > 0);
		}
		
		//Read data, if any:
		if(p.dataHeader.getNumSamp() > 0){
			if(p.blockettes.containsKey(Type.DATA_ONLY)){
				DataOnly d = (DataOnly)
						p.blockettes.get(Type.DATA_ONLY);
				p.dataFormat = d.getFormat();
				p.dataFormatCode = d.getFormatCode();
				p.data = new byte[bytesRemaining(cis,d)];
				fillBuffer(p.data,0,p.data.length,dis);
			}
		}
		
		//Sometimes, packets will contain fewer samples than can fill the
		//520 byte packet length:
		while(cis.getCachedBytesCount() < PACKET_LENGTH) cis.read();
		
		//Set the raw stream bytes in the Packet object before returning:
		p.raw = cis.getCachedData();
		return p;
	}
	
	public static Packet read(InputStream is)
	throws IOException, PacketException{
		return read(is,false);
	}
	
	public static Packet readAscii(String s) throws PacketException{
		try {
			return read(new ByteArrayInputStream(s.getBytes(
					StandardCharsets.US_ASCII)));
		} catch (IOException e) {
			e.printStackTrace();
			//this will never happen:
			throw new PacketException(e);
		}
	}
	
	public static Stream<Packet> stream(InputStream is){
		Packet init = null;
		try{ init = Packet.read(is); } catch (Exception e){};
		
		final Packet first = init;
		Iterator<Packet> i = new Iterator<Packet>(){
			Packet p = first;
			
			@Override
			public boolean hasNext(){ return p != null; }

			@Override
			public Packet next() {
				if(p == null) throw new NoSuchElementException();
				Packet r = p;
				try{ p = Packet.read(is); } catch (Exception e){ p = null; }
				return r;
			}
		};
		
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(i,
				Spliterator.ORDERED),false);
	}
	
	public static enum PacketType{
		DATA, INFO, INFO_TERM;
	}
	
	public static class SampleFormatNotSupportedException
	extends PacketException{
		private static final long serialVersionUID = 1L;
		
		public SampleFormatNotSupportedException(int code){
			super("sample format \""+code+"\" is not supported at this time");
		}
	}
}
