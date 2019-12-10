package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import gms.dataacquisition.seedlink.clientlibrary.data.BitInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Steim2 extends Steim {

  public static final int CODE = 11;

  public Steim2() {
    super(CODE);
  }

  @Override
  public int[] decode(byte[] b, int samples, boolean bigEndian)
      throws Exception {
    return read(new ByteArrayInputStream(b), samples,
        Steim.truncate(b.length) / FRAME_SIZE);
  }

  // below from JWaveform

  /**
   * Read the compressed data stream, composed of N samples.
   */
  private static int[] read(InputStream is, final int N, int frame_count) throws IOException {
    int[] data = new int[N];
    if (N == 0) {
      return data;
    }

    BitInputStream bis = new BitInputStream(is, frame_count * 64);
    int[] codes = new int[16];
    int x0 = 0;
    int xn = 0;

    int i = 0;

    // Process the remaining 64-byte data frames
    for (int j = 0; j < frame_count; j++) {
      // Extract the 16 2-bit codes
      for (int k = 0; k < 16; k++) {
        codes[k] = bis.read(2, false);
      }

      // Check for the first frame and extract the start and end
      // values
      int k = 1;
      if (j == 0) {
        k = 3;

        x0 = bis.read(32, true);
        xn = bis.read(32, true);
      }

      // Read the remaining data values
      for (; k < 16; k++) {
        //  Read any subcode
        int c = codes[k];
        int sc = 0;
        if (word_subcode[c]) {
          sc = bis.read(2, false);
        }

        //  Read any padding
        int pad = word_padding[c][sc];
        if (pad > 0) {
          bis.read(pad, false);
        }

        //  Read the data bits
        int count = word_samples[c][sc];
        if ((N - i) < count) {
          count = N - i;
        }

        int bits = word_bits[c][sc];

        for (int l = 0; l < count; l++) {
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
          "STEIM2 ERROR:  Last value does not match.  Found " + data[N - 1] + ", expecting " + xn);
    }

    return data;
  }

  //  Define whether the subcode is used
  private static final boolean[] word_subcode = new boolean[]{false, false, true, true};

  //  Padding between the code/subcode and the word
  private static final byte[][] word_padding = new byte[][]{
      {32, 32, 32, 32},
      {0, 0, 0, 0},
      {30, 0, 0, 0},
      {0, 0, 2, 30}
  };

  //  Lookup table of # bits/sample in a word
  private static final byte[][] word_bits = new byte[][]{
      {0, 0, 0, 0},
      {8, 0, 0, 0},
      {0, 30, 15, 10},
      {6, 5, 4, 0}
  };
  //  Lookup table of # samples in a word
  private static final byte[][] word_samples = new byte[][]{
      {0, 0, 0, 0},
      {4, 0, 0, 0},
      {0, 1, 2, 3},
      {5, 6, 7, 0}
  };

}
