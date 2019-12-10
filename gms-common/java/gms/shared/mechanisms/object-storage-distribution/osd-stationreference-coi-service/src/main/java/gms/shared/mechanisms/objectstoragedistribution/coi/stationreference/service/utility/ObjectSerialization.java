package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;

/**
 * Utility class for serializing and deserializing objects using Jackson
 */
public class ObjectSerialization {

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

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
}
