package gms.shared.utilities.geotess.util.filebuffer;

import java.io.IOException;
import java.io.Serializable;

/**
 * Abstract Base class file buffer object allows for easy buffered file input
 * and output. This object, and its two concrete derived classes
 * (FileInputBuffer and FileOutputBuffer) are used when large amounts of binary
 * data must be written and read efficiently to and from disk.
 * 
 * The derived classes maintain a byte[] array that is used to read and write
 * data into and out of using a standard DataInputStream and DataOutputStream
 * Java object. In input mode the buffer is filled directly from disk using a
 * single read where the byte count is fixed to some number less than or equal
 * to all file bytes. In output mode the buffer is written directly to disk and
 * then refilled to be written again if necessary.
 *
 * See the derived concrete classes for an interface description
 *     
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public abstract class FileBuffer implements Serializable
{
  /**
   * Public static constants giving the byte size of most intrinsincs.
   */
  public static int DOUBLE_SIZE  = Double.SIZE / 8;
  public static int FLOAT_SIZE   = Float.SIZE / 8;
  public static int LONG_SIZE    = Long.SIZE / 8;
  public static int INT_SIZE     = Integer.SIZE / 8;
  public static int SHORT_SIZE   = Short.SIZE / 8;
  public static int BYTE_SIZE    = Byte.SIZE / 8;
  public static int BOOLEAN_SIZE = 1;

  /**
   * The size of the internal buffer to use for reading and writing large
   * amounts of data in installements no large than this value.
   */
  protected int aBufSize     = 8000000;

  /**
   * Used by array read and write functions to track the size of the array
   * to be read or written from/to disk.
   */
  protected int aArrayCount  = 0;

  /**
   * Used by array read and writes to define the array start index for
   * processing the currently loaded buffer.
   */
  protected int aIStrt       = 0;

  /**
   * Used by array read and writes to define the array end index for
   * processing the currently loaded buffer.
   */
  protected int aIEnd        = 0;

  /**
   * The byte size of the type to be read or written to the currently loaded
   * buffer.
   */
  protected int aTypSize     = 0;

  /**
   * The file name to be read or written.
   */
  protected String aFileName = "";

  /**
   * Returns the array start index for the current buffer.
   * 
   * @return The array start index.
   */
  public int getStart()
  {
    return aIStrt;
  }

  /**
   * Returns the array end index for the current buffer.
   * 
   * @return The array end index.
   */
  public int getEnd()
  {
    return aIEnd;
  }

  /**
   * The file name to be read from or written too.
   * 
   * @return The file name to be read from or written too.
   */
  public String getFileName()
  {
    return aFileName;
  }

  /**
   * Abstract function that sets the buffer size to the input value.
   * 
   * @param sze The new buffer size setting.
   */
  public abstract void setByteBufferSize(int sze);

  /**
   * Abstract function that closes the input or output file buffer.
   * 
   * @throws IOException
   */
  public abstract void close() throws IOException;
}
