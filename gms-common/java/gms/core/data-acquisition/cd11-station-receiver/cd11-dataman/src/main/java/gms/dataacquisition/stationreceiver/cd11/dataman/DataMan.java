package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11StationConfig;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.Cd11DataConsumerConfig;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.DataManConfig;
import gms.dataacquisition.stationreceiver.osdclient.StationReceiverOsdClientAccessLibrary;
import gms.dataacquisition.stationreceiver.osdclient.StationReceiverOsdClientInterface;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is designed to start all data communications. It reads the stations.properties file,
 * and spins off a new Cd11DataConsumer thread to talk to the station.
 */
public class DataMan extends GracefulThread {

  private static Logger logger = LoggerFactory.getLogger(DataMan.class);

  private final DataManConfig config;
  private final StationReceiverOsdClientInterface osdClient;
  private final boolean useSameClientForConsumers;
  private final ConcurrentHashMap<Integer, Cd11DataConsumer> dataConsumerThreads;

  /**
   * Constructor.
   *
   * @param config Configuration object.
   */
  public DataMan(DataManConfig config) {
    this(config, new StationReceiverOsdClientAccessLibrary(config.fsOutputDirectory),
        false);
  }

  /**
   * Constructor.
   *
   * @param config Configuration object.
   */
  public DataMan(DataManConfig config, boolean useSameClientForConsumers) {
    this(config, new StationReceiverOsdClientAccessLibrary(config.fsOutputDirectory),
        useSameClientForConsumers);
  }

  /**
   * Alternate constructor meant for testing only!
   *
   * @param config Configuration object.
   */
  public DataMan(
      DataManConfig config, StationReceiverOsdClientInterface osdClient) {
    this(config, osdClient, false);
  }

  /**
   * Alternate constructor meant for testing only!
   *
   * @param config Configuration object.
   */
  public DataMan(
      DataManConfig config, StationReceiverOsdClientInterface osdClient,
      boolean useSameClientForConsumers) {
    super("CD 1.1 Data Consumer Manager",
        true,
        true);

    this.config = Objects.requireNonNull(config);
    this.osdClient = Objects.requireNonNull(osdClient);
    this.useSameClientForConsumers = useSameClientForConsumers;

    // Create a map to store Data Consumer threads.
    this.dataConsumerThreads = new ConcurrentHashMap<>();
  }

  /**
   * Starts the Data Consumer Manager.
   */
  @Override
  protected void onStart() {
    // Query OSD for list of Data Consumers that need to be spawned.
    List<Cd11DataConsumerConfig> osdListOfDataConsumers = getDataConsumerConfigsFromOsd();

    // Register each Data Consumer.
    for (Cd11DataConsumerConfig dcConfig : osdListOfDataConsumers) {
      this.addDataConsumer(dcConfig);
    }

    // Indicate that this GracefulThread is initialized.
    this.setThreadAsInitialized();

    // Periodically check the status of each thread.
    while (this.keepThreadRunning()) {
      dataConsumerThreads.values().parallelStream().forEach(dcThread -> {
        //for (Cd11DataConsumer dcThread : dataConsumerThreads.values()) {

        // Check that the thread is still running.
        if (!dcThread.isRunning()) {
          logger.warn("Restarting data consumer thread: " + dcThread.getThreadName());

          // Check for an error message.
          if (dcThread.hasErrorMessage()) {
            // Log the error message.
            logger.error(String.format(
                "Data Consumer thread running on port %d shutdown in error: %s",
                dcThread.getCd11ListeningPort(),
                dcThread.getErrorMessage()));
          }

          // Restart the thread.
          try {
            dcThread.start();
          } catch (Exception e) {
            logger.error("Data Consumer thread failed to start.", e);
          }
        }
      });

      // Sleep for a period of time.
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        logger.info(
            "Data Consumer Manager received an InterruptedException, shutting down gracefully.");
        break;
      }
    }

    // Shut down all Data Consumer threads.
    for (Cd11DataConsumer dcThread : dataConsumerThreads.values()) {
      dcThread.onStop();
    }
  }

  private List<Cd11DataConsumerConfig> getDataConsumerConfigsFromOsd() {
    // TODO: In the future, this data should be queried directly from the OSD!
    return Cd11StationConfig.fakeStations.stream().map(netDesc -> {
          Optional<UUID> staIdOptional = Optional.empty();
          try {
            staIdOptional = osdClient.getStationId(netDesc.stationName);
          } catch(Exception ex) {
            logger.error(String.format(
                "Could not find OSD Station ID by name [%s]; using fake value instead.",
                netDesc.stationName), ex);
          }
          UUID staId = staIdOptional.orElseGet(() -> netDesc.osdStationId);

          return Cd11DataConsumerConfig
              .builder(netDesc.dataConsumerPort, staId, netDesc.stationName)
              .setThreadName(
                  String.format("CD 1.1 Data Consumer (Port: %d)", netDesc.dataConsumerPort))
              .setExpectedDataProviderIpAddress(config.expectedDataProviderIpAddress)
              .setDataConsumerIpAddress(config.dataConsumerIpAddress)
              .setFsOutputDirectory(this.config.fsOutputDirectory)
              .build();
        })
        .collect(Collectors.toList());
  }

  //-------------------- Statistics and State Info Methods --------------------

  /**
   * Returns true if a Data Consumer is registered on the given port.
   *
   * @param port Port number.
   * @return True if a data consumer is registered on the given port, false otherwise.
   */
  public boolean isDataConsumerPortRegistered(int port) {
    return dataConsumerThreads.containsKey(port);
  }

  public long getTotalDataFramesReceived(int port) {
    return dataConsumerThreads.get(port).getTotalDataFramesReceived();
  }

  /**
   * Adds a new Data Consumer thread.
   *
   * @param dcConfig Data Consumer configuration.
   */
  public void addDataConsumer(Cd11DataConsumerConfig dcConfig) {
    // Check whether a Data Consumer has already been assigned to this port.
    if (dataConsumerThreads.containsKey(dcConfig.dataConsumerPort)) {
      throw new IllegalArgumentException(String.format(
          "A Data Consumer is already running on port %d.", dcConfig.dataConsumerPort));
    }

    // Create the data consumer thread.
    Cd11DataConsumer dcThread = useSameClientForConsumers ?
        new Cd11DataConsumer(dcConfig, osdClient)
        : new Cd11DataConsumer(dcConfig);

    // Track the thread.
    dataConsumerThreads.put(dcConfig.dataConsumerPort, dcThread);
  }

  /**
   * Removes an active Data Consumer thread.
   *
   * @param port Local port number that the Data Consumer is running on.
   */
  public void removeDataConsumer(int port) {
    Cd11DataConsumer cd11DataConsumer = dataConsumerThreads.remove(port);

    if (cd11DataConsumer == null) {
      throw new IllegalArgumentException(String.format(
          "Data Consumer on port %d does not exist.", port));
    }

    // Stop the data consumer.
    cd11DataConsumer.stop();
    cd11DataConsumer.waitUntilThreadStops();
  }

  /**
   * Returns the total number of CD 1.1 Data Consumer threads that are registered.
   *
   * @return Number of registered Data Consumer threads.
   */
  public int getTotalDataConsumerThreads() {
    return dataConsumerThreads.size();
  }

  /**
   * Returns the set of port numbers used by the CD 1.1 Data Consumer.
   *
   * @return List of port numbers in use.
   */
  public Set<Integer> getPorts() {
    return dataConsumerThreads.keySet();
  }
}
