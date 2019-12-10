package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers;

import spark.Request;

public class HandlerUtil {

  /**
   * Determines if the {@link Request} indicates the client accepts message pack
   *
   * @param request Request, not null
   * @return true if the client accepts application/msgpack
   */
  static boolean shouldReturnMessagePack(Request request) {
    String accept = request.headers("Accept");
    return accept != null && accept.contains(ContentTypes.MSGPACK);
  }
}
