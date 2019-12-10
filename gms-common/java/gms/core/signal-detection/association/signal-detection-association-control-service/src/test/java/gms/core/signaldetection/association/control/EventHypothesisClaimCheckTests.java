package gms.core.signaldetection.association.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EventHypothesisClaimCheckTests {

  @Test
  void testFrom() {

    UUID eventHypothesisId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();

    EventHypothesisClaimCheck claimCheck = EventHypothesisClaimCheck
        .from(
            eventHypothesisId,
            eventId
        );

    Assertions.assertEquals(eventHypothesisId, claimCheck.getEventHypothesisId());
    Assertions.assertEquals(eventId, claimCheck.getEventId());
  }

  @Test
  void testSerializeDeserializeJson() throws IOException {

    // create json object mapper
    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    // create event hypothesis claim check to serialize
    EventHypothesisClaimCheck claimCheck = EventHypothesisClaimCheck
        .from(UUID.randomUUID(), UUID.randomUUID());

    // serialize claim check
    String serializedClaimCheck = jsonObjectMapper.writeValueAsString(claimCheck);

    // deserialize claim check
    EventHypothesisClaimCheck deserializedClaimCheck = jsonObjectMapper
        .readValue(serializedClaimCheck,
            EventHypothesisClaimCheck.class);

    Assertions.assertEquals(claimCheck, deserializedClaimCheck);
  }

  @Test
  void testSerializeDeserializeMsgPack() throws IOException {

    // create message pack object mapper
    ObjectMapper msgPackObjectMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

    // create event hypothesis claim check to serialize
    EventHypothesisClaimCheck claimCheck = EventHypothesisClaimCheck
        .from(UUID.randomUUID(), UUID.randomUUID());

    // serialize claim check
    byte[] serializedClaimCheck = msgPackObjectMapper.writeValueAsBytes(claimCheck);

    // deserialize claim check
    EventHypothesisClaimCheck deserializedClaimCheck = msgPackObjectMapper
        .readValue(serializedClaimCheck,
            EventHypothesisClaimCheck.class);

    Assertions.assertEquals(claimCheck, deserializedClaimCheck);
  }

  @Test
  void testFromNullEventHypothesisId() {

    UUID eventId = UUID.randomUUID();

    Assertions.assertThrows(NullPointerException.class,
        () -> EventHypothesisClaimCheck.from(
            null,
            eventId
        )
    );
  }

  @Test
  void testFromNullEventId() {

    UUID eventHypothesisId = UUID.randomUUID();

    Assertions.assertThrows(NullPointerException.class,
        () -> EventHypothesisClaimCheck.from(
            eventHypothesisId,
            null
        )
    );
  }
}
