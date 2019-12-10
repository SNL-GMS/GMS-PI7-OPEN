package gms.shared.mechanisms.configuration.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility operations for Configuration object serialization
 */
public class ObjectSerialization {

  private static final JavaType mapType = TypeFactory.defaultInstance()
      .constructMapType(HashMap.class, String.class, Object.class);

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getYamlObjectMapper();

  private ObjectSerialization() {
  }

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public static Map<String, Object> toFieldMap(Object o) {
    return objectMapper.convertValue(o, mapType);
  }

  public static <T> T fromFieldMap(Map<String, Object> map, Class<T> clazz) {
    return objectMapper.convertValue(map, clazz);
  }
}
