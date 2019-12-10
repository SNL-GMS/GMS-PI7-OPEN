package gms.dataacquisition.css.converters.data;

import org.kohsuke.args4j.Option;

/**
 * Created by jwvicke on 11/28/17.
 */
public class CssBatchFileWriterCommandLineArgs {

  @Option(name = "-wfDiscFile", required = true, usage = "Path to CSS wfdiscreaders file")
  private String wfdiscFile;

  @Option(name = "-channelsFile", required = true, usage = "Path to ReferenceChannel[] JSON file")
  private String channelsFile;

  @Option(name = "-outputDir", usage = "Output directory for data files from the converter")
  private String outputDir;

  @Option(name = "-batchSize", usage = "Sets the maximum number of updates to transmit to the OSD Gateway Service in a single transaction (default 1).")
  private int batchSize = 1;

  @Option(name = "-batchInterval", usage = "Number of milliseconds to wait before sending the next batch of data to the OSD Gateway Service (default 0).")
  private int batchInterval = 0;

  @Option(name = "-stations", usage = "Comma-separated list of stations")
  private String stations = "";

  @Option(name = "-channels", usage = "Comma-separated list of channels")
  private String channels = "";

  @Option(name = "-timeEpoch", usage = "Start time in epoch seconds to load waveforms")
  private long timeEpoch = -1;

  @Option(name = "-timeDate", usage = "Start time in date format YYYY-MM-DDTHH:MM:SS.SSSZ to load waveforms")
  private String timeDate = "";

  @Option(name = "-endtimeEpoch", usage = "End time in epoch seconds to load waveforms")
  private long endtimeEpoch = -1;

  @Option(name = "-endtimeDate", usage = "End time in date format YYYY-MM-DDTHH:MM:SS.SSSZ to load waveforms")
  private String endtimeDate = "";

  @Option(name = "-includeSamples", usage = "Keep waveforms values part of the output JSON. Default behavior is to write out a reference to .w files instead of the values.")
  private boolean includeSamples = false;

  public String getWfdiscFile() {
    return wfdiscFile;
  }

  public String getChannelsFile() { return channelsFile; }

  public String getOutputDir() { return outputDir; }

  public int getBatchSize() {
    return batchSize;
  }

  public int getBatchInterval() {
    return batchInterval;
  }

  public String getStations() {
    return stations;
  }

  public String getChannels() {
    return channels;
  }

  public long getTimeEpoch() {
    return timeEpoch;
  }

  public String getTimeDate() {
    return timeDate;
  }

  public long getEndtimeEpoch() {
    return endtimeEpoch;
  }

  public String getEndtimeDate() {
    return endtimeDate;
  }
  
  public boolean getIncludeSamples(){return includeSamples;}
}
