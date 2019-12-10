package gms.core.signalenhancement.beam.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Serialization and deserialization utilities
 */
public class ObjectSerialization {

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
      throw new IllegalArgumentException("Unable to deserialize object from json", e);
    }
  }

  /**
   * Deserializes the provided json into an instance of outputType
   *
   * @param json json in a String, not null
   * @param outputType deserialized object type, not null
   * @param <T> output class type
   * @return an instance of T, not null
   * @throws NullPointerException if json or outputType are null
   * @throws IllegalArgumentException if the json can't be deserialized into a T
   */
  public static <T> T readJson(String json, Class<T> outputType) {
    Objects.requireNonNull(json, "Unable to deserialize null json");
    Objects.requireNonNull(outputType, "Unable to deserialize to null class type");

    try {
      return jsonMapper.readValue(json, outputType);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to deserialize object from json", e);
    }
  }

  /**
   * Deserializes the provided json string into a Collection of the provided rawClass type
   * parameterized by the parameterClass
   *
   * @param json string containing Json, not null
   * @param collectionClass deserialized collection type, not null
   * @param rawClass deserialized raw class type, not null
   * @param parameterClass deserialized parameterized type to the raw class, not null
   * @param <T> output class type
   * @return an instance of T, not null
   * @throws NullPointerException if messagePack, collectionClass, rawClass, or parameterClass are
   * null
   * @throws IllegalArgumentException if the messagePack can't be deserialized into a T
   */
  public static <T> T readJsonCollection(String json,
      Class<? extends Collection> collectionClass, Class<?> rawClass, Class<?>... parameterClass) {

    Objects.requireNonNull(json, "Unable to deserialize null json");
    Objects.requireNonNull(collectionClass, "Unable to deserialize to null collectionClass type");
    Objects.requireNonNull(rawClass, "Unable to deserialize to null rawClass type");
    Objects.requireNonNull(parameterClass, "Unable to deserialize to null parameterClass type");

    if (parameterClass.length == 0) {
      throw new IllegalArgumentException("Unable to deserialize to empty parameterClass type");
    }

    if (Arrays.stream(parameterClass).anyMatch(Objects::isNull)) {
      throw new NullPointerException("Unable to deserialize to null parameterClass type");
    }

    JavaType type = jsonMapper.getTypeFactory()
        .constructParametricType(rawClass, parameterClass);

    CollectionType collectionType = jsonMapper.getTypeFactory()
        .constructCollectionType(collectionClass, type);

    try {
      return jsonMapper.readValue(json, collectionType);
    } catch (IOException e) {
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

    if (parameterClass.length == 0) {
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
      throw new IllegalArgumentException("Unable to deserialize object from MessagePack", e);
    }
  }

  public static Map<UUID, ChannelSegment<Waveform>> readChannelSegmentsMsgpack(byte[] messagePack) {
    final TypeFactory tf = messagePackMapper.getTypeFactory();
    final JavaType chanSegWaveformType = tf.constructParametricType(ChannelSegment.class, Waveform.class);
    final JavaType uuidType = tf.constructType(UUID.class);
    final JavaType mapType = tf.constructMapType(HashMap.class, uuidType, chanSegWaveformType);
    try {
      return messagePackMapper.readValue(messagePack, mapType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
