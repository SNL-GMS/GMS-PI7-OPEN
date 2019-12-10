package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests {@link Waveform} creation and usage semantics Created by trsault on 8/25/17.
 */
public class ChannelSegmentTests {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private final UUID segmentId = UUID.fromString("8952f988-ff83-4f3d-a832-a82a04022539"),
      channelId = UUID.fromString("41ea0291-af5e-4694-a551-a215f95c78d1");
  private final ChannelSegment.Type type = ChannelSegment.Type.ACQUIRED;
  private final Timeseries.Type seriestype = Timeseries.Type.WAVEFORM;
  private final Instant start = Instant.EPOCH;
  private final Waveform earlierWaveform = Waveform.withoutValues(
      start, 1.0, 5);
  private final Waveform laterWaveform = Waveform.withoutValues(
      start.plusSeconds(30), 1.0, 10);
  private final List<Waveform> wfs = List
      .of(laterWaveform, earlierWaveform);  // purposely in reverse time order

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ChannelSegment.class);
  }

  @Test
  public void testChannelSegmentCreateNullArguments() throws Exception {
    // Test analog SOH
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ChannelSegment.class, "create",
        channelId, "NAME", type, wfs, CreationInfo.DEFAULT);
  }

  @Test
  public void testChannelSegmentFromNullArguments() throws Exception {
    // Test analog SOH
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ChannelSegment.class, "from", segmentId,
        channelId, "NAME", type, wfs, CreationInfo.DEFAULT);
  }

  @Test
  public void testCreate() {
    final ChannelSegment<Waveform> segment = ChannelSegment
        .create(channelId, "NAME", type, wfs, CreationInfo.DEFAULT);
    assertEquals("NAME", segment.getName());
    assertEquals(channelId, segment.getChannelId());
    assertEquals(type, segment.getType());
    assertEquals(seriestype, segment.getTimeseriesType());
    assertEquals(earlierWaveform.getStartTime(), segment.getStartTime());
    assertEquals(laterWaveform.getEndTime(), segment.getEndTime());
    // below: using set so order doesn't matter.  channel segment sorts it's series.
    assertEquals(new HashSet<>(wfs), new HashSet<>(segment.getTimeseries()));
    assertEquals(CreationInfo.DEFAULT, segment.getCreationInfo());
  }

  @Test
  public void testFrom() {
    final ChannelSegment<Waveform> segment = ChannelSegment
        .from(segmentId, channelId, "NAME", type, wfs, CreationInfo.DEFAULT);
    assertEquals("NAME", segment.getName());
    assertEquals(segmentId, segment.getId());
    assertEquals(channelId, segment.getChannelId());
    assertEquals(type, segment.getType());
    assertEquals(seriestype, segment.getTimeseriesType());
    assertEquals(earlierWaveform.getStartTime(), segment.getStartTime());
    assertEquals(laterWaveform.getEndTime(), segment.getEndTime());
    // below: using set so order doesn't matter.  channel segment sorts it's series.
    assertEquals(new HashSet<>(wfs), new HashSet<>(segment.getTimeseries()));
    assertEquals(CreationInfo.DEFAULT, segment.getCreationInfo());
  }

  @Test
  public void testEmptySeriesExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("ChannelSegment requires at least one timeseries");
    ChannelSegment.create(channelId, "NAME", type, List.of(), CreationInfo.DEFAULT);
  }

  @Test
  public void testBlankNameExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("ChannelSegment requires a non-blank name");
    ChannelSegment.create(channelId, "", type, wfs, CreationInfo.DEFAULT);
  }

  @Test
  public void testOverlappingSeriesIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("ChannelSegment cannot have overlapping timeseries");
    Collection<Waveform> overlappingWaveforms = List.of(
        Waveform.withoutValues(start, 5.0, 50),
        Waveform.withoutValues(start.plusSeconds(1), 100.0, 50));
    ChannelSegment.create(channelId, "NAME", type,
        overlappingWaveforms, CreationInfo.DEFAULT);
  }

  @Test
  public void testCompareExpectNegative() {
    final ChannelSegment<Waveform> segment1 = ChannelSegment
        .create(channelId, "NAME", type, wfs, CreationInfo.DEFAULT);

    final List<Waveform> wfs2 = List.of(Waveform.withoutValues(
        start.plusMillis(1), 1.0, 1));
    final ChannelSegment<Waveform> segment2 = ChannelSegment
        .create(channelId, "NAME", type, wfs2,
            CreationInfo.DEFAULT);

    assertTrue(segment1.compareTo(segment2) < 0);
  }

  @Test
  public void testCompareExpectPositive() throws Exception {
    final List<Waveform> wfs2 = List.of(Waveform.withoutValues(
        start.plusMillis(1), 1.0, 1));
    final ChannelSegment<Waveform> segment1 = ChannelSegment
        .create(channelId, "NAME", type, wfs2,
            CreationInfo.DEFAULT);

    final ChannelSegment<Waveform> segment2 = ChannelSegment
        .create(channelId, "NAME", type, wfs, CreationInfo.DEFAULT);

    assertTrue(segment1.compareTo(segment2) > 0);
  }

  @Test
  public void testCompareEqualExpectPositive() throws Exception {
    final ChannelSegment<Waveform> segment1 = ChannelSegment
        .create(channelId, "NAME", type, wfs, CreationInfo.DEFAULT);

    final ChannelSegment<Waveform> segment2 = ChannelSegment
        .create(channelId, "NAME", type, wfs, CreationInfo.DEFAULT);

    assertTrue(segment1.compareTo(segment2) > 0);
  }
}