package gms.shared.utilities.standardtestdataset.filterdefinitionvalidator.commandline;

import org.kohsuke.args4j.Option;

public class FilterDefinitionValidatorCommandLineArgs {
  @Option(name = "-filterDefFile", required = true, usage = "Path to Filter Definition SMEware file")
  private String filterDefFile;

  public String getFilterDefFile() {
    return filterDefFile;
  }
}
