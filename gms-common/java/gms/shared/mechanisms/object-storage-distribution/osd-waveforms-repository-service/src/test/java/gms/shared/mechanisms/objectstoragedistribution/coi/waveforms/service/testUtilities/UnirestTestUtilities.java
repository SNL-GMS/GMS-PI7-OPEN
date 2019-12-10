package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.testUtilities;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RawBody;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;

/**
 * Utilities for making REST calls using the Unirest. Used in tests.
 */
public class UnirestTestUtilities {

  private static final com.fasterxml.jackson.databind.ObjectMapper msgpackMapper
      = CoiObjectMapperFactory.getMsgpackObjectMapper();

  /**
   * Configure the ObjectMapper Unirest will use to serialize requests
   * to use our custom setup.
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
  public static <T> HttpResponse<T> postJson(Object obj, String url, String acceptType,
      Class<T> responseType) throws Exception {
    return Unirest.post(url)
        .header("Accept", acceptType)
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
  public static <T> HttpResponse<T> postMsgpack(Object obj, String url, String acceptType,
      Class<T> responseType) throws Exception {
    RawBody body = Unirest.post(url)
        .header("Accept", acceptType)
        .header("Content-Type", "application/msgpack")
        .header("Connection", "close")
        .body(msgpackMapper.writeValueAsBytes(obj));
    return body.asObject(responseType);
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
   * Does a GET request to the specific url and gets back the HttpResponse<T>, where T is a type
   * parameter of the response type.
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
}
