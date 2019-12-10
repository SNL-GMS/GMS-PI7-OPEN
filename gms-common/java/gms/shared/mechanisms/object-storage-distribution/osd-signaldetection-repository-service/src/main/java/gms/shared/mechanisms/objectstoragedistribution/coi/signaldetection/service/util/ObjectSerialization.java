package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.util.Objects;

/**
 * Utility class for serializing and deserializing objects using Jackson
 */
public class ObjectSerialization {

  private static ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static ObjectMapper messagePackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  public static <T> T readValue(String string, Class<T> type) {
    try {
      return objectMapper.readValue(string, type);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to deserialize object string", e);
    }
  }

  public static String writeValue(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize object, invalid object type", e);
    }
  }

  /**
   * Serialize the provided object to MessagePack.
   *
   * @param object object to serialize to MessagePack, not null
   * @return String containing JSON serialization of the input object
   * @throws IllegalArgumentException if there is a serialization error
   * @throws NullPointerException if object is null
   */
  public static byte[] writeMessagePack(Object object) {
    Objects.requireNonNull(object, "Unable to serialize null to MessagePack");

    try {
      return messagePackMapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize object to MessagePack", e);
    }
  }

  /**
   * Deserializes the provided MessagePack bytes into an instance of outputType
   *
   * @param messagePack byte array containing MessagePack, not null
   * @param outputType deserialized object type, not null
   * @param <T> output class type
   * @return an instance of T, not null
   * @throws NullPointerException if messagePack or outputType are null
   * @throws IllegalArgumentException if the messagePack can't be deserialized into a T
   */
  public static <T> T readMessagePack(byte[] messagePack, Class<T> outputType) {
    Objects.requireNonNull(messagePack, "Unable to deserialize null MessagePack");
    Objects.requireNonNull(outputType, "Unable to deserialize to null class type");

    try {
      return messagePackMapper.readValue(messagePack, outputType);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to deserialize object from MessagePack", e);
    }
  }
}
