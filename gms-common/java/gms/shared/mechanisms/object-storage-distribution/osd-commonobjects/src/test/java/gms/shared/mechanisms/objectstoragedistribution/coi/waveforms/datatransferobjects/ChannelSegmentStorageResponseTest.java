package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChannelSegmentStorageResponseTest {

  @Test
  void testSerialization() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    Instant startTime = Instant.EPOCH;

    ChannelSegmentStorageResponse expected = ChannelSegmentStorageResponse.builder()
        .addStored(ChannelSegmentDescriptor.from(new UUID(0, 0),
            startTime, startTime.plusSeconds(60)))
        .addFailed(ChannelSegmentDescriptor.from(new UUID(0, 1),
            startTime.plusSeconds(60), startTime.plusSeconds(120)))
        .build();

    String actualJson = objectMapper.writeValueAsString(expected);
    ChannelSegmentStorageResponse actual = objectMapper
        .readValue(actualJson, ChannelSegmentStorageResponse.class);
    
    assertEquals(expected, actual);
  }
}