package gms.utilities.waveformreader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ims20Cm6WaveformReader implements WaveformReaderInterface {
  private static Logger logger = LoggerFactory.getLogger(Ims20Cm6WaveformReader.class);

  /**
   * Reads the InputStream as an IMS 2.0 CM6 waveform.
   *
   * @param input the input stream to read from
   * @param numBytes number of bytes to read
   * @param skip number of bytes to skip (required to be 0 for IMS 2.0 data)
   * @return int[] of digitizer counts from the waveform
   */
  public double[] read(InputStream input, int numBytes, int skip)
      throws IOException {
    Validate.notNull(input);
    // The notion of skipping bytes doesn't apply to IMS 2.0 data since it's a string (not binary)
    Validate.validState(skip == 0);
    Validate.validState(numBytes >= 0);

    String cm6NoSpaces = new String(input.readAllBytes()).replaceAll(" ", "");
    try {
      int[] parsedWaveformInts = cm6ToInt(cm6NoSpaces);

      return Arrays.stream(parsedWaveformInts)
          .asDoubleStream()
          .toArray();
    }
    catch (IOException e) {
      logger.error("Error parsing CM6 data", e);
      throw e;
    }
  }

  private static final int[] ichar = {
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, -1, 1, -1, -1,
      2, 3, 4, 5, 6, 7, 8, 9, 10, 11, -1, -1, -1, -1, -1, -1,
      -1, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
      27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, -1, -1, -1, -1, -1,
      -1, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
      53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -1, -1, -1, -1, -1};

  private static final int N4M1 = 15;
  private static final int N5M1 = 31;
  private static final int CONTROL_BIT = (1 << 5);

  /**
   * Convert CM6 compressed string to int[]
   *
   * @param cm6String CM6 string data to be decompressed into ints
   * @return decompressed CM6 string as int[]
   */
  private int[] cm6ToInt(String cm6String) throws IOException {
    Validate.notEmpty(cm6String);
    // Keep track of the number of integers we have found (index into integerList)
    int intCount = 0;
    // cache to hold bytes as we find them
    int cachedBytes = 0;
    // Cache to hold integers until a control bit is found
    int[] intBuf = {0, 0, 0, 0, 0, 0, 0};
    List<Integer> integerList = new ArrayList<>();

    for (int i = 0; i < cm6String.length(); i++) {
      char currentChar = cm6String.charAt(i);
      // ignore newline and CR-return characters
      // Blank is an invalid character, but ANMO sends it sometimes at the end of a CM6 block
      if (currentChar == '\r' || currentChar == '\n' || currentChar == ' ')
        continue;

      if (ichar[(int) currentChar] == -1) {
        String error = String.format("Invalid ichar: %s", currentChar);
        logger.error(error);
        throw new IOException(error);
      }

      // Bits: U U C S/D D D D D
      //    U .. unused bits 2^7, 2^6
      //    C .. control bit, signals that the next byte belongs to the
      //          current value; bit 2^5
      //    S/D .. sign bit in the first byte of a value, data bit in
      //           all consecutive bytes for the same value; bit 2^4
      //    D .. data bits (bits 2^0 to 2^3)
      //    A value is coded in a maximum of 7 consecutive bytes

      // Map characters to values and cache values until a value without the control bit set is reached
      intBuf[cachedBytes++] = ichar[(int) currentChar];

      // Check for buffer overrun
      if (cachedBytes > 7) {
        String error = String.format(">7 cached bytes: %d", cachedBytes);
        logger.error(error);
        throw new IOException(error);
      }

      if ((intBuf[cachedBytes - 1] & CONTROL_BIT) == CONTROL_BIT)
        continue;

      // All values collected, decode the real value now
      int signflag = ((intBuf[0]) >> 4) & 1;
      // clear signed, control and unused bits
      int tmpInt = intBuf[0] & N4M1;

      for (int j = 1; j < cachedBytes; j++) {
        // clear control and unused bits
        intBuf[j] &= N5M1;
        // shift left current value to make space for the next 5 bits
        tmpInt <<= 5;
        tmpInt += intBuf[j];
      }

      // negative number
      if (1 == signflag)  {
        tmpInt = -tmpInt;
      }

      cachedBytes = 0;
      integerList.add(tmpInt);
      intCount++;
    }

    if (cachedBytes != 0) {
      String error = "cached bytes not 0";
      logger.error(error);
      throw new IOException(error);
    }

    // iout is a name from the original C algorithm; leaving so there's some semblance of traceability
    int[] iout = integerList.stream().mapToInt(x -> x).toArray();
    // Restore original values from second differences
    iout[1] += 2 * iout[0];
    for (int i = 2; i < intCount; i++) {
      iout[i] += 2 * iout[i - 1] - iout[i - 2];
    }

    return iout;
  }
}
