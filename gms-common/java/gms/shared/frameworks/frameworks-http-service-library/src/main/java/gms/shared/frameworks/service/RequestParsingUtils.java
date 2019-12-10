package gms.shared.frameworks.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

/**
 * Contains static utility methods related to parsing service request bodies.
 */
public final class RequestParsingUtils {

  // Not instantiable - only static methods
  private RequestParsingUtils() {}

  /**
   * Extracts the body of a request as a json node.
   *
   * @param request the request
   * @param objectMapper the object mapper to use
   * @return a JsonNode instance
   * @throws DeserializationException if the body of the request is null
   * or not valid JSON or msgpack.
   */
  public static JsonNode extractRequest(
      final Request request,
      final ObjectMapper objectMapper) throws DeserializationException {

    requestNonNull(request);
    objectMapperNonNull(objectMapper);

    try {
      final JsonNode node = request.clientSentMsgpack() ?
          objectMapper.readTree(request.getRawBody())
          : objectMapper.readTree(request.getBody());
      if (node instanceof NullNode) {
        throw new DeserializationException("Request body deserialized to null JsonNode; request: " + request);
      }
      return node;
    } catch (Exception e) {
      throw new DeserializationException(
          "Could not deserialize request body into JsonNode; request: " + request, e);
    }
  }

  /**
   * Extracts the body of a request (either JSON or msgpack)
   * into an object of a desired type.
   *
   * @param request the request
   * @param objectMapper the object mapper to use
   * @param clazz the class of the type to return
   * @return a T instance
   * @throws DeserializationException if the body of the request is null or the request body
   *   is not valid json or msgpack, or if the body cannot be parsed into
   *   the specified type (i.e. it doesn't have the right fields)
   */
  public static <T> T extractRequest(
      final Request request,
      final ObjectMapper objectMapper,
      final Class<? extends T> clazz) throws DeserializationException {

    Objects.requireNonNull(clazz, "clazz must not be null");
    return extractRequest(request, objectMapper,
        objectMapper.constructType(clazz));
  }

  /**
   * Extracts the body of a request (either JSON or msgpack)
   * into an object of a desired type by Jackson JavaType, which can
   * be more customized than a Class (e.g. can include type parameter information).
   *
   * @param request the request
   * @param objectMapper the object mapper to use
   * @param type the type to return
   * @return a T instance
   * @throws DeserializationException if the body of the request is null or the request body
   *   is not valid json or msgpack, or if the body cannot be parsed into
   *   the specified type (i.e. it doesn't have the right fields)
   */
  public static <T> T extractRequest(
      final Request request,
      final ObjectMapper objectMapper,
      final JavaType type) throws DeserializationException {

    requestNonNull(request);
    objectMapperNonNull(objectMapper);
    Objects.requireNonNull(type, "type must not be null");

    try {
      final T result = request.clientSentMsgpack() ?
          objectMapper.readValue(request.getRawBody(), type)
          : objectMapper.readValue(request.getBody(), type);
      if (result == null) {
        throw new DeserializationException("Request body is null: " + request);
      }
      return result;
    } catch (Exception e) {
      throw new DeserializationException(
          String.format("Could not deserialize request body into object of type %s:\n%s",
              type, request.getBody()), e);
    }
  }

  /**
   * Extracts the body of a request (either JSON or msgpack)
   * into a list of objects of a desired type.
   *
   * @param request the request
   * @param objectMapper the object mapper to use
   * @param elementClass the class of the list elements to return
   * @return a T instance
   * @throws DeserializationException if the body of the request is null or the request body
   *   is not valid json or msgpack, or if the body cannot be parsed into
   *   the specified type (i.e. it doesn't have the right fields)
   */
  public static <T> List<T> extractRequestList(
      final Request request,
      final ObjectMapper objectMapper,
      final Class<? extends T> elementClass) throws DeserializationException {

    requestNonNull(request);
    objectMapperNonNull(objectMapper);
    Objects.requireNonNull(elementClass, "elementClass must not be null");

    final boolean scalarCoercionEnabled = objectMapper.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    if (scalarCoercionEnabled) {
      objectMapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    }

    try {
      final CollectionType ct = objectMapper.getTypeFactory()
          .constructCollectionType(ArrayList.class, elementClass);
      final List<T> result = request.clientSentMsgpack() ?
          objectMapper.readValue(request.getRawBody(), ct)
          : objectMapper.readValue(request.getBody(), ct);
      return Collections.unmodifiableList(result);
    } catch (Exception e) {
      throw new DeserializationException("Could not deserialize request " +
          "into list of " + elementClass.getSimpleName(), e);
    } finally {
      if (scalarCoercionEnabled) {
        objectMapper.enable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
      }
    }
  }

  /**
   * Extract a value from a json node
   * associated to the given key as the requested type.
   *
   * @param <T> the type of object the element at the k ey is expected to contain
   * @param node the node from which to extract the element (must not be null)
   * @param objectMapper used for conversion of the request element into the desired type.
   * @param key the field key into the JSON request (must not be null or empty)
   * @param clazz the class of the desired type
   * @return an optional instance of the element type T; not present if the key isn't present
   * or the value of the key is null.
   * @throws DeserializationException if the key is present but it's value cannot be parsed
   * into the specified class
   */
  public static <T> Optional<T> extractRequestElement(
      JsonNode node, ObjectMapper objectMapper,
      String key, Class<? extends T> clazz) throws DeserializationException {

    Objects.requireNonNull(node, "node must not be null");
    Validate.notBlank(key, "key is required");
    objectMapperNonNull(objectMapper);
    Objects.requireNonNull(clazz, "clss must not be null");

    final JsonNode elementNode = node.get(key);

    if (elementNode == null || (elementNode instanceof NullNode)) {
      return Optional.empty();
    }

    final String json = elementNode.toString();

    final boolean scalarCoercionEnabled = objectMapper.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    if (scalarCoercionEnabled) {
      objectMapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    }

    try {
      return Optional.of(objectMapper.readValue(json, clazz));
    } catch (Exception e) {
      throw new DeserializationException(
          String.format("Could not deserialize contents of JSON field \"%s\" into %s object:%n%s",
              key, clazz.getSimpleName(), json), e
      );
    } finally {
      if (scalarCoercionEnabled) {
        objectMapper.enable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
      }
    }
  }

  /**
   * Extract a value from a json node, throwing an exception if the value
   * is not present.
   *
   * @param <T> the type of object the element at the key is expected to contain
   * @param node the node from which to extract the element (must not be null)
   * @param objectMapper used for conversion of the request element into the desired type.
   * @param key the field key into the JSON request (must not be null or empty)
   * @param clazz the class of the desired type
   * @return instance of the element type T
   * @throws DeserializationException if the key is not present or if
   * it's value cannot be parsed into the specified class
   */
  public static <T> T extractRequiredRequestElement(JsonNode node, ObjectMapper objectMapper,
      String key, Class<? extends T> clazz) throws DeserializationException {

    Optional<T> opt = extractRequestElement(node, objectMapper, key, clazz);

    return opt.orElseThrow(() -> new DeserializationException(
        String.format("JSON field \"%s\" is not present in request", key)
    ));
  }

  /**
   * Extract a value from a request that is
   * associated to the given key as the requested type.
   *
   * @param <T> the type of object the request is expected to contain
   * @param request Request from which to extract the element (must not be null).
   * @param objectMapper used for conversion of the request element into the desired type.
   * @param key the field key into the JSON request (must not be null or empty)
   * @param clss the class of the desired type
   * @return an optional instance of the element
   * @throws DeserializationException if the key is present but it's value cannot be parsed
   * into the specified class
   */
  public static <T> Optional<T> extractRequestElement(
      Request request, ObjectMapper objectMapper,
      String key, Class<? extends T> clss) throws DeserializationException {

    return extractRequestElement(
        extractRequest(request, objectMapper),
        objectMapper, key, clss);
  }

  /**
   * Extract a value from a json node, throwing an exception if the value
   * is not present.
   *
   * @param <T> the type of object the element at the key is expected to contain
   * @param request the request from which to extract the element (must not be null)
   * @param objectMapper used for conversion of the request element into the desired type.
   * @param key the field key into the JSON request (must not be null or empty)
   * @param clazz the class of the desired type
   * @return instance of the element type T
   * @throws DeserializationException if the key is not present or if
   * it's value cannot be parsed into the specified class
   */
  public static <T> T extractRequiredRequestElement(Request request, ObjectMapper objectMapper,
      String key, Class<? extends T> clazz) throws DeserializationException {

    Optional<T> opt = extractRequestElement(request, objectMapper, key, clazz);

    return opt.orElseThrow(() -> new DeserializationException(
        String.format("JSON field \"%s\" is not present in request", key)
    ));
  }

  /**
   * Extract a list of objects of the specified type from a json node.
   *
   * @param <T> the type expected of the elements in the list
   * @param node JSON node from which to extract the element (must not be null)
   * @param objectMapper used for conversion of the request element into the desired type.
   * @param key the field key into the JSON request (must not be null or empty)
   * @param elementClass the class of the desired type
   * @return an unmodifiable list containing instances of the elements
   * @throws DeserializationException if the key is present but it's value cannot be parsed
   * into a list containing elements of the specified class
   */
  public static <T> Optional<List<T>> extractRequestElementList(
      JsonNode node, ObjectMapper objectMapper,
      String key, Class<? extends T> elementClass) throws DeserializationException {

    Objects.requireNonNull(node, "jsonNode must not be null");
    Validate.notBlank(key, "key is required");
    objectMapperNonNull(objectMapper);
    Objects.requireNonNull(elementClass, "elementClass must not be null");

    final JsonNode elementNode = node.get(key);

    if (elementNode == null || (elementNode instanceof NullNode)) {
      return Optional.empty();
    }

    final boolean scalarCoercionEnabled = objectMapper.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    if (scalarCoercionEnabled) {
      objectMapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    }

    try {
      return Optional.of(Collections.unmodifiableList(
          objectMapper.readValue(elementNode.toString(),
              objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, elementClass))));
    } catch (Exception e) {
      throw new DeserializationException(
          String.format("Could not deserialize contents of JSON field \"%s\" " +
                  "into list of %s objects:\n%s",
              key, elementClass.getSimpleName(), elementNode.toString()), e);
    } finally {
      if (scalarCoercionEnabled) {
        objectMapper.enable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
      }
    }
  }

  /**
   * Extract a list of objects of the specified type from a json node, throwing an exception
   * if the list field is not present.
   *
   * @param <T> the type expected of the elements in the list
   * @param node JSON node from which to extract the element (must not be null)
   * @param objectMapper used for conversion of the request element into the desired type.
   * @param key the field key into the JSON request (must not be null or empty)
   * @param elementClass the class of the desired type
   * @return an unmodifiable list containing instances of the elements
   * @throws DeserializationException if the key is not present or if it's value cannot be parsed
   * into a list containing elements of the specified class
   */
  public static <T> List<T> extractRequiredRequestElementList(
      JsonNode node, ObjectMapper objectMapper,
      String key, Class<? extends T> elementClass) throws DeserializationException {

    Optional<List<T>> opt = extractRequestElementList(node, objectMapper, key, elementClass);

    return opt.orElseThrow(() -> new DeserializationException(
        String.format("JSON list field \"%s\" is not present in request", key)
    ));
  }

  /**
   * Extract a list of objects of the specified type from a request at the given key.
   *
   * @param <T> the type expected of the elements in the list
   * @param request request from which to extract the element (must not be null)
   * @param objectMapper used for conversion of the request element into the desired type.
   * @param key the field key into the JSON request (must not be null or empty)
   * @param elementClass the class of the desired type
   * @return an unmodifiable list containing instances of the elements
   * @throws DeserializationException if the key is present but it's value cannot be parsed
   * into a list containing elements of the specified class
   */
  public static <T> Optional<List<T>> extractRequestElementList(
      Request request, ObjectMapper objectMapper,
      String key, Class<? extends T> elementClass) throws DeserializationException {

    return extractRequestElementList(
        extractRequest(request, objectMapper),
        objectMapper, key, elementClass);
  }

  /**
   * Extract a list of objects of the specified type from a json node, throwing an exception
   * if the list field is not present.
   *
   * @param <T> the type expected of the elements in the list
   * @param request the request from which to extract the element (must not be null)
   * @param objectMapper used for conversion of the request element into the desired type.
   * @param key the field key into the JSON request (must not be null or empty)
   * @param elementClass the class of the desired type
   * @return an unmodifiable list containing instances of the elements
   * @throws DeserializationException if the key is not present or if it's value cannot be parsed
   * into a list containing elements of the specified class
   */
  public static <T> List<T> extractRequiredRequestElementList(
      Request request, ObjectMapper objectMapper,
      String key, Class<? extends T> elementClass) throws DeserializationException {

    Optional<List<T>> opt = extractRequestElementList(request, objectMapper, key, elementClass);

    return opt.orElseThrow(() -> new DeserializationException(
        String.format("JSON list field \"%s\" is not present in request", key)
    ));
  }

  private static void requestNonNull(Request req) {
    Objects.requireNonNull(req, "request must not be null");
  }

  private static void objectMapperNonNull(ObjectMapper om) {
    Objects.requireNonNull(om, "objectMapper must not be null");
  }

  /**
   * Instances of {@code DeserializationException} are raised to indicate a problem with deserialization.
   */
  public static class DeserializationException extends IOException {

    public DeserializationException(String message, Throwable cause) {
      super(message, cause);
    }

    public DeserializationException(String message) {
      super(message);
    }

  }
}
