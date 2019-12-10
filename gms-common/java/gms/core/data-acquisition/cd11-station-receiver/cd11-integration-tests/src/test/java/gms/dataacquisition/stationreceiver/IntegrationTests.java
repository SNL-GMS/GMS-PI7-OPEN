package gms.dataacquisition.stationreceiver;

import static gms.shared.utilities.javautilities.assertwait.AssertWait.assertTrueWait;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gms.dataacquisition.stationreceiver.cd11.connman.Cd11ConnectionManager;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnectionManagerConfig;
import gms.dataacquisition.stationreceiver.cd11.dataman.DataMan;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.DataManConfig;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.Cd11DataProvider;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.Cd11DataProviderConfig;
import gms.dataacquisition.stationreceiver.osdclient.StationReceiverOsdClientInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@Ignore
public class IntegrationTests {

  private static StationReceiverOsdClientInterface osdClient = mock(
      StationReceiverOsdClientInterface.class);
  private static DataManConfig dataManConfig;
  private static ArgumentCaptor<RawStationDataFrame> frameCaptor;
  private File inDataFrameFile = new File("./src/test/resources/H04S-2400077265749616.json");

  @BeforeClass
  public static void setup() throws Exception {
    // mock the osdClient
    doNothing().when(osdClient).storeChannelStatesOfHealth(any());
    doNothing().when(osdClient).storeChannelSegments(any());
    frameCaptor = ArgumentCaptor.forClass(RawStationDataFrame.class);
    doNothing().when(osdClient).storeRawStationDataFrame(frameCaptor.capture());

    when(osdClient.getChannelId(any(), any())).thenReturn(Optional.of(UUID.randomUUID()));
    when(osdClient.getStationId(any())).thenReturn(Optional.of(UUID.randomUUID()));

    dataManConfig = DataManConfig.builder().build();
  }

  @Test
  public void testPipeline() throws Exception {

    // Run the Connection Manager.
    Cd11ConnectionManagerConfig connManConfig = Cd11ConnectionManagerConfig
        .builder(
            "127.0.0.1",
            8041,
            "127.0.0.1")
        .build();
    Cd11ConnectionManager connMan = new Cd11ConnectionManager(connManConfig);
    connMan.start();
    connMan.waitUntilThreadInitializes();

    String contents = new String(Files.readAllBytes(inDataFrameFile.toPath()));

    RawStationDataFrame frame = CoiObjectMapperFactory.getJsonObjectMapper().readValue(
        contents, RawStationDataFrame.class);
    byte[] inputData = frame.getRawPayload();

    // Run DataMan.
    DataMan dataMan = new DataMan(dataManConfig, osdClient, true);
    dataMan.start();
    dataMan.waitUntilThreadInitializes();

    // Run the Data Provider.
    Cd11DataProviderConfig dataProviderConfig = Cd11DataProviderConfig.builder()
        .setCannedFramePath("./src/test/resources/H04S-2400077265749616.json")
        .build();

    Cd11DataProvider dataProvider = new Cd11DataProvider(dataProviderConfig);
    dataProvider.start();
    dataProvider.waitUntilThreadInitializes();

    // Wait for the data to be written then shut down the data provider.
    assertTrueWait(() -> frameCaptor.getAllValues().size() > 0, 5000);
    dataProvider.stop();
    dataMan.stop();
    dataMan.waitUntilThreadStops();

    // Check that data was persisted to the database successfully.
    List<RawStationDataFrame> finalFrameList = frameCaptor.getAllValues();
    assertNotNull(finalFrameList);
    assertFalse(finalFrameList.isEmpty());
    byte[] fromOSD = finalFrameList.get(0).getRawPayload();
    assertArrayEquals(inputData, fromOSD);
  }
}
