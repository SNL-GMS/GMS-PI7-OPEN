package gms.core.waveformqc.waveformsignalqc.plugin;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class WaveformGapQcPluginParametersTests {

  @Test
  public void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    WaveformGapQcPluginParameters expected = WaveformGapQcPluginParameters
        .from(1);
    assertEquals(expected, objectMapper
        .readValue(objectMapper.writeValueAsString(expected), WaveformGapQcPluginParameters.class));
  }
}
