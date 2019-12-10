package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SignalDetectionHypothesisDescriptorTests {

  @Test
  void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    SignalDetectionHypothesisDescriptor expected = SignalDetectionHypothesisDescriptor.from(
        SignalDetectionHypothesis
            .create(UUID.randomUUID(), EventTestFixtures.arrivalTimeFeatureMeasurement,
                EventTestFixtures.phaseFeatureMeasurement, UUID.randomUUID()), UUID.randomUUID());

    assertEquals(expected, objectMapper.readValue(objectMapper.writeValueAsString(expected),
        SignalDetectionHypothesisDescriptor.class));
  }
}
