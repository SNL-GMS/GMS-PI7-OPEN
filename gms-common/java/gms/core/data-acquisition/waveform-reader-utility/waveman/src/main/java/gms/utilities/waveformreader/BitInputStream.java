package gms.utilities.waveformreader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that allows variable-bit length integers to be read from the provided input
 * stream.  Do not use this input stream for any other reading as it performs internal buffering of
 * the bytes.
 *
 * Read bits from an input stream
 *
 * @author bjmerch
 */
public class BitInputStream extends BufferedInputStream {

  private int _byte;
  private int _bits_remaining = 0;

  /**
   * Construct a bit input stream over the provided input stream, reading N bytes at a time.
   */
  protected BitInputStream(InputStream is, int N) {
    super(is, N);
  }

  /**
   * Read a 32 bit integer constructed from the next n bits of 2's complement data.
   *
   * @param bits number of bits to read (0 to 32)
   * @param signed if true, sign extend the last bit.
   */
  public final int read(int bits, boolean signed) throws IOException {
    int out = 0;
    int N = 0;

    //  Read from the cached byte
    if (_bits_remaining > 0) {
      //  Get the bits
      out = _byte << (32 - _bits_remaining);

      //  Number of bits read from the cached byte
      N = (bits < _bits_remaining ? bits : _bits_remaining);

      _bits_remaining -= N;
    }

    // Read the whole bytes
    for (; N < bits; N += 8) {
      _byte = read() & 0xFF;

      if (N < 24) {
        out |= _byte << (24 - N);
      } else {
        out |= _byte >> (N - 24);
      }

      _bits_remaining = N + 8 - bits;
    }

    //  Shift down and sign extend
    if (signed) {
      out >>= (32 - bits);
    } else {
      out >>>= (32 - bits);
    }

    return out;
  }
}
