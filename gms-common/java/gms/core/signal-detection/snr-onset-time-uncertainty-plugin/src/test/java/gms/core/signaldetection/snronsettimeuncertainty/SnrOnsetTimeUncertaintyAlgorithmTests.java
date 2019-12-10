package gms.core.signaldetection.snronsettimeuncertainty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.utilities.signalprocessing.normalization.Transform;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SnrOnsetTimeUncertaintyAlgorithmTests {

  private static SnrOnsetTimeUncertaintyParameters parameters;

  private static final double MAX_TIME_UNCERTAINTY = 1.0;

  @BeforeAll
  public static void setup() {
    parameters = SnrOnsetTimeUncertaintyParameters.builder()
        .setNoiseWindowOffset(Duration.ofSeconds(66))
        .setNoiseWindowSize(Duration.ofSeconds(60))
        .setSignalWindowOffset(Duration.ofSeconds(3))
        .setSignalWindowSize(Duration.ofSeconds(6))
        .setSlidingWindowSize(Duration.ofSeconds(1))
        .setMinTimeUncertainty(0.1)
        .setMaxTimeUncertainty(MAX_TIME_UNCERTAINTY)
        .setMinSnr(5.0)
        .setMaxSnr(50.0)
        .setTransform(Transform.ABS)
        .build();
  }

  @Test
  void testCalculateOnsetTimeUncertaintyNullWaveform() {
    assertThrows(NullPointerException.class,
        () -> SnrOnsetTimeUncertaintyAlgorithm
            .calculateUncertainty(null, TestFixtures.PICK, parameters));
  }

  @Test
  void testCalculateOnsetTimeUncertaintyNullOnsetTime() {
    assertThrows(NullPointerException.class,
        () -> SnrOnsetTimeUncertaintyAlgorithm
            .calculateUncertainty(TestFixtures.WAVEFORM, null, parameters));
  }

  @Test
  void testCalculateOnsetTimeUncertaintyNullParameters() {
    assertThrows(NullPointerException.class,
        () -> SnrOnsetTimeUncertaintyAlgorithm
            .calculateUncertainty(TestFixtures.WAVEFORM, TestFixtures.PICK, null));
  }

  @Test
  void testCalculateOnsetTimeUncertaintyLowSnr() {
    double onsetTimeUncertainty = SnrOnsetTimeUncertaintyAlgorithm
        .calculateUncertainty(TestFixtures.LOW_SNR_WAVEFORM,
            TestFixtures.PICK,
            parameters);

    assertEquals(parameters.getMaxTimeUncertainty(), onsetTimeUncertainty, .0001);
  }

  @Test
  void testCalculateOnsetTimeUncertaintyHighSnr() {
    double onsetTimeUncertainty = SnrOnsetTimeUncertaintyAlgorithm
        .calculateUncertainty(TestFixtures.HIGH_SNR_WAVEFORM,
            TestFixtures.PICK,
            parameters);

    assertEquals(parameters.getMinTimeUncertainty(), onsetTimeUncertainty, .0001);
  }

  @Test
  void testCalculateOnsetTimeUncertainty() {
    double onsetTimeUncertainty = SnrOnsetTimeUncertaintyAlgorithm
        .calculateUncertainty(TestFixtures.WAVEFORM, TestFixtures.PICK, parameters);

    assertEquals(TestFixtures.EXPECTED_UNCERTAINTY, onsetTimeUncertainty, .000000001);
  }

  @Test
  void testCalculateOnsetTimeUncertaintyInsufficientWaveforms() {
    double onsetTimeUncertainty = SnrOnsetTimeUncertaintyAlgorithm
        .calculateUncertainty(TestFixtures.WAVEFORM
            .trim(TestFixtures.WAVEFORM.getStartTime().plus(Duration.ofSeconds(1)),
                TestFixtures.WAVEFORM.getEndTime()), TestFixtures.PICK, parameters);

    assertEquals(MAX_TIME_UNCERTAINTY, onsetTimeUncertainty);
  }

  @Test
  void testCalculateOnsetTimeUncertaintyLowMinSnr() {
    parameters = SnrOnsetTimeUncertaintyParameters.builder()
        .setNoiseWindowOffset(Duration.ofSeconds(66))
        .setNoiseWindowSize(Duration.ofSeconds(60))
        .setSignalWindowOffset(Duration.ofSeconds(3))
        .setSignalWindowSize(Duration.ofSeconds(6))
        .setSlidingWindowSize(Duration.ofSeconds(1))
        .setMinTimeUncertainty(0.1)
        .setMaxTimeUncertainty(1.0)
        .setMinSnr(0.1)
        .setMaxSnr(50.0)
        .setTransform(Transform.ABS)
        .build();
    double uncertainty = SnrOnsetTimeUncertaintyAlgorithm
        .calculateUncertainty(TestFixtures.WAVEFORM, TestFixtures.PICK, parameters);
    assertEquals(parameters.getMaxTimeUncertainty(), uncertainty, .00001);
  }
}
