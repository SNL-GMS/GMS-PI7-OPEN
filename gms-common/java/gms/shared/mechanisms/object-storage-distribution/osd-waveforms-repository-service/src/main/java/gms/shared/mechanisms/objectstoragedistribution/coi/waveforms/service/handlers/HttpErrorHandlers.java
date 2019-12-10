package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers;

import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;

/**
 * HTTP error handlers for the service.  HTTP errors get routed to these handlers to return
 * appropriate HTTP responses.
 */
public final class HttpErrorHandlers {

  // Prevent class from being instantiated, since it is a "static" class.
  private HttpErrorHandlers() {
  }

  /**
   * @param request the request that caused the exception
   * @param response the response being built when the exception occurred
   * @return json string response
   */
  public static Object Http404(Request request, Response response) {
    return Http404Custom(request,response,"");
  }

  /**
   * Responds to requests when an unhandled internal exception is thrown.
   *
   * @param request the request that caused the exception
   * @param response the response being built when the exception occurred
   * @return json string response
   */
  public static Object Http500(Request request, Response response) {
    return Http500Custom(request,response,"");
  }

  /**
   * @param request the request that caused the exception
   * @param response the response being built when the exception occurred
   * @param errorMessage the error message or error info
   * @return json string response
   */
  public static String Http404Custom(Request request, Response response, String errorMessage) {
    response.type("application/json");
    response.status(HttpStatus.NOT_FOUND_404);
    return "{ \"message\": \"Custom 404 - Not Found\":" + errorMessage + "}";
  }

  /**
   * Responds to requests when an unhandled internal exception is thrown.
   *
   * @param request the request that caused the exception
   * @param response the response being built when the exception occurred
   * @param errorMessage the error message or error info
   * @return json string response
   */
  public static String Http500Custom(Request request, Response response, String errorMessage) {
    response.type("application/json");
    response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
    return "{ \"message\": \"Custom 500 - Internal Server Error\":" + errorMessage + "}";
  }
}

