package gms.shared.utilities.signalprocessing.filter;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class FilterTests {

  private double[] forwardCoeffs = new double[]{5.5, 4.4, 3.3, 2.2, 1.1, -6.6};

  private final FilterDefinition firFilterDefinition = FilterDefinition
      .firBuilder()
      .setName("libTest")
      .setDescription("libTestDesc")
      .setFilterPassBandType(FilterPassBandType.BAND_PASS)
      .setLowFrequencyHz(1.0)
      .setHighFrequencyHz(3.0)
      .setOrder(4)
      .setFilterSource(FilterSource.USER)
      .setFilterCausality(FilterCausality.CAUSAL)
      .setZeroPhase(false)
      .setSampleRate(20.0)
      .setSampleRateTolerance(1.0)
      .setbCoefficients(forwardCoeffs)
      .setGroupDelaySecs(3.0)
      .build();

  private final FilterDefinition iirFilterDefinition = FilterDefinition
      .builder()
      .setName("libTest")
      .setDescription("libTestDesc")
      .setFilterType(FilterType.IIR_BUTTERWORTH)
      .setFilterPassBandType(FilterPassBandType.BAND_PASS)
      .setLowFrequencyHz(1.0)
      .setHighFrequencyHz(3.0)
      .setOrder(4)
      .setFilterSource(FilterSource.USER)
      .setFilterCausality(FilterCausality.CAUSAL)
      .setZeroPhase(false)
      .setSampleRate(20.0)
      .setSampleRateTolerance(1.0)
      .setaCoefficients(new double[]{1.0, 3.0})
      .setbCoefficients(forwardCoeffs)
      .setGroupDelaySecs(3.0)
      .build();

  private final Waveform dummyWaveform = Waveform.withValues(Instant.EPOCH, 20.0, new double[5]);

  @Test
  void testFilter() {

    // Impulse at sample 0 -> output will be coefficients in forward order
    final double[] impulse = new double[forwardCoeffs.length];
    Arrays.fill(impulse, 0.0);
    impulse[0] = 1.0;

    final Waveform inputWaveform = Waveform.withValues(Instant.EPOCH, 20.0, impulse);

    Waveform outputWaveform = Filter.filter(inputWaveform, firFilterDefinition);
    assertNotNull(outputWaveform);
    assertEquals(inputWaveform.getStartTime(), outputWaveform.getStartTime());
    assertEquals(inputWaveform.getEndTime(), outputWaveform.getEndTime());
    assertEquals(inputWaveform.getSampleRate(), outputWaveform.getSampleRate(), Double.MIN_NORMAL);
    assertEquals(inputWaveform.getSampleCount(), outputWaveform.getSampleCount());
    assertArrayEquals(forwardCoeffs, outputWaveform.getValues());
  }

  @Test
  void testSampleRateAboveToleranceExpectIllegalArgumentException() {
    final double sampleRate =
        firFilterDefinition.getSampleRate() + firFilterDefinition.getSampleRateTolerance() + 1;

    verifySampleRateException(
        Waveform.withValues(Instant.EPOCH, sampleRate, new double[20]));
  }

  @Test
  void testSampleRateBelowToleranceExpectIllegalArgumentException() {
    final double sampleRate =
        firFilterDefinition.getSampleRate() - firFilterDefinition.getSampleRateTolerance() - 1;

    verifySampleRateException(
        Waveform.withValues(Instant.EPOCH, sampleRate, new double[20]));
  }

  private void verifySampleRateException(Waveform inputWaveform) {
    final double min =
        firFilterDefinition.getSampleRate() - firFilterDefinition.getSampleRateTolerance();
    final double max =
        firFilterDefinition.getSampleRate() + firFilterDefinition.getSampleRateTolerance();

    IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
        () -> Filter.filter(inputWaveform, firFilterDefinition));

    assertEquals(
        String.format("Filter requires input waveform with sampleRate in [%s, %s]", min, max),
        actualException.getMessage());
  }

  @Test
  void testFilterIirExpectIllegalArgumentException() {
    IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
        () -> Filter.filter(dummyWaveform, iirFilterDefinition));

    assertEquals("Only FIR filtering implemented", actualException.getMessage());
  }

  @Test
  void testFilterNullArguments() {
    assertThrows(NullPointerException.class, () -> Filter.filter(null, firFilterDefinition));
    assertThrows(NullPointerException.class, () -> Filter.filter(dummyWaveform, null));
    assertDoesNotThrow(() -> Filter.filter(dummyWaveform, firFilterDefinition));
  }
}
