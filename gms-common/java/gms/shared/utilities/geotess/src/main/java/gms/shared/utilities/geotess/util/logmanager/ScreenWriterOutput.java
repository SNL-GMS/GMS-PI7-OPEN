package gms.shared.utilities.geotess.util.logmanager;

import gms.shared.utilities.geotess.util.globals.Globals;
import gms.shared.utilities.geotess.util.propertiesplus.PropertiesPlus;
import gms.shared.utilities.geotess.util.propertiesplus.PropertiesPlusException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

/**
 * Utility class to dump a string to the screen and/or a BufferedWriter
 * object and/or an internal StringBuffer. All 3  output destinations
 * can be turned on or off collectively or independently which makes this 
 * a convenient object for dumping standard output in cases where 
 * all, some, or none of the output mechanisms is desirable.
 * 
 * <p> In order to turn on output to the BufferedWriter a writer must
 * first be set with a call to setWriter(BufferedWriter bw). If the
 * BufferedWriter object has not been set the call to turn on output
 * to the BufferedWriter will throw an IOException.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class ScreenWriterOutput implements Serializable
{
	/**
	 * User assigned buffered writer into which all calls to write(String s)
	 * are written if the writer flag (aWriterOutput) is true.
	 */
	private BufferedWriter aWriter = null;

	/**
	 * Optional StringBuffer into which all calls to write(String s)
	 * are written if the buffer flag (aBufferOutput) is true.
	 */
	private StringBuffer aBuffer = null;

	/**
	 * Screen output flag.
	 */
	private boolean aScreenOutput = false;

	/**
	 * Writer output flag.
	 */
	private boolean aWriterOutput = false;

	/**
	 * Buffer output flag.
	 */
	private boolean aBufferOutput = false;
	
	/**
	 * User specified verbosity level.  Not used internally by 
	 * this class but can be accessed by calling applications
	 * using getter and setter.
	 */
	private int     verbosity     = Integer.MAX_VALUE;

	/**
	 * Indentation string appended to the front of all output
	 * (Defaults to "").
	 */
	private String  aIndent       = "";

	/**
	 * Default constructor.
	 */
	public ScreenWriterOutput()
	{
	}

	/**
	 * Construct a new ScreenWriterOutput object based on properties:
	 * <ol>
	 * <li>verbosity
	 * <li>log_file
	 * <li>print_to_screen
	 * </ol>
	 * @param prefix
	 * @param properties
	 * @throws Exception
	 */
	public ScreenWriterOutput(PropertiesPlus properties) throws Exception
	{ this("", properties); }

	/**
	 * Construct a new ScreenWriterOutput object based on properties:
	 * <ol>
	 * <li>prefix+"verbosity".  Default is Integer.MAX_VALUE
	 * <li>prefix+"log_file".  No default 
	 * <li>prefix+"print_to_screen".  Default is true
	 * </ol>
	 * @param prefix
	 * @param properties
	 * @throws Exception
	 */
	public ScreenWriterOutput(String prefix, PropertiesPlus properties) throws Exception
	{
		verbosity = properties.getInt(prefix+"verbosity", Integer.MAX_VALUE);

		File logfile = null;

		if (properties.containsKey(prefix+"log_file"))
			logfile = properties.getFile(prefix+"log_file");

		if (properties.getBoolean(prefix+"print_to_screen", true))
			setScreenOutputOn();

		if (logfile != null)
		{
			setWriter(new BufferedWriter(new FileWriter(logfile)));
			setWriterOutputOn();
		}

		// turn logger off and back on to ensure current status is stored.
		turnOff();
		restore();
	}
	
	/**
	 * Sets the user provided BufferedWriter to writer.
	 * 
	 * @param writer The user provided BufferedWriter object.
	 */
	public void setWriter(BufferedWriter writer)
	{
		aWriter = writer;
	}

	/**
	 * Get the buffered writer.
	 * 
	 * @return the aWriter
	 */
	public BufferedWriter getWriter()
	{
		return aWriter;
	}

	/**
	 * Sets the output mode to off ... No screen, BufferedWriter or StringBuffer output.
	 */
	public void setOutputOff()
	{
		aScreenOutput = false;
		aWriterOutput = false;
		aBufferOutput = false;
	}

	public void setScreenOutputOn()
	{
		aScreenOutput = true;
	}

	public void setScreenOutputOff()
	{
		aScreenOutput = false;
	}

	public void setWriterOutputOn() throws IOException
	{
		if (aWriter == null) throw new IOException();
		aWriterOutput = true;
	}

	public void setWriterOutputOff()
	{
		aWriterOutput = false;
	}

	public void setScreenAndWriterOutputOn() throws IOException
	{
		if (aWriter == null) throw new IOException();
		aScreenOutput = true;
		aWriterOutput = true;
	}

	/**
	 * Set buffer output on.  If the internal StringBuffer
	 * is null, it is instantiated.
	 */
	public void setBufferOutputOn()
	{
		if (aBuffer == null) 
			aBuffer = new StringBuffer();
		aBufferOutput = true;
	}

	public void setBufferOutputOff()
	{
		aBufferOutput = false;
	}
	
	public StringBuffer getStringBuffer()
	{
		return aBuffer;
	}

	/**
	 * Returns true if any output is on (screen or writer)
	 * @return true if any output is on (screen or writer)
	 */
	public boolean isOutputOn()
	{
		return (aScreenOutput || aWriterOutput || aBufferOutput);
	}

	/**
	 * Returns true if screen output is on.
	 * @return true if screen output is on.
	 */
	public boolean isScreenOutputOn()
	{
		return aScreenOutput;
	}

	/**
	 * Returns true if writer output is on.
	 * @return true if writer  output is on.
	 */
	public boolean isWriterOutputOn()
	{
		return aWriterOutput;
	}

	/**
	 * Returns true if buffer output is on.
	 * @return true if buffer  output is on.
	 */
	public boolean isBufferOutputOn()
	{
		return aBufferOutput;
	}

	/**
	 * Primary function that dumps formatted output 
	 * to all output destinations whose output flags are true.
	 *
	 * @param format the format specifier to use to print the items
	 * @param items the items that are to be printed..
	 */
	public void writef(String format, Object... items) 
	{ write(String.format(format, items)); }

	/**
	 * Primary function that dumps the input string s 
	 * to all output destinations whose output flags are true.
	 *
	 * @param s String to be written.
	 */
	public void write(String s) 
	{
		// write string to screen if on

	  s = aIndent + s;
		if (aScreenOutput) System.out.print(s);

		if (aBufferOutput) aBuffer.append(s);

		// write string to buffered writer if on

		if (aWriterOutput && (aWriter != null))
		{
			try
			{
				aWriter.write(s);
				aWriter.flush();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		} 
	}

	/**
	 * Primary function that dumps the input string s to the screen,
	 * if screen output is on, and / or to the buffered writer, if
	 * writer output is on.  Adds newline character(s) after the message.
	 *
	 * @param s String to be written.
	 */
	public void writeln(Object s) 
	{
		if (s instanceof Exception)
			write(((Exception)s));
		else
			write(aIndent + s.toString());
		write(Globals.NL);
	}

	/**
	 * Output BaseConst.NL
	 *
	 * @param s String to be written.
	 */
	public void writeln() { write(Globals.NL); }

	/**
	 * Output exception stack trace.
	 */
	public void write(Exception ex) 
	{
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("    %s%n", ex.getClass().getName()));
		if (ex.getMessage() != null)
			buf.append(String.format("    %s%n", ex.getMessage()));
		for (StackTraceElement trace : ex.getStackTrace())
			buf.append(String.format("        at %s%n", trace));
		write(buf.toString());
	}

	/**
	 * If turnOff() is called, these booleans store the 
	 * current state of the output flags so that they 
	 * can be restored when restore() is called.
	 */
	private boolean aScreenOutputStored, aWriterOutputStored, aBufferOutputStored;

	/**
	 * Turn off all output destinations temporarily.
	 * Current status can be restored later by calling restore()
	 */
	public void turnOff()
	{
		aScreenOutputStored = aScreenOutput;
		aWriterOutputStored = aWriterOutput;
		aBufferOutputStored = aBufferOutput;
		aScreenOutput = false;
		aWriterOutput = false;
		aBufferOutput = false;
	}

	/**
	 * Restore the status of all output destinations to 
	 * status in effect last time turnOff() was called.
	 */
	public void restore()
	{
		aScreenOutput = aScreenOutputStored;
		aWriterOutput = aWriterOutputStored;
		aBufferOutput = aBufferOutputStored;
	}

	/**
	 * @param verbosity the verbosity to set
	 */
	public void setVerbosity(int verbosity)
	{
		this.verbosity = verbosity;
	}

	/**
	 * @return the verbosity
	 */
	public int getVerbosity()
	{
		return verbosity;
	}

  /**
   * Sets the indentation string to the input value.
   * 
   * @param indent The new indentation setting.
   */
  public void setIndent(String indent)
  {
    aIndent = indent;
  }

  /**
   * Create a file base output writer for writing files
   *  
   * @param fn The file path/name of the output writer file
   * @param scrn If true screen output is turned ON, otherwise it is OFF.
   * @param file If true file output is turned ON, otherwise it is OFF.
   * 
   * @return The new ScreenWriterOutput object.
   * 
   * @throws IOException
   */
  public static ScreenWriterOutput createWriter(String fn, boolean scrn, boolean file) throws IOException
  {
  	ScreenWriterOutput sw = new ScreenWriterOutput();
	  File outFile = new File(fn);
	  BufferedWriter outFileWriter = null;
	  if (outFile.exists())
	      outFileWriter = new BufferedWriter(new FileWriter(outFile, true));
	  else
	      outFileWriter = new BufferedWriter(new FileWriter(outFile));
	  sw.setWriter(outFileWriter);
	  
	  if (scrn)
	    sw.setScreenOutputOn();
	  else
	    sw.setScreenOutputOff();

	  if (file)
	    sw.setWriterOutputOn();
	  else
      sw.setWriterOutputOff();

	  return sw;
  }
}
