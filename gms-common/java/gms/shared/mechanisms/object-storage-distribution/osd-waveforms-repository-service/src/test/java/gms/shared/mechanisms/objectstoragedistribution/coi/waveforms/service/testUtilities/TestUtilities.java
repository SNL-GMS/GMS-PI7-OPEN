package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.testUtilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mashape.unirest.http.HttpResponse;
import org.eclipse.jetty.http.HttpStatus;

public class TestUtilities {

  /**
   * Helper method to test get a null or bad parameter request to a specified endpoint, which should
   * return a 'bad request'.
   *
   * @param uri the endpoint to hit
   */
  public static void testMissingParametersForEndpoints(String uri) throws Exception {
    System.out.println("Test: get request with bad or missing Parameters, URI: " + uri);

    HttpResponse<String> response = UnirestTestUtilities
        .getJson(uri, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Helper method to get request specified endpoint, which returns the response.
   *
   * @param uri the endpoint to hit
   * @return HttpResponse<String>
   */
  public static HttpResponse<String> getResponseFromEndPoint(String uri) throws Exception {
    return UnirestTestUtilities.getJson(uri, String.class);
  }

  /**
   * Helper method to get request as msgpack specified endpoint, which returns the response.
   *
   * @param uri the endpoint to hit
   * @return HttpResponse<String>
   */
  public static HttpResponse<String> postMsgpackToEndPointExpectMsgpack(Object obj, String uri)
      throws Exception {
    return UnirestTestUtilities.postMsgpack(obj, uri, "application/msgpack", String.class);
  }

  public static HttpResponse<String> postJsonToEndPointExpectJson(Object obj, String uri)
      throws Exception {
    return UnirestTestUtilities.postJson(obj, uri, "application/json", String.class);
  }

  public static HttpResponse<String> postJsonToEndPointExpectMsgpack(Object obj, String uri)
      throws Exception {
    return UnirestTestUtilities.postJson(obj, uri, "application/msgpack", String.class);
  }

  public static HttpResponse<String> postMsgpackToEndPointExpectJson(Object obj, String uri)
      throws Exception {
    return UnirestTestUtilities.postMsgpack(obj, uri, "application/json", String.class);
  }

  /**
   * Helper method to get as msgpack request specified endpoint, which returns the response.
   *
   * @param uri the endpoint to hit
   * @return HttpResponse<String>
   */
  public static HttpResponse<String> getResponseFromEndPointMsgPack(String uri) throws Exception {
    return UnirestTestUtilities.getMsgPack(uri, String.class);
  }
}

