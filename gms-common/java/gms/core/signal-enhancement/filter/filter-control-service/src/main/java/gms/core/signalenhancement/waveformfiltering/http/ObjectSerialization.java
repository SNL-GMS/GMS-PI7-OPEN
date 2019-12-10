package gms.core.signalenhancement.waveformfiltering.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serialization and deserialization utilities
 */
public class ObjectSerialization {

  private static final Logger logger = LoggerFactory.getLogger(ObjectSerialization.class);

  /**
   * Serializes and deserializes JSON
   */
  private static final ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  /**
   * Serializes and deserializes MessagePack
   */
  private static final ObjectMapper messagePackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  /**
   * Obtains a jason {@link com.mashape.unirest.http.ObjectMapper} for use by Unirest
   *
   * @return an ObjectMapper for json serialization and deserialization
   */
  public static com.mashape.unirest.http.ObjectMapper getJsonClientObjectMapper() {
    return new com.mashape.unirest.http.ObjectMapper() {
      @Override
      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return jsonMapper.readValue(value, valueType);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public String writeValue(Object value) {
        try {
          return jsonMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  /**
   * Serialize the provided object to JSON.
   *
   * @param object object to serialize to JSON, not null
   * @return String containing JSON serialization of the input object
   * @throws IllegalArgumentException if there is a serialization error
   * @throws NullPointerException if object is null
   */
  public static byte[] writeJson(Object object) {
    Objects.requireNonNull(object, "Unable to serialize null to json");

    try {
      return jsonMapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize object to json", e);
    }
  }

  /**
   * Deserializes the provided json into an instance of outputType
   *
   * @param json json in a byte[], not null
   * @param outputType deserialized object type, not null
   * @param <T> output class type
   * @return an instance of T, not null
   * @throws NullPointerException if json or outputType are null
   * @throws IllegalArgumentException if the json can't be deserialized into a T
   */
  public static <T> T readJson(byte[] json, Class<T> outputType) {
    Objects.requireNonNull(json, "Unable to deserialize null json");
    Objects.requireNonNull(outputType, "Unable to deserialize to null class type");

    try {
      return jsonMapper.readValue(json, outputType);
    } catch (IOException e) {
      logger.info("Could not deserialize json", e);
      throw new IllegalArgumentException("Unable to deserialize object from json", e);
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
      logger.info("Could not deserialize MessagePack", e);
      throw new IllegalArgumentException("Unable to deserialize object from MessagePack", e);
    }
  }

  /**
   * Deserializes the provided MessagePack bytes into a Collection of the provided rawClass type
   * parameterized by the parameterClass
   *
   * @param messagePack byte array containing MessagePack, not null
   * @param collectionClass deserialized collection type, not null
   * @param rawClass deserialized raw class type, not null
   * @param parameterClass deserialized parameterized type to the raw class, not null
   * @param <T> output class type
   * @return an instance of T, not null
   * @throws NullPointerException if messagePack, collectionClass, rawClass, or parameterClass are
   * null
   * @throws IllegalArgumentException if the messagePack can't be deserialized into a T
   */
  public static <T> T readMessagePackCollection(byte[] messagePack,
      Class<? extends Collection> collectionClass, Class<?> rawClass, Class<?>... parameterClass) {

    Objects.requireNonNull(messagePack, "Unable to deserialize null MessagePack");
    Objects.requireNonNull(collectionClass, "Unable to deserialize to null collectionClass type");
    Objects.requireNonNull(rawClass, "Unable to deserialize to null rawClass type");
    Objects.requireNonNull(parameterClass, "Unable to deserialize to null parameterClass type");

    if(parameterClass.length == 0) {
      throw new IllegalArgumentException("Unable to deserialize to empty parameterClass type");
    }

    if (Arrays.stream(parameterClass).anyMatch(Objects::isNull)) {
      throw new NullPointerException("Unable to deserialize to null parameterClass type");
    }

    JavaType type = messagePackMapper.getTypeFactory()
        .constructParametricType(rawClass, parameterClass);

    CollectionType collectionType = messagePackMapper.getTypeFactory()
        .constructCollectionType(collectionClass, type);

    try {
      return messagePackMapper.readValue(messagePack, collectionType);
    } catch (IOException e) {
      logger.info("Unable to deserialize object from MessagePack", e);
      throw new IllegalArgumentException("Unable to deserialize object from MessagePack", e);
    }
  }

  /**
   * Deserializes the provided MessagePack bytes into a Map of the provided leftClass type and rightClass
   * parameterized by the parameterClass
   *
   * @param messagePack byte array containing MessagePack, not null
   * @param mapClass deserialized map type, not null
   * @param leftClass deserialized left type for the map, not null
   * @param rightClass deserialized right type for the map, not null
   * @param parameterClass deserialized parameterized type to the right class, not null
   * @param <T> output class type
   * @return an instance of T, not null
   * @throws NullPointerException if messagePack, mapClass, leftClass, rightClass, or parameterClass are
   * null
   * @throws IllegalArgumentException if the messagePack can't be deserialized into a T
   */
  public static <T> T readMessagePackMap(byte[] messagePack,
      Class<? extends Map> mapClass, Class<?> leftClass, Class<?> rightClass, Class<?>... parameterClass) {

    Objects.requireNonNull(messagePack, "Unable to deserialize null MessagePack");
    Objects.requireNonNull(mapClass, "Unable to deserialize to null mapClass type");
    Objects.requireNonNull(leftClass, "Unable to deserialize to null leftClass type");
    Objects.requireNonNull(rightClass, "Unable to deserialize to null rightClass type");
    Objects.requireNonNull(parameterClass, "Unable to deserialize to null parameterClass type");

    if(parameterClass.length == 0) {
      throw new IllegalArgumentException("Unable to deserialize to empty parameterClass type");
    }

    if (Arrays.stream(parameterClass).anyMatch(Objects::isNull)) {
      throw new NullPointerException("Unable to deserialize to null parameterClass type");
    }

    JavaType leftType = messagePackMapper.constructType(leftClass);
    JavaType rightType = messagePackMapper.getTypeFactory().constructParametricType(rightClass, parameterClass);
    JavaType mapType = messagePackMapper.getTypeFactory().constructMapType(mapClass, leftType, rightType);

    try {
      return messagePackMapper.readValue(messagePack, mapType);
    } catch (IOException e) {
      logger.info("Unable to deserialize object from MessagePack", e);
      throw new IllegalArgumentException("Unable to deserialize object from MessagePack", e);
    }
  }
}
