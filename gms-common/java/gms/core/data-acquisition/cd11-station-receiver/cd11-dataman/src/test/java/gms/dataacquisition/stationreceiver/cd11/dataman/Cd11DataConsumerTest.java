package gms.dataacquisition.stationreceiver.cd11.dataman;

import static junit.framework.TestCase.assertTrue;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11SocketConfig;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11FrameHeader;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.FakeDataFrame;
import java.nio.ByteBuffer;
import org.junit.Ignore;
import org.junit.Test;


public class Cd11DataConsumerTest {

  private final String dataProviderIpAddress = "127.0.0.1";
  private final Integer dataProviderPort = 10555;
  private final String dataConsumerWellKnownIpAddress = "127.0.0.1";
  private final Integer dataConsumerAssignedPort = 8100;
  private final Short majorVersion = 1;
  private final Short minorVersion = 1;
  private final String stationName = "H07N";
  private final String stationType = "IDC";
  private final String serviceType = "TCP";
  private final String frameCreator = "TEST";
  private final String frameDestination = "IDC";
  private final Integer authenticationKeyIdentifier = 0;

  private static final int FRAME_TYPE = 5;                                      // [4] 0 - 3
  private static final int TRAILER_OFFSET = 142 + Cd11FrameHeader.FRAME_LENGTH; // [4] 4 - 7
  public static final String CREATOR = "H07N";                              // [8] 8 - 13
  public static final String DESTINATION = "XDC";                          // [8] 14 - 21
  private static final long SEQUENCE_NUMBER = 1512074377000l;                   // [8] 22 - 29
  public static final int SERIES = 123;                                         // [4] 30 - 33

  private static final int DATA_FRAME_HEADER_SIZE = 44;
  private static final int NUM_CHANNELS = 1;
  private static final int FRAME_TIME_LENGTH = 10000;
  private static final String NOMINAL_TIME = "2017346 23:21:10.168";
  private static final int CHANNEL_STRING_COUNT = 10;
  private static final String CHANNEL_STRING = "STA12SHZ01__";

  private static final int DATA_SUBFRAME_SIZE = 100;
  private static final int CHANNEL_LENGTH = 96;
  private static final int AUTHENTICATION_OFFSET = DATA_FRAME_HEADER_SIZE + DATA_SUBFRAME_SIZE - 16;
  private static final byte CHANNEL_DESCRIPTION_AUTHENTICATION = 0;
  private static final byte CHANNEL_DESCRIPTION_TRANSFORMATION = 1;
  private static final byte CHANNEL_DESCRIPTION_SENSOR_TYPE = 0;
  private static final byte CHANNEL_DESCRIPTION_OPTION_FLAG = 0;
  private static final String CHANNEL_DESCRIPTION_SITE_NAME = "STA12";
  private static final String CHANNEL_DESCRIPTION_CHANNEL_NAME = "SHZ";
  private static final String CHANNEL_DESCRIPTION_LOCATION = "01";
  private static final String CHANNEL_DESCRIPTION_DATA_FORMAT = "s4";
  private static final int CHANNEL_DESCRIPTION_CALIB_FACTOR = 0;
  private static final int CHANNEL_DESCRIPTION_CALIB_PER = 0;
  private static final String TIME_STAMP = "2017346 23:21:10.168";
  private static final int SUBFRAME_TIME_LENGTH = 10000;
  private static final int SAMPLES = 8;
  private static final int CHANNEL_STATUS_SIZE = 4;
  private static final int CHANNEL_STATUS = 0;
  private static final int DATA_SIZE = 8;
  private static final byte[] CHANNEL_DATA = new byte[8];
  private static final int SUBFRAME_COUNT = 0;

  private static final int AUTH_KEY = 123;
  private static final int AUTH_SIZE = 8;
  private static final long AUTH_VALUE = 1512076158000l;
  private static final long COMM_VERIFY = 1512076209000l;


  @Ignore
  @Test
  public void testSendMultipleStations() throws InterruptedException {
    short numOfStations = 10;

    Cd11ChannelSubframe[] sfArray = FakeDataFrame.generateFakeChannelSubframes();

    //Get the threads ready, separate loop because we don't want to include this in the time
    Thread[] stationThreads = new Thread[numOfStations];
    for (short i = 0; i < numOfStations; i++) {
      //Generate unique sending and receiving ports
      Integer consumerPortOffset = dataConsumerAssignedPort + i;

      Cd11Socket cd11Socket = new Cd11Socket(Cd11SocketConfig.builder()
          .setStationOrResponderName(stationName)
          .setStationOrResponderType(stationType)
          .setServiceType(serviceType)
          .setFrameCreator(frameCreator)
          .setFrameDestination(frameDestination)
          .setAuthenticationKeyIdentifier(authenticationKeyIdentifier)
          .setProtocolMajorVersion(majorVersion)
          .setProtocolMinorVersion(minorVersion)
          .build());

      FakeStation station = new FakeStation(
          cd11Socket, sfArray, dataConsumerWellKnownIpAddress, consumerPortOffset);
      stationThreads[i] = new Thread(station);
    }

    //Now time how long it takes for the threads to complete
    long startTime = System.currentTimeMillis();
    for (Thread thread : stationThreads) {
      thread.start();
      thread.join();
    }
    long endTime = System.currentTimeMillis();
    long duration = (endTime - startTime);
    System.out.println("Duration = " + duration);
    assertTrue(duration < 10000);
        /* This just asserts we sent on all 10 threads within 10 seconds. After speaking with Dorthe, a physical
        inspection of database logs is sufficient in confirming that we wrote the sent files in under 10 seconds.
        Our usual time is around 2 seconds */
  }

  @Ignore
  @Test
  public void testSendDataFrame() throws Exception {
    int framesToSend = 3;

    Cd11ChannelSubframe sf = new Cd11ChannelSubframe(initChannelSubframeBytes().rewind());
    Cd11ChannelSubframe[] sfArray = new Cd11ChannelSubframe[]{sf};

    Cd11Socket cd11Socket = new Cd11Socket(Cd11SocketConfig.builder()
        .setStationOrResponderName(stationName)
        .setStationOrResponderType(stationType)
        .setServiceType(serviceType)
        .setFrameCreator(frameCreator)
        .setFrameDestination(frameDestination)
        .setAuthenticationKeyIdentifier(authenticationKeyIdentifier)
        .setProtocolMajorVersion(majorVersion)
        .setProtocolMinorVersion(minorVersion)
        .build());

    cd11Socket.connect(
        this.dataConsumerWellKnownIpAddress, this.dataConsumerAssignedPort, 35000);

    for (int i = 0; i < framesToSend; i++) {
      cd11Socket.sendCd11DataFrame(sfArray, i+1);
    }
  }

  @Ignore
  @Test
  public void testSendAlertFrame() {
    try {

      Cd11Socket cd11Socket = new Cd11Socket(Cd11SocketConfig.builder()
          .setStationOrResponderName(stationName)
          .setStationOrResponderType(stationType)
          .setServiceType(serviceType)
          .setFrameCreator(frameCreator)
          .setFrameDestination(frameDestination)
          .setAuthenticationKeyIdentifier(authenticationKeyIdentifier)
          .setProtocolMajorVersion(majorVersion)
          .setProtocolMinorVersion(minorVersion)
          .build());
      try {
        cd11Socket.connect(
            this.dataConsumerWellKnownIpAddress,
            this.dataConsumerAssignedPort,
            35000); // 35 seconds.
        cd11Socket.sendCd11AlertFrame("Terminating connection");
      } catch (Exception e) {
      } finally {
        cd11Socket.disconnect();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private ByteBuffer initChannelSubframeHeaderBytes() {
    ByteBuffer sfh = ByteBuffer.allocate(DATA_FRAME_HEADER_SIZE);

    // Subframe header
    sfh.putInt(NUM_CHANNELS);
    sfh.putInt(FRAME_TIME_LENGTH);
    sfh.put(NOMINAL_TIME.getBytes());
    sfh.putInt(CHANNEL_STRING_COUNT);
    sfh.put(CHANNEL_STRING.getBytes());
    return sfh;
  }

  private ByteBuffer initChannelSubframeBytes() {
    ByteBuffer sf = ByteBuffer.allocate(DATA_SUBFRAME_SIZE);

    for (int i = 0; i < CHANNEL_DATA.length; i++) {
      CHANNEL_DATA[i] = (byte) i;
    }

    // Subframe
    sf.putInt(CHANNEL_LENGTH);
    sf.putInt(AUTHENTICATION_OFFSET);
    sf.put(CHANNEL_DESCRIPTION_AUTHENTICATION);
    sf.put(CHANNEL_DESCRIPTION_TRANSFORMATION);
    sf.put(CHANNEL_DESCRIPTION_SENSOR_TYPE);
    sf.put(CHANNEL_DESCRIPTION_OPTION_FLAG);
    sf.put(CHANNEL_DESCRIPTION_SITE_NAME.getBytes());
    sf.put(CHANNEL_DESCRIPTION_CHANNEL_NAME.getBytes());
    sf.put(CHANNEL_DESCRIPTION_LOCATION.getBytes());
    sf.put(CHANNEL_DESCRIPTION_DATA_FORMAT.getBytes());
    sf.putInt(CHANNEL_DESCRIPTION_CALIB_FACTOR);
    sf.putInt(CHANNEL_DESCRIPTION_CALIB_PER);
    sf.put(TIME_STAMP.getBytes());
    sf.putInt(SUBFRAME_TIME_LENGTH);
    sf.putInt(SAMPLES);
    sf.putInt(CHANNEL_STATUS_SIZE);
    sf.putInt(CHANNEL_STATUS);
    sf.putInt(DATA_SIZE);
    sf.put(CHANNEL_DATA);
    sf.putInt(SUBFRAME_COUNT);
    sf.putInt(AUTH_KEY);
    sf.putInt(AUTH_SIZE);
    sf.putLong(AUTH_VALUE);

    return sf;
  }
}
