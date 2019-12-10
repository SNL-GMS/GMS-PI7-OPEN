package gms.core.signaldetection.onsettimerefinement;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class AicOnsetTimeRefinementAlgorithmTests {

  //convenience function for null assertions
  private static Function<Executable, Executable> assertThrowsNullPointer =
      e -> () -> assertThrows(NullPointerException.class, e);

  private static Waveform waveform;
  private static Instant onsetTime;
  private static Instant refinedOnsetTime;
  private static AicOnsetTimeRefinementParameters parameters;

  @BeforeAll
  public static void loadData() throws IOException {
    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    TypeFactory typeFactory = mapper.getTypeFactory();
    JavaType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
    Map<String, Object> validationData = mapper.readValue(
        AicOnsetTimeRefinementAlgorithmTests.class.getClassLoader()
            .getResourceAsStream("aic_verify.json"),
        mapType);

    double sampleRate = 1.0 / mapper.convertValue(validationData.get("delTime"), Double.class);
    long sampleCount = mapper.convertValue(validationData.get("numSamples"), Long.class);
    double[] values = mapper.convertValue(validationData.get("data"), double[].class);
    waveform = Waveform.from(Instant.EPOCH, sampleRate, sampleCount, values);

    int unrefinedOnsetSample = mapper.convertValue(validationData.get("triggerIndex"),
        Integer.class);
    onsetTime = waveform.computeSampleTime(unrefinedOnsetSample);

    int refinedSample = mapper.convertValue(validationData.get("aicPickSample"), Integer.class);
    refinedOnsetTime = waveform.computeSampleTime(refinedSample);

    Duration noiseWindow = Duration.between(waveform.getStartTime(), onsetTime);
    Duration signalWindow = Duration.between(onsetTime, waveform.getEndTime());

    parameters = AicOnsetTimeRefinementParameters.from(noiseWindow, signalWindow, 8);
  }

  @Test
  void testCalculateOnsetTimeUncertaintyNullArguments() {
    Executable nullWaveform = assertThrowsNullPointer
        .apply(() -> AicOnsetTimeRefinementAlgorithm
            .refineOnsetTime(null, Instant.EPOCH, parameters));
    Executable nullArrivalTime = assertThrowsNullPointer
        .apply(() -> AicOnsetTimeRefinementAlgorithm
            .refineOnsetTime(waveform, null,
                parameters));
    Executable nullParameterFieldMap = assertThrowsNullPointer
        .apply(() -> AicOnsetTimeRefinementAlgorithm
            .refineOnsetTime(waveform, Instant.EPOCH,
                null));

    assertAll("AicOnsetTimeRefinementAlgorithm refineOnsetTime null arguments:",
        nullWaveform, nullArrivalTime, nullParameterFieldMap);
  }

  @Test
  void testRefineOnsetTimeOnsetOutsideWaveform() {
    assertThrows(IllegalStateException.class,
        () -> AicOnsetTimeRefinementAlgorithm
            .refineOnsetTime(waveform, Instant.EPOCH.plusSeconds(235), parameters));
  }

  @Test
  void testRefineOnsetTime() {
    assertEquals(refinedOnsetTime,
        AicOnsetTimeRefinementAlgorithm.refineOnsetTime(waveform, onsetTime, parameters));
  }

  @Test
  void testRefineOnsetTimeFlatWaveform() {
    double[] flatData = new double[waveform.getValues().length];
    Arrays.fill(flatData, 3.0);

    Waveform flatWaveform = Waveform.from(waveform.getStartTime(),
        waveform.getSampleRate(),
        waveform.getSampleCount(),
        flatData);

    assertEquals(onsetTime,
        AicOnsetTimeRefinementAlgorithm.refineOnsetTime(flatWaveform, onsetTime, parameters));
  }

  @Test
  void testRefineOnsetTimeInsufficientData() {
    Waveform shortWaveform = waveform
        .window(waveform.getStartTime(), waveform.getEndTime().minusMillis(200));

    assertEquals(onsetTime,
        AicOnsetTimeRefinementAlgorithm
            .refineOnsetTime(shortWaveform, onsetTime, parameters));
  }
}
