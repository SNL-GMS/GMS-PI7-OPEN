package gms.dataacquisition.seedlink.receiver;

import gms.dataacquisition.seedlink.clientlibrary.Client;
import gms.dataacquisition.seedlink.clientlibrary.Packet;
import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.ReceivedStationDataPacket;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationDataAcquisitionGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for connecting to a seedlink server as defined by an acquisition group and receiving
 * data.
 */
public class SeedlinkConnectionUtility extends GracefulThread {

  private static Logger logger = LoggerFactory.getLogger(SeedlinkConnectionUtility.class);

  private static final int RECONNECT_WAIT_MS = 500;

  private final StationDataAcquisitionGroup acquisitionGroup;
  private final Instant dataStartTime;
  private final Consumer<ReceivedStationDataPacket> packetCallback;
  private Client seedlinkClient;

  /**
   * Creates a connection utility which will automatically connect and start providing data to the
   * callback.
   *
   * @param acquisitionGroup the configuration of what to receive
   * @param dataStartTime the start time to get data for
   * @param packetCallback callback to invoke when data arrives
   */
  public SeedlinkConnectionUtility(StationDataAcquisitionGroup acquisitionGroup,
      Instant dataStartTime, Consumer<ReceivedStationDataPacket> packetCallback) {
    super("seedlink-connection-utility-" + new Random().nextInt(),
        true, true);
    this.acquisitionGroup = Objects.requireNonNull(acquisitionGroup, "null acquisitionGroup");
    this.dataStartTime = Objects.requireNonNull(dataStartTime, "null dataStartTime");
    this.packetCallback = Objects.requireNonNull(packetCallback, "null packetCallback");
  }

  public static void main(String[] args) {
    final List<String> requests = List.of("STATION KDAK II");
    final StationDataAcquisitionGroup sdag = StationDataAcquisitionGroup.create(
        requests, AcquisitionProtocol.SEEDLINK, "127.0.0.1", 18000,
        Instant.now(), Instant.now(), Map.of(), true, "");
    final Instant startTime = Instant.parse("2018-11-27T00:00:00Z");
    new SeedlinkConnectionUtility(sdag, startTime,
        p -> System.out.println("Got packet: " + p))
        .run();
  }


  @Override
  public void onStop() {
    try {
      logger.info("onStop: closing seedlink connection");
      this.seedlinkClient.close();
      logger.info("onStop: seedlink connection closed successfully");
    } catch (IOException e) {
      logger.error("Error closing connection", e);
    }
  }

  @Override
  protected void onStart() throws Exception {
    openConnection();
  }

  // TODO: would this be better be handled by NiFi somehow?
  private void handleDisconnect() {
    logger.info("Disconnected from server, will close and attempt reconnect");
    try {
      this.seedlinkClient.close();
    } catch (IOException e) {
      logger.error("Error closing connection after disconnect", e);
    }
    // keep reconnecting until success
    while (true) {
      try {
        logger.info("Attempting reconnect");
        openConnection();
        logger.info("Re-connection succeeded");
        break;
      } catch (Exception e) {
        logger.error("Error attempting reconnect, will retry in " + RECONNECT_WAIT_MS + " ms", e);
        try {
          Thread.sleep(RECONNECT_WAIT_MS);
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  /**
   * Connects to the seedlink server specified by the acquisition group and performs the handshake.
   * Registers a packet callback which is forwarded to the callback registered with this utility.
   * Also registers a callback for disconnects to attempt to reconnect.
   *
   * To do the handshake, iterate over the request strings in the acquisition group, sending each to
   * the server as a 'modifier command' followed by a DATA modifier command except in the special
   * case when the next command is a SELECT (in which case DATA needs to come after).
   *
   * @throws IOException can't connect, etc.
   */
  private void openConnection() throws Exception {
    logger.info("Opening connection to " + this.acquisitionGroup.getProviderIpAddress()
        + ":" + this.acquisitionGroup.getProviderPort());
    this.seedlinkClient = new Client(this.acquisitionGroup.getProviderIpAddress(),
        this.acquisitionGroup.getProviderPort());
    this.seedlinkClient.addDataPacketHandler(p -> packetCallback.accept(fromSeedlinkPacket(p)));
    this.seedlinkClient.addDisconnectHandler(c -> handleDisconnect());
    final List<String> requestStrings = this.acquisitionGroup.getRequestStrings();
    for (int i = 0; i < requestStrings.size(); i++) {

      final String command = requestStrings.get(i);
      logger.info("Sending command to server: " + command);
      if (!this.seedlinkClient.sendModifierCommand(command)) {
        throw new Exception("Error response from server for command " + command);
      }

      // only send a DATA command if there isn't a SELECT coming.
      if (i >= requestStrings.size() - 1 ||
          !isSelectCommand(requestStrings.get(i + 1))) {

        final String seedlinkStartTime = toSeedlinkTimeFormat(dataStartTime);
        logger.info("Sending command to server: DATA 1 " + seedlinkStartTime);
        // below: passing 1 as sequence num...server was rejecting our start time if sequence number wasn't present.
        if (!this.seedlinkClient.data("1", seedlinkStartTime)) {
          throw new Exception("Server returned error on DATA 1 " + seedlinkStartTime);
        }
      }
    }
    logger.info("Sending END command to server");
    this.seedlinkClient.end();
  }

  private static String toSeedlinkTimeFormat(Instant i) {
    return new SimpleDateFormat(Client.DATA_TIME_FORMAT).format(Date.from(i));
  }

  private ReceivedStationDataPacket fromSeedlinkPacket(Packet p) {
    return ReceivedStationDataPacket.from(p.getOriginalStreamBytes(), Instant.now(),
        Long.parseLong(p.getSeqNum(), 16),  // sequence numbers are hexadecimal strings
        p.getDataHeader().getStationId());
  }

  private static boolean isSelectCommand(String s) {
    return s.toUpperCase().startsWith("SELECT");
  }
}
