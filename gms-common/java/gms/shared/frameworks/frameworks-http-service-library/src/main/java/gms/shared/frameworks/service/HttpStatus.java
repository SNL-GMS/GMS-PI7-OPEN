package gms.shared.frameworks.service;

//
//  ========================================================================
//  Copyright (c) 1995-2017 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

/**
 * <p>
 * Http Status Codes
 * </p>
 * @see <a href="http://www.iana.org/assignments/http-status-codes/">IANA HTTP Status Code Registry</a>
 */
public class HttpStatus
{

  public static final int OK_200 = 200;
  public static final int CREATED_201 = 201;
  public static final int ACCEPTED_202 = 202;
  public static final int NON_AUTHORITATIVE_INFORMATION_203 = 203;
  public static final int NO_CONTENT_204 = 204;
  public static final int RESET_CONTENT_205 = 205;
  public static final int PARTIAL_CONTENT_206 = 206;
  public static final int MULTI_STATUS_207 = 207;

  public static final int BAD_REQUEST_400 = 400;
  public static final int UNAUTHORIZED_401 = 401;
  public static final int PAYMENT_REQUIRED_402 = 402;
  public static final int FORBIDDEN_403 = 403;
  public static final int NOT_FOUND_404 = 404;
  public static final int METHOD_NOT_ALLOWED_405 = 405;
  public static final int NOT_ACCEPTABLE_406 = 406;
  public static final int PROXY_AUTHENTICATION_REQUIRED_407 = 407;
  public static final int REQUEST_TIMEOUT_408 = 408;
  public static final int CONFLICT_409 = 409;
  public static final int GONE_410 = 410;
  public static final int LENGTH_REQUIRED_411 = 411;
  public static final int PRECONDITION_FAILED_412 = 412;
  public static final int PAYLOAD_TOO_LARGE_413 = 413;
  public static final int URI_TOO_LONG_414 = 414;
  public static final int UNSUPPORTED_MEDIA_TYPE_415 = 415;
  public static final int RANGE_NOT_SATISFIABLE_416 = 416;
  public static final int EXPECTATION_FAILED_417 = 417;
  public static final int IM_A_TEAPOT_418 = 418;
  public static final int ENHANCE_YOUR_CALM_420 = 420;
  public static final int MISDIRECTED_REQUEST_421 = 421;
  public static final int UNPROCESSABLE_ENTITY_422 = 422;
  public static final int LOCKED_423 = 423;
  public static final int FAILED_DEPENDENCY_424 = 424;
  public static final int UPGRADE_REQUIRED_426 = 426;
  public static final int PRECONDITION_REQUIRED_428 = 428;
  public static final int TOO_MANY_REQUESTS_429 = 429;
  public static final int REQUEST_HEADER_FIELDS_TOO_LARGE_431 = 431;
  public static final int UNAVAILABLE_FOR_LEGAL_REASONS_451 = 451;

  public static final int INTERNAL_SERVER_ERROR_500 = 500;
  public static final int NOT_IMPLEMENTED_501 = 501;
  public static final int BAD_GATEWAY_502 = 502;
  public static final int SERVICE_UNAVAILABLE_503 = 503;
  public static final int GATEWAY_TIMEOUT_504 = 504;
  public static final int HTTP_VERSION_NOT_SUPPORTED_505 = 505;
  public static final int INSUFFICIENT_STORAGE_507 = 507;
  public static final int LOOP_DETECTED_508 = 508;
  public static final int NOT_EXTENDED_510 = 510;
  public static final int NETWORK_AUTHENTICATION_REQUIRED_511 = 511;

  public static final int MAX_CODE = 511;

  private static final Code[] codeMap = new Code[MAX_CODE+1];

  static
  {
    for (Code code : Code.values())
    {
      codeMap[code.statusCode] = code;
    }
  }


  public enum Code
  {
    OK(OK_200, "OK"),
    CREATED(CREATED_201, "Created"),
    ACCEPTED(ACCEPTED_202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(NON_AUTHORITATIVE_INFORMATION_203, "Non Authoritative Information"),
    NO_CONTENT(NO_CONTENT_204, "No Content"),
    RESET_CONTENT(RESET_CONTENT_205, "Reset Content"),
    PARTIAL_CONTENT(PARTIAL_CONTENT_206, "Partial Content"),
    MULTI_STATUS(MULTI_STATUS_207, "Multi-Status"),

    BAD_REQUEST(BAD_REQUEST_400, "Bad Request"),
    UNAUTHORIZED(UNAUTHORIZED_401, "Unauthorized"),
    PAYMENT_REQUIRED(PAYMENT_REQUIRED_402, "Payment Required"),
    FORBIDDEN(FORBIDDEN_403, "Forbidden"),
    NOT_FOUND(NOT_FOUND_404, "Not Found"),
    METHOD_NOT_ALLOWED(METHOD_NOT_ALLOWED_405, "Method Not Allowed"),
    NOT_ACCEPTABLE(NOT_ACCEPTABLE_406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(PROXY_AUTHENTICATION_REQUIRED_407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(REQUEST_TIMEOUT_408, "Request Timeout"),
    CONFLICT(CONFLICT_409, "Conflict"),
    GONE(GONE_410, "Gone"),
    LENGTH_REQUIRED(LENGTH_REQUIRED_411, "Length Required"),
    PRECONDITION_FAILED(PRECONDITION_FAILED_412, "Precondition Failed"),
    PAYLOAD_TOO_LARGE(PAYLOAD_TOO_LARGE_413, "Payload Too Large"),
    URI_TOO_LONG(URI_TOO_LONG_414, "URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(UNSUPPORTED_MEDIA_TYPE_415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(RANGE_NOT_SATISFIABLE_416, "Range Not Satisfiable"),
    EXPECTATION_FAILED(EXPECTATION_FAILED_417, "Expectation Failed"),
    IM_A_TEAPOT(IM_A_TEAPOT_418, "I'm a Teapot"),
    ENHANCE_YOUR_CALM(ENHANCE_YOUR_CALM_420, "Enhance your Calm"),
    MISDIRECTED_REQUEST(MISDIRECTED_REQUEST_421, "Misdirected Request"),
    UNPROCESSABLE_ENTITY(UNPROCESSABLE_ENTITY_422, "Unprocessable Entity"),
    LOCKED(LOCKED_423, "Locked"),
    FAILED_DEPENDENCY(FAILED_DEPENDENCY_424, "Failed Dependency"),
    UPGRADE_REQUIRED(UPGRADE_REQUIRED_426, "Upgrade Required"),
    PRECONDITION_REQUIRED(PRECONDITION_REQUIRED_428, "Precondition Required"),
    TOO_MANY_REQUESTS(TOO_MANY_REQUESTS_429, "Too Many Requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(REQUEST_HEADER_FIELDS_TOO_LARGE_431, "Request Header Fields Too Large"),
    UNAVAILABLE_FOR_LEGAL_REASONS(UNAVAILABLE_FOR_LEGAL_REASONS_451, "Unavailable for Legal Reason"),

    INTERNAL_SERVER_ERROR(INTERNAL_SERVER_ERROR_500, "Server Error"),
    NOT_IMPLEMENTED(NOT_IMPLEMENTED_501, "Not Implemented"),
    BAD_GATEWAY(BAD_GATEWAY_502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(SERVICE_UNAVAILABLE_503, "Service Unavailable"),
    GATEWAY_TIMEOUT(GATEWAY_TIMEOUT_504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(HTTP_VERSION_NOT_SUPPORTED_505, "HTTP Version Not Supported"),
    INSUFFICIENT_STORAGE(INSUFFICIENT_STORAGE_507, "Insufficient Storage"),
    LOOP_DETECTED(LOOP_DETECTED_508, "Loop Detected"),
    NOT_EXTENDED(NOT_EXTENDED_510, "Not Extended"),
    NETWORK_AUTHENTICATION_REQUIRED(NETWORK_AUTHENTICATION_REQUIRED_511, "Network Authentication Required"),
    ;

    private final int statusCode;
    private final String message;

    Code(int statusCode, String message)
    {
      this.statusCode = statusCode;
      this.message =message;
    }

    public int getStatusCode()
    {
      return statusCode;
    }

    public String getMessage()
    {
      return message;
    }

    @Override
    public String toString()
    {
      return String.format("[%03d %s]",this.statusCode,this.getMessage());
    }

    /**
     * Simple test against an code to determine if it falls into the
     * <code>Success</code> message category as defined in the <a
     * href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>,
     * and <a href="http://tools.ietf.org/html/rfc7231">RFC 7231 -
     * HTTP/1.1</a>.
     *
     * @return true if within range of codes that belongs to
     *         <code>Success</code> messages.
     */
    public boolean isSuccess()
    {
      return HttpStatus.isSuccess(this.statusCode);
    }

    /**
     * Simple test against an code to determine if it falls into the
     * <code>Client Error</code> message category as defined in the <a
     * href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>,
     * and <a href="http://tools.ietf.org/html/rfc7231">RFC 7231 -
     * HTTP/1.1</a>.
     *
     * @return true if within range of codes that belongs to
     *         <code>Client Error</code> messages.
     */
    public boolean isClientError()
    {
      return HttpStatus.isClientError(this.statusCode);
    }

    /**
     * Simple test against an code to determine if it falls into the
     * <code>Server Error</code> message category as defined in the <a
     * href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>,
     * and <a href="http://tools.ietf.org/html/rfc7231">RFC 7231 -
     * HTTP/1.1</a>.
     *
     * @return true if within range of codes that belongs to
     *         <code>Server Error</code> messages.
     */
    public boolean isServerError()
    {
      return HttpStatus.isServerError(this.statusCode);
    }
  }


  /**
   * Get the HttpStatusCode for a specific code
   *
   * @param code
   *            the code to lookup.
   * @return the {@link HttpStatus} if found, or null if not found.
   */
  public static Code getCode(int code)
  {
    if (code <= MAX_CODE)
    {
      return codeMap[code];
    }
    return null;
  }

  /**
   * Get the status message for a specific code.
   *
   * @param code
   *            the code to look up
   * @return the specific message, or the code number itself if code
   *         does not match known list.
   */
  public static String getMessage(int code)
  {
    Code codeEnum = getCode(code);
    if (codeEnum != null)
    {
      return codeEnum.getMessage();
    }
    else
    {
      return Integer.toString(code);
    }
  }

  /**
   * Simple test against an code to determine if it falls into the
   * <code>Success</code> message category as defined in the <a
   * href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and <a
   * href="http://tools.ietf.org/html/rfc7231">RFC 7231 - HTTP/1.1</a>.
   *
   * @param code
   *            the code to test.
   * @return true if within range of codes that belongs to
   *         <code>Success</code> messages.
   */
  public static boolean isSuccess(int code)
  {
    return ((200 <= code) && (code <= 299));
  }

  /**
   * Simple test against an code to determine if it falls into the
   * <code>Client Error</code> message category as defined in the <a
   * href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and <a
   * href="http://tools.ietf.org/html/rfc7231">RFC 7231 - HTTP/1.1</a>.
   *
   * @param code
   *            the code to test.
   * @return true if within range of codes that belongs to
   *         <code>Client Error</code> messages.
   */
  public static boolean isClientError(int code)
  {
    return ((400 <= code) && (code <= 499));
  }

  /**
   * Simple test against an code to determine if it falls into the
   * <code>Server Error</code> message category as defined in the <a
   * href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and <a
   * href="http://tools.ietf.org/html/rfc7231">RFC 7231 - HTTP/1.1</a>.
   *
   * @param code
   *            the code to test.
   * @return true if within range of codes that belongs to
   *         <code>Server Error</code> messages.
   */
  public static boolean isServerError(int code)
  {
    return ((500 <= code) && (code <= 599));
  }
}
