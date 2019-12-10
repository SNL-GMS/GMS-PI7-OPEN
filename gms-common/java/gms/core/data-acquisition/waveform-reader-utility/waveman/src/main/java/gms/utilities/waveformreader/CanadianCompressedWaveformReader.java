package gms.utilities.waveformreader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class CanadianCompressedWaveformReader implements WaveformReaderInterface {

  @Override
  public double[] read(InputStream input, int N, int skip) throws IOException {
    // Extract the canadian compressed data
    double[] data = CanadianCompression.read(input, skip + N, false);

    if (skip == 0)
      return data;

    return Arrays.copyOfRange(data, skip, N);

  }

  private static class CanadianCompression {
    /*
     * bit lengths for various sample codes
     */
    private static final byte[][] bit_lengths = new byte[][]
        {{ 4, 6, 8, 10, 12, 14, 16, 18 },
            { 4, 8, 12, 16, 20, 24, 28, 32 }};

    /*
     * Number of groups of data samples in a data block
     */
    private static final int Ngroups = 5;

    /*
     * Number of samples in a group
     */
    private static final int GroupSize = 4;

    /*
     * Number of samples in a data block
     */
    private static final int Nsamples = Ngroups * GroupSize;

    /**
     * Read the compressed data stream, composed of N samples.
     *
     * @param is
     * @param N
     * @return uncompressed integer samples
     * @throws IOException
     */
    public static double[] read(InputStream is, int N, boolean interlace) throws IOException
    {
      BitInputStream bis = new BitInputStream(is,8192);

      //  Determine the number of blocks
      int N_blocks = (int) Math.ceil(N / ((double) Nsamples));
      double[] data = new double[N];

      if (interlace)
      {
        boolean error = false;
        byte[] bits = new byte[Ngroups];
        int n = 0;

            /*
             * Read each of the blocks.
             */
        for (int i = 0; i < N_blocks && n < N; i++)
        {
          int start_n = n;

          //  Read the index block
          readIndexBlock(bis, bits);

          //  Read the first sample
          int firstSample = bis.read(32, true);
          if (firstSample != data[n] && n > 0)
            error = true;
          data[n++] = firstSample;

          //  Read the differentiated samples
          n = readDataBlock(bis, bits, data, n);

          //  Integrate the data twice
          integrate(data, start_n+1, n);
          integrate(data, start_n, n);

          // Back up to account for block overlap
          n--;
        }

        if ( error )
          System.out.println("CanadianCompression.read:  Warning, error checking samples do not match.");
      }
      else
      {
        //  Read the index blocks
        byte[][] bits = new byte[N_blocks][Ngroups];
        for (int i=0; i<N_blocks; i++)
          readIndexBlock(bis, bits[i]);

        //  Read the first sample
        int first = bis.read(32,true);

        //  Read the data blocks
        for (int i=0, n=0; i<N_blocks && n<N; i++)
          n = readDataBlock(bis, bits[i], data, n);

        //  undo the second difference
        integrate(data, 0, N);

        //  undo the first difference, shifting the samples
        for (int k=0; k<N; k++)
        {
          double save = data[k];
          data[k] = first;
          first += save;
        }
      }

      return data;
    }

    /**
     * Read an index block from the provided input stream and store the number of bits in the provided
     * array.
     *
     * @param bis
     * @param bits
     * @throws IOException
     */
    private static void readIndexBlock(BitInputStream bis, byte[] bits) throws IOException
    {
      int h = bis.read(1, false);
      byte[] bit_lengths_h = bit_lengths[h];

      for (int i=0; i<Ngroups; i++)
        bits[i] = bit_lengths_h[bis.read(3,false)];
    }

    /**
     * Read the data block from the provided input stream and return the number of samples read
     *
     * @param bis
     * @param bits
     * @param data
     * @param n
     * @return
     * @throws IOException
     */
    private static int readDataBlock(BitInputStream bis, byte[] bits, double[] data, int n) throws IOException
    {
      int N = data.length;

      for (int j = 0; j < Ngroups; j++)
      {
        int b = bits[j];

        for (int k = 0; k < GroupSize && n < N; k++, n++)
          data[n] = bis.read(b, true);
      }

      return n;
    }

    /**
     * @param data
     * @param start
     * @param end
     */
    private static void integrate(double[] data, int start, int end)
    {
      if (start >= data.length)
        return;

      double prev = data[start];

      for (start++; start < end; start++)
      {
        prev += data[start];
        data[start] = prev;
      }
    }


  }
}
