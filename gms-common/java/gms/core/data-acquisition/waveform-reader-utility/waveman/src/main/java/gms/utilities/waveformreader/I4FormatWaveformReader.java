package gms.utilities.waveformreader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.Validate;

public class I4FormatWaveformReader implements WaveformReaderInterface{

    /**
     * Reads the InputStream as an S4 waveform.
     *
     * @param input the input stream to read from
     * @param N number of bytes to read
     * @param skip number of bytes to skip
     * @return int[] of digitizer counts from the waveform
     */
    public double[] read(InputStream input, int N, int skip) throws IOException, NullPointerException {
        Validate.notNull(input);

        double[] data = new double[N];
        input.skip(skip);
        DataInputStream dis = new DataInputStream(input);

        int i = 0;
        for (; i < N && dis.available() > 0; i++)
            data[i] = Integer.reverseBytes(dis.readInt());

        //  Check if no data could be read
        if ( i == 0 )
            return null;

        return data;

    }
}
