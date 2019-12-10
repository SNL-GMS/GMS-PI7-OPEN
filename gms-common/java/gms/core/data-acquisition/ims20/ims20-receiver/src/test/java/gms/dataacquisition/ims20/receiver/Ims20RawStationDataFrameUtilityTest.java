package gms.dataacquisition.ims20.receiver;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class Ims20RawStationDataFrameUtilityTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  /**
   * Create a raw station data frame (RSDF) using an array station's response, defined in test
   * fixtures. Assert that the attributes of the created RSDF match what is expected.
   */
  @Test
  public void TestArrayStation() throws Exception {
    byte[] payloadBytes = TestFixtures.kurkResponseString.getBytes();

    final RawStationDataFrame rsdf = Ims20RawStationDataFrameUtility.
        parseAcquiredStationDataPacket(payloadBytes,
            TestFixtures.RECEPTION_TIME, TestFixtures.SDAG_KURK);
    assertEquals(TestFixtures.STATION_ID_KURK, rsdf.getStationId());
    assertEquals(Set.of(TestFixtures.CHANNEL_ID_KUR01_BHZ, TestFixtures.CHANNEL_ID_KURBB_BH1),
        rsdf.getChannelIds());
    assertEquals(TestFixtures.ACQUISITION_PROTOCOL_WAVEFORM, rsdf.getAcquisitionProtocol());
    assertEquals(TestFixtures.PAYLOAD_START_TIME, rsdf.getPayloadDataStartTime());
    assertEquals(TestFixtures.PAYLOAD_END_TIME_1, rsdf.getPayloadDataEndTime());
    assertEquals(TestFixtures.RECEPTION_TIME, rsdf.getReceptionTime());
    assertArrayEquals(payloadBytes, rsdf.getRawPayload());
    assertEquals(AuthenticationStatus.NOT_APPLICABLE, rsdf.getAuthenticationStatus());
  }

  /**
   * Create a raw station data frame (RSDF) using a 3-component station's response, defined in test
   * fixtures. Assert that the attributes of the created RSDF match what is expected.
   */
  @Test
  public void TestThreeComponentStation() throws Exception {
    byte[] payloadBytes = TestFixtures.aakResponseString.getBytes();

    final RawStationDataFrame rsdf = Ims20RawStationDataFrameUtility.
        parseAcquiredStationDataPacket(payloadBytes,
            TestFixtures.RECEPTION_TIME, TestFixtures.SDAG_AAK);
    assertEquals(TestFixtures.STATION_ID_AAK, rsdf.getStationId());
    assertEquals(Set.of(TestFixtures.CHANNEL_ID_AAK_BHE, TestFixtures.CHANNEL_ID_AAK_BHN),
        rsdf.getChannelIds());
    assertEquals(TestFixtures.ACQUISITION_PROTOCOL_WAVEFORM, rsdf.getAcquisitionProtocol());
    assertEquals(TestFixtures.PAYLOAD_START_TIME, rsdf.getPayloadDataStartTime());
    assertEquals(TestFixtures.PAYLOAD_END_TIME_1, rsdf.getPayloadDataEndTime());
    assertEquals(TestFixtures.RECEPTION_TIME, rsdf.getReceptionTime());
    assertArrayEquals(payloadBytes, rsdf.getRawPayload());
    assertEquals(AuthenticationStatus.NOT_APPLICABLE, rsdf.getAuthenticationStatus());
  }

  /**
   * Requests to the IMS server are for one station. Verify that seeing 2 different station names
   * (FOO and BAR) in one response file will throw an exception and not create an RSDF. Expect an
   * exception
   */
  @Test(expected = UnsupportedOperationException.class)
  public void TestTwoDifferentStations() throws Exception {
    String payloadString =
        "WID2 2019/02/14 18:30:00.000 FOO     BHZ      CM6    2400    40.000000   3.04e-01   1.000 STS-2   90.0 90.0\n"
            + "STA2            37.46900   14.35    330 WGS-84       0.735 0.000\n"
            + "DAT2\n"
            + "xv0hu5V7P4NJF6V3n1W-KGV4m2VMBkOHHQV9l8kPVBF-KLkEV4l-VQl2lAWOm5V6kRD9kO9kTVCkKVSm\n"
            + "CHK2 51757323\n"
            + "WID2 2019/02/14 18:40:00.000 BAR     BH1      CM6    24000   80.000000   3.04e-01   1.000 STS-2   90.0 90.0\n"
            + "STA2            37.46900   14.35    330 WGS-84       0.735 0.000\n"
            + "DAT2\n"
            + "xv0hu5V7P4NJF6V3n1W-KGV4m2VMBkOHHQV9l8kPVBF-KLkEV4l-VQl2lAWOm5V6kRD9kO9kTVCkKVSm\n"
            + "CHK2 51757323\n";
    byte[] payloadBytes = payloadString.getBytes();

    final RawStationDataFrame rsdf = Ims20RawStationDataFrameUtility.
        parseAcquiredStationDataPacket(payloadBytes,
            TestFixtures.RECEPTION_TIME, TestFixtures.SDAG);
  }

  /**
   * Each Waveform ID Block should begin with "WID2", otherwise it is invalid (see
   * "FOO"). Expect an error.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void TestWid2() throws Exception {
    String payloadString =
        "FOO 2019/02/14 18:40:00.000 KUR01   BHZ      CM6    2400    40.000000   3.04e-01   1.000 STS-2   90.0 90.0\n"
            + "STA2            37.46900   14.35    330 WGS-84       0.735 0.000\n"
            + "DAT2\n"
            + "xv0hu5V7P4NJF6V3n1W-KGV4m2VMBkOHHQV9l8kPVBF-KLkEV4l-VQl2lAWOm5V6kRD9kO9kTVCkKVSm\n"
            + "CHK2 51757323\n"
            + "FOO 2019/02/14 18:40:00.000 KURBB   BH1      CM6    24000   80.000000   3.04e-01   1.000 STS-2   90.0 90.0\n"
            + "STA2            37.46900   14.35    330 WGS-84       0.735 0.000\n"
            + "DAT2\n"
            + "xv0hu5V7P4NJF6V3n1W-KGV4m2VMBkOHHQV9l8kPVBF-KLkEV4l-VQl2lAWOm5V6kRD9kO9kTVCkKVSm\n"
            + "CHK2 51757323\n";
    byte[] payloadBytes = payloadString.getBytes();

    final RawStationDataFrame rsdf = Ims20RawStationDataFrameUtility.
        parseAcquiredStationDataPacket(payloadBytes,
            TestFixtures.RECEPTION_TIME, TestFixtures.SDAG);
  }

  /**
   * Test that an empty StationAndChannelId map, a Station Data AcquisitionGroup (SDAG) parameter
   * fails. Expect an exception.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void TestBadStationAndChannelIdInSdag() throws Exception {
    byte[] payloadBytes = TestFixtures.kurkResponseString.getBytes();

    final RawStationDataFrame rsdf = Ims20RawStationDataFrameUtility.
        parseAcquiredStationDataPacket(payloadBytes,
            TestFixtures.RECEPTION_TIME, TestFixtures.SDAG);
  }

  /**
   * Tests that cm6 data can be successfully read and parsed into waveform channel segments without
   * throwing an exception.
   */
  @Test
  public void testChannelSegmentCreation() throws Exception {
    // Read in contents of CM6 file
    final InputStream cm6Is = this.getClass().getResourceAsStream(TestFixtures.KURK_CM6_FILE);
    assertNotNull(cm6Is);

    final byte[] cm6Bytes = cm6Is.readAllBytes();
    final List<Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>>> channelSegments =
        Ims20RawStationDataFrameUtility.parseRawStationDataFrame(cm6Bytes);

    Ims20RawStationDataFrameUtility.parseRawStationDataFrame(cm6Bytes);
    assertNotNull(channelSegments);
    assertEquals(2, channelSegments.size());

    for (Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>> cs : channelSegments) {
      assertTrue(cs.isPresent());
      final Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>> pair = cs.get();
      assertNotNull(pair);
      final ChannelSegment<Waveform> segment = pair.getLeft();
      assertNotNull(segment);
      assertEquals(Type.ACQUIRED, segment.getType());
      assertEquals(Timeseries.Type.WAVEFORM, segment.getTimeseriesType());
      final List<Waveform> waveforms = segment.getTimeseries();
      assertEquals(1, waveforms.size());
      final Collection<AcquiredChannelSoh> sohs = pair.getRight();
      assertNotNull(sohs);
      // TODO: when SOH parsing is implemented, add testing on SOH and remove below assertion
      assertTrue(sohs.isEmpty());
    }

    final Waveform firstWaveform = channelSegments.get(0).get().getLeft().getTimeseries().get(0);
    assertEquals(TestFixtures.KURK_CM6_FILE_START_TIME, firstWaveform.getStartTime());
    final Waveform secondWaveform = channelSegments.get(1).get().getLeft().getTimeseries().get(0);
    assertEquals(TestFixtures.KURK_CM6_FILE_START_TIME, secondWaveform.getStartTime());
  }
}
