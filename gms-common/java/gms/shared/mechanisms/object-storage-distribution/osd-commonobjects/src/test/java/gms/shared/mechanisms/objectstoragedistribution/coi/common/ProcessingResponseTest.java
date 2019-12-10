package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ProcessingResponseTest {

  @Test
  void testSerialization() throws Exception {
    ProcessingResponse<SignalDetectionHypothesisDescriptor> expected = ProcessingResponse
        .from(
            List.of(SignalDetectionHypothesisDescriptor.from(SignalDetectionHypothesis
                    .from(UUID.randomUUID(), UUID.randomUUID(), false,
                        List.of(EventTestFixtures.arrivalTimeFeatureMeasurement,
                            EventTestFixtures.phaseFeatureMeasurement), UUID.randomUUID()),
                UUID.randomUUID())),
            List.of(SignalDetectionHypothesisDescriptor.from(SignalDetectionHypothesis
                    .from(UUID.randomUUID(), UUID.randomUUID(), false,
                        List.of(EventTestFixtures.arrivalTimeFeatureMeasurement,
                            EventTestFixtures.phaseFeatureMeasurement), UUID.randomUUID()),
                UUID.randomUUID())),
            List.of(SignalDetectionHypothesisDescriptor.from(SignalDetectionHypothesis
                    .from(UUID.randomUUID(), UUID.randomUUID(), false,
                        List.of(EventTestFixtures.arrivalTimeFeatureMeasurement,
                            EventTestFixtures.phaseFeatureMeasurement), UUID.randomUUID()),
                UUID.randomUUID())));

    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    ProcessingResponse<SignalDetectionHypothesisDescriptor> actual = objectMapper
        .readValue(objectMapper.writeValueAsString(expected),
            new TypeReference<ProcessingResponse<SignalDetectionHypothesisDescriptor>>() {
            });

    assertEquals(expected, actual);
  }
}
