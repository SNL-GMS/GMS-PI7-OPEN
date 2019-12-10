package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration.DataframeParserConfig;
import gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration.DataframeParserConfigurationLoader;
import gms.dataacquisition.stationreceiver.osdclient.StationReceiverOsdClientInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Ignore
@SuppressWarnings("unchecked") // if the conversion fails, the test will fail.
public class DataframeParserTest {

  private static final Instant
      H04_START_TIME = Instant.parse("2018-03-21T21:42:00Z"),
      H04C_START_TIME = Instant.parse("2018-03-21T21:42:00.680Z"),
      H04C_END_TIME = H04C_START_TIME.plusSeconds(9),  // sample rate is 1.0
      H04N_END_TIME = Instant.parse("2018-03-21T21:42:09.996Z"),
      H04_END_TIME = H04_START_TIME.plusSeconds(10),
      H07S_START_TIME = Instant.parse("2018-02-20T17:31:20Z"),
      H07S_END_TIME = Instant.parse("2018-02-20T17:31:29.990Z");
  private static final UUID
      H04N1_EDH_ID = UUID.randomUUID(),
      H04N2_EDH_ID = UUID.randomUUID(),
      H04N3_EDH_ID = UUID.randomUUID(),
      H04C1_LEV_ID = UUID.randomUUID(),
      H04C1_LEA_ID = UUID.randomUUID();
  private static final double
      H04N_SAMPLE_RATE = 250.0,
      H04C_SAMPLE_RATE = 1.0,
      H07S_SAMPLE_RATE = 100.0;
  private static final double TOLERANCE = 1e-10;
  private static final StationReceiverOsdClientInterface mockClient = mock(
      StationReceiverOsdClientInterface.class);
  private static final SystemControllerNotifier sysControllerNotifier = mock(
      SystemControllerNotifier.class);
  private static DataframeParserConfig dataframeParserConfig;
  private static ArgumentCaptor<Collection<String>> sysControllerCaptor
      = ArgumentCaptor.forClass(Collection.class);
  private static ArgumentCaptor<RawStationDataFrame> frameCaptor
      = ArgumentCaptor.forClass(RawStationDataFrame.class);
  private static ArgumentCaptor<Collection<ChannelSegment<Waveform>>> segmentCaptor
      = ArgumentCaptor.forClass(Collection.class);
  private static ArgumentCaptor<Collection<AcquiredChannelSoh>> sohCaptor
      = ArgumentCaptor.forClass(Collection.class);

  @BeforeClass
  public static void setup() throws Exception {
    // copy test files into monitored directory
    // setup mock OSD client
    doNothing().when(mockClient).storeRawStationDataFrame(frameCaptor.capture());
    doNothing().when(mockClient).storeChannelSegments(segmentCaptor.capture());
    doNothing().when(mockClient).storeChannelStatesOfHealth(sohCaptor.capture());
    when(mockClient.getChannelId(any(), any()))
        .thenReturn(Optional.of(UUID.randomUUID()));
    when(mockClient.getChannelId("H04N1", "EDH"))
        .thenReturn(Optional.of(H04N1_EDH_ID));
    when(mockClient.getChannelId("H04N2", "EDH"))
        .thenReturn(Optional.of(H04N2_EDH_ID));
    when(mockClient.getChannelId("H04N3", "EDH"))
        .thenReturn(Optional.of(H04N3_EDH_ID));
    when(mockClient.getChannelId("H04C1", "LEV"))
        .thenReturn(Optional.of(H04C1_LEV_ID));
    when(mockClient.getChannelId("H04C1", "LEA"))
        .thenReturn(Optional.of(H04C1_LEA_ID));
    // setup mock system controller notifier to capture what is sent to it
    doNothing().when(sysControllerNotifier).notifyMissingFiles(sysControllerCaptor.capture());
    // initialize data frame parser config
    dataframeParserConfig = DataframeParserConfigurationLoader.load();
    //Create Monitored Dir for testing purposes
    createOrReplaceFileAndDirectories(dataframeParserConfig.monitoredDirLocation);
    copyTestFilesIntoMonitoredDirectory();
  }

  @AfterClass
  public static void teardown() throws IOException {
    //Restore shared volume to exist, be fresh, and have the right permissions
    createOrReplaceFileAndDirectories(dataframeParserConfig.monitoredDirLocation);
    File monitoredDir = new File(dataframeParserConfig.monitoredDirLocation);
    monitoredDir.setReadable(true, false);
    monitoredDir.setWritable(true, false);
  }

  @Test
  public void testRunDataframeParser() throws Exception {
    // initialize and start data frame parser
    DataframeParser dfp = new DataframeParser(
        dataframeParserConfig, sysControllerNotifier, mockClient);
    Thread t = new Thread(dfp::blockingMonitor);
    t.start();
    // there are four .json frame files in the test directory
    final int expectedNumberOfFrames = 4;
    List<RawStationDataFrame> storedFrames = new ArrayList<>();
    int tries = 0;
    while (tries++ < 5000 && storedFrames.size() < expectedNumberOfFrames) {
      storedFrames = frameCaptor.getAllValues();
      Thread.sleep(1);
    }
    // take down parser
    t.interrupt();
    assertEquals(expectedNumberOfFrames, storedFrames.size());
    // assert data on i4Frame; checks that the parser can read these frames from .json
    List<RawStationDataFrame> frames = storedFrames.stream()
            .filter(f -> f.getId().equals("217e3354-fb0d-40df-84ac-77387ead6d8f"))
        .collect(Collectors.toList());
    assertEquals(1, frames.size());
    RawStationDataFrame h04n_frame = frames.get(0);
    assertEquals(UUID.fromString("101156c4-c31a-4363-b21e-6691e26de5d5"), h04n_frame.getStationId());
    assertEquals(H04_START_TIME, h04n_frame.getPayloadDataStartTime());
    assertEquals(H04_END_TIME, h04n_frame.getPayloadDataEndTime());
    assertEquals(Instant.parse("2018-03-21T21:43:08.757228Z"), h04n_frame.getReceptionTime());
    assertEquals(AuthenticationStatus.NOT_YET_AUTHENITCATED, h04n_frame.getAuthenticationStatus());
    assertEquals(AcquisitionProtocol.CD11, h04n_frame.getAcquisitionProtocol());
    // check channel segments and states-of-health for i4 frame.
    List<ChannelSegment<Waveform>> h04N_segments = segmentCaptor.getAllValues().stream()
        .flatMap(Collection::stream)
            .filter(s -> s.getId().equals("0ffaf5cf-538b-4ee0-b7a3-deff75f4049c"))
        .collect(Collectors.toList());
    assertEquals(3, h04N_segments.size());
    // check start times, end times, that waveforms exist, sample rates
    for (ChannelSegment<Waveform> h04N_segment : h04N_segments) {
      assertEquals(H04_START_TIME, h04N_segment.getStartTime());
      assertEquals(H04N_END_TIME, h04N_segment.getEndTime());
      List<Waveform> waveforms = h04N_segment.getTimeseries();
      assertEquals(1, waveforms.size());
      assertEquals(H04N_SAMPLE_RATE, waveforms.get(0).getSampleRate(), TOLERANCE);
      // TODO: assert specific waveform sample values
    }
    // check H04C1
    List<ChannelSegment<Waveform>> h04C1_segments = segmentCaptor.getAllValues().stream()
        .flatMap(Collection::stream)
        .filter(s -> s.getName().contains("H04C1"))
        .collect(Collectors.toList());
    assertEquals(2, h04C1_segments.size());
    // check start times, end times, that waveforms exist, sample rates
    for (ChannelSegment<Waveform> h04C1_segment : h04C1_segments) {
      assertEquals(H04C_START_TIME, h04C1_segment.getStartTime());
      assertEquals(H04C_END_TIME, h04C1_segment.getEndTime());
      List<Waveform> waveforms = h04C1_segment.getTimeseries();
      assertEquals(1, waveforms.size());
      assertEquals(H04C_SAMPLE_RATE, waveforms.get(0).getSampleRate(), TOLERANCE);
      // TODO: assert specific waveform sample values?
    }

    List<AcquiredChannelSoh> h04N1_sohs = sohCaptor.getAllValues().stream()
        .flatMap(Collection::stream)
        .filter(soh -> soh.getChannelId().equals(H04N1_EDH_ID))
        .collect(Collectors.toList());
    assertEquals(17, h04N1_sohs.size());
    List<AcquiredChannelSohAnalog> h04N1_analog_sohs = h04N1_sohs.stream()
        .filter(soh -> (soh instanceof AcquiredChannelSohAnalog))
        .map(AcquiredChannelSohAnalog.class::cast)
        .collect(Collectors.toList());
    assertEquals(1, h04N1_analog_sohs.size());
    assertEquals(0.0, h04N1_analog_sohs.get(0).getStatus(), 0.0000000001);
    assertEquals(AcquiredChannelSohType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD,
        h04N1_analog_sohs.get(0).getType());
    assertEquals(H04_START_TIME, h04N1_analog_sohs.get(0).getStartTime());
    assertEquals(H04N_END_TIME, h04N1_analog_sohs.get(0).getEndTime());
    List<AcquiredChannelSohBoolean> h04N1_boolean_sohs = h04N1_sohs.stream()
        .filter(soh -> (soh instanceof AcquiredChannelSohBoolean))
        .map(AcquiredChannelSohBoolean.class::cast)
        .collect(Collectors.toList());
    assertEquals(16, h04N1_boolean_sohs.size());
    Set<AcquiredChannelSohType> booleanSohTypes = new HashSet<>();
    for (AcquiredChannelSohBoolean boolSoh : h04N1_boolean_sohs) {
      assertFalse(boolSoh.getStatus());
      assertEquals(H04_START_TIME, boolSoh.getStartTime());
      assertEquals(H04N_END_TIME, boolSoh.getEndTime());
      booleanSohTypes.add(boolSoh.getType());
    }
    assertEquals(booleanSohTypes, Set.of(AcquiredChannelSohType.DEAD_SENSOR_CHANNEL,
        AcquiredChannelSohType.ZEROED_DATA, AcquiredChannelSohType.CLIPPED,
        AcquiredChannelSohType.CALIBRATION_UNDERWAY, AcquiredChannelSohType.EQUIPMENT_HOUSING_OPEN,
        AcquiredChannelSohType.VAULT_DOOR_OPENED, AcquiredChannelSohType.AUTHENTICATION_SEAL_BROKEN,
        AcquiredChannelSohType.EQUIPMENT_MOVED, AcquiredChannelSohType.CLOCK_DIFFERENTIAL_TOO_LARGE,
        AcquiredChannelSohType.GPS_RECEIVER_OFF, AcquiredChannelSohType.GPS_RECEIVER_UNLOCKED,
        AcquiredChannelSohType.DIGITIZER_ANALOG_INPUT_SHORTED,
        AcquiredChannelSohType.DIGITIZER_CALIBRATION_LOOP_BACK,
        AcquiredChannelSohType.DIGITIZING_EQUIPMENT_OPEN,
        AcquiredChannelSohType.MAIN_POWER_FAILURE, AcquiredChannelSohType.BACKUP_POWER_UNSTABLE));
    // do similar tests for channel segments from s4DataFrame.json.
    List<ChannelSegment<Waveform>> h07S_segments = segmentCaptor.getAllValues().stream()
        .flatMap(Collection::stream)
        .filter(s -> s.getName().contains("H07S"))
        .sorted(Comparator.comparing(ChannelSegment::getName))
        .collect(Collectors.toList());
    assertEquals(3, h07S_segments.size());
    for (ChannelSegment<Waveform> h07S_segment : h07S_segments) {
      assertEquals(H07S_START_TIME, h07S_segment.getStartTime());
      assertEquals(H07S_END_TIME, h07S_segment.getEndTime());
      List<Waveform> waveforms = h07S_segment.getTimeseries();
      assertEquals(1, waveforms.size());
      assertEquals(H07S_SAMPLE_RATE, waveforms.get(0).getSampleRate(), TOLERANCE);
      // TODO: assert specific waveform sample values?
    }
    // assert that the SystemControllerNotifier was told about the file
    // that was in the manifest but not present.
    List<String> allNotifiedFileNames = sysControllerCaptor.getAllValues()
        .stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    Set<String> uniqueFileNames = new HashSet<>(allNotifiedFileNames);
    // assert no duplicates, then assert content is correct.
    assertEquals(allNotifiedFileNames.size(), uniqueFileNames.size());
    assertEquals(Set.of("non-existent-frame.json"), uniqueFileNames);
  }

  /**
   * Deletes the target if it exists then creates a new empty file.
   */
  private static void createOrReplaceFileAndDirectories(String target) throws IOException {

    Path path = Paths.get(target);
    if(Files.exists(path)){
      final List<Path> filesToDelete = Files.walk(path).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
      for(Path filePath : filesToDelete) {
        Files.deleteIfExists(filePath);
      }
      System.out.println("Deleted existing directory: " + target);
    }
    System.out.println("Target monitored directory will be created at: " + target);

    Files.createDirectories(path);
  }

  private static void copyTestFilesIntoMonitoredDirectory() throws Exception {
    for (String f : Constants.testFiles) {
      Files.copy(Paths.get(Constants.RESOURCES_DIR + f),
          Paths.get(dataframeParserConfig.monitoredDirLocation + File.separator + f));
    }
  }
}

