package gms.core.waveformqc.waveformsignalqc.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class WaveformRepeatedAmplitudeQcPluginComponentTests {

  private static final ChannelSegment<Waveform> testWaveform = ChannelSegment
      .from(new UUID(0L, 0L), new UUID(0L, 0L), "default",
          Type.RAW, Timeseries.Type.WAVEFORM, List.of(
              Waveform.withoutValues(Instant.EPOCH, 40, 100)), CreationInfo.DEFAULT);


  private static Map<String, Object> pluginConfiguration() {
    return Map.of(
        "minSeriesLengthInSamples", 2,
        "maxDeltaFromStartAmplitude", 2.5,
        "maskMergeThresholdSeconds", 0.001);
  }

  @Test
  public void testGetName() {
    assertEquals("waveformRepeatedAmplitudeQcPlugin",
        new WaveformRepeatedAmplitudeQcPluginComponent().getName());
  }

  @Test
  public void testGenerateNullChannelSegmentsExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> new WaveformRepeatedAmplitudeQcPluginComponent()
        .generateQcMasks(null, Collections.emptyList(), Collections.emptyList(),
            Collections.emptyMap()));
  }

  @Test
  public void testGenerateNullSohStatusExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> new WaveformRepeatedAmplitudeQcPluginComponent()
        .generateQcMasks(testWaveform, null, Collections.emptyList(), Collections.emptyMap()));
  }

  @Test
  public void testGenerateNullExistingQcMasksExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> new WaveformRepeatedAmplitudeQcPluginComponent()
        .generateQcMasks(testWaveform, Collections.emptyList(), null, Collections.emptyMap()));
  }

  @Test
  public void testGenerateNullParameterFieldMapExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> new WaveformRepeatedAmplitudeQcPluginComponent()
        .generateQcMasks(testWaveform, Collections.emptyList(), Collections.emptyList(), null));
  }
}
