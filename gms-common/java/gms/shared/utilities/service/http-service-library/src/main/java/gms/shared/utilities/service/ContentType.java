package gms.shared.utilities.service;

import java.util.Objects;

/**
 * Represents a small set of Content-Type HTTP headers used by GMS.
 */
public enum ContentType {
  APPLICATION_JSON(ContentType.jsonType),
  APPLICATION_MSGPACK(ContentType.messagePackType),
  TEXT_PLAIN(ContentType.plainTextType),
  UNKNOWN(ContentType.unknownType),
  ANY(ContentType.anyType);

  private static final String jsonType = "application/json";
  private static final String messagePackType = "application/msgpack";
  private static final String plainTextType = "text/plain";
  private static final String unknownType = "unknown";
  private static final String anyType = "*/*";

  private final String type;

  ContentType(String type) {
    this.type = type;
  }

  public static ContentType parse(String contentType) {
    Objects.requireNonNull(contentType, "parse requires non-null contentType");

    switch (contentType.trim().toLowerCase()) {
      case jsonType:
        return APPLICATION_JSON;
      case messagePackType:
        return APPLICATION_MSGPACK;
      case plainTextType:
        return TEXT_PLAIN;
      case anyType:
        return ANY;
      default:
        return UNKNOWN;
    }
  }

  @Override
  public String toString() {
    return type;
  }
}
