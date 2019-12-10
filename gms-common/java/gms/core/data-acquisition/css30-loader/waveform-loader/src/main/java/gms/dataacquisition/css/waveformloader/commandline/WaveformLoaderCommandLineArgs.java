package gms.dataacquisition.css.waveformloader.commandline;

import org.kohsuke.args4j.Option;

public class WaveformLoaderCommandLineArgs {

  @Option(name = "-segmentsDir", required = true, usage = "Directory containing COI ChannelSegment<Waveform> JSON files. Should also contain chan-seg-id-to-w.json if linking waveform values is required.")
  private String segmentsDir;

  @Option(name = "-waveformsDir", usage = "Directory containing .w file of waveform samples.")
  private String waveformsDir;

  @Option(name = "-hostname", required = true, usage = "Hostname of the service that stores waveforms")
  private String hostname;

  public String getSegmentsDir() {
    return segmentsDir;
  }

  public String getWaveformsDir() { return waveformsDir; }

  public String getHostname() { return hostname; }
}
