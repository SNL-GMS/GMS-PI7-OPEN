package gms.shared.utilities.geotess.util.exceptions;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * @author Sandy Ballard
 * @version 2.0
 */
@SuppressWarnings("serial")
public class GMPException extends Exception {
    public GMPException() {
        super();
    }

    public GMPException(String string) {
        super(string);
    }

    public GMPException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public GMPException(Throwable throwable) {
        super(throwable);
    }
    
    static public String getStackTraceAsString(Exception ex)
    {
    	StringBuffer buf = new StringBuffer();
		// Recreate the stack trace into the error String.
		for (int i = 0; i < ex.getStackTrace().length; i++)
			buf.append(ex.getStackTrace()[i].toString()).append("\n");
    	return buf.toString();
    }
    
}
