package gms.core.waveformqc.waveformsignalqc.algorithm;

import static gms.core.waveformqc.waveformsignalqc.algorithm.TestUtility.createChannelSegment;
import static gms.core.waveformqc.waveformsignalqc.algorithm.TestUtility.createWaveform;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.stream.DoubleStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class WaveformSpike3PtInterpreterTest {

  private static ChannelSegment<Waveform> noSpike;
  private static ChannelSegment<Waveform> oneSpike;
  private static ChannelSegment<Waveform> twoSpike;
  private static Waveform firstSpike;
  private static ChannelSegment<Waveform> endPointTest;
  private static ChannelSegment<Waveform> endPointTestGap;
  private static ChannelSegment<Waveform> sawtooth;
  private static Instant sawtoothTime;

  private static double spikeThreshold = 0.8;
  private static double spikeThreshold2 = 0.9;

  private static int rmsLeadDifferences = 9;
  private static int rmsLagDifferences = 9;
  private static double rmsAmplitudeRatioThreshold = 2.0;

  @BeforeAll
  public static void setUp() throws Exception {

    final double samplesPerSec = 40.0;
    final double samplePeriodSec = 1.0 / 40.0;

    final Instant startTime = Instant.EPOCH;
    final Waveform first = createWaveform(startTime, startTime.plusSeconds(10), samplesPerSec);

    Instant startTime2 = first.getEndTime().plusMillis((long) (samplePeriodSec * 1000));
    final Waveform secondNoSpike = createWaveform(startTime2, startTime2.plusSeconds(10),
        samplesPerSec);
    noSpike = createChannelSegment(List.of(first, secondNoSpike));

    firstSpike = createWaveform(startTime2, startTime2.plusSeconds(10),
        samplesPerSec);
    firstSpike.getValues()[20] = -1.0;

    oneSpike = createChannelSegment(List.of(first, firstSpike));

    Waveform twoSpikes = createWaveform(startTime2, startTime2.plusSeconds(10),
        samplesPerSec);
    twoSpikes.getValues()[20] = -1.0;
    twoSpikes.getValues()[30] = -1.0;

    twoSpike = createChannelSegment(List.of(first, twoSpikes));

    // this data sets up two spikes: one on the second to last point of the first waveform, and a
    // second on the first point of the second waveform. The spike on the last point of the first
    // waveform is created from the three samples -.7, 1.0, -1.0, where the first two values are
    // the last two samples of the first waveform and the last value is the first sample of the
    // second waveform. The spike on the first point of the second waveform is created from the
    // three samples 1.0, -1.0, 1.0, where the first value is the last sample of the first waveform
    // and the next two values are the first two samples of the second waveform. The default sample
    // values for both waveforms are set to 1.0 so we only need to set the second to last sample
    // of the first waveform (-.7) and the first sample of the second waveform (-1.0).
    Waveform waveformEP0 = createWaveform(startTime, startTime.plusSeconds(10), samplesPerSec);
    Instant startTimeEP1 = waveformEP0.getEndTime().plusMillis(24);
    Waveform waveformEP1 = createWaveform(startTimeEP1, startTimeEP1.plusSeconds(10),
        samplesPerSec);
    Instant startTimeEP2 = waveformEP1.getEndTime().plusMillis(24);
    Waveform waveformEP2 = createWaveform(startTimeEP2, startTimeEP2.plusSeconds(10),
        samplesPerSec);
    endPointTest = createChannelSegment(List.of(waveformEP0, waveformEP1, waveformEP2));
    waveformEP0.getValues()[(int) waveformEP0.getSampleCount() - 2] = -.7;
    waveformEP1.getValues()[0] = -1.0;

    // this data sets up the same test as above but uses 26 milliseconds between the first and
    // second waveforms so that spike tests are ignored at the end points (25 milliseconds is the
    // max period for this data to test for spikes).
    Instant startTimeEP1Gap = waveformEP0.getEndTime().plusMillis(26);
    Waveform waveformEP1Gap = createWaveform(startTimeEP1Gap, startTimeEP1Gap.plusSeconds(10),
        samplesPerSec);
    Instant startTimeEP2Gap = waveformEP1Gap.getEndTime().plusMillis(24);
    Waveform waveformEP2Gap = createWaveform(startTimeEP2Gap, startTimeEP2Gap.plusSeconds(10),
        samplesPerSec);
    endPointTestGap = createChannelSegment(List.of(waveformEP0, waveformEP1Gap, waveformEP2Gap));
    waveformEP1Gap.getValues()[0] = -1.0;

    // Create a sawtooth waveform with a single spike.  This fails if the algorithm does not check
    // amplitude ratio.
    double[] sawToothValues = DoubleStream.iterate(0, d -> 1 - d).limit(400).toArray();
    sawToothValues[200] = -100000.0;
    Waveform sawToothWf = Waveform.withValues(startTime, samplesPerSec, sawToothValues);
    sawtooth = TestUtility.createChannelSegment(List.of(sawToothWf));
    sawtoothTime = sawToothWf.computeSampleTime(200);
  }

  @Test
  public void testCreateWaveformSpike3PtQcMasksNoSpike() {
    List<WaveformSpike3PtQcMask> masks = (new WaveformSpike3PtInterpreter())
        .createWaveformSpike3PtQcMasks(noSpike, spikeThreshold, rmsLeadDifferences,
            rmsLagDifferences,
            rmsAmplitudeRatioThreshold);
    assertEquals(0, masks.size());
  }

  @Test
  public void testCreateWaveformSpike3PtQcMasksOneSpike() {

    List<WaveformSpike3PtQcMask> masks = (new WaveformSpike3PtInterpreter())
        .createWaveformSpike3PtQcMasks(oneSpike, spikeThreshold, rmsLeadDifferences,
            rmsLagDifferences,
            rmsAmplitudeRatioThreshold);
    assertEquals(1, masks.size());

    //Check the QcMask time is the same as expected start time
    assertEquals(firstSpike.computeSampleTime(20), masks.get(0).getStartTime());

    //Check the QcMask time is the same as expected end time
    assertEquals(firstSpike.computeSampleTime(20), masks.get(0).getEndTime());

    //Check the ChannelSegmentId is correct
    assertEquals(oneSpike.getId(), masks.get(0).getChannelSegmentId());

    //Check the Channel Id matches the ChannelSegment processing id
    assertEquals(oneSpike.getChannelId(), masks.get(0).getChannelId());
  }

  @Test
  public void testCreateWaveformSpike3PtQcMasksTwoSpike() {
    List<WaveformSpike3PtQcMask> masks = (new WaveformSpike3PtInterpreter())
        .createWaveformSpike3PtQcMasks(twoSpike, spikeThreshold, rmsLeadDifferences,
            rmsLagDifferences,
            rmsAmplitudeRatioThreshold);
    assertEquals(2, masks.size());

    //Retrieve collection of waveforms
    List<Waveform> waveforms = twoSpike.getTimeseries();

    //Create an iterator to obtain the 2nd waveform containing the spikes
    Iterator<Waveform> it = waveforms.iterator();

    //Iterate over the first waveform - containing NO spikes
    it.next();

    //Obtain the 2nd waveform containing the spikes
    Waveform waveform = it.next();

    //Check the QcMask time is the same as expected start time
    assertEquals(waveform.computeSampleTime(20), masks.get(0).getStartTime());

    //Check the QcMask time is the same as expected end time
    assertEquals(waveform.computeSampleTime(20), masks.get(0).getEndTime());

    //Check the QcMask time is the same as expected start time
    assertEquals(waveform.computeSampleTime(30), masks.get(1).getStartTime());

    //Check the QcMask time is the same as expected end time
    assertEquals(waveform.computeSampleTime(30), masks.get(1).getEndTime());
  }

  @Test
  public void testCreateNullChannelSegmentExpectNullPointerException() {
    assertEquals(
        "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires non-null channelSegment",
        assertThrows(NullPointerException.class, () -> (new WaveformSpike3PtInterpreter())
            .createWaveformSpike3PtQcMasks(null, spikeThreshold, rmsLeadDifferences,
                rmsLagDifferences,
                rmsAmplitudeRatioThreshold)).getMessage());
  }

  @Test
  public void testCreateNonPositiveThresholdExpectIllegalArgumentException() {
    assertIllegalArgThrown(
        "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0",
        () -> (new WaveformSpike3PtInterpreter())
            .createWaveformSpike3PtQcMasks(noSpike, 0.0, rmsLeadDifferences, rmsLagDifferences,
                rmsAmplitudeRatioThreshold));
  }

  @Test
  public void testCreateThresholdOver1ExpectIllegalArgumentException() {
    assertIllegalArgThrown(
        "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0",
        () -> (new WaveformSpike3PtInterpreter())
            .createWaveformSpike3PtQcMasks(noSpike, 1.0, rmsLeadDifferences, rmsLagDifferences,
                rmsAmplitudeRatioThreshold));
  }

  @Test
  public void testCreateLeadSamplesNegativeExpectIllegalArgumentException() {
    assertIllegalArgThrown(
        "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires rmsLeadSampleDifferences >= 0",
        () -> (new WaveformSpike3PtInterpreter())
            .createWaveformSpike3PtQcMasks(noSpike, spikeThreshold, -1, rmsLagDifferences,
                rmsAmplitudeRatioThreshold));
  }

  @Test
  public void testCreateLagSamplesNegativeExpectIllegalArgumentException() {
    assertIllegalArgThrown(
        "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires rmsLagSampleDifferences >= 0",
        () -> (new WaveformSpike3PtInterpreter())
            .createWaveformSpike3PtQcMasks(noSpike, spikeThreshold, rmsLeadDifferences, -1,
                rmsAmplitudeRatioThreshold));
  }

  @Test
  public void testCreateNoLeadLagSamplesNegativeExpectIllegalArgumentException() {
    assertIllegalArgThrown(
        "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires (rmsLeadSampleDifferences + rmsLagSampleDifferences) >= 2",
        () -> (new WaveformSpike3PtInterpreter())
            .createWaveformSpike3PtQcMasks(noSpike, spikeThreshold, 1, 0,
                rmsAmplitudeRatioThreshold));
  }

  @Test
  public void testCreateRmsAmplitudeRatioThresholdOneExpectIllegalArgumentException() {
    assertIllegalArgThrown(
        "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires rmsAmplitudeRatioThreshold > 1.0",
        () -> (new WaveformSpike3PtInterpreter())
            .createWaveformSpike3PtQcMasks(noSpike, spikeThreshold, rmsLeadDifferences,
                rmsLagDifferences, 1.0));
  }

  private static void assertIllegalArgThrown(String msg, Executable exec) {
    assertEquals(msg, assertThrows(IllegalArgumentException.class, exec).getMessage());
  }

  /**
   * This test will find two spikes: one on the last point of the first waveform and another on the
   * first point of the second waveform.
   */
  @Test
  public void testCreateEndPointMasks() {

    // This test has two spikes within 3 samples of eachother.  The baseline amplitude value is
    // 1.0 and the 3 samples are (-0.7, 1.0, -1.0).  The two spikes are -0.7 and -1.0
    // Set the minConsecutiveSampleDifferenceSpikeThreshold to 0.85 to avoid finding a spike
    // at the middle sample (2 * 0.85 = 1.7).
    List<WaveformSpike3PtQcMask> masks = (new WaveformSpike3PtInterpreter())
        .createWaveformSpike3PtQcMasks(endPointTest, 0.85, rmsLeadDifferences, rmsLagDifferences,
            rmsAmplitudeRatioThreshold);

    assertEquals(2, masks.size());

    //Retrieve collection of waveforms
    List<Waveform> waveforms = endPointTest.getTimeseries();

    //Create an iterator to obtain the 1st waveform containing a spike on its last point
    Iterator<Waveform> it = waveforms.iterator();

    //Obtain the 1st waveform containing the spike
    Waveform waveform = it.next();
    final Instant firstMaskTime = waveform.computeSampleTime(waveform.getSampleCount() - 2);

    //Check the QcMask start time is the same as the end time of the first waveform
    assertEquals(firstMaskTime, masks.get(0).getStartTime());

    //Check the QcMask end time is the same as the end time of the first waveform
    assertEquals(firstMaskTime, masks.get(0).getEndTime());

    //Obtain the 1st waveform containing the spike
    waveform = it.next();

    //Check the QcMask start time is the same as the end time of the second waveform
    assertEquals(waveform.getStartTime(), masks.get(1).getStartTime());

    //Check the QcMask end time is the same as the end time of the second waveform
    assertEquals(waveform.getStartTime(), masks.get(1).getEndTime());
  }

  /**
   * This test uses the same data as in test testCreateEndPointMasks except that the time between
   * the last data point of the first waveform and the first data point of the second waveform (26
   * msec) is greater than the sample periods of either waveform (25 msec). The correct result is
   * that no spikes are discovered.
   */
  @Test
  public void testCreateNoEndPointMasks() {
    List<WaveformSpike3PtQcMask> masks = (new WaveformSpike3PtInterpreter())
        .createWaveformSpike3PtQcMasks(endPointTestGap, spikeThreshold, rmsLeadDifferences,
            rmsLagDifferences, rmsAmplitudeRatioThreshold);

    assertEquals(0, masks.size());
  }

  /**
   * This test uses a different threshold than used in the first end point test
   * (testCreateEndPointMasks) such that the mask on the last point of the first waveform is not
   * created but the spike on the first point of the second waveform is still defined.
   */
  @Test
  public void testCreateOneEndPointMask() {
    // Set rmsAmplitudeRatioThreshold to 3.5294 so the spike at the second to last point of the first
    // waveform is not created.  The baseline value is 1.0 and the three points in question are
    // (-0.7, 1.0, -1.0).  This threshold is just large enough for the spike difference RMS of 1.7
    // to not exceed the background difference RMS of ~0.56666...
    List<WaveformSpike3PtQcMask> masks = (new WaveformSpike3PtInterpreter())
        .createWaveformSpike3PtQcMasks(endPointTest, spikeThreshold2, rmsLeadDifferences,
            rmsLagDifferences, 3.5294);

    assertEquals(1, masks.size());

    //Retrieve collection of waveforms
    List<Waveform> waveforms = endPointTest.getTimeseries();

    //Create an iterator to obtain the 1st waveform containing a spike on its last point
    Iterator<Waveform> it = waveforms.iterator();

    //Obtain the 2nd waveform containing the spike
    it.next();
    Waveform waveform = it.next();

    //Check the QcMask start time is the same as the end time of the second waveform
    assertEquals(waveform.getStartTime(), masks.get(0).getStartTime());

    //Check the QcMask end time is the same as the end time of the second waveform
    assertEquals(waveform.getStartTime(), masks.get(0).getEndTime());
  }

  @Test
  public void testSawtoothOneSpike() throws Exception {
    final List<WaveformSpike3PtQcMask> masks = (new WaveformSpike3PtInterpreter())
        .createWaveformSpike3PtQcMasks(sawtooth, spikeThreshold, rmsLeadDifferences,
            rmsLagDifferences,
            rmsAmplitudeRatioThreshold);

    assertEquals(1, masks.size());

    final WaveformSpike3PtQcMask mask = masks.get(0);
    assertEquals(sawtoothTime, mask.getStartTime());
    assertEquals(sawtoothTime, mask.getEndTime());
    assertEquals(sawtooth.getId(), mask.getChannelSegmentId());
    assertEquals(sawtooth.getChannelId(), mask.getChannelId());
    assertEquals(QcMaskType.SPIKE, mask.getQcMaskType());
  }
}
