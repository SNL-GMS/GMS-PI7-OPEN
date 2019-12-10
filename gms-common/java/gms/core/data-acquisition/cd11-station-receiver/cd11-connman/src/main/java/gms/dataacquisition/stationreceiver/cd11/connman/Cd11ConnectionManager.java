package gms.dataacquisition.stationreceiver.cd11.connman;

import com.google.common.collect.EvictingQueue;
import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11StationConfig;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnectionConfig;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnectionManagerConfig;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11ConnectionManager extends GracefulThread {

  private static Logger logger = LoggerFactory.getLogger(Cd11ConnectionManager.class);

  public final int MAX_CONNECTION_LOGS = 100;
  private final Cd11ConnectionManagerConfig config;

  private ServerSocket serverSocket;
  private ConcurrentMap<String, Cd11Station> cd11StationsLookup;
  private EvictingQueue<ConnectionLog> connectionLogs;

  /**
   * Constructor.
   *
   * @param config Configuration.
   */
  public Cd11ConnectionManager(Cd11ConnectionManagerConfig config) {
    super("CD 1.1 Connection Manager",
        true,
        true);

    this.config = config;
  }

  //-------------------- GracefulThread Methods --------------------

  /**
   * Starts the connection manager.
   */
  @Override
  protected void onStart() {
    // Query the OSD for all registered CD 1.1 stations.
    this.InitializeCd11StationsLookup();

    // Initialize the connection log.
    connectionLogs = EvictingQueue.create(MAX_CONNECTION_LOGS);

    // Create a ServerSocket to listen for Data Provider connection requests.
    try {
      serverSocket = new ServerSocket(config.connectionManagerPort);
    } catch (IOException e) {
      logger.error(
          "Error binding to socket on port " + config.connectionManagerPort, e);
      return;
    }

    logger.info(String.format(
        "Connection Manager is now listening on well-know address %s:%d",
        config.connectionManagerIpAddress, config.connectionManagerPort));

    // Listen for incoming connections.
    while (true) {
      // Check whether we need to shut down.
      if (this.shutThreadDown()) {
        break; // Exit the event loop.
      }

      // Indicate that this GracefulThread is initialized.
      this.setThreadAsInitialized();

      // Listen for the desired connection to arrive.
      Socket socket;
      try {
        socket = serverSocket.accept(); // Blocking call.
        socket.setSoLinger(true, 3);
      } catch (SocketException e) {
        logger.info("ConnMan received a SocketException, shutting down.");
        return;
      } catch (Exception e) {
        logger.error(
            "ConnMan's server socket threw an exception while listening for new connections.", e);
        continue;
      }

      logger.debug(String.format(
          "Connection Manager received TCP connection from %s:%d (passing to handler thread).",
          ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString(),
          socket.getPort()));

      // Pass the socket connection to a Connection Handler.
      try {
        Cd11ConnectionConfig cd11ConnectionConfig = Cd11ConnectionConfig
            .builder()
            .setFrameCreator(config.frameCreator)
            .setFrameDestination(config.frameDestination)
            .setResponderName(config.responderName)
            .setResponderType(config.responderType)
            .setServiceType(config.serviceType)
            .build();
        Cd11Connection cd11Connection = new Cd11Connection(
            cd11ConnectionConfig,
            socket,
            this::lookupCd11Station,
            this::addConnectionLog);
        cd11Connection.start();
      } catch (Exception e) {
        logger.error("Cd11Connection thread could not be started.", e);
      }
    }
  }

  /**
   * Closes the server socket, since it blocks on the accept() method.
   */
  @Override
  public void onStop() {
    // Close the server socket, if it is listening for connections.
    if (serverSocket != null && !serverSocket.isClosed()) {
      try {
        // Close the ServerSocket, which will break the a blocking call to serverSocket.accept().
        serverSocket.close();
      } catch (Exception e) {
        // Do nothing.
      }
    }
  }

  //-------------------- CD 1.1 Station Registration Methods --------------------

  private void InitializeCd11StationsLookup() {
    // Initialize the data structure.
    cd11StationsLookup = new ConcurrentHashMap<>();

    // TODO: In the future, this data should be queried directly from the OSD!
    // NOTE: Use the osdClient object that already exists to query the OSD.
    Cd11StationConfig.fakeStations
        .forEach(netDesc -> this.addCd11Station(
              netDesc.stationName,
              config.dataProviderIpAddress, // TODO: This should be configured on a per station basis (temporary hack).
              config.connectionManagerIpAddress, // TODO: This should be configured on a per station basis (temporary hack).
              netDesc.dataConsumerPort)
        );
  }

  /**
   * Register a new CD 1.1 station.
   *
   * @param stationName Name of the station.
   * @param expectedDataProviderIpAddress Expected IP Address of the Data Provider connecting to this Connection Manager.
   * @param dataConsumerIpAddress IP Address of the Data Consumer to redirect this request to.
   * @param dataConsumerPort Port number of the Data Consumer to redirect this request to.
   */
  public void addCd11Station(
      String stationName,
      String expectedDataProviderIpAddress,
      String dataConsumerIpAddress,
      int dataConsumerPort) {

    // Add a new station to the list, or replace an existing station.
    cd11StationsLookup.put(stationName, new Cd11Station(
        expectedDataProviderIpAddress,
        dataConsumerIpAddress,
        dataConsumerPort));
  }

  /**
   * Remove a registered CD 1.1 Station.
   *
   * @param stationName Remove a CD 1.1 station.
   */
  public void removeCd11Station(String stationName) {
    // Remove the station, if it exists.
    cd11StationsLookup.remove(stationName);
  }

  /**
   * Looks up the CD 1.1 Station, and returns connection information (or null if it does not exist).
   *
   * @param stationName Name of the station.
   * @return Cd11Station info.
   */
  public Cd11Station lookupCd11Station(String stationName) {
    return cd11StationsLookup.getOrDefault(stationName, null);
  }

  /**
   * Returns the total number of registered CD 1.1 stations.
   *
   * @return Total stations.
   */
  public int getTotalCd11Stations() {
    return cd11StationsLookup.size();
  }

  //-------------------- Connection Log Methods --------------------

  private synchronized void addConnectionLog(ConnectionLog connectionLog) {
    this.connectionLogs.add(connectionLog);
  }

  /**
   * Returns the current snapshot of the connection logs.
   *
   * @return Connection logs.
   */
  public synchronized ConnectionLog[] getConnectionLogs() {
    return connectionLogs.toArray(new ConnectionLog[0]);
  }

  /**
   * The total number of connection logs stored up to the current moment.
   *
   * @return Total connections.
   */
  public synchronized int getTotalConnectionLogs() {
    return connectionLogs.size();
  }

  /**
   * The total number of valid connections that currently exist in the connection logs.
   *
   * @return Total connections.
   */
  public synchronized long getTotalValidConnectionLogs() {
    return connectionLogs.stream()
        .filter(x -> x.isValidConnectionRequest)
        .count();
  }

  /**
   * The total number of invalid connections that currently exist in the connection logs.
   *
   * @return Total connections.
   */
  public synchronized long getTotalInvalidConnectionLogs() {
    return connectionLogs.stream()
        .filter(x -> !x.isValidConnectionRequest)
        .count();
  }
}
