package gms.core.signalenhancement.fk.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class FkSpectraCommandTests {

  private FkSpectraCommand validCommand;

  @Before
  public void setUp() {
    validCommand = FkSpectraCommand.builder()
        .setStartTime(Instant.EPOCH)
        .setSampleRate(5.0)
        .setSampleCount((long) 20)
        .setChannelIds(Set.of(UUID.randomUUID()))
        .setWindowLead(Duration.ZERO)
        .setWindowLength(Duration.ofSeconds(4))
        .setLowFrequency(1.0)
        .setHighFrequency(2.0)
        .setUseChannelVerticalOffset(true)
        .setNormalizeWaveforms(false)
        .setPhaseType(PhaseType.P)
        .setSlowStartX(3.0)
        .setSlowDeltaX(4.0)
        .setSlowCountX(5)
        .setSlowStartY(3.0)
        .setSlowDeltaY(4.0)
        .setSlowCountY(5)
        .setOutputChannelId(UUID.randomUUID())
        .build();
  }

  @Test
  public void testBuildNegativeSampleRate() {
    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> validCommand.toBuilder().setSampleRate(-1.0).build());
    assertEquals("Error creating FkSpectraCommand: Sample Rate must be greater than 0", e.getMessage());
  }

  @Test
  public void testBuildNegativeSampleCount() {
    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> validCommand.toBuilder().setSampleCount((long) -3).build());

    assertEquals("Error creating FkSpectraCommand: Sample Count must be greater than 0", e.getMessage());
  }

  @Test
  public void testBuildEmptyChannelIds() {
    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> validCommand.toBuilder().setChannelIds(Set.of()).build());

    assertEquals("Error creating FkSpectraCommand: Channel IDs cannot be empty", e.getMessage());
  }

  @Test
  public void testSerialization() throws IOException {
    String json = CoiObjectMapperFactory.getJsonObjectMapper().writeValueAsString(validCommand);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    FkSpectraCommand deserialized = CoiObjectMapperFactory.getJsonObjectMapper().readValue(json, FkSpectraCommand.class);
    assertEquals(validCommand, deserialized);
  }
}
