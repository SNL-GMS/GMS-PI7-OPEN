package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util;

import spark.Request;

/**
 * Utility class for handling http requests
 */
public class RequestUtil {

  /**
   * Determines if the {@link Request} indicates the client accepts application/json
   *
   * @param request Request, not null
   * @return true if the client accepts application/json
   */
  public static boolean clientAcceptsJson(Request request) {
    String accept = request.headers("Accept");
    return accept != null && accept.contains("application/json");
  }

  /**
   * Determines if the {@link Request} indicates the client accepts application/msgpack
   *
   * @param request Request, not null
   * @return true if the client accepts application/msgpack
   */
  public static boolean clientAcceptsMsgpack(Request request) {
    String accept = request.headers("Accept");
    return accept != null && accept.contains("application/msgpack");
  }
}
