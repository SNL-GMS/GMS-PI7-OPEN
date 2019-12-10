package gms.shared.frameworks.service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for a function that handles an HTTP request and returns an HTTP response.
 * The type parameter denotes the return type of the response.
 * */
@FunctionalInterface
public interface RequestHandler<R> {

  /**
   * Function to handle a Request and return a Response.
   *
   * @param request the request
   * @return a response to the request
   */
  Response<R> handle(Request request, ObjectMapper deserializer);
}
