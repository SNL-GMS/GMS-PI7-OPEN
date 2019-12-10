package gms.shared.utilities.standardtestdataset.beamconverter.commandline;

import org.kohsuke.args4j.Option;

public class BeamConverterCommandLineArgs {

  @Option(name = "-beamDefinitionDir", required = true, usage = "Path to Beam Definition directory")
  private String beamDefinitionDir;

  @Option(name = "-outputDir", usage = "Output directory for data files from the converter")
  private String outputDir;

  public String getOutputDir() {
    return outputDir;
  }

  public String getBeamDefinitionDir() {
    return beamDefinitionDir;
  }

}
