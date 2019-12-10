package gms.dataacquisition.css.processingconverter.commandline;

import org.kohsuke.args4j.Option;


public class CssEventAndSignalDetectionConverterArguments {

  @Option(name = "-stationsFile", required = true, usage = "Path to ReferenceStation[] JSON file")
  private String stationsFile;

  @Option(name = "-aridToWfidFile", required = true, usage = "Path to JSON file mapping arid=>wfid")
  private String aridToWfidFile;

  @Option(name = "-event", required = true, usage = "Path to CSS event file")
  private String eventFile;

  @Option(name = "-origin", required = true, usage = "Path to CSS origin file")
  private String originFile;

  @Option(name = "-origerr", required = true, usage = "Path to CSS origerr file")
  private String origerrFile;

  @Option(name = "-assoc", required = true, usage = "Path to CSS assoc file")
  private String assocFile;

  @Option(name = "-arrival", required = true, usage = "Path to CSS arrival file")
  private String arrivalFile;

  @Option(name = "-amplitude", required = true, usage = "Path to CSS amplitude file")
  private String amplitudeFile;

  @Option(name = "-outputDir", usage = "Output directory for data files from the converter")
  private String outputDir;

  public String getStationsFile() { return stationsFile; }

  public String getAridToWfidFile() { return aridToWfidFile; }

  public String getEventFile() {
    return eventFile;
  }

  public String getOriginFile() {
    return originFile;
  }

  public String getOrigerrFile() {
    return origerrFile;
  }

  public String getAssocFile() {
    return assocFile;
  }

  public String getArrivalFile() {
    return arrivalFile;
  }

  public String getAmplitudeFile() {
    return amplitudeFile;
  }

  public String getOutputDir() {
    return outputDir;
  }
}
