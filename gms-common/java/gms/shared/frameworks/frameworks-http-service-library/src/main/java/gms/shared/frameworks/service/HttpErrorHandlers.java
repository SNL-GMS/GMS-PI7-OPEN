package gms.shared.frameworks.service;

import spark.Request;
import spark.Response;

/**
 * HTTP error handlers for the service.  HTTP errors get routed to these handlers to return
 * appropriate HTTP responses.
 */
final class HttpErrorHandlers {

  private HttpErrorHandlers() {
  }

  /**
   * @param request the request that caused the exception
   * @param response the response being built when the exception occurred
   * @return json string response
   */
  static Object handleNotFound(Request request, Response response) {
    response.type("application/text");
    response.status(HttpStatus.NOT_FOUND_404);
    return "404 not found: " + request.url();
  }

  /**
   * Responds to requests when an unhandled internal exception is thrown.
   *
   * @param response the response being built when the exception occurred
   * @return json string response
   */
  static Object handleInternalServerError(Response response) {
    response.type("application/json");
    response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
    return "500 - Internal Server Error";
  }
}

