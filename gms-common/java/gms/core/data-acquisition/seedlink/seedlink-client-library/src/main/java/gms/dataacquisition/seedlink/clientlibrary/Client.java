package gms.dataacquisition.seedlink.clientlibrary;

import gms.dataacquisition.seedlink.clientlibrary.Packet.PacketType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Pure-Java implementation of a SEEDLink clientlibrary based on the following:
 * <ul>
 * <li><a href=http://www.seiscomp3.org/doc/seattle/2013.015/apps/seedlink.html>Seiscomp3 Documentation</a></li>
 * <li><a href=http://ds.iris.edu/ds/nodes/dmc/services/seedlink>IRIS SEEDLink Documentation</a></li>
 * <li><a href=http://www.fdsn.org/seed_manual/SEEDManual_V2.4.pdf>SEED Manual v2.4</a></li>
 * </ul>
 * @author bjlawry
 */
public class Client implements AutoCloseable{
	/**
	 * A set of select string patterns that should return every seismic and
	 * infrasonic station available when using the SELECT command.
	 */
	public static final String[] SAPL_SELECTS = new String[]{"????Z.D",
			"????N.D","????E.D","????1.D","????2.D","???DF.D"};
	public static final String LOCAL_HOST = "127.0.0.1";
	public static final int DEFAULT_PORT = 18000;
	public static final String DATA_TIME_FORMAT = "yyyy,MM,dd,HH,mm,ss";
	private static final Charset CS = StandardCharsets.US_ASCII;
	private static final byte[] NL_BYTES = new byte[]{13,10};
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private List<Consumer<Packet>> pHandlers;
	private List<Consumer<Client>> dHandlers;

	public Client(String host, int port) throws IOException{
		pHandlers = new LinkedList<>();
		dHandlers = new LinkedList<>();
		socket = new Socket(host,port);
		out = socket.getOutputStream();
		in = socket.getInputStream();
		out.flush();
	}
	
	public Client() throws IOException{ this(LOCAL_HOST,DEFAULT_PORT); }

	public String getLocalAddress() {
		return this.socket.getLocalAddress().getHostAddress();
	}
	
	private synchronized void sendCommand(String command, Object ... os)
			throws IOException{
		if(command == null) throw new NullPointerException("null command");
		StringBuilder sb = new StringBuilder(command);
		for(Object o : os) sb.append(" ").append(o.toString());
		System.out.println("sendCommand: " + sb.toString());
		out.write(sb.toString().getBytes(CS));
		out.write(NL_BYTES);
		out.flush();
	}
	
	private synchronized String receiveLine() throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[]{-1,-1};
		
		do{
			b[0] = b[1];
			b[1] = (byte)in.read();
			baos.write(b[1]);
		} while(!Arrays.equals(b,NL_BYTES));
		
		byte[] read = baos.toByteArray();
		return new String(Arrays.copyOf(read,read.length-2),CS);
	}
	
	private void receiveData() throws IOException{
		Packet p = null;
		Packet prev = null;
		
		synchronized(this){
			do{
				try {
					prev = p;
					p = Packet.read(in);
				} catch (PacketException e) {
					e.printStackTrace();
					System.err.println("previous packet ("+(prev != null ? 
							prev.getOriginalStreamBytes().length+" bytes) " :
								"null)")+":\n"+(prev == null ? "null" :
									new String(prev.getOriginalStreamBytes(),
											StandardCharsets.US_ASCII)));
					break;
				}

				if(p != null) handlePacket(p);
			} while(p != null && !socket.isClosed());
		}
		System.out.println("disconnecting");
		handleDisconnect();
	}
	
	protected synchronized void handlePacket(Packet p){
		for(Consumer<Packet> h : pHandlers) h.accept(p);
	}
	
	protected void handleDisconnect(){
		synchronized(dHandlers){
			for(Consumer<Client> h : dHandlers) h.accept(this);
		}
	}
	
	public synchronized void addDataPacketHandler(Consumer<Packet> c){
		pHandlers.add(c);
	}
	
	public void addDisconnectHandler(Consumer<Client> c){
		synchronized(dHandlers){ dHandlers.add(c); }
	}
	
	public synchronized void removeDataPacketHandler(Consumer<Packet> c){
		pHandlers.remove(c);
	}
	
	public void removeDisconnectHandler(Consumer<Client> c){
		synchronized(dHandlers){ dHandlers.remove(c); }
	}
	
	public List<Consumer<Packet>> getDataPacketHandlers(){
		List<Consumer<Packet>> list = new ArrayList<>();
		synchronized(this){ list.addAll(pHandlers); }
		return list;
	}
	
	/**
	 * Issues the "HELLO" command to the server and returns the two lines of
	 * text it is expected to return in the form of a String[2] array.
	 * @return
	 * @throws IOException
	 */
	public synchronized String[] sayHello() throws IOException{
		sendCommand("HELLO");
		return new String[]{receiveLine(),receiveLine()};
	}
	
	/**
	 * Returns the 
	 * @param level
	 * @return
	 * @throws IOException
	 */
	public InputStream infoStream(InfoLevel level) throws IOException{
		//Caches up to 1MB before blocking:
		PipedInputStream is = new PipedInputStream(1024*1024);
		PipedOutputStream os = new PipedOutputStream(is);
		
		new Thread(() -> {
			synchronized(Client.this){
				try{
					sendCommand("INFO",level.name());
					Packet p;
					do{
						os.write((p = Packet.read(in)).getData());
					} while(p != null && p.getPacketType() != 
							PacketType.INFO_TERM);
				} catch (Exception x){
					x.printStackTrace();
				} finally {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		return is;
	}
	
	public String infoString(InfoLevel level) throws IOException{
		InputStream is = infoStream(level);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int r;
		while((r = is.read()) != -1) baos.write(r);
		String xml = new String(baos.toByteArray(),StandardCharsets.US_ASCII);
		
		try{
			return Utils.formatXml(xml,2);
		} catch (Exception e){
			return xml;
		}
	}
	
	/**
	 * Performs the "SELECT" command using the specified stream pattern.
	 * Patterns supplied must be in the form of "LLCCC.T", where "LL" is the
	 * location code, "CCC" is the channel type, and "T" is the type (one of
	 * [D,E,C,O,T,L], meaning "data", "event", "calibration", "blockette",
	 * "timing", and "log", respectively).
	 * @param patterns
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean select(String ... patterns) throws IOException{
		boolean b = true;
		for(String s : patterns){
			b &= sendModifierCommand("SELECT", s);
		}
		return b;
	}

	public synchronized boolean sendModifierCommand(String command) throws IOException {
		sendCommand(command);
		return receiveLine().equalsIgnoreCase("OK");
	}

	public synchronized boolean sendModifierCommand(String command, Object... args) throws IOException {
		sendCommand(command, args);
		return receiveLine().equalsIgnoreCase("OK");
	}
	
	/**
	 * Performs the "STATION" command.
	 * @param stationCode
	 * @param networkCode
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean station(String stationCode, String networkCode)
	throws IOException{
		return sendModifierCommand("STATION", stationCode, networkCode);
	}
	
	public synchronized boolean station(String stationCode) throws IOException{
		return sendModifierCommand("STATION",stationCode);
	}
	
	/**
	 * Signals the end of handshaking in multi-station mode. This is an action
	 * command because it starts data transfer. No explicit response is
	 * returned.
	 * @throws IOException
	 */
	public synchronized void end() throws IOException{
		sendCommand("END");
		//System.out.println("END command sent, waiting for incoming data ...");
		receiveData();
	}
	
	public synchronized void bye() throws IOException{ sendCommand("BYE"); }
	
	private Object[] filterNullArgs(String ... args){
		List<String> l = new LinkedList<>();
		for(String a : args) if(a != null) l.add(a);
		return l.toArray(new Object[0]);
	}

	public synchronized boolean data(String seqNum, String beginTime) throws IOException {
		return sendModifierCommand("DATA",filterNullArgs(seqNum,beginTime));
	}
	
	public boolean data(String seqNum) throws IOException {
	  return data(seqNum,null);
	}
	
	public boolean data() throws IOException {
	  return data(null);
	}
	
	/**
	 * Wrapper for the "INFO STATIONS" command that returns a List of
	 * {@link StationInfo} objects.
	 * @return list of station info objects without stream objects
	 * @throws Exception
	 */
	public List<StationInfo> requestStationInfo() throws Exception{
		List<StationInfo> list = new LinkedList<>();
		
		Document d = Utils.parseXml(infoString(InfoLevel.STATIONS));
		Element sl = d.getDocumentElement();
		NodeList nl = sl.getElementsByTagName("station");
		for(int i = 0; i < nl.getLength(); i++){
			Node n = nl.item(i);
			NamedNodeMap map = n.getAttributes();
			
			list.add(new StationInfo(
					map.getNamedItem("name").getNodeValue(),
					map.getNamedItem("network").getNodeValue(),
					map.getNamedItem("description").getNodeValue(),
					Integer.valueOf(map.getNamedItem("begin_seq")
							.getNodeValue(),16),
					Integer.valueOf(map.getNamedItem("end_seq")
							.getNodeValue(),16),
					Collections.emptyList()));
		}
		
		return list;
	}
	
	/**
	 * Wrapper for the "INFO STREAMS" command that returns a list of
	 * StationInfo objects, populated with StreamInfo objects.
	 * @return list of station info objects with associated stream objects
	 * @throws Exception
	 */
	public List<StationInfo> requestStreamInfo() throws Exception{
		List<StationInfo> list = new LinkedList<>();
		
		Document d = Utils.parseXml(infoString(InfoLevel.STREAMS));
		Element sl = d.getDocumentElement();
		NodeList nl = sl.getElementsByTagName("station");
		DateFormat df = new SimpleDateFormat(StreamInfo.TIME_FORMAT);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		for(int i = 0; i < nl.getLength(); i++){
			Node sta = nl.item(i);
			NamedNodeMap staMap = sta.getAttributes();
			List<StreamInfo> streams = new LinkedList<>();
			
			NodeList children = sta.getChildNodes();
			for(int j = 0; j < children.getLength(); j++){
				Node c = children.item(j);
				if("stream".equals(c.getNodeName())){
					NamedNodeMap strMap = c.getAttributes();
					streams.add(new StreamInfo(
							strMap.getNamedItem("seedname").getNodeValue(),
							strMap.getNamedItem("location").getNodeValue(),
							strMap.getNamedItem("type").getNodeValue(),
							df.parse(strMap.getNamedItem("begin_time")
									.getNodeValue()).getTime(),
							df.parse(strMap.getNamedItem("end_time")
									.getNodeValue()).getTime()
							));
				}
			}
			
			list.add(new StationInfo(
					staMap.getNamedItem("name").getNodeValue(),
					staMap.getNamedItem("network").getNodeValue(),
					staMap.getNamedItem("description").getNodeValue(),
					Integer.valueOf(staMap.getNamedItem("begin_seq")
							.getNodeValue(),16),
					Integer.valueOf(staMap.getNamedItem("end_seq")
							.getNodeValue(),16),
					streams));
		}
		
		return list;
	}
	
	@Override
	public void close() throws IOException { socket.close(); }
	
	public enum InfoLevel{
		ID, CAPABILITIES, STATIONS, STREAMS, GAPS, CONNECTIONS, ALL;
	}

}
