package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class WaveformRepeatedAmplitudeQcPluginParametersTests {

  @Test
  public void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    WaveformRepeatedAmplitudeQcPluginParameters expected = WaveformRepeatedAmplitudeQcPluginParameters
        .from(2, 0.25, 1.2);
    assertEquals(expected, objectMapper
        .readValue(objectMapper.writeValueAsString(expected),
            WaveformRepeatedAmplitudeQcPluginParameters.class));
  }

  @Test
  public void testCreateSeriesLengthTooShortExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformRepeatedAmplitudeQcPluginParameters.from(1, 0.25, 1.0));
  }

  @Test
  public void testCreateAmplitudeDeltaNegativeExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformRepeatedAmplitudeQcPluginParameters.from(2, 0.0 - Double.MIN_NORMAL, 1.0));
  }

  @Test
  public void testCreateMergeThresholdNegativeExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformRepeatedAmplitudeQcPluginParameters.from(2, 1.0, 0.0 - Double.MIN_NORMAL));
  }
}
