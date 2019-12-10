package gms.core.waveformqc.channelsohqc.plugin;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ChannelSohPluginParametersTests {

  @Test
  public void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    ChannelSohPluginParameters expected = ChannelSohPluginParameters
        .from(Duration.ofHours(1), List.of(AcquiredChannelSohType.VAULT_DOOR_OPENED));
    assertEquals(expected, objectMapper
        .readValue(objectMapper.writeValueAsString(expected), ChannelSohPluginParameters.class));
  }
}
