package gms.core.signaldetection.staltapowerdetector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.signaldetection.staltapowerdetector.StaLtaAlgorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.StaLtaAlgorithm.WaveformTransformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StaLtaParametersTests {

  private static final AlgorithmType algorithmType = AlgorithmType.STANDARD;
  private static final WaveformTransformation waveformTransformation = WaveformTransformation.RECTIFIED;
  private static final Duration staLead = Duration.ofSeconds(5);
  private static final Duration staLength = Duration.ofSeconds(4);
  private static final Duration ltaLead = Duration.ofSeconds(10);
  private static final Duration ltaLength = Duration.ofSeconds(7);
  private static final double triggerThreshold = 12.0;
  private static final double detriggerThreshold = 10.0;
  private static final double interpolateGapsSampleRateTolerance = 1.2;
  private static final double mergeSampleRateTolerance = 1.3;
  private static final Duration mergeMinimumGapLength = Duration.ofMillis(25);

  @Test
  void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    StaLtaParameters expected = StaLtaParameters
        .from(algorithmType, waveformTransformation, staLead, staLength, ltaLead, ltaLength,
            triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength);

    assertEquals(expected, objectMapper
        .readValue(objectMapper.writeValueAsString(expected), StaLtaParameters.class));
  }

  private static Stream<Arguments> handlerNegativeArguments() {
    return Stream.of(
        arguments(algorithmType, waveformTransformation, staLead, Duration.ofMillis(-10), ltaLead,
            ltaLength, triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength),
        arguments(algorithmType, waveformTransformation, staLead, staLength, ltaLead,
            Duration.ofMillis(-10), triggerThreshold, detriggerThreshold,
            interpolateGapsSampleRateTolerance, mergeSampleRateTolerance, mergeMinimumGapLength),
        arguments(algorithmType, waveformTransformation, staLead, staLength, ltaLead, ltaLength,
            -.1,
            detriggerThreshold, interpolateGapsSampleRateTolerance, mergeSampleRateTolerance,
            mergeMinimumGapLength),
        arguments(algorithmType, waveformTransformation, staLead, staLength, ltaLead, ltaLength,
            triggerThreshold, -71, interpolateGapsSampleRateTolerance, mergeSampleRateTolerance,
            mergeMinimumGapLength)
    );
  }

  @ParameterizedTest
  @MethodSource("handlerNegativeArguments")
  void testCreateNegativeArguments(AlgorithmType algorithmType,
      WaveformTransformation waveformTransformation, Duration staLead, Duration staLength,
      Duration ltaLead, Duration ltaLength, double triggerThreshold, double detriggerThreshold,
      double interpolateGapsSampleRateTolerance, double mergeSampleRateTolerance,
      Duration mergeMinimumGapLength) {
    assertThrows(IllegalArgumentException.class, () -> StaLtaParameters
        .from(algorithmType, waveformTransformation, staLead, staLength, ltaLead, ltaLength,
            triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
            mergeSampleRateTolerance, mergeMinimumGapLength));
  }
}
