package gms.core.signalenhancement.beam.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.signalenhancement.beam.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BeamStreamingCommandTests {

  private ChannelSegment<Waveform> waveforms;
  private BeamDefinition beamDefinition;

  @BeforeEach
  public void setUp() {
    final Waveform wf = Waveform.withValues(Instant.EPOCH, 4, new double[]{1, 2, 3, 4, 5});

    waveforms = ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "MockSegment",
        ChannelSegment.Type.FILTER, List.of(wf), CreationInfo.DEFAULT);

    beamDefinition = TestFixtures.getBeamDefinition();
  }

  @Test
  void testSerialization() throws IOException {
    BeamStreamingCommand command = BeamStreamingCommand.builder()
        .setBeamDefinition(beamDefinition)
        .setOutputChannelId(UUID.randomUUID())
        .setWaveforms(Set.of(waveforms))
        .build();

    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    String json = jsonObjectMapper.writeValueAsString(command);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    BeamStreamingCommand deserialized = jsonObjectMapper.readValue(json,
        BeamStreamingCommand.class);
    assertEquals(command, deserialized);
  }

}
