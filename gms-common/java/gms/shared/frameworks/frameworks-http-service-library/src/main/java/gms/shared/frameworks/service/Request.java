package gms.shared.frameworks.service;

import gms.shared.frameworks.common.ContentType;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for an HTTP request.  You can retrieve things from it such as it's body or query
 * params.  Intended to be implemented by using an underlying HTTP library.
 */
public interface Request {

  /**
   * Gets a path parameter, which is one that is part of the URL e.g. /person/john, where 'john' is
   * the path parameter
   *
   * @param name the name of the parameter
   * @return optional value of the parameter with the specified name
   */
  Optional<String> getPathParam(String name);

  /**
   * Gets the body as a String
   *
   * @return the body
   */
  String getBody();

  /**
   * Gets the body of the request as a raw byte array to be used in e.g. messagepack
   * deserialization
   *
   * @return the raw bytes of the body of the request
   */
  byte[] getRawBody();

  /**
   * Gets the header with the specified name
   *
   * @param name the name of the header
   * @return optional value of the header with the specified name
   */
  Optional<String> getHeader(String name);

  /**
   * Gets all headers
   * @return a map from the name of the header to the value of the header
   */
  Map<String, String> getHeaders();

  /**
   * Gets the content type of the request
   *
   * @return the content type as an enum of the supported types
   */
  default Optional<ContentType> getContentType() {
    try {
      return getHeader("Content-Type").map(ContentType::parse);
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  /**
   * Determines if the request specified msgpack response
   *
   * @return true if the request specified msgpack is allowable, false otherwise
   */
  default boolean clientAcceptsMsgpack() {
    return getHeader("Accept")
        .filter(ContentType.MSGPACK.toString()::equals).isPresent();
  }

  /**
   * Determines if the request sent message pack format
   *
   * @return true if the request header (Content-Type) indicates msgpack, false otherwise
   */
  default boolean clientSentMsgpack() {
    return getContentType().filter(ContentType.MSGPACK::equals).isPresent();
  }

}
