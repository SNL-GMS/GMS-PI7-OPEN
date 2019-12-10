package gms.shared.frameworks.client;

/**
 * Client abstraction for use with GMS.
 */
public interface ServiceClient {

  /**
   * Send a request and gets a response
   * @param request the request to send
   * @param <T> type param of the expected response
   * @return a ResponseType
   * @throws ConnectionFailed with wrapped exception when the connection fails
   * (e.g. hostname not found)
   * @throws InternalServerError with body of response from server if
   * server responds it had internal error
   * @throws BadRequest with body of response from server if server
   * rejects the client request
   * @throws IllegalArgumentException if the url in the request is invalid,
   * the requestFormat or responseFormat's in the request are unsupported,
   * or request serialization fails.
   * @throws IllegalStateException if response deserialization fails
   */
  <T> T send(ServiceRequest request);

  /**
   * Exception for when the server has an internal error.
   */
  class InternalServerError extends RuntimeException {
    InternalServerError(String msg) {
      super(msg);
    }
  }

  /**
   * Exception for when the server rejects the client request.
   */
  class BadRequest extends RuntimeException {
    BadRequest(String msg) {
      super(msg);
    }
  }

  /**
   * Exception for when the server cannot be reached because of network issues,
   * an unknown hostname, etc.
   */
  class ConnectionFailed extends RuntimeException {
    ConnectionFailed(String msg, Exception e) {
      super(msg, e);
    }
  }
}
