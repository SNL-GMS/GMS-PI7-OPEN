package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util;

import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;

/**
 * Utility class for handling http responses
 */
public class ResponseUtil {

  public static String notAcceptable(Request request, Response response) {
    response.status(HttpStatus.NOT_ACCEPTABLE_406);
    return "ERROR_406_NOT_ACCEPTABLE";
  }

  public static String notFound(Request request, Response response) {
    response.status(HttpStatus.NOT_FOUND_404);
    return "ERROR_404_NOT_FOUND";
  }

}
