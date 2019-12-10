package gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration;

import org.apache.commons.lang3.Validate;


public class DataframeParserConfig {

  public static final String DEFAULT_MONITORED_DIR_LOCATION = "./shared-volume/dataframes/";
  public static final int DEFAULT_PARSER_THREADS = 10;
  public static final int DEFAULT_MANIFEST_TIME_THRESHOLD_MS = 600000; //10 minutes

  // Define location from the connection properties file.
  public final String monitoredDirLocation;
  public final int parserThreads;
  public final int manifestTimeThresholdMs;

  private DataframeParserConfig(String monitoredDirLocation,
      int parserThreads, int manifestTimeThresholdMs) {
    this.monitoredDirLocation = monitoredDirLocation;
    this.parserThreads = parserThreads;
    this.manifestTimeThresholdMs = manifestTimeThresholdMs;
  }

  /**
   * Obtains an instance of {@link Builder}
   *
   * @return a Builder, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Constructs instances of {@link DataframeParserConfig} following the builder pattern
   */
  public static class Builder {

    private String monitoredDirLocation;
    private int parserThreads;
    private int manifestTimeThresholdMs;

    private Builder() {
      this.monitoredDirLocation = DEFAULT_MONITORED_DIR_LOCATION;
      this.parserThreads = DEFAULT_PARSER_THREADS;
      this.manifestTimeThresholdMs = DEFAULT_MANIFEST_TIME_THRESHOLD_MS;
    }

    /**
     * Construct the {@link DataframeParserConfig}
     *
     * @return Configuration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if manifestTimeThresholdMs is negative
     */
    public DataframeParserConfig build() {
      Validate.isTrue(manifestTimeThresholdMs >= 0);
      return new DataframeParserConfig(monitoredDirLocation,
          parserThreads, manifestTimeThresholdMs);
    }

    /**
     * Set the service's monitoredDirLocation
     *
     * @param monitoredDirLocation location of RawStationDataFrame files
     * @return this {@link Builder}
     */
    public Builder setMonitoredDirLocation(String monitoredDirLocation) {
      this.monitoredDirLocation = monitoredDirLocation;
      return this;
    }

    /**
     * Set the service's parserThreads
     *
     * @param parserThreads the number of parser threads to run in parallel
     * @return this {@link Builder}
     */
    public Builder setParserThreads(int parserThreads) {
      this.parserThreads = parserThreads;
      return this;
    }

    public Builder setManifestTimeThresholdMs(int manifestTimeThresholdMs) {
      this.manifestTimeThresholdMs = manifestTimeThresholdMs;
      return this;
    }
  }
}
