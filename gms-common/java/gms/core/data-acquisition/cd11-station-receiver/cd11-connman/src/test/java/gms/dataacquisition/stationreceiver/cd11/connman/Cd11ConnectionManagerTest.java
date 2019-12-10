package gms.dataacquisition.stationreceiver.cd11.connman;

import static gms.shared.utilities.javautilities.assertwait.AssertWait.assertTrueWait;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11SocketConfig;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionRequestFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11FrameHeader;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11FrameTrailer;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnectionManagerConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11ConnectionManagerTest {

  private final String SERVER_ADDRESS = "127.0.0.1";
  private final short WELL_KNOWN_PORT = 8041;

  private final short majorVersion = 0;
  private final short minorVersion = 1;
  private final String responderName = "H07N";
  private final String responderType = "IDC";
  private final String serviceType = "TCP";
  private final String ipAddress = "127.0.0.1";
  private final int port = 111;
  private final String secondIpAddress = "127.0.0.1";
  private final Integer secondPort = 111;

  private Cd11ConnectionManagerConfig cd11ConnectionManagerConfig = Cd11ConnectionManagerConfig
      .builder(SERVER_ADDRESS, WELL_KNOWN_PORT, SERVER_ADDRESS)
      .build();

  private Cd11FrameHeader frameHeader = FrameHeaderTestUtility.createHeaderForConnectionRequest(
      "KCC:IDC", "KCC:0");

  private Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(1, new byte[0]);

  private Cd11ConnectionRequestFrame connRequestFrame;

  private static Logger logger = LoggerFactory.getLogger(Cd11ConnectionManagerTest.class);

  /**
   * Constructor.
   */
  public Cd11ConnectionManagerTest() throws Exception {
    this.connRequestFrame = new Cd11ConnectionRequestFrame(
        majorVersion, minorVersion, responderName, responderType, serviceType,
        ipAddress, port, secondIpAddress, secondPort);
    this.connRequestFrame.setFrameHeader(frameHeader);
    this.connRequestFrame.setFrameTrailer(frameTrailer);
  }

  /**
   * Tests that ConnMan can receive a connection request, and send a connection response.
   */
  @Test
  public void requestAndEstablishConnection() throws Exception {
    // Start the Cd11ConnectionManager.
    Cd11ConnectionManager connMan = new Cd11ConnectionManager(cd11ConnectionManagerConfig);
    connMan.start();
    connMan.waitUntilThreadInitializes();

    int baselineTotalCd11Stations = connMan.getTotalCd11Stations();

    // Add a CD 1.1 station.
    String newStationName = "BLAH";
    String newStationExpectedIpAddress = "127.0.0.1";
    String newStationDataConsumerIpAddress = "22.22.22.22";
    int newStationDataConsumerPortNumber = 48484;
    connMan.addCd11Station(
        newStationName,
        newStationExpectedIpAddress,
        newStationDataConsumerIpAddress,
        newStationDataConsumerPortNumber);
    assertTrue(connMan.getTotalCd11Stations() == baselineTotalCd11Stations + 1);
    Cd11Station blahStation = connMan.lookupCd11Station(newStationName);
    assertNotNull(blahStation);
    assertTrue(blahStation.expectedDataProviderIpAddress.equals(newStationExpectedIpAddress));
    assertTrue(blahStation.dataConsumerIpAddress.equals(newStationDataConsumerIpAddress));
    assertTrue(blahStation.dataConsumerPort == newStationDataConsumerPortNumber);

    // Check for zero logs.
    assertTrue(connMan.getTotalConnectionLogs() == 0);

    // Connect to the Connection Manager.
    Cd11Socket cd11Socket = new Cd11Socket(Cd11SocketConfig.builder()
        .setStationOrResponderName(newStationName)
        .build());
    cd11Socket.connect(SERVER_ADDRESS, WELL_KNOWN_PORT, 2000, 65001);

    // Send a Connection Request frame to the Connection Manager.
    cd11Socket.sendCd11ConnectionRequestFrame();

    // Wait for response from the Connection Manager.
    Cd11Frame resp = cd11Socket.read(2000);
    Cd11ConnectionResponseFrame connResponseFrame =
        resp.asFrameType(Cd11ConnectionResponseFrame.class);

    // Check the CD 1.1 connection response.
    assertTrue(FrameUtilities.intToIpAddressString(connResponseFrame.ipAddress)
        .equals(newStationDataConsumerIpAddress));
    assertTrue(connResponseFrame.port == newStationDataConsumerPortNumber);

    // Check that one log was made.
    assertTrueWait(() -> connMan.getTotalConnectionLogs() == 1, 1000);

    // Remove the CD 1.1 station.
    connMan.removeCd11Station(newStationName);
    assertTrue(connMan.getTotalCd11Stations() == baselineTotalCd11Stations);
  }
}
