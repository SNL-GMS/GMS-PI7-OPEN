package gms.shared.utilities.geotess.util.containers.hash;

import java.io.Serializable;

//import sun.misc.DoubleConsts;
//import sun.misc.FloatConsts;

/**
 * Base class for all intrinsic HashSet and HashMap objects. Contains
 * definitions for default capacity and load factors as well as maximum allowed
 * capacities. Also defines the local capacity, load factor, threshold (capacity
 * * load factor), and size for each instance of an intrinsic HashSet or
 * HashMap. Basic functionality includes size() and isEmpty() tests, a
 * constructor to calculate power of 2 capacity and threshold given input
 * capacity request and load factor, hash code functions for Long, Float, and
 * Double intrinsics, and finally, a function that returns the hash table index
 * given an input type based hash code and table length.
 * 
 * @author Jim Hipp
 * 
 */
public abstract class HashIntrinsic implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 8058099372006904458L;

  /**
   * The default initial capacity - MUST be a power of two.
   */
  protected static final int   DEFAULT_INITIAL_CAPACITY = 16;

  /**
   * The maximum capacity, used if a higher value is implicitly specified by
   * either of the constructors with arguments. MUST be a power of two <= 1<<30.
   */
  protected static final int   MAXIMUM_CAPACITY         = 1 << 30;

  /**
   * The load factor used when none specified in constructor.
   */
  protected static final float DEFAULT_LOAD_FACTOR      = 0.75f;

  /**
   * The number of key-value mappings contained in this map.
   */
  protected int                size;

  /**
   * The next size value at which to resize (capacity * load factor).
   * 
   * @serial
   */
  protected int                threshold;

  /**
   * The load factor for the hash table.
   * 
   * @serial
   */
  protected float              loadFactor;

  /**
   * The storage table capacity minus 1.
   */
  protected int                capMinus1;

  // These constants were copied from sun.misc.DoubleConsts
  // and sun.misc.FloatConsts
  public static final int FLOAT_EXP_BIT_MASK = 2139095040;
  public static final int FLOAT_SIGNIF_BIT_MASK = 8388607;
  public static final long DOUBLE_EXP_BIT_MASK = 9218868437227405312L;
  public static final long DOUBLE_SIGNIF_BIT_MASK = 4503599627370495L;

  protected HashIntrinsic(int initialCapacity, float loadFactor)
  {
    // throw error if initial capacity is <= 0 or if load factor
    // is <= 0

    if (initialCapacity <= 0)
      throw new IllegalArgumentException("Illegal initial capacity: "
          + initialCapacity);
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
      throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

    // if initial capacity exceeds maximum set to maximum

    if (initialCapacity > MAXIMUM_CAPACITY) initialCapacity = MAXIMUM_CAPACITY;

    // Find a power of 2 >= initialCapacity

    int capacity = 1;
    while (capacity < initialCapacity)
      capacity <<= 1;

    // set load factor and threshold and create new table

    capMinus1 = capacity - 1;
    this.loadFactor = loadFactor;
    threshold = (int) (capacity * loadFactor);
  }

  /**
   * Returns a hash code for this <code>Long</code>. The result is the exclusive
   * OR of the two halves of the primitive <code>long</code> value held by this
   * <code>Long</code> object. That is, the hashcode is the value of the
   * expression: <blockquote>
   * 
   * <pre>
   * (int) (this.longValue() &circ; (this.longValue() &gt;&gt;&gt; 32))
   * </pre>
   * 
   * </blockquote>
   * 
   * @return a hash code value for this object.
   */
  protected static int hashCodeLong(long value)
  {
    return (int) (value ^ (value >>> 32));
  }

  /**
   * Hash code for a float.
   * 
   * @param value
   *          Input float value.
   * @return The type based hash code for an input float intrinsic.
   * @see floatToIntBits(value)
   */
  protected static int hashCodeFloat(float value)
  {
    return floatToIntBits(value);
  }

  /**
   * Hash code for a double. The result is the exclusive OR of the two halves of
   * the primitive <code>double</code> value held by the <code>Long</code> of
   * the double. That is, the hashcode is the value of the expression:
   * <blockquote>
   * 
   * <pre>
   * (int) (this.doubleValue() &circ; (this.doubleValue() &gt;&gt;&gt; 32))
   * </pre>
   * 
   * </blockquote>
   * 
   * 
   * @param value
   *          Input double value.
   * @return The type based hash code for an input double intrinsic.
   * @see doubleToLongBits(value)
   */
  protected static int hashCodeDouble(double value)
  {
    long bits = doubleToLongBits(value);
    return (int) (bits ^ (bits >>> 32));
  }

  /**
   * Returns a representation of the specified floating-point value according to
   * the IEEE 754 floating-point "single format" bit layout.
   * <p>
   * Bit 31 (the bit that is selected by the mask <code>0x80000000</code>)
   * represents the sign of the floating-point number. Bits 30-23 (the bits that
   * are selected by the mask <code>0x7f800000</code>) represent the exponent.
   * Bits 22-0 (the bits that are selected by the mask <code>0x007fffff</code>)
   * represent the significand (sometimes called the mantissa) of the
   * floating-point number.
   * <p>
   * If the argument is positive infinity, the result is <code>0x7f800000</code>.
   * <p>
   * If the argument is negative infinity, the result is <code>0xff800000</code>.
   * <p>
   * If the argument is NaN, the result is <code>0x7fc00000</code>.
   * <p>
   * In all cases, the result is an integer that, when given to the
   * intBitsToFloat(int) method, will produce a floating-point value
   * the same as the argument to <code>floatToIntBits</code> (except all NaN
   * values are collapsed to a single &quot;canonical&quot; NaN value).
   * 
   * @param value
   *          a floating-point number.
   * @return the bits that represent the floating-point number.
   */
  public static int floatToIntBits(float value)
  {
    int result = Float.floatToRawIntBits(value);

    // Check for NaN based on values of bit fields, maximum
    // exponent and nonzero significand.

//    if (((result & FloatConsts.EXP_BIT_MASK) == FloatConsts.EXP_BIT_MASK)
//            && (result & FloatConsts.SIGNIF_BIT_MASK) != 0) result = 0x7fc00000;
    
    if (((result & FLOAT_EXP_BIT_MASK) == FLOAT_EXP_BIT_MASK)
            && (result & FLOAT_SIGNIF_BIT_MASK) != 0) result = 0x7fc00000;
    return result;
  }

  /**
   * Returns a representation of the specified floating-point value according to
   * the IEEE 754 floating-point "double format" bit layout.
   * <p>
   * Bit 63 (the bit that is selected by the mask
   * <code>0x8000000000000000L</code>) represents the sign of the floating-point
   * number. Bits 62-52 (the bits that are selected by the mask
   * <code>0x7ff0000000000000L</code>) represent the exponent. Bits 51-0 (the
   * bits that are selected by the mask <code>0x000fffffffffffffL</code>)
   * represent the significand (sometimes called the mantissa) of the
   * floating-point number.
   * <p>
   * If the argument is positive infinity, the result is
   * <code>0x7ff0000000000000L</code>.
   * <p>
   * If the argument is negative infinity, the result is
   * <code>0xfff0000000000000L</code>.
   * <p>
   * If the argument is NaN, the result is <code>0x7ff8000000000000L</code>.
   * <p>
   * In all cases, the result is a <code>long</code> integer that, when given to
   * the longBitsToDouble(long) method, will produce a floating-point
   * value the same as the argument to <code>doubleToLongBits</code> (except all
   * NaN values are collapsed to a single &quot;canonical&quot; NaN value).
   * 
   * @param value
   *          a <code>double</code> precision floating-point number.
   * @return the bits that represent the floating-point number.
   */
  public static long doubleToLongBits(double value)
  {
    long result = Double.doubleToRawLongBits(value);
    // Check for NaN based on values of bit fields, maximum
    // exponent and nonzero significand.
    if (((result & DOUBLE_EXP_BIT_MASK) == DOUBLE_EXP_BIT_MASK)
        && (result & DOUBLE_SIGNIF_BIT_MASK) != 0L)
      result = 0x7ff8000000000000L;
    return result;
  }

  /**
   * Returns the table index given an input type hash code. The function applies
   * a supplemental hash function to a given input type hashCode, which defends
   * against poor quality hash functions. This is critical because all intrinsic
   * hash objects uses power-of-two length hash tables, that otherwise encounter
   * collisions for hashCodes that do not differ in the lower bits.
   * 
   * @param hc
   *          The input type hash code.
   * @param lm1
   *          The table length minus 1.
   * @return The table index for the input hash code.
   */
  protected static int tableIndex(int hc, int lm1)
  {
    hc ^= (hc >>> 20) ^ (hc >>> 12);
    hc ^= (hc >>> 7) ^ (hc >>> 4);
    return hc & lm1;
  }

  /**
   * Returns the number of key entries in this set.
   * 
   * @return The number of key entries in this set.
   */
  public int size()
  {
    return size;
  }

  /**
   * Returns <tt>true</tt> if this set contains no key entries.
   * 
   * @return <tt>true</tt> if this set contains no key entries.
   */
  public boolean isEmpty()
  {
    return size == 0;
  }

  /**
   * Standard clear() function that must be implemented for each intrinsic
   * HashSet and HashMap extending this super class.
   */
  public abstract void clear();
}
