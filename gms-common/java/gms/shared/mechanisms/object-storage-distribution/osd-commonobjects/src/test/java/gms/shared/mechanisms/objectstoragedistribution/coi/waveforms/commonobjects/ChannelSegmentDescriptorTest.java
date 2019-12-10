package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChannelSegmentDescriptorTest {

  @Test
  void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    ChannelSegmentDescriptor expected = ChannelSegmentDescriptor.from(new UUID(0, 0),
        Instant.EPOCH, Instant.EPOCH.plusSeconds(60));

    assertEquals(expected, objectMapper
        .readValue(objectMapper.writeValueAsString(expected), ChannelSegmentDescriptor.class));
  }
}