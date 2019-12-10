package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class ProcessingGroupDescriptorTests {

  @Test
  public void testSerialization() throws IOException {
    ProcessingGroupDescriptor command = ProcessingGroupDescriptor.create(UUID.randomUUID(),
        Instant.EPOCH,
        Instant.now());

    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    String json = jsonObjectMapper.writeValueAsString(command);
    assertNotNull(json);
    assertTrue(json.length() > 0);

    ProcessingGroupDescriptor deserialized = jsonObjectMapper
        .readValue(json, ProcessingGroupDescriptor.class);
    assertEquals(command, deserialized);
  }
}
