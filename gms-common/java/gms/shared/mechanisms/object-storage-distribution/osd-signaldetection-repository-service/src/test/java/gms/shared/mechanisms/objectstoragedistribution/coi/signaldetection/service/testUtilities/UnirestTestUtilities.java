package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;

/**
 * Utilities for making REST calls using the Unirest. Used in tests.
 */
public class UnirestTestUtilities {

  /*
   * Configure the ObjectMapper Unirest will use to serialize requests to use our custom setup.
   */
  static {

    com.fasterxml.jackson.databind.ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    Unirest.setObjectMapper(new ObjectMapper() {

      public <T> T readValue(String s, Class<T> aClass) {
        try {
          return objectMapper.readValue(s, aClass);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object o) {
        try {
          return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  /**
   * Posts JSON serialized from the input obj to the specific url
   * and gets back the HttpResponse<T>, where T is a type parameter of the response type,
   * expecting Json back.
   *
   * @param obj the obj to post to the url as json
   * @param url the url to post to
   * @param responseType the response type expected back, a type parameter
   * @param <T> the type parameter of the response type
   * @return http response from the url, if any.
   * @throws Exception if the url is unreachable, serialization issues, malformed request, etc.
   */
  public static <T> HttpResponse<T> postJson(Object obj, String url, Class<T> responseType)
      throws Exception {
    return Unirest.post(url)
        .header("Accept", "application/json")
        .header("Content-type", "application/json")
        .header("Connection", "close")
        .body(obj)
        .asObject(responseType);
  }

   /**
   * Posts JSON serialized from the input obj to the specific url and gets back the HttpResponse<T>,
   * where T is a type parameter of the response type, expecting msgpack back.
   *
   * @param obj the obj to post to the url as json
   * @param url the url to post to
   * @param responseType the response type expected back, a type parameter
   * @param <T> the type parameter of the response type
   * @return http response from the url, if any.
   * @throws Exception if the url is unreachable, serialization issues, malformed request, etc.
   */
  public static <T> HttpResponse<T> postMsgpack(Object obj, String url, Class<T> responseType)
      throws Exception {
    return Unirest.post(url)
        .header("Accept", "application/msgpack")
        .header("Content-Type", "application/msgpack")
        .header("Connection", "close")
        .body(obj)
        .asObject(responseType);
  }

  /**
   * Does a GET request to the specific url and gets back the HttpResponse<T>, where T is a type
   * parameter of the response type, expecting msgpack back.
   *
   * @param url the url to post to
   * @param responseType the response type expected back, a type parameter
   * @param <T> the type parameter of the response type
   * @return http response from the url, if any.
   * @throws Exception if the url is unreachable, serialization issues, malformed request, etc.
   */
  public static <T> HttpResponse<T> getMsgPack(String url, Class<T> responseType) throws Exception {
    return Unirest.get(url)
        .header("Accept", "application/msgpack")
        .header("Connection", "close")
        .asObject(responseType);
  }

   /**
   * Does a GET request to the specific url and gets back the HttpResponse<T>, where T is a type
   * parameter of the response type.
   *
   * @param url the url to post to
   * @param responseType the response type expected back, a type parameter
   * @param <T> the type parameter of the response type
   * @return http response from the url, if any.
   * @throws Exception if the url is unreachable, serialization issues, malformed request, etc.
   */
  public static <T> HttpResponse<T> getJson(String url, Class<T> responseType) throws Exception {
    return Unirest.get(url)
        .header("Accept", "application/json")
        .header("Connection", "close")
        .asObject(responseType);
  }

  /**
   * Performs a GET request to the provided URL and returns the result in a String.  Requests
   * the server respond with Content-Type of application/json
   *
   * @param url the url to get
   * @return {@link HttpResponse} from the server with a String body
   * @throws UnirestException if the url is unreachable, serialization issues, malformed request,
   * etc.
   */
  public static HttpResponse<String> getJson(String url) throws UnirestException {
    return get(url, "application/json");
  }

  /**
   * Performs a GET request to the provided URL and returns the result in a String.  Requests
   * the server respond with the provided responseContentType (e.g. application/json)
   *
   * @param url get this url
   * @param acceptType request the server provide a response with this type
   * @return {@link HttpResponse} from the server with a String body
   * @throws UnirestException if the url is unreachable, serialization issues, malformed
   * request,etc.
   */
  public static HttpResponse<String> get(String url, String acceptType) throws UnirestException {
    return Unirest.get(url)
        .header("Accept", acceptType)
        .header("Connection", "close")
        .asObject(String.class);
  }

}
