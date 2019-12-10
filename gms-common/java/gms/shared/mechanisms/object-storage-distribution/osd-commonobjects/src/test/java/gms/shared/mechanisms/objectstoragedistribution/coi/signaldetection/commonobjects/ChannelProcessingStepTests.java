package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingStep.ChannelProcessingStepType;
import java.time.Instant;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChannelProcessingStepTests {

  private final ChannelProcessingStepType type = ChannelProcessingStepType.COHERENT_BEAM;
  private final Instant actualChangeTime = Instant.now().minusSeconds(500);
  private final Instant systemChangeTime = Instant.now();
  private final String status = "This is tha status";
  private final String comment = "This is a comment";


  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreate() {
    UUID processingGroupId = UUID.randomUUID();
    String name = "Test";
    ChannelProcessingStep step = ChannelProcessingStep
        .create(processingGroupId, name, type, actualChangeTime, systemChangeTime, status, comment);

    assertEquals(step.getProcessingGroupId(), processingGroupId);
    assertEquals(step.getName(), name);
    assertEquals(step.getType(), type);
    assertEquals(step.getActualChangeTime(), actualChangeTime);
    assertEquals(step.getSystemChangeTime(), systemChangeTime);
    assertEquals(step.getStatus(), status);
    assertEquals(step.getComment(), comment);
  }

  @Test
  public void testCreateNullGroupIdExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingStep expects non-null channel processing processing group id.");
    ChannelProcessingStep step = ChannelProcessingStep
        .create(null, "Test", type, actualChangeTime, systemChangeTime, status, comment);
  }

  @Test
  public void testCreateNullNameExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingStep expects non-null name.");
    ChannelProcessingStep step = ChannelProcessingStep
        .create(UUID.randomUUID(), null, type, actualChangeTime, systemChangeTime, status, comment);
  }

  @Test
  public void testCreateNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingStep expects non-null channel processing step type.");
    ChannelProcessingStep step = ChannelProcessingStep
        .create(UUID.randomUUID(), "Test", null, actualChangeTime, systemChangeTime, status, comment);
  }

  @Test
  public void testCreateNullActualTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingStep expects non-null actual change time.");
    ChannelProcessingStep step = ChannelProcessingStep
        .create(UUID.randomUUID(), "Test", type, null, systemChangeTime, status, comment);
  }

  @Test
  public void testCreateNullSystemTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingStep expects non-null system change time.");
    ChannelProcessingStep step = ChannelProcessingStep
        .create(UUID.randomUUID(), "Test", type, actualChangeTime, null, status, comment);
  }

  @Test
  public void testCreateNullStatusExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingStep expects non-null status.");
    ChannelProcessingStep step = ChannelProcessingStep
        .create(UUID.randomUUID(), "Test", type, actualChangeTime, systemChangeTime, null, comment);
  }

  @Test
  public void testCreateNullCommentExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ChannelProcessingStep expects non-null comment.");
    ChannelProcessingStep step = ChannelProcessingStep
        .create(UUID.randomUUID(), "Test", type, actualChangeTime, systemChangeTime, status, null);
  }

}