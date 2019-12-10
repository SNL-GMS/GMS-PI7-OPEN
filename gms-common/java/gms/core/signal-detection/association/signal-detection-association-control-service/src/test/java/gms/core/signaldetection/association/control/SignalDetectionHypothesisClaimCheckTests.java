package gms.core.signaldetection.association.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class SignalDetectionHypothesisClaimCheckTests {

  @Test
  void testFrom() {
    UUID signalDetectionHypothesisId = UUID.randomUUID();
    UUID signalDetectionId = UUID.randomUUID();

    SignalDetectionHypothesisClaimCheck claimCheck = SignalDetectionHypothesisClaimCheck.from(
        signalDetectionHypothesisId,
        signalDetectionId
    );

    Assertions
        .assertEquals(signalDetectionHypothesisId, claimCheck.getSignalDetectionHypothesisId());
    Assertions.assertEquals(signalDetectionId, claimCheck.getSignalDetectionId());
  }

  @Test
  void testSerializeDeserializeJson() throws IOException {

    // create JSON object mapper
    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    // create signal detection hypothesis claim check to serialize
    SignalDetectionHypothesisClaimCheck claimCheck = SignalDetectionHypothesisClaimCheck
        .from(UUID.randomUUID(), UUID.randomUUID());

    // serialize claim check
    String serializedClaimCheck = jsonObjectMapper.writeValueAsString(claimCheck);

    // deserialize claim check
    SignalDetectionHypothesisClaimCheck deserializedClaimCheck = jsonObjectMapper
        .readValue(serializedClaimCheck, SignalDetectionHypothesisClaimCheck.class);

    Assertions.assertEquals(claimCheck, deserializedClaimCheck);
  }

  @Test
  void testSerializeDeserializeMsgPack() throws IOException {

    // create MessagePack object mapper
    ObjectMapper msgPackObjectMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

    // create signal detection hypothesis claim check to serialize
    SignalDetectionHypothesisClaimCheck claimCheck = SignalDetectionHypothesisClaimCheck
        .from(UUID.randomUUID(), UUID.randomUUID());

    // serialize claim check
    byte[] serializedClaimCheck = msgPackObjectMapper.writeValueAsBytes(claimCheck);

    // deserialize claim check
    SignalDetectionHypothesisClaimCheck deserializedClaimCheck = msgPackObjectMapper
        .readValue(serializedClaimCheck, SignalDetectionHypothesisClaimCheck.class);

    Assertions.assertEquals(claimCheck, deserializedClaimCheck);
  }

  @Test
  void testFromNullSignalDetectionHypothesisId() {
    UUID signalDetectionId = UUID.randomUUID();

    Assertions.assertThrows(NullPointerException.class,
        () -> SignalDetectionHypothesisClaimCheck.from(
            null,
            signalDetectionId
        )
    );
  }

  @Test
  void testFromNullSignalDetectionId() {
    UUID signalDetectionHypothesisId = UUID.randomUUID();

    Assertions.assertThrows(NullPointerException.class,
        () -> SignalDetectionHypothesisClaimCheck.from(
            signalDetectionHypothesisId,
            null
        )
    );
  }
}
