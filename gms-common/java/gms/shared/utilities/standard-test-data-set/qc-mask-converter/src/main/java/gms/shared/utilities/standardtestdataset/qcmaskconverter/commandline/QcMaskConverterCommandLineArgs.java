package gms.shared.utilities.standardtestdataset.qcmaskconverter.commandline;

import org.kohsuke.args4j.Option;

public class QcMaskConverterCommandLineArgs {
  @Option(name = "-qcDir", required = true, usage = "Path to Qc Mask directory")
  private String qcDir;

  @Option(name = "-channelsFile", required = true, usage = "Path to ReferenceChannel[] JSON file")
  private String channelsFile;

  @Option(name = "-outputDir", usage = "Output directory for data files from the converter")
  private String outputDir;

  public String getQcDir() {
    return qcDir;
  }

  public String getOutputDir() {
    return outputDir;
  }

  public String getChannelsFile() { return channelsFile; }
}
