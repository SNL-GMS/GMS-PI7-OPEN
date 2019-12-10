package gms.core.signaldetection.snronsettimeuncertainty;


import gms.core.signaldetection.snronsettimeuncertainty.SnrOnsetTimeUncertaintyParameters.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.utilities.signalprocessing.normalization.Transform;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class SnrOnsetTimeUncertaintyParametersTest {

  private static final Duration NOISE_WINDOW_OFFSET = Duration.ofSeconds(66);
  private static final Duration NOISE_WINDOW_SIZE = Duration.ofSeconds(60);
  private static final Duration SIGNAL_WINDOW_OFFSET = Duration.ofSeconds(3);
  private static final Duration SIGNAL_WINDOW_SIZE = Duration.ofSeconds(6);
  private static final Duration SLIDING_WINDOW_SIZE = Duration.ofSeconds(1);
  private static final double MIN_TIME_UNCERTAINTY = 0.1;
  private static final double MAX_TIME_UNCERTAINTY = 1.0;
  private static final double MIN_SNR = 5.0;
  private static final double MAX_SNR = 50.0;
  private static final Transform TRANSFORM = Transform.ABS;

  private static Builder defaultBuilder() {
    return SnrOnsetTimeUncertaintyParameters.builder()
        .setNoiseWindowOffset(NOISE_WINDOW_OFFSET)
        .setNoiseWindowSize(NOISE_WINDOW_SIZE)
        .setSignalWindowOffset(SIGNAL_WINDOW_OFFSET)
        .setSignalWindowSize(SIGNAL_WINDOW_SIZE)
        .setSlidingWindowSize(SLIDING_WINDOW_SIZE)
        .setMinTimeUncertainty(MIN_TIME_UNCERTAINTY)
        .setMaxTimeUncertainty(MAX_TIME_UNCERTAINTY)
        .setMinSnr(MIN_SNR)
        .setMaxSnr(MAX_SNR)
        .setTransform(TRANSFORM);
  }

  @Test
  void testSerialization() throws IOException {
    TestUtilities
        .testSerialization(defaultBuilder().build(), SnrOnsetTimeUncertaintyParameters.class);
  }
}
