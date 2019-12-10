package gms.core.signalenhancement.fk.service;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public enum ContentType {
  APPLICATION_JSON(ContentType.jsonType) {
    @Override
    <T> BiFunction<byte[], Class<T>, T> getDeserializationOp() {
      return ObjectSerialization::readJson;
    }

    @Override
    public Function<Object, byte[]> getSerializer() {
      return ObjectSerialization::writeJson;
    }
  },
  APPLICATION_MSGPACK(ContentType.messagePackType) {
    @Override
    <T> BiFunction<byte[], Class<T>, T> getDeserializationOp() {
      return ObjectSerialization::readMessagePack;
    }

    @Override
    public Function<Object, byte[]> getSerializer() {
      return ObjectSerialization::writeMessagePack;
    }
  },
  APPLICATION_ANY(ContentType.anyType) {
    @Override
    <T> BiFunction<byte[], Class<T>, T> getDeserializationOp() {
      return ObjectSerialization::readJson;
    }

    @Override
    public Function<Object, byte[]> getSerializer() {
      return ObjectSerialization::writeJson;
    }
  },
  TEXT_PLAIN(ContentType.plainTextType),
  UNKNOWN(ContentType.unknownType);

  private static final String jsonType = "application/json";
  private static final String messagePackType = "application/msgpack";
  private static final String anyType = "application/*";
  private static final String plainTextType = "text/plain";
  private static final String unknownType = "unknown";

  private final String type;

  public static final Predicate<ContentType> isJson = ContentType.APPLICATION_JSON::equals;
  public static final Predicate<ContentType> isMsgPack = ContentType.APPLICATION_MSGPACK::equals;
  public static final Predicate<ContentType> isAny = ContentType.APPLICATION_ANY::equals;
  public static final Predicate<ContentType> isPlainText = ContentType.TEXT_PLAIN::equals;
  public static final Predicate<ContentType> isUnknown = ContentType.UNKNOWN::equals;

  ContentType(String type) {
    this.type = type;
  }

  /**
   * Obtains the deserialization operation to invoke to deserialize a byte[] in format {@link
   * ContentType} into a T
   *
   * @param <T> type of the deserialized object
   * @return BiFunction to map a (byte[], Class) into a T
   */
  <T> BiFunction<byte[], Class<T>, T> getDeserializationOp() {
    //return ObjectSerialization::readJson;
    throw new IllegalStateException("No deserializer defined for " + this);
  }

  public <T> Function<byte[], T> getDeserializer(Class<T> classType) {
    // There is a type error when all of this is on one line.  Doubtless it involves type erasure.
    BiFunction<byte[], Class<T>, T> deserialization = this.getDeserializationOp();
    return b -> deserialization.apply(b, classType);
  }

  /**
   * Obtains the serialization operation to invoke to serialize an object into a byte[] in format
   * {@link ContentType}
   *
   * @return Function to serialize an Object into a byte[]
   */
  public Function<Object, byte[]> getSerializer() {
    //return ObjectSerialization::writeJson;
    throw new IllegalStateException("No serializer defined for " + this);
  }

  public static ContentType parse(String contentType) {
    Objects.requireNonNull(contentType, "parse requires non-null contentType");

    switch (contentType.trim().toLowerCase()) {
      case jsonType:
        return APPLICATION_JSON;
      case messagePackType:
        return APPLICATION_MSGPACK;
      case anyType:
        return APPLICATION_ANY;
      case plainTextType:
        return TEXT_PLAIN;
      default:
        return UNKNOWN;
    }
  }

  @Override
  public String toString() {
    return type;
  }
}
