/*
 * This software is in the public domain because it contains materials
 * that originally came from the United States Geological Survey,
 * an agency of the United States Department of Interior. For more
 * information, see the official USGS copyright policy at
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 */

package gms.dataacquisition.stationreceiver.cd11.common;

/**
 * This class was derived from a C module CRC64.c given to us by James BlinkHorn of the Canadian
 * data center.  Below are comments from the original source: <p> Defined to be the 64 bit Cyclic
 * Redundancy Check with the polynomial x64 + x4 + x3 + x1 + x0. The high order bit is implicitly 1,
 * so this is specified by the value 11011 in binary or 0x1b. To speed computation, we pre-compute a
 * "T" vector. T[i] is the remainder of dividing i*x64 by the polynomial.n For more information on
 * CRC see D.V. Sarwate, "Computation of cyclic redundancy via table look-up," Comm. ACM 31(8), Aug.
 * 1988, p. 1008-1013.
 *
 * @author davidketchum
 */
public final class CRC64 {

  // This contains the pre-computed coefficients for each of the possible 256 values.
  private static long[] tvec;

  static {
    // compute the 256 CRC elements the first time
    tvec = new long[256];
    long crcPoly = 0x1BL;
    for (int i = 0; i < 256; i++) {
      tvec[i] = 0;
      for (int j = 7; j >= 0; j--) {
        if ((i & (1 << j)) != 0) {
          tvec[i] ^= (crcPoly << j);
        }
      }
      //Util.prt(i+"="+Util.toHex(tvec[i]));
    }
  }

  /**
   * Computes a CRC on the array b of length len.
   *
   * @param b The byte array to compute a CRC for.
   * @return CRC value.
   */
  public static long compute(byte[] b) {
    return compute(b, b.length);
  }

  /**
   * Computes a CRC on the array b of length len.
   *
   * @param b The byte array to compute a CRC for.
   * @param len The length of the array in bytes.
   * @return CRC value.
   */
  public static long compute(byte[] b, int len) {
    long crc = 0L;
    for (int i = 0; i < len; i++) {
      crc = tvec[(int) ((crc >> 56) & 0xffL)] ^ (crc << 8 | (((long) (b[i])) & 0xffL));
    }
    return crc;
  }

  /**
   * Generates a CRC for the given byte array, and checks that it matches the expected value.
   *
   * @param b The byte array to compute a CRC for.
   * @param len The length of the array in bytes.
   * @param expectedCrcValue The expected value.
   * @return true if matches, false otherwise
   */
  public static boolean isValidCrc(byte[] b, int len, long expectedCrcValue) {
    return (compute(b, len) == expectedCrcValue);
  }
}
