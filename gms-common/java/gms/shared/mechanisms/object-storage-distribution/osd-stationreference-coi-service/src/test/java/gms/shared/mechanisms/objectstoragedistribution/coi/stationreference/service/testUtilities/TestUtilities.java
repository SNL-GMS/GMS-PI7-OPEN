package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities;

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
    HttpResponse<String> response = UnirestTestUtilities
        .getJson(uri, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
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
   * Helper method to get request specified endpoint, which returns the response.
   *
   * @param uri the endpoint to hit
   * @return HttpResponse<String>
   */
  public static HttpResponse<String> postResponseFromEndPoint(Object obj, String uri)
      throws Exception {
    return UnirestTestUtilities.postJson(obj, uri, String.class);
  }
}

