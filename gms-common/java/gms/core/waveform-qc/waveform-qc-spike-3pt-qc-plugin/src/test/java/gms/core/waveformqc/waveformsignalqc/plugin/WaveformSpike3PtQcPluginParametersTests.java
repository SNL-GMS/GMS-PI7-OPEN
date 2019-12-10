package gms.core.waveformqc.waveformsignalqc.plugin;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class WaveformSpike3PtQcPluginParametersTests {

  @Test
  public void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    WaveformSpike3PtQcPluginParameters expected = WaveformSpike3PtQcPluginParameters.from(.9,
        2, 3, 4);
    assertEquals(expected, objectMapper.readValue(objectMapper.writeValueAsString(expected),
        WaveformSpike3PtQcPluginParameters.class));
  }

  @Test
  public void testCreateNotPositiveConsecutiveSampleDifferenceExceedsLowerLimitExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformSpike3PtQcPluginParameters.from(0, 2.0, 3, 4));
  }

  @Test
  public void testCreateNotPositiveConsecutiveSampleDifferenceExceedsUpperLimitExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformSpike3PtQcPluginParameters.from(1, 2.0, 3, 4));
  }

  @Test
  public void testCreateNotPositiveRmsAmplitudeRatioThresholdExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformSpike3PtQcPluginParameters.from(0.9, 0.0, 3, 4));
  }

  @Test
  public void testCreateNotPositiveRmsLeadSampleDifferenceExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformSpike3PtQcPluginParameters.from(0.9, 2.0, -1, 4));
  }

  @Test
  public void testCreateNotPositiveRmsLagSampleDifferenceExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformSpike3PtQcPluginParameters.from(0.9, 2.0, 3, -1));
  }

  @Test
  public void testCreateNotPositiveRmsLeadLagSumExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> WaveformSpike3PtQcPluginParameters.from(0.9, 2.0, 1, 0));
  }
}
