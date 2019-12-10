package gms.dataacquisition.seedlink.clientlibrary;

public class PacketException extends Exception{
	private static final long serialVersionUID = 1L;

	public PacketException(Throwable cause){ super(cause); }
	
	public PacketException(String message, Throwable cause){
		super(message,cause);
	}
	
	public PacketException(String message){ super(message); }
	
	public PacketException(){ super(); }
}
