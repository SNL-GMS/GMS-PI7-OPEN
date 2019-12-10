package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import java.util.Set;

public class Constants {

  public static final String RESOURCES_DIR = "src/test/resources/";

  public static final Set<String> testFiles = Set.of(
      "i4DataFrame.json", "s4DataFrame.json",
      "seismic-3c-dataframe.json", "seismic-cc-dataframe.json",
      "manifest.inv");
}
