package gms.shared.utilities.service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for a function that handles an HTTP request and returns an HTTP response.
 * */
@FunctionalInterface
public interface RequestHandler<ResponseType> {

  /**
   * Function to handle a Request and return a Response.
   *
   * @param request the request
   * @return a response to the request
   */
  Response<ResponseType> handle(Request request, ObjectMapper deserializer);
}
