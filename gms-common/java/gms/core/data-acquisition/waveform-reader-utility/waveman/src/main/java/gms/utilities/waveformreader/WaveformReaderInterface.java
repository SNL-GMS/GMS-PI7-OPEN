package gms.utilities.waveformreader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Functional interface for a WaveformReader; takes an InputStream, number of bytes to skip, and
 * number of bytes to read, returning a parsed int[] (digitized counts of a waveform).
 */
@FunctionalInterface
public interface WaveformReaderInterface {

    /**
     * Reads a waveform given an InputStream.
     *
     * @param input the input stream to read from
     * @param N number of bytes to read
     * @param skip number of bytes to skip
     * @return digitizer counts as int[]
     * @throws IOException if I/O problems occur during reading from InputStream
     */
    double[] read(InputStream input, int N, int skip) throws IOException;
}
