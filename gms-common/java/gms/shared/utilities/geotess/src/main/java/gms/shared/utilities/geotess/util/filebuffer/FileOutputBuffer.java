package gms.shared.utilities.geotess.util.filebuffer;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**

/**
 * Output (write only) file buffer object allows for easy buffered file output.
 * This object is used when large amounts of binary data must be written
 * efficiently to disk.
 * 
 * This class maintains a ByteArrayOutputStream that is used to contain data
 * from a standard DataOutputStream Java object. These two objects comprise the
 * in-core temporary storage for all data written into the FileOutputBuffer.
 * The class also maintains a out-of-core disk-based DataOutputStream opened
 * for a user defined file name into which all data in the ByteArrayOutputStream
 * is written after the byte array becomes full. Every time the internal buffer
 * is filled it is automatically written to disk. This avoids many small writes
 * to disk and speeds up overall operation for large files.
 * 
 * This function extends the standard interface of the Java DataOutput object
 * to include array writes. The following write functions are defined
 * 
 *    Single entity:
 *       writeByte(byte b);
 *       writeBoolean(boolean b);
 *       writeShort(short s);
 *       writeInt(int i);
 *       writeLong(long l);
 *       writeFloat(float f);
 *       writeDouble(double d);
 *       writeString(String s);
 *
 *    Array entity:
 *       writeBytes(byte[] b);
 *       writeBooleans(boolean[] b);
 *       writeShorts(short[] s);
 *       writeInts(int[] i);
 *       writeLongs(long[] l);
 *       writeFloats(float[] f);
 *       writeDoubles(double[] d);
 *       writeStrings(String[] s);
 *       writeBytes(byte[] b, int offst, int len);
 *       writeBooleans(boolean[] b, int offst, int len);
 *       writeShorts(short[] s, int offst, int len);
 *       writeInts(int[] i, int offst, int len);
 *       writeLongs(long[] l, int offst, int len);
 *       writeFloats(float[] f, int offst, int len);
 *       writeDoubles(double[] d, int offst, int len);
 *       writeStrings(String[] s, int offst, int len);
 * 
 *    Note that if an array is written of zero size that the corresponding
 *    input function defined in FileInputBuffer will return a null array.
 *
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class FileOutputBuffer extends FileBuffer
{
  /**
   * The in-core byte array of size aBuffSize used to contain all written
   * information before it is written in its entirety to the disk based
   * stream.
   */
  private ByteArrayOutputStream aBAOS = new ByteArrayOutputStream(aBufSize);

  /**
   * The in-core file stream through which all information is written in a
   * standard formatted fashion into the in-core byte buffer.
   */
  private DataOutputStream aBufDOS    = new DataOutputStream(aBAOS);

  /**
   * The output disk-based file stream assigned at construction.
   */
  DataOutputStream aDOS               = null;

  /**
   * Standard constructor. Creates an internal in-core data output stream and an
   * out-of-core disk based data output stream opened with the input file name.
   * The FileOutputBuffer is ready for use immediately following construction.
   * 
   * @param filenm The name of the file into which all data from this object will
   *               be written.
   * @throws FileNotFoundException
   */
  public FileOutputBuffer(String filenm) throws FileNotFoundException
  {
    aFileName = filenm;
    aDOS = new DataOutputStream(new FileOutputStream(filenm));
  }

  /**
   * Changes the size of the output buffer used to hold data before it written
   * to disk. This size should only be changed before writing begins. If an
   * internal buffer is written partially full with data before calling this
   * function all contained data will be lost and program operation may be
   * compromised.
   * 
   * @param sze The size of the new internal buffer.
   */
  @Override
  public void setByteBufferSize(int sze)
  {
    aBufSize = sze;
    aBAOS    = new ByteArrayOutputStream(aBufSize);
    aBufDOS  = new DataOutputStream(aBAOS);
  }

  /**
   * Writes the input byte to the internal buffer.
   * 
   * @param b The input byte to be written to the internal buffer.
   * @throws IOException
   */
  public void writeByte(byte b) throws IOException
  {
    // check to see if the buffer is full and write to disk if necessary
    // write the byte to the buffer

    checkWriteNextType(FileBuffer.BYTE_SIZE);
    aBufDOS.writeByte(b);
  }

  /**
   * Writes an array of bytes to disk.
   * 
   * @param b The array of bytes to be written to disk.
   * @throws IOException
   */
  public void writeBytes(byte[] b) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(b.length, FileBuffer.BYTE_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeByte(b[i]);
    }
  }

  /**
   * Writes an array of bytes to disk.
   * 
   * @param b The array of bytes to be written to disk.
   * @param offst The start offset in the array.
   * @param len The number of entries to write.
   * @throws IOException
   */
  public void writeBytes(byte[] b, int offst, int len) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(len - offst, FileBuffer.BYTE_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeByte(b[i+offst]);
    }
  }

  /**
   * Writes the input boolean to the internal buffer.
   * 
   * @param b The input boolean to be written to the internal buffer.
   * @throws IOException
   */
  public void writeBoolean(boolean b) throws IOException
  {
    // check to see if the buffer is full and write to disk if necessary
    // write the boolean to the buffer

    checkWriteNextType(FileBuffer.BOOLEAN_SIZE);
    aBufDOS.writeBoolean(b);
  }

  /**
   * Writes an array of booleans to disk.
   * 
   * @param b The array of booleans to be written to disk.
   * @throws IOException
   */
  public void writeBooleans(boolean[] b) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(b.length, FileBuffer.BOOLEAN_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeBoolean(b[i]);
    }
  }

  /**
   * Writes an array of booleans to disk.
   * 
   * @param b The array of booleans to be written to disk.
   * @param offst The start offset in the array.
   * @param len The number of entries to write.
   * @throws IOException
   */
  public void writeBooleans(boolean[] b, int offst, int len) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(len - offst, FileBuffer.BOOLEAN_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeBoolean(b[i+offst]);
    }
  }

  /**
   * Writes the input short to the internal buffer.
   * 
   * @param s The input short to be written to the internal buffer.
   * @throws IOException
   */
  public void writeShort(short s) throws IOException
  {
    // check to see if the buffer is full and write to disk if necessary
    // write the short to the buffer

    checkWriteNextType(FileBuffer.SHORT_SIZE);
    aBufDOS.writeInt(s);
  }

  /**
   * Writes an array of shorts to disk.
   * 
   * @param s The array of shorts to be written to disk.
   * @throws IOException
   */
  public void writeShorts(short[] s) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(s.length, FileBuffer.SHORT_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeShort(s[i]);
    }
  }

  /**
   * Writes an array of shorts to disk.
   * 
   * @param s The array of shorts to be written to disk.
   * @param offst The start offset in the array.
   * @param len The number of entries to write.
   * @throws IOException
   */
  public void writeShorts(short[] s, int offst, int len) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(len - offst, FileBuffer.SHORT_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeShort(s[i+offst]);
    }
  }

  /**
   * Writes the input int to the internal buffer.
   * 
   * @param i The input int to be written to the internal buffer.
   * @throws IOException
   */
  public void writeInt(int i) throws IOException
  {
    // check to see if the buffer is full and write to disk if necessary
    // write the int to the buffer

    checkWriteNextType(FileBuffer.INT_SIZE);
    aBufDOS.writeInt(i);
  }

  /**
   * Writes an array of ints to disk.
   * 
   * @param i The array of ints to be written to disk.
   * @throws IOException
   */
  public void writeInts(int[] i) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(i.length, FileBuffer.INT_SIZE);
    while (writeNext())
    {
      for (int j = aIStrt; j < aIEnd ; ++j) aBufDOS.writeInt(i[j]);
    }
  }

  /**
   * Writes an array of ints to disk.
   * 
   * @param i The array of ints to be written to disk.
   * @param offst The start offset in the array.
   * @param len The number of entries to write.
   * @throws IOException
   */
  public void writeInts(int[] i, int offst, int len) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(len - offst, FileBuffer.INT_SIZE);
    while (writeNext())
    {
      for (int j = aIStrt; j < aIEnd ; ++j) aBufDOS.writeInt(i[j+offst]);
    }
  }

  /**
   * Writes the input long to the internal buffer.
   * 
   * @param l The input long to be written to the internal buffer.
   * @throws IOException
   */
  public void writeLong(long l) throws IOException
  {
    // check to see if the buffer is full and write to disk if necessary
    // write the long to the buffer

    checkWriteNextType(FileBuffer.LONG_SIZE);
    aBufDOS.writeLong(l);
  }

  /**
   * Writes an array of longs to disk.
   * 
   * @param l The array of longs to be written to disk.
   * @throws IOException
   */
  public void writeLongs(long[] l) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(l.length, FileBuffer.LONG_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeLong(l[i]);
    }
  }

  /**
   * Writes an array of longs to disk.
   * 
   * @param l The array of longs to be written to disk.
   * @param offst The start offset in the array.
   * @param len The number of entries to write.
   * @throws IOException
   */
  public void writeLongs(long[] l, int offst, int len) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(len - offst, FileBuffer.LONG_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeLong(l[i+offst]);
    }
  }

  /**
   * Writes the input float to the internal buffer.
   * 
   * @param f The input float to be written to the internal buffer.
   * @throws IOException
   */
  public void writeFloat(float f) throws IOException
  {
    // check to see if the buffer is full and write to disk if necessary
    // write the float to the buffer

    checkWriteNextType(FileBuffer.FLOAT_SIZE);
    aBufDOS.writeFloat(f);
  }

  /**
   * Writes an array of floats to disk.
   * 
   * @param f The array of floats to be written to disk.
   * @throws IOException
   */
  public void writeFloats(float[] f) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(f.length, FileBuffer.FLOAT_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeFloat(f[i]);
    }
  }

  /**
   * Writes an array of floats to disk.
   * 
   * @param f The array of floats to be written to disk.
   * @param offst The start offset in the array.
   * @param len The number of entries to write.
   * @throws IOException
   */
  public void writeFloats(float[] f, int offst, int len) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(len - offst, FileBuffer.FLOAT_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeFloat(f[i+offst]);
    }
  }

  /**
   * Writes the input double to the internal buffer.
   * 
   * @param d The input double to be written to the internal buffer.
   * @throws IOException
   */
  public void writeDouble(double d) throws IOException
  {
    // check to see if the buffer is full and write to disk if necessary
    // write the double to the buffer

    checkWriteNextType(FileBuffer.DOUBLE_SIZE);
    aBufDOS.writeDouble(d);
  }

  /**
   * Writes an array of doubles to disk.
   * 
   * @param d The array of doubles to be written to disk.
   * @throws IOException
   */
  public void writeDoubles(double[] d) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(d.length, FileBuffer.DOUBLE_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeDouble(d[i]);
    }
  }

  /**
   * Writes an array of doubles to disk.
   * 
   * @param d The array of doubles to be written to disk.
   * @param offst The start offset in the array.
   * @param len The number of entries to write.
   * @throws IOException
   */
  public void writeDoubles(double[] d, int offst, int len) throws IOException
  {
    // set the array and type sizes and write the array to disk

    setBufferSize(len - offst, FileBuffer.DOUBLE_SIZE);
    while (writeNext())
    {
      for (int i = aIStrt; i < aIEnd ; ++i) aBufDOS.writeDouble(d[i+offst]);
    }
  }

  /**
   * Writes the input string to the internal buffer. Note: the string
   * is written as a byte array.
   * 
   * @param s The input string to be written to the internal buffer.
   * @throws IOException
   */
  public void writeString(String s) throws IOException
  {
    // convert the string to a byte array and write it

    writeBytes(s.getBytes());
  }

  /**
   * Writes an array of strings to disk.
   * 
   * @param s The array of strings to be written to disk.
   * @throws IOException
   */
  public void writeStrings(String[] s) throws IOException
  {
    // write array size and array to disk

    writeInt(s.length);
    for (int i = 0; i < s.length; ++i) writeString(s[i]);
  }

  /**
   * Writes an array of strings to disk.
   * 
   * @param s The array of strings to be written to disk.
   * @param offst The start offset in the array.
   * @param len The number of entries to write.
   * @throws IOException
   */
  public void writeStrings(String[] s, int offst, int len) throws IOException
  {
    // write array size and array to disk

    writeInt(len - offst);
    for (int i = 0; i < s.length; ++i) writeString(s[i+offst]);
  }

  /**
   * Used by all array writes to set the array size, and type size
   * parameters. This function also initializes the array end counter
   * to zero and writes the array size out to disk. 
   * 
   * @param arraysze The array size to be written to disk.
   * @param typesze The array type size.
   * @throws IOException
   */
  private void setBufferSize(int arraysze, int typesze) throws IOException
  {
    writeInt(arraysze);
    aArrayCount = arraysze;
    aTypSize = typesze;
    aIEnd = 0;
  }

  /**
   * Used by array write functions to write the buffer to disk when required
   * and to update the array input counters for each successive buffer write.
   * This function returns true as long as more array data is available for
   * writing. When all data has been written false is returned.
   * 
   * @return True if more array data must be written to disk.
   * @throws IOException
   */
  private boolean writeNext() throws IOException
  {
    if (aIEnd == aArrayCount) return false;
    
    checkWriteNextType(aTypSize);

    aIStrt = aIEnd;
    aIEnd += (aBufSize - aBAOS.size()) / aTypSize;
    if (aIEnd > aArrayCount) aIEnd = aArrayCount;

    return true;
  }

  /**
   * Writes out the buffer to disk, flushes, and resets the internal buffer so that 
   * it is ready to accept more data.
   *  
   * @throws IOException
   */
  private void writeBufferX() throws IOException
  {
    // write the buffer size and the buffer to disk
    
    aDOS.writeInt(aBAOS.size());
    aBAOS.writeTo(aDOS);

    // flush the buffer and reset the internal buffer\

    aDOS.flush();
    aBAOS.reset();
  }

  /**
   * Checks the buffer to see if it is full and writes it out if it is.
   *  
   * @throws IOException
   */
  private void checkWriteNextType(int typesze) throws IOException
  {
    if (aBAOS.size() + typesze > aBufSize) writeBufferX();    
  }

  /**
   * Flushes the file stream.
   *  
   * @throws IOException
   */
  public void flush() throws IOException
  {
    if (aBAOS.size() > 0) writeBufferX();
  }

  /**
   * Closes the file stream.
   *  
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    flush();
    aDOS.close();
  }
}
