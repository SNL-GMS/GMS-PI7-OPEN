package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11SocketConfig;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AcknackFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.Cd11DataProviderConfig;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Connects to a CD 1.1 data consumer on a well known port, then switches to a data port and
 * transmits data.
 */
public class Cd11DataProvider extends GracefulThread {

  private static Logger logger = LoggerFactory.getLogger(Cd11DataProvider.class);

  private final Cd11DataProviderConfig config;
  private final String expectedFrameSetAcked;

  private final Cd11Socket cd11Socket;
  private final Message SHUTDOWN_EVENT = new Message(MessageType.Shutdown);
  private final ConcurrentLinkedDeque<ImmutablePair<Long, Long>> currentDataConsumerGaps =
      new ConcurrentLinkedDeque<>();

  // TODO: In the future, this needs to be increased over time.
  private final AtomicLong lowestSequenceNumber = new AtomicLong(1);

  // TODO: In the future, this should be tied to a data frame.
  private final AtomicLong nextDataFrameSequenceNumber = new AtomicLong(1);

  private AtomicBoolean sendGapDataFlag = new AtomicBoolean(false);

  private File rawDataFrameFile = null;

  // Event queue.
  private final BlockingQueue<Message> eventQueue = new LinkedBlockingQueue<>();

  // Event generators.
  private final Cd11DataProviderConnectionExpiredThread connectionExpiredEvent;
  private final Cd11DataProviderNewFrameReceivedThread newFrameReceivedEvent;
  private final Cd11DataProviderSendAcknackThread sendAcknackEvent;
  private final Cd11DataProviderSendDataThread sendDataEvent;

  // Statistics and state information.
  private final AtomicReference<String> dataConsumerIpAddress = new AtomicReference<>(null);
  private final AtomicInteger dataConsumerPort = new AtomicInteger(-1);

  private static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  //-------------------- Constructors --------------------

  /**
   * Constructor.
   *
   * @param config Configuration data required to run the application.
   */
  public Cd11DataProvider(Cd11DataProviderConfig config) {
    this(config, Cd11DataProvider.class.getName());
  }

  /**
   * Sets up a socket connection to the data consumer.
   *
   * dataProviderIpAddress          IP address on the local machine to bind to. dataProviderPort
   *           Port number on the local machine to bind to. dataConsumerWellKnownIpAddress Well
   * Known IP Address of the remote data consumer. dataConsumerWellKnownPort      Well Known port
   * number of the remote data consumer. protocolMajorVersion           Major version number of this
   * protocol (CD 1.1 Connection Request/Response Frames). protocolMinorVersion           Minor
   * version number of this protocol (CD 1.1 Connection Request/Response Frames). stationName
   *             Name of the station that this machine represents (CD 1.1 Connection
   * Request/Response Frames). stationType                    IDC, IMS, etc. (CD 1.1 Connection
   * Request/Response Frames). serviceType                    TCP or UDP (CD 1.1 Connection
   * Request/Response Frames). frameCreator                   Identifier of the creator of the frame
   * (CD 1.1 Header Frame). frameDestination               Identifier of the destination fo the
   * frame (CD 1.1 Header Frame). authenticationKeyIdentifier    Identifier for public key used for
   * authentication (CD 1.1 Footer Frame).
   *
   * @param config Configuration data required to run the application.
   */
  public Cd11DataProvider(Cd11DataProviderConfig config, String threadName) {
    super(threadName, true, true);

    // Check whether we are expected to use UDP (which is not yet implemented).
    if (config.serviceType.equals("UDP")) {
      throw new IllegalStateException("UDP is not yet implemented.");
    }

    // Initialize properties.
    this.config = config;
    this.expectedFrameSetAcked = config.frameCreator + ":" + config.frameDestination;

    // Create a CD 1.1 client.
    this.cd11Socket = new Cd11Socket(Cd11SocketConfig.builder()
        .setStationOrResponderName(config.stationName)
        .setStationOrResponderType(config.stationType)
        .setServiceType(config.serviceType)
        .setFrameCreator(config.frameCreator)
        .setFrameDestination(config.frameDestination)
        .setAuthenticationKeyIdentifier(config.authenticationKeyIdentifier)
        .setProtocolMajorVersion(config.protocolMajorVersion)
        .setProtocolMinorVersion(config.protocolMinorVersion)
        .build());

    // Initialize the event generators.
    newFrameReceivedEvent = new Cd11DataProviderNewFrameReceivedThread(
        "DataFrameReceivedEvent", eventQueue, cd11Socket);
    sendAcknackEvent = new Cd11DataProviderSendAcknackThread(
        "SendAcknackEvent", eventQueue, cd11Socket);
    connectionExpiredEvent = new Cd11DataProviderConnectionExpiredThread(
        "ConnectionExpiredEvent", eventQueue, cd11Socket,
        config.connectionExpiredTimeLimitSec);
    sendDataEvent = new Cd11DataProviderSendDataThread(
        "sendDataEvent", eventQueue, cd11Socket,
        config.dataFrameSendingIntervalMs);
  }

  //-------------------- Graceful Thread Methods --------------------

  @Override
  protected boolean onBeforeStart() {
    dataConsumerIpAddress.set(null);
    dataConsumerPort.set(-1);
    return true;
  }

  @Override
  protected void onStart() throws Exception {
    // Connect to the "Well-Known" address, and retrieve the Data Consumer's address.
    logger.info(String.format(
        "Connecting to the Connection Manager on %1$s:%2$d.",
        config.connectionManagerIpAddress,
        config.connectionManagerPort));

    // Connect to the Connection Manager.
    cd11Socket.connect(
        config.connectionManagerIpAddress, config.connectionManagerPort,
        config.maxSocketConnectWaitTimeMs,
        config.localIpAddress, config.localPort);

    // Check if the thread needs to shut down.
    if (this.shutThreadDown()) {
      shutdownGracefully();
      return; // Exit the thread.
    }

    logger.debug("Sending a CD 1.1 Connection Request frame to the Connection Manager.");

    // Generate and send a CD 1.1 Connection Request frame.
    cd11Socket.sendCd11ConnectionRequestFrame();

    logger.debug("Waiting for a CD 1.1 Connection Response frame from the Connection Manager.");

    // Read the CD 1.1 Frame returned by the Connection Manager.
    Cd11Frame connManResponseFrame = cd11Socket.read(30000); // 30 seconds

    logger.debug("Connection Response frame received from the Connection Manager.");

    // Check that we received a Connection Response frame.
    Cd11ConnectionResponseFrame responseFrame =
        connManResponseFrame.asFrameType(Cd11ConnectionResponseFrame.class);

    // Retrieve the IP and Port of the Data Consumer.
    dataConsumerIpAddress.set(InetAddresses.fromInteger(responseFrame.ipAddress).getHostAddress());
    dataConsumerPort.set(responseFrame.port);

    logger.info(String.format(
        "Connecting to the Data Consumer at %1$s:%2$s.", dataConsumerIpAddress, dataConsumerPort));

    // Check if the thread needs to shut down.
    if (this.shutThreadDown()) {
      shutdownGracefully();
      return; // Exit the thread.
    }

    // Connect to the Data Consumer.
    cd11Socket.connect(
        dataConsumerIpAddress.get(), dataConsumerPort.get(),
        config.maxSocketConnectWaitTimeMs,
        config.localIpAddress, config.localPort);

    // Check if the thread needs to shut down.
    if (this.shutThreadDown()) {
      shutdownGracefully();
      return; // Exit the thread.
    }

    // Start up a thread to listen for incoming data frames.
    newFrameReceivedEvent.start();

    // Start up a thread that triggers an event when it is time to send an Acknack frame.
    sendAcknackEvent.start();

    // Start up a thread that triggers an event when the connection has expired due to lack of contact.
    connectionExpiredEvent.start();

    // Start up a thread that triggers an event when it is time to send the next data frame.
    sendDataEvent.start();

    // Wait until all of the threads have started.
    newFrameReceivedEvent.waitUntilThreadInitializes();
    sendAcknackEvent.waitUntilThreadInitializes();
    connectionExpiredEvent.waitUntilThreadInitializes();
    sendDataEvent.waitUntilThreadInitializes();

    // Indicate that this GracefulThread is initialized.
    this.setThreadAsInitialized();

    // Enter the event loop.
    while (this.keepThreadRunning()) {

      // Check that all event threads are running.
      if (!eventQueue.contains(SHUTDOWN_EVENT) && (
          !connectionExpiredEvent.isRunning() ||
              !newFrameReceivedEvent.isRunning() ||
              !sendAcknackEvent.isRunning() ||
              !sendDataEvent.isRunning())) {
        logger.error(
            "One or more event threads shut down unexpectedly, shutting down the Data Provider.");
        this.shutdownGracefully();
        return; // Exit the thread.
      }

      // Read the next message from the event queue.
      Message mt;
      try {
        mt = eventQueue.take(); // NOTE: This is a blocking call!
      } catch (InterruptedException e) {
        logger.debug(String.format(
            "InterruptedException thrown in thread %1$s, closing thread.", this.getThreadName()),
            e);
        this.shutdownGracefully();
        return; // Exit the thread.
      }

      // Process the queue message.
      switch (mt.messageType) {

        case NewFrameReceived:
          this.processNewFrame(mt.cd11Frame);
          break;

        case SendAcknack:
          this.sendAcknack(cd11Socket);
          break;

        case SendData:
          // Check whether we want to send data from a file (for testing only).
          if (config.cannedFramePath == null) {
            // TODO: Replace fake frames and sequence numbers with generated frames (or real data).
            if (sendGapDataFlag.get() && !currentDataConsumerGaps.isEmpty()) {

              // Retrieve the next gap.
              long gapSequenceNumber = retrieveNextGapDataFrame();

              // Send gap data.
              cd11Socket.sendCd11DataFrame(
                  FakeDataFrame.generateFakeChannelSubframes(), gapSequenceNumber);

              // Next time, send latest data.
              sendGapDataFlag.set(false);
            } else {
              // Send latest data.
              cd11Socket.sendCd11DataFrame(
                  FakeDataFrame.generateFakeChannelSubframes(), nextDataFrameSequenceNumber.get());

              // Next time, send gap data.
              sendGapDataFlag.set(true);
            }

            // Increment the next data frame sequence number.
            nextDataFrameSequenceNumber.incrementAndGet();
          } else {
            // TODO: For testing only.
            sendRawDataFrame(cd11Socket);
          }
          break;

        case Shutdown:
          this.shutdownGracefully();
          return; // Exit the thread.

        default:
          String errMsg = "Invalid MessageType received (this should never occur).";
          logger.error(errMsg);
          throw new IllegalStateException(errMsg);
      }
    }

    // Shut down this thread.
    shutdownGracefully();
  }

  /**
   * Runs when GracefulThread catches an unhandled exception. (NOTE: This should never occur in
   * practice, since we are taking care to catch all exceptions.)
   *
   * @param thread Thread that threw the uncaught exception.
   * @param throwable Exception object.
   */
  @Override
  protected void onUncaughtException(Thread thread, Throwable throwable) {
    // Gracefully shut down the thread.
    try {
      this.shutdownGracefully();
    } catch (Exception ex) {
    }
  }

  //-------------------- Private Methods --------------------

  /**
   * Processes a newly arrived CD 1.1 frame.
   */
  private void processNewFrame(Cd11Frame cd11Frame) {
    switch (cd11Frame.frameType) {

      case ACKNACK:
        // Deserialize the Acknack frame.
        Cd11AcknackFrame acknackFrame = cd11Frame.asFrameType(Cd11AcknackFrame.class);

        // Check if the "frame set acked" field is valid.
        if (!acknackFrame.framesetAcked.equals(expectedFrameSetAcked)) {
          logger.error(String.format(
              "Invalid 'frame set acked' value found in an Acknack frame received " +
                  "from the Data Consumer (expected [%s], received [%s]). Ignoring the frame!",
              expectedFrameSetAcked, acknackFrame.framesetAcked));
        } else {
          // Load the latest Data Consumer gaps.
          this.loadDataConsumerGaps(acknackFrame);
        }

        break;

      case ALERT:
        try {
          eventQueue.put(new Message(MessageType.Shutdown));
        } catch (Exception e) {
          this.stop();
        }
        break;

      case CD_ONE_ENCAPSULATION:
        // TODO: Handle CD 1 Encapsulation frames.
        logger.error("Received CD_ONE_ENCAPSULATION frame, which is not yet supported!");
        break;

      case COMMAND_REQUEST:
        logger.info("Received COMMAND_REQUEST frame, which is not supported; ignoring frame.");
        break;

      case COMMAND_RESPONSE:
        logger.info("Received COMMAND_RESPONSE frame, which is not supported; ignoring frame.");
        break;

      case CONNECTION_REQUEST:
        logger.info(
            "Received CONNECTION_REQUEST frame, which should never have been sent by the Data Consumer! Ignoring the frame.");
        break;

      case CONNECTION_RESPONSE:
        logger.error(
            "Received CONNECTION_RESPONSE frame, which should never have been sent by the Data Consumer! Ignoring the frame.");
        break;

      case DATA:
        logger.warn(
            "Received a DATA frame, which should never have been sent by the Data Consumer! Ignoring the frame! Ignoring the frame.");
        break;

      case OPTION_REQUEST:
        logger.error("Received OPTION_REQUEST frame, which is not supported! Ignoring the frame.");
        break;

      case OPTION_RESPONSE:
        logger.error("Received OPTION_RESPONSE frame, which is not supported! Ignoring the frame.");
        break;

      default:
        logger.error("Invalid CD 1.1 frame messageType received (this should never occur).");
        throw new IllegalStateException(
            "Invalid CD 1.1 frame messageType received (this should never occur).");
    }
  }

  private synchronized void loadDataConsumerGaps(Cd11AcknackFrame acknackFrame) {
    // Clear the queue.
    this.currentDataConsumerGaps.clear();

    // Add missing gaps to the queue.
    for (int i = 0; i + 1 < acknackFrame.gapRanges.length; i += 2) {
      // Skip sequences that represent invalid ranges, or that fall outside of the current range.
      if (Long.compareUnsigned(acknackFrame.gapRanges[i], acknackFrame.gapRanges[i+1]) >= 0 ||
          Long.compareUnsigned(acknackFrame.gapRanges[i+1], lowestSequenceNumber.get()) < 0 ||
          Long.compareUnsigned(acknackFrame.gapRanges[i], nextDataFrameSequenceNumber.get()) >= 0) {
        continue;
      }

      // Check if a gap spans a sequence range boundary.
      if (Long.compareUnsigned(acknackFrame.gapRanges[i], lowestSequenceNumber.get()) < 0 &&
          Long.compareUnsigned(acknackFrame.gapRanges[i+1], lowestSequenceNumber.get()) > 0) {
        // Modify the lower end of the gap.
        currentDataConsumerGaps.offerLast(new ImmutablePair<>(
            lowestSequenceNumber.get(), acknackFrame.gapRanges[i+1]));
      } else if (Long.compareUnsigned(acknackFrame.gapRanges[i], nextDataFrameSequenceNumber.get()) < 0 &&
          Long.compareUnsigned(acknackFrame.gapRanges[i+1], nextDataFrameSequenceNumber.get()) >= 0) {
        // Modify the higher end of the gap.
        currentDataConsumerGaps.offerLast(new ImmutablePair<>(
            acknackFrame.gapRanges[i], nextDataFrameSequenceNumber.get() - 1));
      } else {
        // Add the gap as is.
        currentDataConsumerGaps.offerLast(new ImmutablePair<>(
            acknackFrame.gapRanges[i], acknackFrame.gapRanges[i+1]));
      }
    }
  }

  private synchronized long retrieveNextGapDataFrame() {
    ImmutablePair<Long, Long> gapRange = currentDataConsumerGaps.removeFirst();
    Long nextSequenceNumber = gapRange.getLeft();

    // Check if this gap range contains more than one gap.
    if (Long.compareUnsigned((gapRange.getLeft() + 1), gapRange.getRight()) < 0) {
      currentDataConsumerGaps.offerFirst(new ImmutablePair<>(
          gapRange.getLeft()+1, gapRange.getRight()));
    }

    return nextSequenceNumber;
  }

  /**
   * Sends an acknack frame.
   *
   * @param cd11Socket CD 1.1 Socket object.
   */
  private void sendAcknack(Cd11Socket cd11Socket) throws Exception {
    //TODO: Haven't implemented gaps yet so we are sending an empty byte array.
    //TODO: Haven't implemented sequence numbers yet so it is hard-coded in Cd11Socket.

    cd11Socket.sendCd11AcknackFrame(
        cd11Socket.getFrameCreator(),
        1,
        nextDataFrameSequenceNumber.get() - 1,
        new long[0]);
  }

  private void sendRawDataFrame(Cd11Socket cd11Socket) throws Exception {
    // TODO: This method is called for testing only!!!

    if (rawDataFrameFile == null) {
      rawDataFrameFile = new File(config.cannedFramePath);
    }
    String contents = new String(Files.readAllBytes(rawDataFrameFile.toPath()));

    RawStationDataFrame frame = objMapper.readValue(contents, RawStationDataFrame.class);
    cd11Socket.send(frame.getRawPayload());
  }

  /**
   * Shuts down all event threads, and closes the CD 1.1 Socket connection.
   */
  private void shutdownGracefully() {
    // Signal that this thread needs to stop running.
    this.stop();

    // If connected to a Data Consumer, attempt to send a CD 1.1 Alert frame.
    try {
      if (cd11Socket.isConnected() &&
          (
              !cd11Socket.getRemoteIpAddressAsString().equals(config.connectionManagerIpAddress) ||
                  cd11Socket.getRemotePort() != (int) config.localPort
          )) {
        cd11Socket.sendCd11AlertFrame("Shutting down.");
      }
    } catch (Exception e) {
      // Do nothing.
    }

    // Stop all running threads.
    connectionExpiredEvent.stop();
    newFrameReceivedEvent.stop();
    sendAcknackEvent.stop();
    sendDataEvent.stop();
    connectionExpiredEvent.waitUntilThreadStops();
    newFrameReceivedEvent.waitUntilThreadStops();
    sendAcknackEvent.waitUntilThreadStops();
    sendDataEvent.waitUntilThreadStops();

    // Collect all "last error messages" written by this thread, and all of its event threads.
    this.setErrorMessage(GracefulThread.aggregateErrorMessages(
        this, connectionExpiredEvent, newFrameReceivedEvent,
        sendAcknackEvent, sendDataEvent));

    // Disconnect the CD 1.1 Socket.
    cd11Socket.disconnect();
  }

  //-------------------- Statistics and State Info Methods --------------------

  /**
   * Returns the IP Address of the Data Consumer that this Data Provider is connected to, or null if
   * it has not yet received this information from the Connection Manager.
   *
   * @return IP Address, or null if not yet known.
   */
  public String getDataConsumerIpAddress() {
    return dataConsumerIpAddress.get();
  }

  /**
   * Returns the Port Number of the Data Consumer that this Data Provider is connected to, or null
   * if it has not yet received this information from the Connection Manager.
   *
   * @return IP Address, or null if not yet known.
   */
  public Integer getDataConsumerPort() {
    int port = dataConsumerPort.get();
    return (port < 0) ? null : port;
  }
}
