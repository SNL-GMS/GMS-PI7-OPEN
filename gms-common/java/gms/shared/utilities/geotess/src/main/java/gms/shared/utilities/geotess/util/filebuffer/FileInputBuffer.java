package gms.shared.utilities.geotess.util.filebuffer;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Input file buffer object allows for easy buffered file input when large
 * amounts of binary data must be read efficiently from disk.
 * 
 * This class maintains a byte[] array that is used to contain data read
 * directly from a file based DataInputStream using byte[] array input.
 * A new buffer is read from disk every time the current internal buffer
 * has been completely parsed by high level read functions. This action
 * is automatic and does not require any user intervention. Buffering
 * the input data avoids many small reads from disk and speeds up overall
 * operation for large files.
 * 
 * This function extends the standard interface of the Java DataInput object
 * to include array reads. The following read functions are defined
 * 
 *    Single entity:
 *       byte    b = readByte();
 *       boolean b = readBoolean();
 *       short   s = readShort();
 *       int     i = readInt();
 *       long    l = readLong();
 *       float   f = readFloat();
 *       double  d = readDouble();
 *       String  s = readString();
 *
 *    Array entity:
 *       byte[]    b = readBytes();
 *       boolean[] b = readBooleans();
 *       short[]   s = readShorts();
 *       int[]     i = readInts();
 *       long[]    l = readLongs();
 *       float[]   f = readFloats();
 *       double[]  d = readDoubles();
 *       String[]  s = readStrings();
 * 
 *    The array entity functions assume that the equivalent array was
 *    written with the corresponding FileOutputBuffer command. If this is
 *    true then an array of the same size, and containing the same contents,
 *    will be returned from these functions.
 * 
 *    If the size of the written array was zero then the returned array from
 *    the input functions will be null.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class FileInputBuffer extends FileBuffer
{
  /**
   * Array of bytes that backs the ByteArrayInputStream aBAIS.
   */
  private byte[] aBytes = new byte [aBufSize];

  /**
   * ByteArrayInputStream that handles low-level input processing.
   */
  private ByteArrayInputStream aBAIS = new ByteArrayInputStream(aBytes);

  /**
   * DataInputStream that provides a high level interface for writing into
   * the low level ByteArrayInputStream (aBAIS).
   */
  private DataInputStream aBufDIS    = new DataInputStream(aBAIS);

  /**
   * Disk based DataInputStream that reads chucks of data (byte arrays) at a
   * time from the disk into the internal buffer.
   */
  DataInputStream aDIS               = null;

  /**
   * The current internal buffer read count.
   */
  private int     aReadCount         = 0;

  /**
   * The current internal buffer data size count.
   */
  private int     aBufCount          = 0;

  /**
   * Default constructor.
   * 
   * @param filenm The file name from which data will be read.
   * @throws FileNotFoundException
   */
  public FileInputBuffer(String filenm) throws FileNotFoundException
  {
    aFileName = filenm;
    aDIS      = new DataInputStream(new FileInputStream(filenm));
  }

  /**
   * Changes the size of the input buffer used to hold data read from disk.
   * This size should only be changed before reading begins. If an internal
   * buffer is read only part way before calling this function all remaining
   * data will be lost and program operation may be compromised.
   * 
   * @param sze The size of the new internal buffer.
   */
  @Override
  public void setByteBufferSize(int sze)
  {
    aBufSize = sze;
    aBytes   = new byte [aBufSize];
    aBAIS    = new ByteArrayInputStream(aBytes);
    aBufDIS  = new DataInputStream(aBAIS);
  }

  /**
   * Read a single byte from the input buffer.
   * 
   * @return A single byte from the input buffer.
   * @throws IOException
   */
  public byte readByte() throws IOException
  {
    // make sure the next byte is available and read it

    checkReadNextType(FileBuffer.BYTE_SIZE);
    return aBufDIS.readByte();
  }

  /**
   * Read in an array of bytes.
   * 
   * @return An array of bytes.
   * @throws IOException
   */
  public byte[] readBytes() throws IOException
  {
    // set array size and create array

    setBufferSize(FileBuffer.BYTE_SIZE);
    if (aArrayCount == 0) return null;
    byte[] a = new byte [aArrayCount];
    
    // read entire array through as many buffers as required

    while (readNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) a[i] = aBufDIS.readByte();
    }

    // return result

    return a;
  }

  /**
   * Read a single boolean from the input buffer.
   * 
   * @return A single boolean from the input buffer.
   * @throws IOException
   */
  public boolean readBoolean() throws IOException
  {
    // make sure the next boolean is available and read it

    checkReadNextType(FileBuffer.BOOLEAN_SIZE);
    return aBufDIS.readBoolean();
  }

  /**
   * Read in an array of booleans.
   * 
   * @return An array of booleans.
   * @throws IOException
   */
  public boolean[] readBooleans() throws IOException
  {
    // set array size and create array

    setBufferSize(FileBuffer.BOOLEAN_SIZE);
    if (aArrayCount == 0) return null;
    boolean[] a = new boolean [aArrayCount];
    
    // read entire array through as many buffers as required

    while (readNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) a[i] = aBufDIS.readBoolean();
    }

    // return result

    return a;
  }

  /**
   * Read a single short from the input buffer.
   * 
   * @return A single short from the input buffer.
   * @throws IOException
   */
  public short readShort() throws IOException
  {
    // make sure the next short is available and read it

    checkReadNextType(FileBuffer.SHORT_SIZE);
    return aBufDIS.readShort();
  }

  /**
   * Read in an array of shorts.
   * 
   * @return An array of shorts.
   * @throws IOException
   */
  public short[] readShorts() throws IOException
  {
    // set array size and create array

    setBufferSize(FileBuffer.SHORT_SIZE);
    if (aArrayCount == 0) return null;
    short[] a = new short [aArrayCount];
    
    // read entire array through as many buffers as required

    while (readNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) a[i] = aBufDIS.readShort();
    }

    // return result

    return a;
  }

  /**
   * Read a single int from the input buffer.
   * 
   * @return A single int from the input buffer.
   * @throws IOException
   */
  public int readInt() throws IOException
  {
    // make sure the next int is available and read it

    checkReadNextType(FileBuffer.INT_SIZE);
    return aBufDIS.readInt();
  }

  /**
   * Read in an array of ints.
   * 
   * @return An array of ints.
   * @throws IOException
   */
  public int[] readInts() throws IOException
  {
    // set array size and create array

    setBufferSize(FileBuffer.INT_SIZE);
    if (aArrayCount == 0) return null;
    int[] a = new int [aArrayCount];
    
    // read entire array through as many buffers as required

    while (readNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) a[i] = aBufDIS.readInt();
    }

    // return result

    return a;
  }

  /**
   * Read a single long from the input buffer.
   * 
   * @return A single long from the input buffer.
   * @throws IOException
   */
  public long readLong() throws IOException
  {
    // make sure the next long is available and read it

    checkReadNextType(FileBuffer.LONG_SIZE);
    return aBufDIS.readLong();
  }

  /**
   * Read in an array of longs.
   * 
   * @return An array of longs.
   * @throws IOException
   */
  public long[] readLongs() throws IOException
  {
    // set array size and create array

    setBufferSize(FileBuffer.LONG_SIZE);
    if (aArrayCount == 0) return null;
    long[] a = new long [aArrayCount];
    
    // read entire array through as many buffers as required

    while (readNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) a[i] = aBufDIS.readLong();
    }

    // return result

    return a;
  }

  /**
   * Read a single float from the input buffer.
   * 
   * @return A single float from the input buffer.
   * @throws IOException
   */
  public float readFloat() throws IOException
  {
    // make sure the next float is available and read it

    checkReadNextType(FileBuffer.FLOAT_SIZE);
    return aBufDIS.readFloat();
  }

  /**
   * Read in an array of floats.
   * 
   * @return An array of floats.
   * @throws IOException
   */
  public float[] readFloats() throws IOException
  {
    // set array size and create array

    setBufferSize(FileBuffer.FLOAT_SIZE);
    if (aArrayCount == 0) return null;
    float[] a = new float [aArrayCount];
    
    // read entire array through as many buffers as required

    while (readNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) a[i] = aBufDIS.readFloat();
    }

    // return result

    return a;
  }

  /**
   * Read a single double from the input buffer.
   * 
   * @return A single double from the input buffer.
   * @throws IOException
   */
  public double readDouble() throws IOException
  {
    // make sure the next double is available and read it

    checkReadNextType(FileBuffer.DOUBLE_SIZE);
    return aBufDIS.readDouble();
  }

  /**
   * Read in an array of doubles.
   * 
   * @return An array of doubles.
   * @throws IOException
   */
  public double[] readDoubles() throws IOException
  {
    // set array size and create array

    setBufferSize(FileBuffer.DOUBLE_SIZE);
    if (aArrayCount == 0) return null;
    double[] a = new double [aArrayCount];

    // read entire array through as many buffers as required

    while (readNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) a[i] = aBufDIS.readDouble();
    }

    // return result

    return a;
  }

  /**
   * Read a single string from the input buffer. Note: the string
   * is read as a byte array.
   * 
   * @return A single string from the input buffer.
   * @throws IOException
   */
  public String readString() throws IOException
  {
    // get current read position and check for a buffer read

    byte[] b = readBytes();
    if (b == null)
      return "";
    else
      return new String(b);
  }

  /**
   * Read in an array of strings.
   * 
   * @return An array of strings.
   * @throws IOException
   */
  public String[] readStrings() throws IOException
  {
    // get array size and create array

    aArrayCount = readInt();
    if (aArrayCount == 0) return null;
    String[] s = new String [aArrayCount];

    // read in all array strings

    for (int i = 0; i < s.length; ++i) s[i] = readString();

    // return array

    return s;
  }

  /**
   * Used for array reads to continually update the input buffer until the entire
   * array has been read.
   * 
   * @return True if a new buffer of data is available to be read else false.
   * @throws IOException
   */
  private boolean readNext() throws IOException
  {
    // exit if done

    if ((aIEnd > 0) && (aIEnd == aArrayCount)) return false;

    // if no bytes remain then readBufferX

    if (aReadCount == aBufCount) readBufferX();


    // set aIStrt and aIEnd and update the read count
    
    aIStrt = aIEnd;
    aIEnd += (aBufCount - aReadCount) / aTypSize;
    if (aIEnd > aArrayCount) aIEnd = aArrayCount;
    aReadCount += (aIEnd - aIStrt) * aTypSize;

    // done ... return true

    return true;
  }

  /**
   * Used by array reads to input the array size, set the type size, and
   * rest the read end count to 0.
   * 
   * @param typesze The array type size.
   * @throws IOException
   */
  private void setBufferSize(int typesze) throws IOException
  {
    aArrayCount = readInt(); 
    aTypSize = typesze;
    aIEnd = 0;
  }

  /**
   * Reads in another buffer full of data and initializes to continue read
   * processing.
   * 
   * @throws IOException
   */
  private void readBufferX() throws IOException
  {
    // read in the buffer size and reset the byte buffer stream

    aBufCount = aDIS.readInt();
    aBAIS.reset();

    // read in all bytes into the byte buffer backing array (abytes) and
    // reset the read count to 0

    aDIS.read(aBytes, 0, aBufCount);
    aReadCount = 0;
  }

  /**
   * Checks the read buffer to see if another buffer load is required.
   * 
   * @param typesze The type size of data to be read.
   * @throws IOException
   */
  private void checkReadNextType(int typesze) throws IOException
  {
    // read more data if required and update the read count

    if (aReadCount == aBufCount) readBufferX();
    aReadCount += typesze;
  }

  /**
   * Closes the file stream.
   */
  @Override
  public void close() throws IOException
  {
    aDIS.close();
  }
}
