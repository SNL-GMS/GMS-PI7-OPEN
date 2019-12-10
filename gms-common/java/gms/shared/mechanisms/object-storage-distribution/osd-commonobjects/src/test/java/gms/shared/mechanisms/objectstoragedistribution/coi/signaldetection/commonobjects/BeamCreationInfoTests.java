package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class BeamCreationInfoTests {

  private final UUID id = UUID.randomUUID();
  private final Instant creationTime = Instant.EPOCH;
  private final String name = "CHAN SEG NAME (test)";
  private final UUID processingGroupId = UUID.randomUUID();
  private final UUID channelId = UUID.randomUUID();
  private final UUID channelSegmentId = UUID.randomUUID();
  private final Instant requestedStartTime = Instant.ofEpochSecond(12345);
  private final Instant requestedEndTime = requestedStartTime.plus(Duration.ofSeconds(90));
  private final Set<UUID> usedInputChannelIds = new HashSet<>(Arrays.asList(
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

  private final BeamDefinition beamDefinition = SignalDetectionTestFixtures.BEAM_DEFINITION;
  private BeamCreationInfo beamCreationInfo = SignalDetectionTestFixtures.BEAM_CREATION_INFO;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(beamCreationInfo,
        BeamCreationInfo.class);
  }

  @Test
  public void testFromEmptyNameThrowsIllegalStateException() {
    assertThrows(IllegalStateException.class,
        () -> BeamCreationInfo.from(
            id,
            creationTime,
            "",
            Optional.of(processingGroupId),
            channelId,
            channelSegmentId,
            requestedStartTime,
            requestedEndTime,
            beamDefinition,
            usedInputChannelIds));
  }

  @Test
  public void testFrom() {
    BeamCreationInfo beamCreationInfo = BeamCreationInfo.from(
        id,
        creationTime,
        name,
        Optional.of(processingGroupId),
        channelId,
        channelSegmentId,
        requestedStartTime,
        requestedEndTime,
        beamDefinition,
        usedInputChannelIds);

    assertEquals(id, beamCreationInfo.getId());
    assertEquals(creationTime, beamCreationInfo.getCreationTime());
    assertEquals(name, beamCreationInfo.getName());

    assertTrue(beamCreationInfo.getProcessingGroupId().isPresent());
    assertEquals(processingGroupId, beamCreationInfo.getProcessingGroupId().get());
    assertEquals(requestedStartTime, beamCreationInfo.getRequestedStartTime());
    assertEquals(requestedEndTime, beamCreationInfo.getRequestedEndTime());
    assertEquals(beamDefinition, beamCreationInfo.getBeamDefinition());
  }

  @Test
  public void testBuilderEmptyNameThrowsIllegalStateException() {
    assertThrows(IllegalStateException.class,
        () -> beamCreationInfo.toBuilder().setName("").build());
  }

}
