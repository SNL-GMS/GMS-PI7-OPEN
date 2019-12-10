package gms.shared.frameworks.client;

import java.util.function.Function;
import jdk.incubator.http.HttpRequest.BodyProcessor;

/**
 * A protocol for encoding HTTP request body content.
 * WireType is the type of the data on the wire, e.g. String or byte[].
 * @param <W> the type on the wire, e.g. String or byte[]
 */
interface RequestContentProtocol<W> {

  /**
   * A function from the WireType to a JDK HTTP BodyProcessor.
   * This is typically implemented with a method reference on the BodyProcessor class,
   * e.g. BodyProcessor::fromString;
   */
  Function<W, BodyProcessor> bodyEncoder();

  /**
   * Serializes an object into the WireType.
   * This is typically implemented by using a Jackson ObjectMapper.
   * @param data the data to serialize
   * @return an instance of the WireType
   * @throws Exception if serialization fails, etc.
   */
  W serialize(Object data) throws Exception;
}
