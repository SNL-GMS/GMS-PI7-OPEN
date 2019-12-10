package gms.core.signaldetection.onsettimerefinement;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class AicOnsetTimeRefinementPluginTests {

  //convenience function for null assertions
  private static Function<Executable, Executable> assertThrowsNullPointer =
      e -> () -> assertThrows(NullPointerException.class, e);

  private final Duration NOISE_WINDOW_SIZE = Duration.ofSeconds(6);
  private final Duration SIGNAL_WINDOW_SIZE = Duration.ofSeconds(10);
  private final Integer ORDER = 10;
  private final AicOnsetTimeRefinementParameters parameters = AicOnsetTimeRefinementParameters
      .from(NOISE_WINDOW_SIZE, SIGNAL_WINDOW_SIZE, ORDER);

  @Test
  void testCalculateOnsetTimeUncertaintyNullArguments() {
    AicOnsetTimeRefinementPlugin onsetTimeRefinementPlugin = new AicOnsetTimeRefinementPlugin();

    Executable nullWaveform = assertThrowsNullPointer
        .apply(() -> onsetTimeRefinementPlugin
            .refineOnsetTime(null, Instant.EPOCH, ObjectSerialization.toFieldMap(parameters)));
    Executable nullArrivalTime = assertThrowsNullPointer
        .apply(() -> onsetTimeRefinementPlugin
            .refineOnsetTime(Waveform.withoutValues(Instant.EPOCH, 40.0, 200), null,
                ObjectSerialization.toFieldMap(parameters)));
    Executable nullParameterFieldMap = assertThrowsNullPointer
        .apply(() -> onsetTimeRefinementPlugin
            .refineOnsetTime(Waveform.withoutValues(Instant.EPOCH, 40.0, 200), Instant.EPOCH,
                null));

    assertAll("AicOnsetTimeRefinementPlugin refineOnsetTime null arguments:",
        nullWaveform, nullArrivalTime, nullParameterFieldMap);
  }
}
