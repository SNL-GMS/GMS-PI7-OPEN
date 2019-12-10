package gms.core.signaldetection.staltapowerdetector;

import static gms.core.signaldetection.staltapowerdetector.TestFixtures.randomChannelSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import gms.core.signaldetection.staltapowerdetector.StaLtaAlgorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.StaLtaAlgorithm.WaveformTransformation;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.WaveformUtility;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class StaLtaPowerDetectorPluginTests {

  //convenience function for null assertions
  private static Function<Executable, Executable> assertThrowsNullPointer =
      e -> () -> assertThrows(NullPointerException.class, e);

  private final AlgorithmType type = AlgorithmType.STANDARD;
  private final WaveformTransformation transformation = WaveformTransformation.RECTIFIED;
  private final int staLead = 2;
  private final int staLength = 4;
  private final int ltaLead = 4;
  private final int ltaLength = 4;
  private final double triggerThreshold = 12.0;
  private final double detriggerThreshold = 10.0;
  private final double interpolateGapsSampleRateTolerance = 1.2;
  private final double mergeSampleRateTolerance = 1.3;
  private final int length = 100;
  private final double sampleRate = 40.0;
  private final long sampleLengthMillis = (long) ((1.0 / sampleRate) * 1000);
  private final Duration mergeMinLength = Duration.ofMillis((long) (sampleLengthMillis * 0.5));

  private final StaLtaParameters staLtaParameters = StaLtaParameters.from(
      type, transformation, Duration.ofMillis(sampleLengthMillis).multipliedBy(staLead),
      Duration.ofMillis(sampleLengthMillis).multipliedBy(staLength),
      Duration.ofMillis(sampleLengthMillis).multipliedBy(ltaLead),
      Duration.ofMillis(sampleLengthMillis).multipliedBy(ltaLength), triggerThreshold,
      detriggerThreshold, interpolateGapsSampleRateTolerance, mergeSampleRateTolerance,
      mergeMinLength);

  @org.junit.jupiter.api.Test
  void testGetName() {
    assertEquals("staLtaPowerDetectorPlugin", new StaLtaPowerDetectorPlugin().getName());
  }

  @Test
  void testDetectSignalsNullArguments() {
    StaLtaPowerDetectorPlugin plugin = new StaLtaPowerDetectorPlugin();

    Executable nullChannelSegments = assertThrowsNullPointer
        .apply(() -> plugin.detectSignals(null, Collections.emptyMap()));
    Executable nullParameterFieldMap = assertThrowsNullPointer
        .apply(() -> plugin.detectSignals(randomChannelSegment(), null));

    assertAll("StaLtaPowerDetectorPlugin detectSignals null arguments:",
        nullChannelSegments, nullParameterFieldMap);
  }

  @Test
  void testCreateConditionsChannelSegment() {

    StaLtaPowerDetectorPlugin plugin = new StaLtaPowerDetectorPlugin();

    // Mock wf1
    final double[] samples1 = new double[length];
    final Waveform wf1 = Waveform.withValues(Instant.EPOCH, sampleRate, samples1);

    // Mock wf2 - begins less than LTA from the end of wf1
    final double sampleRate2 = sampleRate - (sampleRate * 0.0001);
    final double[] samples2 = new double[200];
    final Waveform wf2 = Waveform
        .withValues(
            wf1.getEndTime()
                .plus(Duration.ofMillis((long) ((ltaLength - 2) * 1000.0 / sampleRate))),
            sampleRate2, samples2);

    final ChannelSegment<Waveform> channelSegment = channelSegmentFromWaveforms(List.of(wf1, wf2));

    // Condition channelSegment
    // Interpolates because the waveforms are closer than the LTA window length
    // Merges after interpolation because the sample rates are within tolerance
    // Can't mock ChannelSegment so compute the conditioned ChannelSegments here
    final OptionalDouble nominalSampleRate = channelSegment.getTimeseries().stream()
        .mapToDouble(Waveform::getSampleRate).findFirst();

    final List<Waveform> conditioned = WaveformUtility
        .interpolateWaveformGap(channelSegment.getTimeseries(),
            interpolateGapsSampleRateTolerance, ltaLength);

    final List<Waveform> merged = WaveformUtility
        .mergeWaveforms(conditioned, mergeSampleRateTolerance,
            fractionalSamplesFromDuration(nominalSampleRate.orElse(0.0), mergeMinLength));

    // Make sure conditioning actually occurred.  The rest of the test requires this.
    assertEquals(1, merged.size());

    // Mock results of processing conditioned samples
    final double[] mergedSamples = merged.get(0).getValues();

    StaLtaAlgorithm spyStaLtaAlgorithm = spy(new StaLtaAlgorithm());

    plugin.setAlgorithm(spyStaLtaAlgorithm);

    plugin.detectSignals(channelSegment, ObjectSerialization.toFieldMap(staLtaParameters));

    // Make sure the algorithm is only called once and only with the expected parameters
    verify(spyStaLtaAlgorithm)
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, mergedSamples);

    verifyNoMoreInteractions(spyStaLtaAlgorithm);
  }

  @Test
  void testCreateConditionsChannelSegmentWithLtaLength() {
    StaLtaPowerDetectorPlugin plugin = new StaLtaPowerDetectorPlugin();

    // Mock wf1
    final double[] samples1 = new double[length];
    final Waveform wf1 = Waveform.withValues(Instant.EPOCH, sampleRate, samples1);

    // Mock wf2 - begins more than LTA from the end of wf1
    final double sampleRate2 = sampleRate - (sampleRate * 0.0001);
    final double[] samples2 = new double[200];
    final Waveform wf2 = Waveform
        .withValues(wf1.getEndTime().plus(Duration.ofSeconds((long) (ltaLength * sampleRate))),
            sampleRate2, samples2);

    // Current ChannelSegment can't be mocked so have to check the conditioning here.
    final ChannelSegment<Waveform> channelSegment = channelSegmentFromWaveforms(List.of(wf1, wf2));

    // Conditioned ChannelSegment should still have two Waveforms.  These mock's won't work
    // correctly if conditioning changes the ChannelSegment and the test will fail.
    // Can't mock ChannelSegment so assume conditioning occurs based on other unit tests.

    StaLtaAlgorithm spyStaLtaAlgorithm = spy(new StaLtaAlgorithm());

    plugin.setAlgorithm(spyStaLtaAlgorithm);

    plugin.detectSignals(channelSegment, ObjectSerialization.toFieldMap(staLtaParameters));

    // Make sure the algorithm is only called with the two waveforms expected parameters
    verify(spyStaLtaAlgorithm)
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples1);

    verify(spyStaLtaAlgorithm)
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples2);

    verifyNoMoreInteractions(spyStaLtaAlgorithm);
  }

  @Test
  void testDetect() {

    StaLtaAlgorithm mockStaLtaAlgorithm = mock(StaLtaAlgorithm.class);
    StaLtaPowerDetectorPlugin plugin = new StaLtaPowerDetectorPlugin();

    plugin.setAlgorithm(mockStaLtaAlgorithm);

    // Mock Algorithm results
    final double[] samples = new double[length];
    final Waveform wf = Waveform.withValues(Instant.now(), sampleRate, samples);

    final Set<Integer> triggerIndices = Set.of(1, 5, 10);
    final Set<Instant> expectedTriggerTimes = triggerIndices.stream().map(wf::computeSampleTime)
        .collect(Collectors.toSet());

    given(mockStaLtaAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples)).willReturn(triggerIndices);

    final ChannelSegment<Waveform> channelSegment = channelSegmentFromWaveforms(List.of(wf));

    final Collection<Instant> actualTriggerTimes = plugin
        .detectSignals(channelSegment, ObjectSerialization.toFieldMap(staLtaParameters));

    assertEquals(expectedTriggerTimes.size(), actualTriggerTimes.size());
    assertTrue(expectedTriggerTimes.containsAll(actualTriggerTimes));
    assertTrue(actualTriggerTimes.containsAll(expectedTriggerTimes));
  }

  @Test
  void testDetectTwoWaveforms() {

    StaLtaAlgorithm mockStaLtaAlgorithm = mock(StaLtaAlgorithm.class);
    StaLtaPowerDetectorPlugin plugin = new StaLtaPowerDetectorPlugin();
    plugin.setAlgorithm(mockStaLtaAlgorithm);

    // Mock Algorithm results on wf1
    final double[] samples1 = new double[length];
    final Waveform wf1 = Waveform.withValues(Instant.EPOCH, sampleRate, samples1);

    final Set<Integer> triggerIndices1 = Set.of(1, 5, 10);
    given(mockStaLtaAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples1)).willReturn(triggerIndices1);

    // Mock Algorithm results on wf2
    final double[] samples2 = new double[200];
    final Waveform wf2 = Waveform
        .withValues(Instant.EPOCH.plus(Duration.ofDays(5000)), sampleRate, samples2);

    final Set<Integer> triggerIndices2 = Set.of(99, 17);
    given(mockStaLtaAlgorithm
        .staLta(type, transformation, staLead, staLength, ltaLead, ltaLength, triggerThreshold,
            detriggerThreshold, samples2)).willReturn(triggerIndices2);

    // Get expected trigger times
    final Set<Instant> expectedTriggerTimes = Stream.concat(
        triggerIndices1.stream().map(wf1::computeSampleTime),
        triggerIndices2.stream().map(wf2::computeSampleTime))
        .collect(Collectors.toSet());

    final ChannelSegment<Waveform> channelSegment = channelSegmentFromWaveforms(List.of(wf1, wf2));
    final Collection<Instant> actualTriggerTimes = plugin
        .detectSignals(channelSegment, ObjectSerialization.toFieldMap(staLtaParameters));

    assertEquals(expectedTriggerTimes.size(), actualTriggerTimes.size());
    assertTrue(expectedTriggerTimes.containsAll(actualTriggerTimes));
    assertTrue(actualTriggerTimes.containsAll(expectedTriggerTimes));
  }

  private static double fractionalSamplesFromDuration(double samplesPerSec, Duration duration) {
    return (samplesPerSec * duration.getSeconds()) + (samplesPerSec * duration.getNano() / 1.0e9);
  }

  private static ChannelSegment<Waveform> channelSegmentFromWaveforms(List<Waveform> waveforms) {
    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.Type.RAW, waveforms, CreationInfo.DEFAULT);
  }
}
