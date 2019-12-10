package gms.utilities.waveformreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Float4FormatWaveformReader implements WaveformReaderInterface {

  private static final int vax_single_bias = 0x81;

  private static final int ieee_single_bias = 0x7f;

  @Override
  public double[] read(InputStream input, int N, int skip) throws IOException {
    int skipBytes = skip * Float.SIZE / Byte.SIZE;
    input.skip(Math.min(input.available(), skipBytes));

    DataInputStream dis = new DataInputStream(input);

    double[] data = new double[N];
    int i = 0;
    for (; i < N && dis.available() > 0; i++) {
      data[i] = dis.readFloat();
    }

    // Check if no data could be read
    if (i == 0)
      return null;

    return data;

  }

  private static float vax2float(float f)
  {
    int i = Float.floatToRawIntBits(f);
    i = Integer.reverseBytes(i);

    // Extract the sign (0=positive, 1=negative)
    int sign = ((i >> 15) & 0x1);

    // Extract the exponent
    int exp = ((i >> 7) & 0xFF) - vax_single_bias + ieee_single_bias;

    // Extract the mantissa
    int mantissa1 = (i & 0x7F);
    int mantissa2 = ((i >> 16) & 0xFFFF);

    // Re-assemble the float
    i = (sign << 31) | (exp << 23) | (mantissa1 << 16) | mantissa2;

    return Float.intBitsToFloat(i);
  }

}
