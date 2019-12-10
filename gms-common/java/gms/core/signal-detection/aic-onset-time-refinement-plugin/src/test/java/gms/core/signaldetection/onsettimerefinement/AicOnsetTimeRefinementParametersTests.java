package gms.core.signaldetection.onsettimerefinement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class AicOnsetTimeRefinementParametersTests {

  private final Duration NOISE_WINDOW_SIZE = Duration.ofSeconds(6);
  private final Duration SIGNAL_WINDOW_SIZE = Duration.ofSeconds(10);
  private final Integer ORDER = 10;

  @Test
  void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    AicOnsetTimeRefinementParameters expected = AicOnsetTimeRefinementParameters
        .from(NOISE_WINDOW_SIZE, SIGNAL_WINDOW_SIZE, ORDER);

    assertEquals(expected, objectMapper
        .readValue(objectMapper.writeValueAsString(expected),
            AicOnsetTimeRefinementParameters.class));
  }
}
