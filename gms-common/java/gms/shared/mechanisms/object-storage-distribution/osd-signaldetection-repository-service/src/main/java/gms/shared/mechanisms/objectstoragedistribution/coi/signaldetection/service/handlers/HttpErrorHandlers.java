package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import spark.Request;
import spark.Response;

/**
 * HTTP error handlers for the service.  HTTP errors get routed to these handlers
 * to return appropriate HTTP responses.
 */
public final class HttpErrorHandlers {

  // Prevent class from being instantiated, since it is a "static" class.
  private HttpErrorHandlers() {
  }

  /**
   * Responds to "bad" requests (i.e. requests that are incomplete, missing
   * input parameters, contain invalid input values, etc).
   *
   * @param request the request that caused the exception
   * @param response the response being built when the exception occurred
   * @return json string response
   */
  public static Object Http400(Request request, Response response) {
    response.type("application/json");
    return "{ \"message\": \"Custom 400 - Bad Request\" }";
  }

  /**
   * @param request the request that caused the exception
   * @param response the response being built when the exception occurred
   * @return json string response
   */
  public static Object Http404(Request request, Response response) {
    response.type("application/json");
    return "{ \"message\": \"Custom 404 - Not Found\" }";
  }

  /**
   * Responds to requests when an unhandled internal exception is thrown.
   *
   * @param request the request that caused the exception
   * @param response the response being built when the exception occurred
   * @return json string response
   */
  public static Object Http500(Request request, Response response) {
    response.type("application/json");
    return "{ \"message\": \"Custom 500 - Internal Server Error\" }";
  }
}
