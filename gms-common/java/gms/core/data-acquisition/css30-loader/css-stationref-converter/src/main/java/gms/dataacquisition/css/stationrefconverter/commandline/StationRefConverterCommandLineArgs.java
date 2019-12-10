package gms.dataacquisition.css.stationrefconverter.commandline;

import org.kohsuke.args4j.Option;

public class StationRefConverterCommandLineArgs {

  @Option(name = "-affiliation", required = true, usage = "Path to CSS affiliation file")
  private String affiliationFile;

  @Option(name = "-instrument", required = true, usage = "Path to CSS instrument file")
  private String instrumentFile;

  @Option(name = "-network", required = true, usage = "Path to CSS network file")
  private String networkFile;

  @Option(name = "-sensor", required = true, usage = "Path to CSS sensor file")
  private String sensorFile;

  @Option(name = "-site", required = true, usage = "Path to CSS site file")
  private String siteFile;

  @Option(name = "-sitechan", required = true, usage = "Path to CSS sitechan file")
  private String siteChanFile;

  @Option(name = "-outputDir", usage = "Output directory for data files from the converter")
  private String outputDir;

  public String getAffiliationFile() {
    return affiliationFile;
  }

  public String getInstrumentFile() {
    return instrumentFile;
  }

  public String getNetworkFile() {
    return networkFile;
  }

  public String getSensorFile() {
    return sensorFile;
  }

  public String getSiteFile() {
    return siteFile;
  }

  public String getSiteChanFile() {
    return siteChanFile;
  }

  public String getOutputDir() { return outputDir; }
}
