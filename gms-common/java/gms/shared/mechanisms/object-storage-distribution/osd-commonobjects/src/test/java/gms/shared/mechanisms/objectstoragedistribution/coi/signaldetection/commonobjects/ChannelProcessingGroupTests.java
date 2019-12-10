package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChannelProcessingGroupTests {

  private final UUID id = UUID.randomUUID();
  private final ChannelProcessingGroupType type = ChannelProcessingGroupType.BEAM;
  private final Instant actualChangeTime = Instant.now().minusSeconds(500);
  private final Instant systemChangeTime = Instant.now();
  private final String status = "This is tha status";
  private final String comment = "This is a comment";
  private final Set<UUID> channelIds = Set.of(UUID.randomUUID());

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.channelProcessingGroup,
        ChannelProcessingGroup.class);
  }

  @Test
  public void testCreate() {
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, channelIds, actualChangeTime, systemChangeTime, status, comment);

    assertEquals(group.getType(), type);
    assertEquals(group.getChannelIds(), channelIds);
    assertEquals(group.getActualChangeTime(), actualChangeTime);
    assertEquals(group.getSystemChangeTime(), systemChangeTime);
    assertEquals(group.getStatus(), status);
    assertEquals(group.getComment(), comment);
  }

  @Test
  public void testFrom() {
    ChannelProcessingGroup group = ChannelProcessingGroup
        .from(id, type, channelIds, actualChangeTime, systemChangeTime, status, comment);

    assertEquals(group.getId(), id);
    assertEquals(group.getType(), type);
    assertEquals(group.getChannelIds(), channelIds);
    assertEquals(group.getActualChangeTime(), actualChangeTime);
    assertEquals(group.getSystemChangeTime(), systemChangeTime);
    assertEquals(group.getStatus(), status);
    assertEquals(group.getComment(), comment);
  }

  @Test
  public void testNullIdExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null channel processing id.");
    ChannelProcessingGroup group2 = ChannelProcessingGroup
        .from(null, type, channelIds,
            actualChangeTime, systemChangeTime, status, comment);
  }

  @Test
  public void testNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null channel processing type.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(null, channelIds,
            actualChangeTime, systemChangeTime, status, comment);

    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null channel processing type.");
    ChannelProcessingGroup group2 = ChannelProcessingGroup
        .from(id, null, channelIds,
            actualChangeTime, systemChangeTime, status, comment);
  }

  @Test
  public void testNullReferenceChannelIdsExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("ChannelProcessingGroup expects non-null set of channel ids.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, null,
            actualChangeTime, systemChangeTime, status, comment);

    exception.expect(NullPointerException.class);
    exception
        .expectMessage("ChannelProcessingGroup expects non-null set of channel ids.");
    ChannelProcessingGroup group2 = ChannelProcessingGroup
        .from(id, type, null,
            actualChangeTime, systemChangeTime, status, comment);
  }

  @Test
  public void testNullActualTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null actual change time.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, channelIds,
            null, systemChangeTime, status, comment);

    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null actual change time.");
    ChannelProcessingGroup group2 = ChannelProcessingGroup
        .from(id, type, channelIds,
            null, systemChangeTime, status, comment);
  }

  @Test
  public void testNullSystemTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null system change time.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, channelIds,
            actualChangeTime, null, status, comment);

    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null system change time.");
    ChannelProcessingGroup group2 = ChannelProcessingGroup
        .from(id, type, channelIds,
            actualChangeTime, null, status, comment);
  }

  @Test
  public void testNullStatusExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null status.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, channelIds,
            actualChangeTime, systemChangeTime, null, comment);

    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null status.");
    ChannelProcessingGroup group2 = ChannelProcessingGroup
        .from(id, type, channelIds,
            actualChangeTime, systemChangeTime, null, comment);
  }

  @Test
  public void testNullCommentExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null comment.");
    ChannelProcessingGroup group = ChannelProcessingGroup
        .create(type, channelIds,
            actualChangeTime, systemChangeTime, status, null);

    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingGroup expects non-null comment.");
    ChannelProcessingGroup group2 = ChannelProcessingGroup
        .from(id, type, channelIds,
            actualChangeTime, systemChangeTime, status, null);
  }

}

