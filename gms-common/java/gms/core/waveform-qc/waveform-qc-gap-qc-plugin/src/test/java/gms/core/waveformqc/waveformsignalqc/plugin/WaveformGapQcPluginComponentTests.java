package gms.core.waveformqc.waveformsignalqc.plugin;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WaveformGapQcPluginComponentTests {

  ChannelSegment<Waveform> testWaveform = ChannelSegment
      .from(new UUID(0L, 0L), new UUID(0L, 0L), "default",
          Type.RAW, Timeseries.Type.WAVEFORM, List.of(
              Waveform.withoutValues(Instant.EPOCH, 40, 100)), CreationInfo.DEFAULT);

  private static Map<String, Object> getDefaultPluginConfiguration() {
    return Map.of("minLongGapLengthInSamples", 10);
  }

  @Test
  void testGetName() {
    assertEquals("waveformGapQcPlugin", new WaveformGapQcPluginComponent().getName());
  }

  @Test
  void testGenerateNullChannelSegmentsExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new WaveformGapQcPluginComponent()
            .generateQcMasks(null, emptyList(), emptyList(), getDefaultPluginConfiguration()));
  }

  @Test
  void testGenerateNullSohStatusExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new WaveformGapQcPluginComponent()
            .generateQcMasks(testWaveform, null, emptyList(), getDefaultPluginConfiguration()));
  }

  @Test
  void testGenerateNullExistingQcMasksExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new WaveformGapQcPluginComponent()
            .generateQcMasks(testWaveform, emptyList(), null, getDefaultPluginConfiguration()));
  }

  @Test
  void testGenerateNullParameterFieldMapExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new WaveformGapQcPluginComponent()
            .generateQcMasks(testWaveform, emptyList(), emptyList(), null));
  }

}
