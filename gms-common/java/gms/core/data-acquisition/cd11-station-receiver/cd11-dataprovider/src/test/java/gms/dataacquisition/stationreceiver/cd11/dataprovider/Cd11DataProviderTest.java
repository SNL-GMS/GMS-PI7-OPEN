package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import static org.junit.Assert.assertTrue;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11SocketConfig;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11DataFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame.FrameType;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.Cd11DataProviderConfig;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeSet;
import org.junit.Ignore;
import org.junit.Test;

public class Cd11DataProviderTest {

  private final String connectionManagerWellKnownIpAddress = "127.0.0.1";
  private final Integer connectionManagerWellKnownPort = 10555;
  private final String dataConsumerIpAddress = "127.0.0.1";
  private final Integer dataConsumerPort = 8041;
  private final Short majorVersion = 1;
  private final Short minorVersion = 1;
  private final String stationName = "H07N";
  private final String stationType = "IDC";
  private final String serviceType = "TCP";
  private final String frameCreator = "TEST";
  private final String frameDestination = "IDC";
  private final Integer authenticationKeyIdentifier = 0;

  @Ignore
  @Test(expected = IllegalArgumentException.class)
  public void testUdpServiceType() throws Exception {
    Cd11DataProviderConfig.builder()
        .setServiceType("UDP") // Throws exception, since it is not yet implemented.
        .build();
  }

  @Ignore
  @Test
  public void testIgnoreAcknackWithBadFrameSetAcked() throws Exception {
    // Start the data provider.
    Cd11DataProviderConfig dpConfig = Cd11DataProviderConfig.builder()
        .setConnectionManagerIpAddress(connectionManagerWellKnownIpAddress)
        .setConnectionManagerPort(connectionManagerWellKnownPort)
        .setFrameCreator(frameCreator)
        .setFrameDestination(frameDestination)
        .setDataFrameSendingIntervalMs(5)
        .build();
    Cd11DataProvider dataProvider = new Cd11DataProvider(dpConfig);
    dataProvider.start();

    // Connect to the Data Consumer.
    Cd11SocketConfig sockConfig = Cd11SocketConfig.builder()
        .setFrameDestination(frameDestination)
        .build();
    Cd11Socket cd11Socket = new Cd11Socket(sockConfig);
    ServerSocket fakeConnMan = new ServerSocket(connectionManagerWellKnownPort);
    Socket socket = fakeConnMan.accept();
    fakeConnMan.close();
    cd11Socket.connect(socket);
    cd11Socket.sendCd11ConnectionResponseFrame(
        dataConsumerIpAddress, dataConsumerPort, null, null);
    cd11Socket.disconnect();
    ServerSocket fakeDataConsumer = new ServerSocket(dataConsumerPort);
    socket = fakeDataConsumer.accept();
    fakeDataConsumer.close();
    cd11Socket.connect(socket);
    dataProvider.waitUntilThreadInitializes();

    // Grab a data frame, memorize its sequence number.
    Cd11DataFrame gap1 = null;
    ArrayList<Cd11DataFrame> gap2 = new ArrayList<>();
    ArrayList<Cd11DataFrame> gap3 = new ArrayList<>();
    for (int i=0; i < 9; i++) {
      Cd11DataFrame dataFrame;
      while (true) {
        Cd11Frame cd11Frame = cd11Socket.read(1000);
        if (cd11Frame.frameType == FrameType.DATA) {
          dataFrame = cd11Frame.asFrameType(Cd11DataFrame.class);
          break;
        }
      }
      if (i < 1) {
        gap1 = dataFrame;
      } else if (i > 1 && i < 4) {
        gap2.add(dataFrame);
      } else if (i > 4) {
        gap3.add(dataFrame);
      }
    }
    long[] gapRanges = new long[] {
        gap1.getFrameHeader().sequenceNumber,
        gap1.getFrameHeader().sequenceNumber+1,
        gap2.get(0).getFrameHeader().sequenceNumber,
        gap2.get(gap2.size() - 1).getFrameHeader().sequenceNumber+1,
        gap3.get(0).getFrameHeader().sequenceNumber,
        gap3.get(gap3.size() - 1).getFrameHeader().sequenceNumber+1 };
    Long lastGapSeqNum = gap3.get(gap3.size()-1).getFrameHeader().sequenceNumber;

    // Create a set containing all of the gap sequence numbers.
    TreeSet<Long> gapSequenceNumbers = new TreeSet<>();
    gapSequenceNumbers.add(gap1.getFrameHeader().sequenceNumber);
    gap2.forEach(x -> gapSequenceNumbers.add(x.getFrameHeader().sequenceNumber));
    gap3.forEach(x -> gapSequenceNumbers.add(x.getFrameHeader().sequenceNumber));

    // Send an Acknack with a bad frame set name.
    cd11Socket.sendCd11AcknackFrame(
        "BAD:0", 1, 100, gapRanges);

    // Check that the gap does NOT get returned by the Data Provider.
    int totalDataFrames = 0;
    while (true) {
      Cd11Frame cd11Frame = cd11Socket.read(500);
      if (cd11Frame.frameType == FrameType.DATA) {
        Cd11DataFrame dataFrame = cd11Frame.asFrameType(Cd11DataFrame.class);
        assertTrue(cd11Frame.getFrameHeader().sequenceNumber > lastGapSeqNum);
        totalDataFrames++;
      }
      if (totalDataFrames >= 10) {
        break;
      }
    }

    // Send Acknack frame with the correct frame set name.
    cd11Socket.sendCd11AcknackFrame(
        frameCreator + ":" + frameDestination,
        1, 100, gapRanges);

    // Check that the gap data DOES get returned by the Data Provider.
    totalDataFrames = 0;
    int expectedTotalGapDataFrames = 1 + gap2.size() + gap3.size();
    while (true) {
      Cd11Frame cd11Frame = cd11Socket.read(500);
      if (cd11Frame.frameType == FrameType.DATA) {
        Cd11DataFrame dataFrame = cd11Frame.asFrameType(Cd11DataFrame.class);
        long dataFrameSeqNum = dataFrame.getFrameHeader().sequenceNumber;
        gapSequenceNumbers.remove(dataFrameSeqNum);
        totalDataFrames++;
      }
      if (gapSequenceNumbers.isEmpty()) {
        // All gaps were received!
        break;
      } else if (totalDataFrames >= (expectedTotalGapDataFrames * 3)) {
        assertTrue("Data Provider failed to send the expected gap data.", false);
      }
    }

    // Shutdown.
    cd11Socket.disconnect();
    dataProvider.stop();
    dataProvider.waitUntilThreadStops();
  }
}
