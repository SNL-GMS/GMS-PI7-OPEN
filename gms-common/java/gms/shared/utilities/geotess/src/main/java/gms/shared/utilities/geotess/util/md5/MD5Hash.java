package gms.shared.utilities.geotess.util.md5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Wrapper around MessageDigest.getInstance("MD5") that adds
 * methods to update Strings, shorts, ints, longs, floats, doubles
 * int[], float[], double[], float[][] and double[][].
 * <p>Also implements toString() that returns a
 * 32 element string containing the 32 hexadecimal characters that
 * represent the byte[16] MD5 hash.
 *
 * @version 1.0
 */
public class MD5Hash
{
  private MessageDigest msgDigest;

  public MD5Hash()
  {
    try
    {
      msgDigest = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException ex)
    {
      // this is unimaginable!
      ex.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * returns a 32 element String containing the 32 hexadecimal characters that
   * represent the MD5 hash.  After a call to this method, the MessageDigest
   * is reset.  If this method is called on an empty digest, or one that has
   * been reset, it returns "D41D8CD98F00B204E9800998ECF8427E".
   * <p>To convert the hexadecimal String to byte[],
   * use method hexStringToByteArray().
   * @return String
   */
  @Override
  public String toString()
  {
    return byteArrayToHexString(msgDigest.digest());
  }
  
  public byte[] getByteArray()
  {
	  return msgDigest.digest();
  }

  public int getHashValue()
  {
	  return ByteBuffer.wrap(msgDigest.digest()).hashCode();
  }

  /**
   *
   * @param x byte
   */
  public MD5Hash update(byte x)
  {
    msgDigest.update(x);
    return this;
  }

  /**
   *
   * @param x byte[]
   */
  public MD5Hash update(byte[] x)
  {
    msgDigest.update(x);
    return this;
  }

  /**
   *
   * @param x byte[]
   * @param offset int
   * @param len int
   */
  public MD5Hash update(byte[] x, int offset, int len)
  {
    msgDigest.update(x, offset, len);
    return this;
  }

  /**
   *
   * @param x ByteBuffer
   */
  public MD5Hash update(ByteBuffer x)
  {
    msgDigest.update(x);
    return this;
  }

  /**
   *
   * @param x String
   */
  public MD5Hash update(String x)
  {
    msgDigest.update(x.getBytes());
    return this;
  }

  /**
   *
   * @param x short
   */
  public MD5Hash update(short x)
  {
    msgDigest.update(toByteBuffer(x));
    return this;
  }

  /**
   *
   * @param x int
   */
  public MD5Hash update(int x)
  {
    msgDigest.update(toByteBuffer(x));
    return this;
  }

  /**
   *
   * @param x int[]
   */
  public MD5Hash update(int[] x)
  {
    msgDigest.update(toByteBuffer(x));
    return this;
  }

  /**
  *
  * @param x int[][]
  */
 public MD5Hash update(int[][] x)
 {
   for (int i = 0; i < x.length; ++i)
     update(x[i]);
   return this;
 }

  /**
   *
   * @param x long
   */
  public MD5Hash update(long x)
  {
    msgDigest.update(toByteBuffer(x));
    return this;
  }

  /**
   *
   * @param x float
   */
  public MD5Hash update(float x)
  {
    msgDigest.update(toByteBuffer(x));
    return this;
  }

  /**
   *
   * @param x float[]
   */
  public MD5Hash update(float[] x)
  {
    msgDigest.update(toByteBuffer(x));
    return this;
  }

  /**
   *
   * @param x float[][]
   */
  public MD5Hash update(float[][] x)
  {
    for (int i = 0; i < x.length; ++i)
      update(x[i]);
    return this;
  }

  /**
   *
   * @param x double
   */
  public MD5Hash update(double x)
  {
    msgDigest.update(toByteBuffer(x));
    return this;
  }

  /**
   *
   * @param x double[]
   */
  public MD5Hash update(double[] x)
  {
    msgDigest.update(toByteBuffer(x));
    return this;
  }

  /**
   *
   * @param x double[][]
   */
  public MD5Hash update(double[][] x)
  {
    for (int i = 0; i < x.length; ++i)
      update(x[i]);
    return this;
  }

  /**
   *
   * @param file File
   * @throws IOException
   */
  public MD5Hash update(File file) throws IOException
  {
    FileInputStream istream = new FileInputStream(file);
    int num;
    byte[] buffer = new byte[1024 * 16];

    // now read the data from input stream, and update the MD5 hash
    // to reflect the contents
    do
    {
      // read from buffer and update md
      num = istream.read(buffer);
      if (num > 0)
        msgDigest.update(buffer, 0, num);
    }
    while (num != -1);

    istream.close();
    return this;
  }

   /**
   * Array useful for converting chars to bytes when converting a String of
   * hexidecimal chars into a byte array.  Given char c, hexNibble[(byte)c-48]
   * will return the corresponding byte.  For example, if c = '1', then
   * hexNiblles[(byte)c-48] will be a byte with value 1. if c = 'A', then
   * hexNiblles[(byte)c-48] will be a byte with value 10, etc.  Only works
   * for c in [0..9,A..F].
   * <p>byte[] hexNibbles = new byte[]
   *   {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15};
   */
  public final static byte[] hexNibbles = new byte[]
      {
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13,
      14, 15};

  /**
   * Takes a character and returns a byte that corresponds to that character in the
   * hexadecimal world. So, for ch =
   * 'D', 13 will be returned and for ch = '4', 4 will be returned.
   * @param ch hex character the function will return a corresponding byte for
   * @return byte representation of the hexadecimal char in ch
   */
  public static byte toNibble(char ch)
  {
    return hexNibbles[ (byte) ch - 48];
  }

  /**
   * Convert a hex String into a byte array.
   * @param hex hex String to convert to a byte[]
   * @return byte array representation of the hex String.  Byte array
   * will have half the number of elements that String has.
   */
  public static byte[] hexStringToByteArray(String hex)
  {
    byte[] bytes = new byte[hex.length() / 2];
    for (int i = 0; i < bytes.length; i++)
      bytes[i] = (byte) ( (toNibble(hex.charAt(i * 2)) << 4)
                         | toNibble(hex.charAt(i * 2 + 1)));

    return bytes;
  }

  /**
   * Convert a byte[] array to readable string format. This makes the "hex" readable!
   * @param in byte[] to convert to string format
   * @return byte array in String format; null if in is null or is empty
   */
  public static String byteArrayToHexString(byte in[])
  {
    if (in == null || in.length <= 0)
      return null;

	StringBuffer buf = new StringBuffer();
	String s;
	for(byte b: in) 
	{
		s = Integer.toHexString(b & 0xff);
		if(s.length() == 1)
			buf.append('0');  // prepend 0 in case function returns string of length 1
		buf.append(s);
	}
	return buf.toString();		
  }

  /**
   *
   * @param x float
   * @return ByteBuffer
   */
  public static ByteBuffer toByteBuffer(float x)
  {
    ByteBuffer buf = ByteBuffer.allocate(8);
    buf.asFloatBuffer().put(x);
    return buf;
  }

  /**
   *
   * @param x float[]
   * @return ByteBuffer
   */
  public static ByteBuffer toByteBuffer(float[] x)
  {
    ByteBuffer buf = ByteBuffer.allocate(8 * x.length);
    buf.asFloatBuffer().put(x);
    return buf;
  }

  /**
   *
   * @param x double
   * @return ByteBuffer
   */
  public static ByteBuffer toByteBuffer(double x)
  {
    ByteBuffer buf = ByteBuffer.allocate(8);
    buf.asDoubleBuffer().put(x);
    return buf;
  }

  /**
   *
   * @param x double[]
   * @return ByteBuffer
   */
  public static ByteBuffer toByteBuffer(double[] x)
  {
    ByteBuffer buf = ByteBuffer.allocate(8 * x.length);
    buf.asDoubleBuffer().put(x);
    return buf;
  }

  /**
   *
   * @param x long
   * @return ByteBuffer
   */
  public static ByteBuffer toByteBuffer(long x)
  {
    ByteBuffer buf = ByteBuffer.allocate(8);
    buf.asLongBuffer().put(x);
    return buf;
  }

  /**
   *
   * @param x int
   * @return ByteBuffer
   */
  public static ByteBuffer toByteBuffer(int x)
  {
    ByteBuffer buf = ByteBuffer.allocate(4);
    buf.asIntBuffer().put(x);
    return buf;
  }

  /**
   *
   * @param x int[]
   * @return ByteBuffer
   */
  public static ByteBuffer toByteBuffer(int[] x)
  {
    ByteBuffer buf = ByteBuffer.allocate(4 * x.length);
    buf.asIntBuffer().put(x);
    return buf;
  }

  /**
   *
   * @param x short
   * @return ByteBuffer
   */
  public static ByteBuffer toByteBuffer(short x)
  {
    ByteBuffer buf = ByteBuffer.allocate(2);
    buf.asShortBuffer().put(x);
    return buf;
  }

  /**
   *
   * @param x double
   * @return byte[]
   */
  public static byte[] toBytes(double x)
  {
    return toByteBuffer(x).array();
  }

  /**
   *
   * @param x double[]
   * @return byte[]
   */
  public static byte[] toBytes(double[] x)
  {
    return toByteBuffer(x).array();
  }

  /**
   *
   * @param x long
   * @return byte[]
   */
  public static byte[] toBytes(long x)
  {
    return toByteBuffer(x).array();
  }

  /**
   *
   * @param x int
   * @return byte[]
   */
  public static byte[] toBytes(int x)
  {
    return toByteBuffer(x).array();
  }

  /**
   *
   * @param x short
   * @return byte[]
   */
  public static byte[] toBytes(short x)
  {
    return toByteBuffer(x).array();
  }

  /**
   *
   * @param bytes byte[]
   * @return double
   */
  public static double toDouble(byte[] bytes)
  {
    return ByteBuffer.wrap(bytes).asDoubleBuffer().get();
  }

  /**
   *
   * @param bytes byte[]
   * @return double[]
   */
  public static double[] toDoubleArray(byte[] bytes)
  {
    ByteBuffer buf = ByteBuffer.wrap(bytes);
    double[] x = new double[bytes.length / 8];
    for (int i = 0; i < x.length; ++i)
      x[i] = buf.asDoubleBuffer().get(i);
    return x;
  }

  /**
   *
   * @param bytes byte[]
   * @return long
   */
  public static long toLong(byte[] bytes)
  {
    return ByteBuffer.wrap(bytes).asLongBuffer().get();
  }

  /**
   *
   * @param bytes byte[]
   * @return int
   */
  public static int toInt(byte[] bytes)
  {
    return ByteBuffer.wrap(bytes).asIntBuffer().get();
  }

  /**
   *
   * @param bytes byte[]
   * @return short
   */
  public static short toShort(byte[] bytes)
  {
    return ByteBuffer.wrap(bytes).asShortBuffer().get();
  }

  /**
   *
   * @param bytes byte[]
   * @param offset int
   * @param len int
   * @return double
   */
  public static double toDouble(byte[] bytes, int offset, int len)
  {
    return ByteBuffer.wrap(bytes, offset, len).asDoubleBuffer().get();
  }

  /**
   *
   * @param bytes byte[]
   * @param offset int
   * @param len int
   * @return long
   */
  public static long toLong(byte[] bytes, int offset, int len)
  {
    return ByteBuffer.wrap(bytes, offset, len).asLongBuffer().get();
  }

  /**
   *
   * @param bytes byte[]
   * @param offset int
   * @param len int
   * @return int
   */
  public static int toInt(byte[] bytes, int offset, int len)
  {
    return ByteBuffer.wrap(bytes, offset, len).asIntBuffer().get();
  }

  /**
   *
   * @param bytes byte[]
   * @param offset int
   * @param len int
   * @return short
   */
  public static short toShort(byte[] bytes, int offset, int len)
  {
    return ByteBuffer.wrap(bytes, offset, len).asShortBuffer().get();
  }

}
