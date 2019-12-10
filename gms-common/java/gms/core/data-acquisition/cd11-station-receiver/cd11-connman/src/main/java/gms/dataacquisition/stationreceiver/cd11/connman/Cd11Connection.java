package gms.dataacquisition.stationreceiver.cd11.connman;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11SocketConfig;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionRequestFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnectionConfig;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11Connection extends GracefulThread {

  private static final Logger logger = LoggerFactory.getLogger(Cd11Connection.class);

  private final Cd11Socket cd11Socket;
  private final Cd11ConnectionConfig config;
  private final Function<String, Cd11Station> cd11StationLookup;
  private final Consumer<ConnectionLog> connectionLogCallback;

  private final String remoteIpAddress;
  private final int remotePort;

  private AtomicReference<ConnectionLog> connectionLog = new AtomicReference<>();

  /**
   * Constructor.
   *
   * @param config Configuration.
   * @param connectionLogCallback Lambda for returning a connection log to the Connection Manager.
   * @throws NullPointerException if cd11Socket is null
   */
  public Cd11Connection(
      Cd11ConnectionConfig config,
      Socket socket,
      Function<String, Cd11Station> cd11StationLookup,
      Consumer<ConnectionLog> connectionLogCallback)
      throws Exception {

    super(config.threadName, false, false);

    // Validate input parameters.
    Validate.notNull(socket);
    Validate.isTrue(!socket.isClosed(), "Socket is closed.");
    Validate.isTrue(socket.isBound(), "Socket is not bound.");
    Validate.isTrue(socket.isConnected(), "Socket is not connected.");
    Validate.notNull(cd11StationLookup);

    // Store input.
    this.config = Validate.notNull(config);
    this.cd11StationLookup = cd11StationLookup;
    this.connectionLogCallback = connectionLogCallback;

    // Create a CD 1.1 socket object.
    this.cd11Socket = new Cd11Socket(Cd11SocketConfig.builder()
        .setStationOrResponderName(config.responderName)
        .setStationOrResponderType(config.responderType)
        .setServiceType(config.serviceType)
        .setFrameCreator(config.frameCreator)
        .setFrameDestination(config.frameDestination)
        .setAuthenticationKeyIdentifier(7)
        .setProtocolMajorVersion((short) 2)
        .setProtocolMinorVersion((short) 0)
        .build());
    this.cd11Socket.connect(socket);

    // Determine the remote IP address and port.
    this.remoteIpAddress = ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString();

    this.remotePort = socket.getPort();
  }

  /**
   * Runnable method for this object (threaded).
   */
  @Override
  protected void onStart() {
    try {
      // Wait for the next frame to arrive.
      Cd11Frame cd11Frame = this.cd11Socket.read(config.socketReadTimeoutMs);

      // Validate CRC for frame.
      if (!cd11Frame.isValidCRC()) {
        logger.error("CRC check failed for frame!!!");
      }

      // Retrieve the connection request frame.
      Cd11ConnectionRequestFrame connRequestFrame = cd11Frame
          .asFrameType(Cd11ConnectionRequestFrame.class);

      logger.info(String.format(
          "Received connection request for station %s at %s:%d",
          connRequestFrame.stationName, remoteIpAddress, remotePort));

      boolean isValidConnectionRequest = false;

      // Find the station info.
      Cd11Station cd11Station = cd11StationLookup.apply(connRequestFrame.stationName);

      // Check that the station name is known.
      if (cd11Station == null) {
        logger.warn(String.format(
            "Connection request received from unknown station [%s]; ignoring connection.",
            connRequestFrame.stationName));
      }
      // Check that the request originates from the expected IP Address.
      else {
        // TODO: In the future, this should reject the request (keeping for testing purposes).
        if (!remoteIpAddress.equals(cd11Station.expectedDataProviderIpAddress)) {
          logger.warn(String.format(
              "Connection request received from recognized station [%s], " +
                  "but originating from an unexpected IP address (expected %s, received %s).",
              connRequestFrame.stationName,
              cd11Station.expectedDataProviderIpAddress,
              remoteIpAddress));
        }

        logger.info(String.format(
            "Connection Request received from station %s. Redirecting station to %s:%d ",
            connRequestFrame.stationName,
            cd11Station.dataConsumerIpAddress,
            cd11Station.dataConsumerPort));

        // TODO: Check that the remote connection is originating from the expected IP address.

        // Send out the Connection Response Frame.
        cd11Socket.sendCd11ConnectionResponseFrame(
            cd11Station.dataConsumerIpAddress, cd11Station.dataConsumerPort,
            null, null);

        logger.info(String.format(
            "Connection Response Frame sent to station %s.", connRequestFrame.stationName));

        // Indicate the the connection request was valid.
        isValidConnectionRequest = true;
      }

      // Close the socket connection.
      cd11Socket.disconnect();

      // Create the connection log.
      connectionLog.set(new ConnectionLog(
          remoteIpAddress, remotePort,
          connRequestFrame.stationName, isValidConnectionRequest));

      // Send the connection log to the Connection Manager (via the callback).
      if (connectionLogCallback != null) {
        connectionLogCallback.accept(connectionLog.get());
      }

    } catch (InterruptedException e) {
      logger.error("InterruptedException throw in Cd11Connection: ", e);
    } catch (Exception e) {
      logger.error("Error reading or parsing frame: ", e);
    }
  }

  /**
   * Signals the Connection Manager to gracefully shutdown.
   */
  @Override
  public void onStop() {
    cd11Socket.disconnect();
  }

  /**
   * Returns the connection log, or null if one has not been created.
   */
  public ConnectionLog getConnectionLog() {
    return (connectionLog == null) ? null : connectionLog.get();
  }
}
