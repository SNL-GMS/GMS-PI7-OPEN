package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;

/**
 * Utilities for making REST calls using the Unirest. Used in tests.
 */
public class UnirestTestUtilities {

  /**
   * Configure the ObjectMapper Unirest will use to serialize requests
   * to use our custom setup.
   */
  static {

    Unirest.setObjectMapper(new ObjectMapper() {

      public <T> T readValue(String s, Class<T> aClass) {
        try {
          return TestFixtures.objectMapper.readValue(s, aClass);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object o) {
        try {
          return TestFixtures.objectMapper.writeValueAsString(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  /**
   * Posts JSON serialized from the input obj to the specific url and gets back the HttpResponse<T>,
   * where T is a type parameter of the response type.
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
        .header("Content-Type", "application/json")
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
        .header("Accept", "application/json")
        .header("Content-Type", "application/msgpack")
        .header("Connection", "close")
        .body(obj)
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
}
