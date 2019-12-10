package gms.core.signaldetection.snronsettimeuncertainty;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.utilities.signalprocessing.normalization.Transform;
import java.time.Duration;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class SnrOnsetTimeUncertaintyPluginTests {

  private static SnrOnsetTimeUncertaintyParameters parameters;

  //convenience function for null assertions
  private static Function<Executable, Executable> assertThrowsNullPointer =
      e -> () -> assertThrows(NullPointerException.class, e);

  @BeforeAll
  public static void setupClass() {
    parameters = SnrOnsetTimeUncertaintyParameters.builder()
        .setNoiseWindowOffset(Duration.ofSeconds(66))
        .setNoiseWindowSize(Duration.ofSeconds(60))
        .setSignalWindowOffset(Duration.ofSeconds(3))
        .setSignalWindowSize(Duration.ofSeconds(6))
        .setSlidingWindowSize(Duration.ofSeconds(1))
        .setMinTimeUncertainty(0.1)
        .setMaxTimeUncertainty(1.0)
        .setMinSnr(5.0)
        .setMaxSnr(50.0)
        .setTransform(Transform.ABS)
        .build();
  }

  @Test
  void testCalculateOnsetTimeUncertainty() {
    SnrOnsetTimeUncertaintyPlugin onsetTimeUncertaintyPlugin = new SnrOnsetTimeUncertaintyPlugin();

    Duration uncertainty = onsetTimeUncertaintyPlugin
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.PICK,
            ObjectSerialization.toFieldMap(parameters));
    assertEquals(TestFixtures.EXPECTED_UNCERTAINTY_DURATION, uncertainty);
  }

  @Test
  void testCalculateOnsetTimeUncertaintyNullArguments() {
    SnrOnsetTimeUncertaintyPlugin onsetTimeUncertaintyPlugin = new SnrOnsetTimeUncertaintyPlugin();

    Executable nullWaveform = assertThrowsNullPointer
        .apply(() -> onsetTimeUncertaintyPlugin
            .calculateOnsetTimeUncertainty(null, TestFixtures.PICK,
                ObjectSerialization.toFieldMap(parameters)));
    Executable nullPick = assertThrowsNullPointer
        .apply(() -> onsetTimeUncertaintyPlugin
            .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, null,
                ObjectSerialization.toFieldMap(parameters)));
    Executable nullParameterFieldMap = assertThrowsNullPointer
        .apply(() -> onsetTimeUncertaintyPlugin
            .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.PICK,
                null));

    assertAll("SnrOnsetTimeUncertaintyPlugin calculateOnsetTimeUncertainty null arguments:",
        nullWaveform, nullPick, nullParameterFieldMap);
  }

}