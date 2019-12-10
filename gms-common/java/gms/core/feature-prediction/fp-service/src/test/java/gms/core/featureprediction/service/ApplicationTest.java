package gms.core.featureprediction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.utilities.service.HttpService;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ApplicationTest {

  private static int PORT = 10252;
  private static String endpoint1 = "/feature-measurement/prediction/for-source-and-receiver-locations";
  private static String endpoint2 = "/feature-measurement/prediction/for-location-solution-and-channel";
  private static String aliveEndpoint = "/is-alive";

  private HttpService httpService;

  @Before
  public void setUp() {
    Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
      private ObjectMapper jacksonObjectMapper =
          CoiObjectMapperFactory.getJsonObjectMapper();

      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return jacksonObjectMapper.readValue(value, valueType);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object value) {
        try {
          return jacksonObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    });

    httpService = new HttpService(Application.getHttpServiceDefinition(PORT));
    httpService.start();
  }

  @After
  public void tearDown() {
    httpService.stop();
  }

  @Test
  public void testIsAlive() throws Exception {
    HttpResponse<String> response = Unirest.post("http://127.0.0.1:" + PORT + aliveEndpoint)
        .asString();

    System.out.println("RESPONSE:" + response.getBody());
    Pattern p = Pattern.compile("\\d+");
    Matcher m = p.matcher(response.getBody());
    assertTrue(m.find());
  }

  //TODO: This is begin ignored because the servce is responding with actual values since the
  //incorporation of plugins
  @Test
  public void testFeaturePredictionServiceArrivalTime() throws Exception {

    // Test that we get a response.
    HttpResponse<JsonNode> jsonResponse = Unirest
        .post("http://127.0.0.1:" + PORT + endpoint1)
        .header("accept", "application/json")
        .body(TestFixtures.streamingFeaturePredictionsForSourceAndReceiverLocationsArrivalTime)
        .asJson();

    // Get the JSON input.
    ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();
    String json = om
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(
            TestFixtures.streamingFeaturePredictionsForSourceAndReceiverLocationsArrivalTime);

    // show how to get the raw Spark object of the request
    FeaturePrediction[] coi = om.readValue(
        jsonResponse.getBody().toString(),
        FeaturePrediction[].class);
  }

  //TODO: This is begin ignored because the servce is responding with actual values since the
  //incorporation of plugins
  @Test
  public void testFeaturePredictionServiceSlownessPcalc() throws Exception {

    // Test that we get a response.
    HttpResponse<JsonNode> jsonResponse = Unirest
        .post("http://127.0.0.1:" + PORT + endpoint1)
        .header("accept", "application/json")
        .body(TestFixtures.streamingFeaturePredictionsForSourceAndReceiverLocationsSlowness)
        .asJson();

    // Get the JSON input.
    ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();
    String json = om
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(
            TestFixtures.streamingFeaturePredictionsForSourceAndReceiverLocationsSlowness);

    // show how to get the raw Spark object of the request
    FeaturePrediction[] coi = om.readValue(
        jsonResponse.getBody().toString(),
        FeaturePrediction[].class);

  }

  @Test
  public void testFeaturePredictionServiceSlownessFeatureTest() throws Exception {

    // Test that we get a response.
    HttpResponse<JsonNode> jsonResponse = Unirest
        .post("http://127.0.0.1:" + PORT + endpoint1)
        .header("accept", "application/json")
        .body(TestFixtures.featureCoi)
        .asJson();

    // Get the JSON input.
    ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();
    String json = om
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(
            TestFixtures.featureCoi);

    // show how to get the raw Spark object of the request
    FeaturePrediction[] coi = om.readValue(
        jsonResponse.getBody().toString(),
        FeaturePrediction[].class);

  }

  @Test
  public void testFeaturePredictionServiceEllipticityFeatureTest() throws Exception {

    // Test that we get a response.
    HttpResponse<JsonNode> jsonResponse = Unirest
        .post("http://127.0.0.1:" + PORT + endpoint1)
        .header("accept", "application/json")
        .body(TestFixtures.featureCoi2)
        .asJson();

    // Get the JSON input.
    ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();
    String json = om
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(
            TestFixtures.featureCoi2);

    // show how to get the raw Spark object of the request
    FeaturePrediction[] coi = om.readValue(
        jsonResponse.getBody().toString(),
        FeaturePrediction[].class);
  }

  /**
   * When the ellipticity correction plugin will throw an OutOfRangeException when distances aren't
   * in the allowable range. When this happens the feature prediction should contain an Optional.empty
   * prediction value, because the underlying correction is NaN.
   */
  @Test
  public void testDistancesOutOfRange() throws Exception {

    final StreamingFeaturePredictionsForSourceAndReceiverLocations input =
        TestFixtures.streamingFeaturePredictionsForSlownessDistanceOutOfRange;

    final int numPredictions = input.getFeatureMeasurementTypes().size() *
        input.getReceiverLocations().size();

    // Test that we get a response.
    HttpResponse<String> jsonResponse = Unirest
        .post("http://127.0.0.1:" + PORT + endpoint1)
        .header("accept", "application/json")
        .body(input)
        .asString();

    // Get the JSON input.
    ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();

    // show how to get the raw Spark object of the request
    List<FeaturePrediction<?>> coi = om.readValue(
        jsonResponse.getBody(), new TypeReference<List<FeaturePrediction<?>>>() {
        });

    assertEquals(numPredictions, coi.size());

    coi.forEach(featurePrediction -> assertFalse(featurePrediction.getPredictedValue().isPresent()));

  }

  /**
   * When the ellipticity correction plugin throws an OutOfRangeException when even location's depthKm
   * is out of bounds. When this happens the feature prediction should contain an Optional.empty
   * prediction value, because the underlying correction is NaN.
   */
  @Test
  public void testDepthKmOutOfRange() throws Exception {

    final StreamingFeaturePredictionsForSourceAndReceiverLocations input =
        TestFixtures.streamingFeaturePredictionsForArrivalTimeDepthKmOutOfRange;

    final int numPredictions = input.getFeatureMeasurementTypes().size() *
        input.getReceiverLocations().size();

    // Test that we get a response.
    HttpResponse<JsonNode> jsonResponse = Unirest
        .post("http://127.0.0.1:" + PORT + endpoint1)
        .header("accept", "application/json")
        .body(input)
        .asJson();

    // Get the JSON input.
    ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();

    // show how to get the raw Spark object of the request
    List<FeaturePrediction<?>> coi = om.readValue(
        jsonResponse.getBody().toString(),
        new TypeReference<List<FeaturePrediction<?>>>() {
        });

    assertEquals(numPredictions, coi.size());

    coi.forEach(featurePrediction -> assertFalse(featurePrediction.getPredictedValue().isPresent()));
  }

}
