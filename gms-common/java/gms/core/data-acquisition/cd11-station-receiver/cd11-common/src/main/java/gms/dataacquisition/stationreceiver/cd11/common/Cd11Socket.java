package gms.dataacquisition.stationreceiver.cd11.common;

import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11SocketConfig;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AcknackFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AlertFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ByteFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandRequestFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionRequestFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11DataFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11FrameHeader;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11FrameTrailer;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11OptionRequestFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11OptionResponseFrame;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class Cd11Socket {

  private static Logger logger = LoggerFactory.getLogger(Cd11Socket.class);

  private Socket socket = null;
  private DataInputStream socketIn = null;
  private DataOutputStream socketOut = null;
  private AtomicLong lastContactTimeNs = new AtomicLong(0);
  private AtomicLong lastAcknackSentTimeNs = new AtomicLong(0);
  private AtomicLong lastDataSentTimeNs = new AtomicLong(0);

  private final Cd11SocketConfig config;

  private final Object READ_LOCK = new Object();
  private final Object WRITE_LOCK = new Object();

  private String framesetAcked;

  /**
   * CD 1.1 client and server base class.
   *
   * @param config Configuration object.
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11Socket(Cd11SocketConfig config) throws IllegalArgumentException {

    Validate.notNull(config);

    // Initialize properties and validate input.
    this.config = config;
    this.framesetAcked = config.frameCreator + ":" + config.frameDestination;
  }

  public String getRemoteIpAddressAsString() {
    // Use InetSocketAddress.getHostString since this.socket.getInetAddress().getHostAddress()
    // was flagged by Fortify
    return new InetSocketAddress(this.socket.getInetAddress(), this.socket.getPort())
        .getHostString();
  }

  public String getLocalIpAddressAsString() {
    // Use InetSocketAddress.getHostString since this.socket.getInetAddress().getHostAddress()
    // was flagged by Fortify
    return new InetSocketAddress(this.socket.getLocalAddress(), this.socket.getLocalPort())
        .getHostString();
  }

  public int getRemotePort() {
    return this.socket.getPort();
  }

  //---------- Sockets and Networking Methods ----------

  /**
   * Returns true if the socket is connected to the Data Consumer, false otherwise.
   *
   * @return True if socket is connected to the Data Consumer.
   */
  public boolean isConnected() {
    // Check that the socket is fully connected.
    return (
        this.socket != null &&
            this.socketIn != null &&
            this.socketOut != null &&
            !this.socket.isClosed() &&
            this.socket.isBound() &&
            this.socket.isConnected() &&
            !this.socket.isInputShutdown() &&
            !this.socket.isOutputShutdown());
  }

  /**
   * Returns true if data has been received from the Data Consumer, and is ready to be read.
   *
   * @return True if data is ready to be read from the socket.
   */
  public boolean isNextFrameReadyToRead() throws IllegalStateException, IOException {
    // Check that the socket is ready to use.
    if (!this.isConnected()) {
      throw new IllegalStateException("Socket connection is not open.");
    }

    return (this.socketIn.available() > Cd11FrameHeader.FRAME_LENGTH);
  }

  /**
   * Establishes a socket connection to the Data Consumer, and initializes its I/O streams.
   *
   * @param dataConsumerWellKnownIpAddress Data Consumer's "Well Known" IP Address.
   * @param dataConsumerWellKnownPort Data Consumer's "Well Known" port number.
   * @param maxWaitTimeMs Continuously attempt to connect until this limit is exceeded.
   *
   * - Less than 0: Wait forever.
   *
   * - Equal to 0: Only try to connect once.
   *
   * - Greater than 0: Wait for the specified period of time.
   * @throws Exception Thrown on socket, I/O stream, or network connection errors.
   */
  public void connect(
      String dataConsumerWellKnownIpAddress, int dataConsumerWellKnownPort,
      long maxWaitTimeMs) throws Exception {
    this.connect(
        dataConsumerWellKnownIpAddress, dataConsumerWellKnownPort,
        maxWaitTimeMs, null, 0);
  }

  /**
   * Establishes a socket connection to the Data Consumer, and initializes its I/O streams.
   *
   * @param dataConsumerWellKnownIpAddress Data Consumer's "Well Known" IP Address.
   * @param dataConsumerWellKnownPort Data Consumer's "Well Known" port number.
   * @param maxWaitTimeMs Continuously attempt to connect until this limit is exceeded.
   *
   * - Less than 0: Wait forever.
   *
   * - Equal to 0: Only try to connect once.
   *
   * - Greater than 0: Wait for the specified period of time.
   * @param localPort Local port to bind to (if 0, use ephemeral port).
   * @throws Exception Thrown on socket, I/O stream, or network connection errors.
   */
  public void connect(
      String dataConsumerWellKnownIpAddress, int dataConsumerWellKnownPort,
      long maxWaitTimeMs, int localPort) throws Exception {
    this.connect(
        dataConsumerWellKnownIpAddress, dataConsumerWellKnownPort,
        maxWaitTimeMs, null, localPort);
  }

  /**
   * Establishes a socket connection to the Data Consumer, and initializes its I/O streams.
   *
   * @param dataConsumerWellKnownIpAddress Data Consumer's "Well Known" IP Address.
   * @param dataConsumerWellKnownPort Data Consumer's "Well Known" port number.
   * @param maxWaitTimeMs Continuously attempt to connect until this limit is exceeded.
   *
   * - Less than 0: Wait forever.
   *
   * - Equal to 0: Only try to connect once.
   *
   * - Greater than 0: Wait for the specified period of time.
   * @param localIpAddress Local IP address to bind to (if null, then listen on all IPs).
   * @param localPort Local port to bind to (if 0, use ephemeral port).
   * @throws Exception Thrown on socket, I/O stream, or network connection errors.
   */
  public void connect(
      String dataConsumerWellKnownIpAddress, int dataConsumerWellKnownPort,
      long maxWaitTimeMs,
      String localIpAddress, int localPort) throws Exception {

    // Validate arguments.
    Cd11Validator.validIpAddress(dataConsumerWellKnownIpAddress);
    Cd11Validator.validPortNumber(dataConsumerWellKnownPort);
    if (localIpAddress != null) {
      Cd11Validator.validIpAddress(localIpAddress);
      Cd11Validator.validNonZeroPortNumber(localPort);
    } else {
      Cd11Validator.validPortNumber(localPort);
    }

    long endTimeNs = (maxWaitTimeMs < 0) ? 0 : System.nanoTime() + (maxWaitTimeMs * 1000 * 1000);
    while (true) {
      try {
        // Bind to the local socket, and connect to the remote machine.
        if (localIpAddress == null && localPort == 0) {
          // Listen on all IPs, and use an ephemeral port.
          this.socket = new Socket(
              InetAddresses.forString(dataConsumerWellKnownIpAddress),
              dataConsumerWellKnownPort);
        } else if (localIpAddress == null) {
          // Listen on all IPs, but specify the local port.
          this.socket = new Socket(
              InetAddresses.forString(dataConsumerWellKnownIpAddress),
              dataConsumerWellKnownPort,
              null,
              localPort);
        } else {
          // Specify the local IP and port.
          this.socket = new Socket(
              InetAddresses.forString(dataConsumerWellKnownIpAddress),
              dataConsumerWellKnownPort,
              InetAddresses.forString(localIpAddress),
              localPort);
        }

        // Set the SoLinger.
        this.socket.setSoLinger(true, 3);

        // Connection established, now break from the loop.
        break;
      } catch (IOException e) {
        if (maxWaitTimeMs < 0 || System.nanoTime() < endTimeNs) {
          // Try again.
          logger.debug(String.format(
              "Data Provider connection attempt to %s:%d failed (will retry).",
              dataConsumerWellKnownIpAddress, dataConsumerWellKnownPort), e);
          continue;
        } else {
          logger.error(String.format(
              "Data Provider could not connect to %s:%d within the maximum wait time limit (%d ms).",
              dataConsumerWellKnownIpAddress, dataConsumerWellKnownPort, maxWaitTimeMs), e);
          throw e;
        }
      } catch (Exception e) {
        logger.error(String.format(
            "Exception thrown while Data Provider was attempting to open socket connection to %1$s:%2$d.",
            dataConsumerWellKnownIpAddress, dataConsumerWellKnownPort));
        throw e;
      }
    }

    // Connect to the socket's I/O streams.
    this.connectSocketIoStreams();

    // Initialize the "last contact", "last Acknack sent", and "last data sent" time stamps.
    long tstamp = System.nanoTime();
    lastContactTimeNs.set(tstamp);
    lastAcknackSentTimeNs.set(tstamp);
    lastDataSentTimeNs.set(tstamp);

    // Log the connection info.
    logger.debug(String.format(
        "CD11Client established connection from %1$s:%2$d to %3$s:%4$d.",

        // Local IP/port first, since we are initiating the connection.
        getLocalIpAddressAsString(),
        this.socket.getLocalPort(),

        // Remote IP/port second.
        getRemoteIpAddressAsString(),
        this.socket.getPort()));
  }

  /**
   * Establishes a socket connection to the Data Provider, and initializes its I/O streams.
   *
   * @param newSocket The socket connect to the data provider.
   * @throws Exception Thrown on socket, I/O stream, or network connection errors.
   */
  public void connect(Socket newSocket) throws Exception {
    // Validate input.
    if (newSocket == null) {
      throw new NullPointerException("Socket object received is null.");
    }
    if (newSocket.isClosed() || !newSocket.isConnected() || !newSocket.isBound()) {
      throw new IllegalArgumentException(
          "Socket is either closed, or was never connected to a remote address.");
    }

    // Accept the socket.
    this.socket = newSocket;

    // Connect to the socket's I/O streams.
    this.connectSocketIoStreams();

    // Initialize the "last contact", "last Acknack sent", and "last data sent" time stamps.
    long tstamp = System.nanoTime();
    lastContactTimeNs.set(tstamp);
    lastAcknackSentTimeNs.set(tstamp);
    lastDataSentTimeNs.set(tstamp);

    // Log the connection info.
    logger.debug(String.format(
        "CD11Client established connection from %1$s:%2$d to %3$s:%4$d.",

        // Remote IP/port first, since we are receiving the connection.
        getRemoteIpAddressAsString(),
        this.socket.getPort(),

        // Local IP/port second.
        getLocalIpAddressAsString(),
        this.socket.getLocalPort()));
  }

  /**
   * Connects this object to a socket's I/O streams.
   *
   * @throws Exception Thrown on socket, I/O stream, or network connection errors.
   */
  private void connectSocketIoStreams() throws Exception {
    // Connect to the output data streams.
    try {
      this.socketOut = new DataOutputStream(socket.getOutputStream());
    } catch (Exception e) {
      logger.error("Socket output stream could not be established.", e);
      throw e;
    }

    // Connect to the input data streams.
    try {
      this.socketIn = new DataInputStream(socket.getInputStream());
    } catch (Exception e) {
      logger.error("Socket input stream could not be established.", e);
      throw e;
    }
  }

  /**
   * Gracefully closes the socket, and its I/O streams.
   */
  public void disconnect() {
    if (this.socket != null) {
      try {
        this.socket.close();
      } catch (Exception e) {
        logger.warn("Socket could not be closed.", e);
      } finally {
        this.socket = null;
      }
    }

    if (this.socketIn != null) {
      try {
        this.socketIn.close();
      } catch (Exception e) {
        logger.warn("Input stream could not be closed: %1s", e);
      } finally {
        this.socketIn = null;
      }
    }

    if (this.socketOut != null) {
      try {
        this.socketOut.close();
      } catch (Exception e) {
        logger.warn("Output stream could not be closed: %1s", e);
      } finally {
        this.socketOut = null;
      }
    }

    // Reset the "last contact", "last Acknack sent", and "last Data sent" time stamps.
    lastContactTimeNs.set(0);
    lastAcknackSentTimeNs.set(0);
    lastDataSentTimeNs.set(0);
  }

  /**
   * Sends a CD 1.1 frame to the Data Consumer.
   *
   * @throws Exception Thrown on byte serialization, or socket errors.
   */
  public void write(Cd11Frame cd11Frame) throws Exception {

    synchronized (WRITE_LOCK) {
      // Check that the socket is ready to use.
      if (!this.isConnected()) {
        throw new IllegalStateException("Socket connection is not open.");
      }

      socketOut.write(cd11Frame.toBytes());
      socketOut.flush();
    }

    // Check if an Acknack or Data frame was just sent out.
    if (cd11Frame.frameType == FrameType.ACKNACK) {
      // Update the "last Acknack sent" time stamp.
      lastAcknackSentTimeNs.set(System.nanoTime());
    } else if (cd11Frame.frameType == FrameType.DATA) {
      // Update the "last Data sent" time stamp.
      lastDataSentTimeNs.set(System.nanoTime());
    }
  }

  /**
   * Receives a CD 1.1 frame from the Data Consumer. NOTE: This operation is meant for use in
   * single-threaded processing.
   *
   * @param maxWaitTimeMs The maximum amount of time to wait for a data frame to arrive.
   * @return CD 1.1 frame.
   * @throws InterruptedException Thrown if the maximum wait time was exceeded before any data had
   * arrived.
   * @throws Exception Thrown on socket, parsing, validation, or object construction error; or if
   * the maximum wait time was exceeded after some data had been read from the socket.
   */
  public Cd11Frame read(long maxWaitTimeMs) throws InterruptedException, Exception {
    Validate.isTrue(maxWaitTimeMs > 0, "Max wait time must be greater than zero.");

    long endTimeNs = System.nanoTime() + (maxWaitTimeMs * 1000 * 1000);
    return read(() -> (System.nanoTime() >= endTimeNs));
  }

  /**
   * Receives a CD 1.1 frame from the Data Consumer. NOTE: This operation is meant for use in
   * multi-threaded processing.
   *
   * @param haltReadOperation The maximum amount of time to wait for frame data to arrive.
   * @return CD 1.1 object received from the Data Consumer.
   * @throws InterruptedException Thrown if the maximum wait time was exceeded before any data had
   * arrived.
   * @throws Exception Thrown on socket, parsing, validation, or object construction errors.
   * @throws Exception Thrown on socket, parsing, validation, or object construction error; or if
   * the haltReadOperation was triggered after some data had been read from the socket.
   */
  public Cd11Frame read(BooleanSupplier haltReadOperation)
      throws InterruptedException, Exception {

    Cd11ByteFrame cd11ByteFrame;

    synchronized (READ_LOCK) {
      // Check that the socket is ready to use.
      if (!this.isConnected()) {
        throw new IllegalStateException("Socket connection is not open.");
      }

      // Parse the CD 1.1 frame.
      cd11ByteFrame = new Cd11ByteFrame(this.socketIn, haltReadOperation);
    }

    // Update the "last contact" time stamp.
    lastContactTimeNs.set(System.nanoTime());

    // Construct the appropriate CD 1.1 frame.
    switch (cd11ByteFrame.getFrameType()) {
      case ACKNACK:
        //Set the Frame Creator from the first frame we receive on the data consumer (always option request)
        Cd11AcknackFrame cd11AcknackFrame = new Cd11AcknackFrame(cd11ByteFrame);
        this.framesetAcked = cd11AcknackFrame.framesetAcked;
        return new Cd11AcknackFrame(cd11ByteFrame);
      case ALERT:
        return new Cd11AlertFrame(cd11ByteFrame);
      case CD_ONE_ENCAPSULATION:
        throw new IllegalStateException("Not yet implemented");
      case COMMAND_REQUEST:
        throw new IllegalStateException("Not yet implemented");
      case COMMAND_RESPONSE:
        throw new IllegalStateException("Not yet implemented");
      case CONNECTION_REQUEST:
        return new Cd11ConnectionRequestFrame(cd11ByteFrame);
      case CONNECTION_RESPONSE:
        return new Cd11ConnectionResponseFrame(cd11ByteFrame);
      case DATA:
        return new Cd11DataFrame(cd11ByteFrame);
      case OPTION_REQUEST:
        return new Cd11OptionRequestFrame(cd11ByteFrame);
      case OPTION_RESPONSE:
        throw new IllegalStateException("Not yet implemented");
      default:
        throw new IllegalArgumentException("Frame type does not exist.");
    }
  }

  //---------- CD 1.1 Frame Creation Methods ----------

  /**
   * Generates a CD 1.1 Acknack frame.
   *
   * @param framesetAcked full name of the frame set being acknowledged (for example, “SG7:0”)
   * @param lowestSeqNum lowest valid sequence number sent considered during the current connection
   * for the set (0 until a frame set is no longer empty)
   * @param highestSeqNum highest valid sequence number considered during the current connection for
   * the set (–1 until a frame set is no longer empty)
   * @param gaps each gap contains two long entries for start time and end time
   * @return CD 1.1 Acknack frame.
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11AcknackFrame createCd11AcknackFrame(
      String framesetAcked, long lowestSeqNum, long highestSeqNum, long[] gaps)
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11AcknackFrame newFrame = new Cd11AcknackFrame(
        framesetAcked, lowestSeqNum, highestSeqNum, gaps);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.ACKNACK,
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length,
        this.config.frameCreator,
        this.config.frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        this.config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);
    return newFrame;
  }

  /**
   * Generates a CD 1.1 Alert frame.
   *
   * @param message The alert message.
   * @return CD 1.1 Alert frame.
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11AlertFrame createCd11AlertFrame(String message)
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11AlertFrame newFrame = new Cd11AlertFrame(message);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.ALERT,
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length,
        this.config.frameCreator,
        this.config.frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        this.config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  /**
   * Generates a CD 1.1 Connection Request frame.
   *
   * @return CD 1.1 Connection Request frame.
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11ConnectionRequestFrame createCd11ConnectionRequestFrame()
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11ConnectionRequestFrame newFrame = new Cd11ConnectionRequestFrame(
        config.protocolMajorVersion, config.protocolMinorVersion,
        config.stationOrResponderName, config.stationOrResponderType, config.serviceType,
        (socket == null) ? "0.0.0.0" : getLocalIpAddressAsString(),
        (socket == null) ? 0 : socket.getLocalPort(),
        null, null); // TODO: Support secondary IP and Port???

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.CONNECTION_REQUEST,
        Cd11FrameHeader.FRAME_LENGTH + Cd11ConnectionRequestFrame.FRAME_LENGTH,
        config.frameCreator,
        config.frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  /**
   * Generates a CD 1.1 Connection Response frame.
   *
   * @return CD 1.1 Connection Response frame.
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11ConnectionResponseFrame createCd11ConnectionResponseFrame(
      String ipAddress, int port, String secondIpAddress, Integer secondsPort)
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11ConnectionResponseFrame newFrame = new Cd11ConnectionResponseFrame(
        config.protocolMajorVersion, config.protocolMinorVersion,
        config.stationOrResponderName, config.stationOrResponderType, config.serviceType,
        ipAddress, port, secondIpAddress, secondsPort);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.CONNECTION_RESPONSE,
        Cd11FrameHeader.FRAME_LENGTH + Cd11ConnectionResponseFrame.FRAME_LENGTH,
        config.frameCreator,
        config.frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  /**
   * Generates a CD 1.1 Data frame.
   *
   * @return CD 1.1 Data frame.
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11DataFrame createCd11DataFrame(
      Cd11ChannelSubframe[] channelSubframes, long sequenceNumber)
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11DataFrame newFrame = new Cd11DataFrame(channelSubframes);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.DATA,
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length,
        config.frameCreator,
        config.frameDestination,
        sequenceNumber);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  /**
   * Generates a CD 1.1 Option Request frame.
   *
   * @return CD 1.1 Option Request frame
   */
  public Cd11OptionRequestFrame createCd11OptionRequestFrame(int optionType, String optionRequest)
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11OptionRequestFrame newFrame = new Cd11OptionRequestFrame(optionType, optionRequest);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.OPTION_REQUEST,
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length,
        config.frameCreator,
        config.frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  /**
   * Generates a CD 1.1 Option Response frame.
   *
   * @return CD 1.1 Option Response frame
   */
  public Cd11OptionResponseFrame createCd11OptionResponseFrame(int optionType,
      String optionResponse)
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11OptionResponseFrame newFrame = new Cd11OptionResponseFrame(optionType, optionResponse);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.OPTION_RESPONSE,
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length,
        config.frameCreator,
        config.frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  /**
   * Generates a CD 1.1 Command Request frame.
   *
   * @return CD 1.1 Command Request Frame
   */
  public Cd11CommandRequestFrame createCd11CommandRequestFrame(
      String stationName, String site, String channel, String locName,
      Instant timestamp, String commandMessage)
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11CommandRequestFrame newFrame = new Cd11CommandRequestFrame(
        stationName, site, channel, locName, timestamp, commandMessage);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.COMMAND_REQUEST,
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length,
        config.frameCreator,
        config.frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  /**
   * Generates a CD 1.1 Command Response frame.
   *
   * @return CD 1.1 Command Response Frame
   */
  public Cd11CommandResponseFrame createCd11CommandResponseFrame(
      String responderStation, String site, String channel, String locName,
      Instant timestamp, String commandRequestMessage, String responseMessage)
      throws IllegalArgumentException, IOException {

    // Create the frame body.
    Cd11CommandResponseFrame newFrame = new Cd11CommandResponseFrame(
        responderStation, site, channel, locName, timestamp, commandRequestMessage,
        responseMessage);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        FrameType.COMMAND_RESPONSE,
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length,
        config.frameCreator,
        config.frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        config.authenticationKeyIdentifier, frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  //---------- CD 1.1 Frame Create-and-Send Methods ----------

  /**
   * Generates and sends a CD 1.1 Acknack frame.
   *
   * @param framesetAcked full name of the frame set being acknowledged (for example, “SG7:0”)
   * @param lowestSeqNum lowest valid sequence number sent considered during the current connection
   * for the set (0 until a frame set is no longer empty)
   * @param highestSeqNum highest valid sequence number considered during the current connection for
   * the set (–1 until a frame set is no longer empty)
   * @param gaps each gap contains two long entries for start time and end time
   */
  public void sendCd11AcknackFrame(
      String framesetAcked, long lowestSeqNum, long highestSeqNum, long[] gaps)
      throws IllegalArgumentException, IOException, Exception {

    this.write(this.createCd11AcknackFrame(framesetAcked, lowestSeqNum, highestSeqNum, gaps));
  }

  /**
   * Generates and sends a CD 1.1 Alert frame.
   *
   * @param message The alert message.
   */
  public void sendCd11AlertFrame(String message)
      throws IllegalArgumentException, IOException, Exception {
    // Create and send the object.
    this.write(this.createCd11AlertFrame(message));
  }

  /**
   * Generates and sends a CD 1.1 Connection Request frame.
   */
  public void sendCd11ConnectionRequestFrame()
      throws IllegalArgumentException, IOException, Exception {
    // Create and send the object.
    this.write(this.createCd11ConnectionRequestFrame());
  }

  /**
   * Generates and sends a CD 1.1 Connection Response frame.
   *
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public void sendCd11ConnectionResponseFrame(
      String ipAddress, int port, String secondIpAddress, Integer secondPort)
      throws IllegalArgumentException, IOException, Exception {
    // Create and send the object.
    this.write(
        this.createCd11ConnectionResponseFrame(ipAddress, port, secondIpAddress, secondPort));
  }

  /**
   * Generates and sends a CD 1.1 Data frame.
   *
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public void sendCd11DataFrame(Cd11ChannelSubframe[] channelSubframes, long sequenceNumber)
      throws Exception {
    // Create and send the object.
    this.write(this.createCd11DataFrame(channelSubframes, sequenceNumber));
  }

  /**
   * Generates and sends a CD 1.1 Option Request frame.
   *
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public void sendCd11OptionRequestFrame(int optionType, String optionRequest) throws Exception {
    // Create and send the object.
    this.write(this.createCd11OptionRequestFrame(optionType, optionRequest));
  }

  /**
   * Generates and sends a CD 1.1 Option Response frame.
   *
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public void sendCd11OptionResponseFrame(int optionType, String optionResponse) throws Exception {
    // Create and send the object.
    Cd11OptionResponseFrame frame = this.createCd11OptionResponseFrame(optionType, optionResponse);
    logger.info("Sending option response: " + frame);
    this.write(frame);
  }

  /**
   * Generates and sends a CD 1.1 Command Request frame.
   *
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public void sendCd11CommandRequestFrame(
      String stationName, String site, String channel, String locName,
      Instant timestamp, String commandMessage) throws Exception {
    // Create and send the object.
    this.write(this.createCd11CommandRequestFrame(
        stationName, site, channel, locName, timestamp, commandMessage));
  }

  /**
   * Generates and sends a CD 1.1 Option Response frame.
   *
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public void sendCd11CommandResponseFrame(
      String responderStation, String site, String channel, String locName,
      Instant timestamp, String commandRequestMessage, String responseMessage) throws Exception {
    // Create and send the object.
    this.write(this.createCd11CommandResponseFrame(
        responderStation, site, channel, locName, timestamp, commandRequestMessage,
        responseMessage));
  }

  //---------- Misc ----------

  /**
   * Returns the name of the station or responder.
   *
   * @return Name of station or responder.
   */
  public String getStationOrResponderName() {
    return config.stationOrResponderName;
  }

  /**
   * The total number of seconds since the last frame was read from the socket. (NOTE: This method
   * is thread safe.)
   *
   * @return Seconds since last contact.
   */
  public long secondsSinceLastContact() {
    long lastTimestamp = lastContactTimeNs.get();
    return (lastTimestamp > 0) ? (System.nanoTime() - lastTimestamp) / 1000000000 : 0;
  }

  /**
   * The total number of seconds since the last Acknack frame was sent out (from the local socket to
   * the remote socket). (NOTE: This method is thread safe.)
   *
   * @return Seconds since last Acknack sent.
   */
  public long secondsSinceLastAcknackSent() {
    long lastTimestamp = lastAcknackSentTimeNs.get();
    return (lastTimestamp > 0) ? (System.nanoTime() - lastTimestamp) / 1000000000 : 0;
  }

  /**
   * The total number of milliseconds since the last Data frame was sent out (from the local socket
   * to the remote socket). (NOTE: This method is thread safe.)
   *
   * @return Milliseconds since last Data sent.
   */
  public long millisecondsSinceLastDataSent() {
    long lastTimestamp = lastDataSentTimeNs.get();
    return (lastTimestamp > 0) ? (System.nanoTime() - lastTimestamp) / 1000000 : 0;
  }

  /**
   * Returns the frame creator, registered in the constructor.
   *
   * @return frame creator
   */
  public synchronized String getFrameCreator() {
    return config.frameCreator;
  }

  /**
   * Returns the framesetAcked,
   *
   * @return framesetAcked
   */
  public synchronized String getFramesetAcked() {
    return this.framesetAcked;
  }

  public void send(byte[] packet) throws Exception {
    synchronized (WRITE_LOCK) {
      // Check that the socket is ready to use.
      if (!this.isConnected()) {
        throw new IllegalStateException("Socket connection is not open.");
      }

      socketOut.write(packet);
      socketOut.flush();
    }

  }

}
