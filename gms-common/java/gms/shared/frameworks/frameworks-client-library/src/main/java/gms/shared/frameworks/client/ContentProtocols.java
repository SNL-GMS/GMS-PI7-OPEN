package gms.shared.frameworks.client;

import static gms.shared.frameworks.common.ContentType.JSON;
import static gms.shared.frameworks.common.ContentType.MSGPACK;

import gms.shared.frameworks.common.ContentType;
import com.fasterxml.jackson.databind.JavaType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.util.Map;
import java.util.function.Function;
import jdk.incubator.http.HttpRequest.BodyProcessor;
import jdk.incubator.http.HttpResponse.BodyHandler;

/**
 * Provides content protocols from ContentType's.
 */
class ContentProtocols {

  private ContentProtocols() {
  }

  private static final ContentProtocol<String> JSON_PROTOCOL = new Json();
  private static final ContentProtocol<byte[]> MSGPACK_PROTOCOL = new Msgpack();

  private static final Map<ContentType, ContentProtocol> typeToProtocol = Map.of(
      JSON, JSON_PROTOCOL,
      MSGPACK, MSGPACK_PROTOCOL);

  /**
   * Gets a ContentProtocol given a ContentType.
   *
   * @param contentType the content type
   * @param <T> the type param returned - unchecked, but marked here so callers don't have to
   * suppress.
   * @return a ContentProtocol that handles the specified content type
   * @throws IllegalArgumentException if there is no ContentProtocol implementation' for the
   * specified content type
   */
  @SuppressWarnings("unchecked")
  public static <T> ContentProtocol<T> from(ContentType contentType) {
    if (!typeToProtocol.containsKey(contentType)) {
      throw new IllegalArgumentException("Unknown content type: " + contentType);
    }
    return typeToProtocol.get(contentType);
  }

  private static final class Json implements ContentProtocol<String> {

    @Override
    public Function<String, BodyProcessor> bodyEncoder() {
      return BodyProcessor::fromString;
    }

    @Override
    public String serialize(Object data) throws Exception {
      return CoiObjectMapperFactory.getJsonObjectMapper().writeValueAsString(data);
    }

    @Override
    public BodyHandler<String> bodyHandler() {
      return BodyHandler.asString();
    }

    @Override
    public <T> T deserialize(String data, JavaType type) throws Exception {
      return CoiObjectMapperFactory.getJsonObjectMapper().readValue(data, type);
    }
  }

  private static final class Msgpack implements ContentProtocol<byte[]> {

    @Override
    public Function<byte[], BodyProcessor> bodyEncoder() {
      return BodyProcessor::fromByteArray;
    }

    @Override
    public byte[] serialize(Object data) throws Exception {
      return CoiObjectMapperFactory.getMsgpackObjectMapper().writeValueAsBytes(data);
    }

    @Override
    public BodyHandler<byte[]> bodyHandler() {
      return BodyHandler.asByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, JavaType type) throws Exception {
      return CoiObjectMapperFactory.getMsgpackObjectMapper().readValue(data, type);
    }
  }

}
