package gms.dataacquisition.stationreceiver.cd11.dataman;

import static gms.shared.utilities.javautilities.assertwait.AssertWait.assertTrueWait;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11SocketConfig;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AlertFrame;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.Cd11DataConsumerConfig;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.DataManConfig;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class DataManTest {

  private Cd11DataConsumerConfig testStation1 = Cd11DataConsumerConfig.builder(
      55530,
      UUID.nameUUIDFromBytes("TEST_STATION_1".getBytes()),
      "DP1")
      .setExpectedDataProviderIpAddress("127.0.0.1")
      .setThreadName("TEST_DC_1")
      .setDataConsumerIpAddress("127.0.0.1")
      .build();

  @BeforeClass
  public static void setup() throws Exception {
  }

  @AfterClass
  public static void teardown() {
  }

  @Test
  public void testDataMan() throws Exception {
    // Run the DC Manager.
    DataManConfig dmConfig = DataManConfig.builder().build();
    DataMan dataMan = new DataMan(dmConfig);
    dataMan.start();
    dataMan.waitUntilThreadInitializes();

    // Test that the thread is running.
    assertTrue(dataMan.isRunning());

    // Count the number of data consumers spawned by default.
    int baselineTotalDCs = dataMan.getTotalDataConsumerThreads();

    // Check that our test station is not currently registered.
    assertFalse(dataMan.isDataConsumerPortRegistered(testStation1.dataConsumerPort));

    // Add a data consumer.
    dataMan.addDataConsumer(testStation1);

    // Now check that our test station is registered.
    assertTrueWait(
        () -> dataMan.isDataConsumerPortRegistered(testStation1.dataConsumerPort),
        100);

    // Test that the number of Data Consumers registered matches the expectation.
    assertTrue(dataMan.getTotalDataConsumerThreads() == (baselineTotalDCs + 1));
    assertTrue(dataMan.getPorts().size() == (baselineTotalDCs + 1));

    // Connect to the test station.
    Cd11Socket cd11Socket = new Cd11Socket(Cd11SocketConfig.builder()
        .setStationOrResponderName(testStation1.dataProviderStationName)
        .build());
    cd11Socket.connect(
        testStation1.dataConsumerIpAddress, testStation1.dataConsumerPort,
        10000,
        testStation1.dataConsumerIpAddress, 55590);

    // Send and receive an Alert frame.
    cd11Socket.sendCd11AlertFrame("Time to shut down Data Consumer.");
    Cd11AlertFrame alertFrame = cd11Socket.read(10000)
        .asFrameType(Cd11AlertFrame.class);
    System.out.println(String.format("Data Consumer's Alert message: %s", alertFrame.message));

    // Remove the test station.
    dataMan.removeDataConsumer(testStation1.dataConsumerPort);

    // Check that one less station is registered.
    assertFalse(dataMan.isDataConsumerPortRegistered(testStation1.dataConsumerPort));

    // Test that the DC Manager can be stopped.
    dataMan.stop();
    dataMan.waitUntilThreadStops();
    assertFalse(dataMan.isRunning());
  }
}
