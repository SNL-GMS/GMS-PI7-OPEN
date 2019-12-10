package gms.utilities.waveformreader;

import java.io.InputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaveformReader {

  private static final Logger logger = LoggerFactory.getLogger(WaveformReader.class);

  // Mapping from Format to WaveformReader.  This gives a WaveformReader that can be used to read
  // a particular Format of waveform.
  private static final Map<FormatCode, WaveformReaderInterface> formatReaders = Map.of(
      FormatCode.F4, new Float4FormatWaveformReader(),
      FormatCode.S4, new Sun4FormatWaveformReader(),
      FormatCode.I4, new I4FormatWaveformReader(),
      FormatCode.CD, new CanadianCompressedWaveformReader(),
      FormatCode.CC, new CanadianCompressedWaveformReader(),
      FormatCode.CM6, new Ims20Cm6WaveformReader());


  /**
   * Calls the proper waveform reader and reads the data bytes
   *
   * @param input data bytes
   * @param format the format code, e.g. 's4' or 'b#'.
   * @param samplesToRead number of bytes to read
   * @param skip number of bytes to skip
   * @return WaveformReader for the given format code, or null if the format code is unknown or
   * there is no WaveformReader for it.
   */
  public static double[] readSamples(InputStream input, String format, int samplesToRead, int skip)
      throws Exception {
    WaveformReaderInterface reader = readerFor(format);
    return reader.read(input, samplesToRead, skip);
  }

  public static double[] readSamples(InputStream input, String format) throws Exception {
    WaveformReaderInterface reader = readerFor(format);
    return reader.read(input, input.available(), 0);
  }

  /**
   * Looks up a WaveformReader corresponding to the given format code (CSS 3.0).
   *
   * @param fc the format code, e.g. 's4' or 'b#'.
   * @return WaveformReader for the given format code, or null if the format code is unknown or
   * there is no WaveformReader for it.
   */
  public static WaveformReaderInterface readerFor(String fc) throws Exception {
    FormatCode format = FormatCode.fcFromString(fc);
    if (format == null || !formatReaders.containsKey(format)) {
      String error = "Unsupported format: " + fc;
      logger.error(error);
      throw new Exception(error);
    }
    return formatReaders.get(format);
  }
}
