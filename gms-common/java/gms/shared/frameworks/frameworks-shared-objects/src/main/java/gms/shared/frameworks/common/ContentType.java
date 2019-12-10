package gms.shared.frameworks.common;

import java.util.Objects;

/**
 * Represents a small set of Content-Type HTTP headers used by GMS.
 */
public enum ContentType {

  JSON(ContentType.JSON_NAME),
  MSGPACK(ContentType.MSGPACK_NAME);

  public static final String JSON_NAME = "application/json";
  public static final String MSGPACK_NAME = "application/msgpack";

  private final String type;

  ContentType(String type) {
    this.type = type;
  }

  public static ContentType defaultContentType() {
    return JSON;
  }

  public static ContentType parse(String contentType) {
    Objects.requireNonNull(contentType, "parse requires non-null contentType");

    switch (contentType.trim().toLowerCase()) {
      case JSON_NAME:
        return JSON;
      case MSGPACK_NAME:
        return MSGPACK;
      default:
        throw new IllegalArgumentException("Unknown content type: " + contentType);
    }
  }

  @Override
  public String toString() {
    return type;
  }
}
