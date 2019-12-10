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

class WaveformSpike3PtQcPluginComponentTests {

  private static final ChannelSegment<Waveform> testWaveform = ChannelSegment
      .from(new UUID(0L, 0L), new UUID(0L, 0L), "default",
          Type.RAW, Timeseries.Type.WAVEFORM, List.of(
              Waveform.withoutValues(Instant.EPOCH, 40, 100)), CreationInfo.DEFAULT);

  private static Map<String, Object> pluginConfiguration() {
    return Map.of(
        "minConsecutiveSampleDifferenceSpikeThreshold", 0.8,
        "rmsAmplitudeRatioThreshold", 3.0,
        "rmsLeadSampleDifferences", 3,
        "rmsLagSampleDifferences", 4);
  }

  @Test
  void testGetName() {
    assertEquals("waveformSpike3PtQcPlugin",
        new WaveformSpike3PtQcPluginComponent().getName());
  }

  @Test
  void testGenerateNullChannelSegmentsExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new WaveformSpike3PtQcPluginComponent()
            .generateQcMasks(null, emptyList(), emptyList(), pluginConfiguration()));
  }

  @Test
  void testGenerateNullSohStatusExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new WaveformSpike3PtQcPluginComponent()
            .generateQcMasks(testWaveform, null, emptyList(), pluginConfiguration()));
  }

  @Test
  void testGenerateNullExistingQcMasksExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new WaveformSpike3PtQcPluginComponent()
            .generateQcMasks(testWaveform, emptyList(), null, pluginConfiguration()));
  }

  @Test
  void testGenerateNullCreationInfoIdExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new WaveformSpike3PtQcPluginComponent()
            .generateQcMasks(testWaveform, emptyList(), emptyList(), null));
  }

}
