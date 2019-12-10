package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import gms.dataacquisition.seedlink.clientlibrary.data.BitInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Steim1 extends Steim {

  public static final int CODE = 10;

  public Steim1() {
    super(CODE);
  }

  /**
   * Based on the Steim1 description found on page 143 in the SEED v2.4 Manual.
   */
  @Override
  public int[] decode(byte[] b, int samples, boolean bigEndian)
      throws Exception {
    return read(new ByteArrayInputStream(b), samples,
        Steim.truncate(b.length) / FRAME_SIZE);
  }

  // below from JWaveform

  //  Padding between the code/subcode and the word
  private static byte[] word_padding = new byte[]{32, 0, 0, 0};

  //  Lookup table of # bits/sample in a word
  private static byte[] word_bits = new byte[]{0, 8, 16, 32};

  //  Lookup table of # samples in a word
  private static byte[] word_samples = new byte[]{0, 4, 2, 1};

  /**
   * Read the compressed data stream, composed of N samples.
   */
  private static int[] read(InputStream is, int N, int frame_count) throws IOException {
    BitInputStream bis = new BitInputStream(is, 64);
    int[] codes = new int[16];
    int x0 = 0;
    int xn = 0;

    int[] data = new int[N];
    int i = 0;

    // Process the remaining 64-byte data frames
    for (int j = 0; j < frame_count && i < N; j++) {
      // Extract the 16 2-bit codes
      for (int k = 0; k < 16; k++) {
        codes[k] = bis.read(2, false);
      }

      // Check for the first frame and extract the start and end
      // values
      int k_start = 1;
      if (j == 0) {
        k_start = 3;

        x0 = bis.read(32, true);
        xn = bis.read(32, true);
      }

      // Read the remaining data values
      for (int k = k_start; k < 16 && i < N; k++) {
        int code = codes[k];

        //  Read any padding
        bis.read(word_padding[code], false);

        //  Read the data bits
        int count = word_samples[code];
        int bits = word_bits[code];

        for (int l = 0; l < count & i < N; l++) {
          data[i++] = bis.read(bits, true);
        }
      }
    }

    //  Forward integrate the first-differences
    data[0] = x0;
    Integrator.integrate(data, 0, N);

    // Verify that the end value is correct
    if (data[N - 1] != xn) {
      System.out.println(
          "STEIM1 ERROR:  Last value does not match.  Found " + data[N - 1] + ", expecting " + xn);
    }

    return data;
  }

}
