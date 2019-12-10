package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.configuration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraConfig {

  private static final Logger logger = LoggerFactory.getLogger(CassandraConfig.class);

  private static final String DEFAULT_CONNECT_POINTS = "cassandra";
  private static final int DEFAULT_PORT = 9042;
  private static final String DEFAULT_USER = "gms";
  private static final String DEFAULT_PASS = "gmsdb:gms@cassandra=element-act-mist";
  private static final String DEFAULT_CLUSTER_NAME = "GMS";
  private static final String DEFAULT_KEYSPACE = "gms_timeseries_data";
  private static final String DEFAULT_WAVEFORMS_TABLE = "waveforms";
  private static final String DEFAULT_FKSPECTRA_TABLE = "fk_spectra";

  public final String connectPoints;
  public final int port;
  public final String user;
  public final String pass;
  public final String clusterName;
  public final String timeseriesKeySpace;
  public final String waveformTable;
  public final String fkSpectraTable;
  private Cluster cluster;

  public CassandraConfig(String connectPoints, int port, String user, String pass,
      String clusterName, String timeseriesKeySpace,
      String waveformTable, String fkSpectraTable) {
    this.connectPoints = Objects.requireNonNull(connectPoints);
    this.port = port;
    this.user = Objects.requireNonNull(user);
    this.pass = Objects.requireNonNull(pass);
    this.clusterName = Objects.requireNonNull(clusterName);
    this.timeseriesKeySpace = Objects.requireNonNull(timeseriesKeySpace);
    this.waveformTable = Objects.requireNonNull(waveformTable);
    this.fkSpectraTable = Objects.requireNonNull(fkSpectraTable);
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
   * Constructs instances of {@link CassandraConfig} following the builder pattern
   */
  public static class Builder {

    private String connectPoints;
    private int port;
    private String user;
    private String pass;
    private String clusterName;
    private String timeseriesKeySpace;
    private String waveformTable;
    private String fkSpectraTable;

    private Builder() {
      this.connectPoints = DEFAULT_CONNECT_POINTS;
      this.port = DEFAULT_PORT;
      this.user = DEFAULT_USER;
      this.pass = DEFAULT_PASS;
      this.clusterName = DEFAULT_CLUSTER_NAME;
      this.timeseriesKeySpace = DEFAULT_KEYSPACE;
      this.waveformTable = DEFAULT_WAVEFORMS_TABLE;
      this.fkSpectraTable = DEFAULT_FKSPECTRA_TABLE;
    }

    /**
     * Construct the {@link CassandraConfig}
     *
     * @return Configuration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if osdGatewayPort is negative; if osdGatewayPort is beyond
     * the valid range
     */
    public CassandraConfig build() {
      return new CassandraConfig(connectPoints, port, user, pass,
          clusterName, timeseriesKeySpace, waveformTable, fkSpectraTable);
    }

    public Builder setConnectPoints(String connectPoints) {
      this.connectPoints = Objects.requireNonNull(connectPoints);
      return this;
    }

    public Builder setPort(int port) {
      this.port = port;
      return this;
    }

    public Builder setUser(String user) {
      this.user = Objects.requireNonNull(user);
      return this;
    }

    public Builder setPass(String pass) {
      this.pass = Objects.requireNonNull(pass);
      return this;
    }

    public Builder setClusterName(String clusterName) {
      this.clusterName = Objects.requireNonNull(clusterName);
      return this;
    }

    public Builder setTimeseriesKeySpace(String timeseriesKeySpace) {
      this.timeseriesKeySpace = Objects.requireNonNull(timeseriesKeySpace);
      return this;
    }

    public Builder setWaveformTable(String waveformTable) {
      this.waveformTable = Objects.requireNonNull(waveformTable);
      return this;
    }

    public Builder setFkSpectraTable(String fkSpectraTable) {
      this.fkSpectraTable = Objects.requireNonNull(fkSpectraTable);
      return this;
    }
  }

  /**
   * Get a session for Cassandra.
   *
   * @return A Session object.
   * @throws StorageUnavailableException if there was a failure to establish a connection to Cassandra
   */
  public Session getConnection() {
    if (this.cluster == null || this.cluster.isClosed()) {
      this.cluster = Cluster.builder().addContactPoints(this.connectPoints)
          .withPort(this.port)
          .withCredentials(this.user, this.pass)
          .withAuthProvider(new PlainTextAuthProvider(this.user, this.pass))
          .build();
    }
    logger.info("Created Cassandra Cluster for {}, keyspace: {}, user: {}", this.clusterName,
        this.timeseriesKeySpace, this.user);

    try {
      return this.cluster.connect();
    } catch (RuntimeException e) {
      logger.error("Error connecting to cluster", e);
      throw new StorageUnavailableException(e);
    }
  }
}
