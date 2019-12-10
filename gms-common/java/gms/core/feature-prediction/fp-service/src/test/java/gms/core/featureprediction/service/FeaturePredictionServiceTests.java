package gms.core.featureprediction.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;


class FeaturePredictionServiceTests {

  // Host to run integration tests against.
  private static String host;

  // ObjectMapper for serializing/deserializing data to/from Json
  private static ObjectMapper jsonObjectMapper;

  // Precision used for determining equality between doubles
  private static final double PRECISION = 0.0001;


  // Loads configuration and sets the appropriate host to run integration tests against.  Throws
  // IllegalStateException if configuration cannot be loaded.
  @BeforeAll
  static void init() {

    Configuration configuration;

    try {

      configuration = FeaturePredictionServiceTests.loadConfiguration();
    } catch (ConfigurationException e) {

      throw new IllegalStateException("Unable to load required configuration", e);
    }

    FeaturePredictionServiceTests.host = FeaturePredictionServiceTests.resolveHost(configuration);

    // Set Json ObjectMapper to use for serialization/deserialization
    FeaturePredictionServiceTests.jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  }


  // Tests predicting arrival time via source and receiver location without corrections.
  @IntegrationTest
  void testSourceAndReceiverLocationArrivalTime() throws IOException, UnirestException {

    // Create input object to pass to feature prediction endpoint
    StreamingFeaturePredictionsForSourceAndReceiverLocations input = StreamingFeaturePredictionsForSourceAndReceiverLocations
        .from(
            List.of(FeatureMeasurementTypes.ARRIVAL_TIME),
            EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH),
            List.of(Location.from(10.0, 0.0, 0.0, 0.0)),
            PhaseType.P,
            "ak135",
            List.of(),
            ProcessingContext.from(
                Optional.empty(),
                Optional.empty(),
                StorageVisibility.PUBLIC
            )
        );

    // Serialize input object to Json
    String inputJson = FeaturePredictionServiceTests.jsonObjectMapper.writeValueAsString(input);

    // Request a feature prediction for source and receiver locations
    HttpResponse<String> response = Unirest.post(FeaturePredictionServiceTests.host
        + "/feature-measurement/prediction/for-source-and-receiver-locations").body(inputJson)
        .asString();

    // Assert we got 200 response code.  If not, provide (hopefully) informative error message
    Assertions.assertEquals(200, response.getStatus(), String
        .format("Received response code \"%d\" (%s): %s", response.getStatus(),
            response.getStatusText(), response.getBody()));

    // Deserialize response body into list of feature predictions
    List<FeaturePrediction> predictionResults = FeaturePredictionServiceTests.jsonObjectMapper
        .readValue(response.getBody(), new TypeReference<List<FeaturePrediction>>() {
        });

    // Assert we received the correct number of predictions from service
    final int numExpectedPredictions = 1;
    Assertions.assertEquals(numExpectedPredictions, predictionResults.size(), String
        .format("Expected %d predictions returned from service but %d were returned instead",
            numExpectedPredictions, predictionResults.size()));

    // Define expected predicted value, standard deviation, and unit type
    final double expectedPredictedValue = 144.8956;
    final double expectedStandardDeviation = 1.16;
    final Units expectedUnitType = Units.SECONDS;

    // Extract the single FeaturePrediction from the list of returned FeaturePredictions
    FeaturePrediction<?> featurePrediction = predictionResults.iterator().next();

    // Assert we received correct predicted value
    final Instant instant = ((InstantValue) featurePrediction.getPredictedValue()
        .orElseThrow(AssertionError::new)).getValue();
    final double predictedValue =
        instant.getEpochSecond() + (double) instant.getNano() / 1_000_000_000;
    Assertions.assertEquals(expectedPredictedValue, predictedValue,
        FeaturePredictionServiceTests.PRECISION, String
            .format("Received predicted value %f but expected %f", predictedValue,
                expectedPredictedValue));

    // Assert we received correct standard deviation
    final double standardDeviation =
        ((InstantValue) featurePrediction.getPredictedValue().orElseThrow(AssertionError::new))
            .getStandardDeviation().toNanos()
            / 1_000_000_000.0;
    Assertions.assertEquals(expectedStandardDeviation, standardDeviation,
        FeaturePredictionServiceTests.PRECISION, String
            .format("Received standard deviation %f but expected %f", standardDeviation,
                expectedStandardDeviation));

    // Assert we received correct unit type
    //TODO: NOTE: FeaturePrediction<InstantMeasurmentType> does not have units - the value is and InstantValue class.
    /*final Units unitType = featurePrediction.getPredictedValue().getUnits();
    Assertions.assertEquals(expectedUnitType, unitType, String
        .format("Received unit type %s but expected %s", unitType, expectedUnitType));*/
  }


  // Tests predicting azimuth and slowness via location solution and channel without corrections.
  @IntegrationTest
  void testLocationSolutionAndChannelAzimuthSlowness() throws IOException, UnirestException {

    // Create input object to pass to feature prediction endpoint
    StreamingFeaturePredictionsForLocationSolutionAndChannel input = StreamingFeaturePredictionsForLocationSolutionAndChannel
        .from(
            List.of(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
                FeatureMeasurementTypes.SLOWNESS),
            LocationSolution.create(
                EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH),
                LocationRestraint.from(
                    RestraintType.UNRESTRAINED,
                    null,
                    RestraintType.UNRESTRAINED,
                    null,
                    DepthRestraintType.UNRESTRAINED,
                    null,
                    RestraintType.UNRESTRAINED,
                    null
                ),
                null,
                Set.of(),
                Set.of()
            ),
            List.of(Channel.create("FAKE", ChannelType.HIGH_BROADBAND_HIGH_GAIN_NORTH_SOUTH,
                ChannelDataType.SEISMIC_3_COMPONENT, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)),
            PhaseType.P,
            "ak135",
            List.of(),
            ProcessingContext.from(
                Optional.empty(),
                Optional.empty(),
                StorageVisibility.PUBLIC
            )
        );

    // Serialize input object to Json
    String inputJson = FeaturePredictionServiceTests.jsonObjectMapper.writeValueAsString(input);

    // Request a feature prediction for location solution and channel
    HttpResponse<String> response = Unirest.post(FeaturePredictionServiceTests.host
        + "/feature-measurement/prediction/for-location-solution-and-channel").body(inputJson)
        .asString();

    // Assert we got 200 response code.  If not, provide (hopefully) informative error message
    Assertions.assertEquals(200, response.getStatus(), String
        .format("Received response code \"%d\" (%s): %s", response.getStatus(),
            response.getStatusText(), response.getBody()));

    // Deserialize response into LocationSolution
    LocationSolution locationSolutionResult = FeaturePredictionServiceTests.jsonObjectMapper
        .readValue(response.getBody(), LocationSolution.class);

    // Extract FeaturePredictions from returned LocationSolution
    Set<FeaturePrediction<?>> predictionResults = locationSolutionResult.getFeaturePredictions();

    // Assert we received the correct number of predictions from service
    final int numExpectedPredictions = 2;
    Assertions.assertEquals(numExpectedPredictions, predictionResults.size(), String
        .format("Expected %d predictions returned from service but %d were returned instead",
            numExpectedPredictions, predictionResults.size()));

    // Extract azimuth prediction from set of returned FeaturePredictions.  If more than one
    // prediction is found, throw IllegalStateException.  Stream.reduce() returns an optional if no values
    // are present, so if the returned optional is empty, no prediction was found, so throw
    // IllegalStateException.
    FeaturePrediction<?> azimuthPrediction = predictionResults.stream().filter(fp ->
        fp.getPredictionType().equals(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH)
    ).reduce((a, b) -> {
          throw new IllegalStateException(
              "Received two azimuth predictions when only one was expected");
        }
    ).orElseThrow(
        () -> new IllegalStateException("Received no azimuth predictions when one was expected"));

    // Extract slowness prediction from set of returned FeaturePredictions.  If more than one
    // prediction is found, throw IllegalStateException.  Stream.reduce() returns an optional if no values
    // are present, so if the returned optional is empty, no prediction was found, so throw
    // IllegalStateException.
    FeaturePrediction<?> slownessPrediction = predictionResults.stream().filter(fp ->
        fp.getPredictionType().equals(FeatureMeasurementTypes.SLOWNESS)
    ).reduce((a, b) -> {
          throw new IllegalStateException(
              "Received two slowness predictions when only one was expected");
        }
    ).orElseThrow(
        () -> new IllegalStateException("Received no slowness predictions when one was expected"));

    // Define expected predicted value, standard deviation, and unit type for azimuth prediction
    final DoubleValue expectedPredictedValueAzimuth = DoubleValue.from(
        180.0,
        20,
        Units.DEGREES
    );

    // Extract actual predicted value, standard deviation, and unit type for azimuth prediction
    final DoubleValue predictedValueAzimuth = ((NumericMeasurementValue) azimuthPrediction
        .getPredictedValue().orElseThrow(() -> new IllegalStateException(""))).getMeasurementValue();

    // Validate that the actual prediction matches the expected prediction
    this.validatePredictedValue(expectedPredictedValueAzimuth, predictedValueAzimuth);

    // Define expected predicted value, standard deviation, and unit type for azimuth prediction
    final DoubleValue expectedPredictedValueSlowness = DoubleValue.from(
        13.7006,
        3.10,
        Units.SECONDS_PER_DEGREE
    );

    // Extract actual predicted value, standard deviation, and unit type for azimuth prediction
    final DoubleValue predictedValueSlowness = ((NumericMeasurementValue) slownessPrediction
        .getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue();

    // Validate that the actual prediction matches the expected prediction
    this.validatePredictedValue(expectedPredictedValueSlowness, predictedValueSlowness);
  }


  // Utility method for asserting that the individual components of two DoubleValues are equal
  private void validatePredictedValue(DoubleValue expected, DoubleValue predicted) {

    // Extract expected predicted value, standard deviation, and unit type
    final double expectedPredictedValue = expected.getValue();
    final double expectedStandardDeviation = expected.getStandardDeviation();
    final Units expectedUnitType = expected.getUnits();

    // Extract predicted value, standard deviation, and unit type
    final double predictedValue = predicted.getValue();
    final double standardDeviation = predicted.getStandardDeviation();
    final Units unitType = predicted.getUnits();

    // Assert we received correct predicted value
    Assertions.assertEquals(expectedPredictedValue, predictedValue,
        FeaturePredictionServiceTests.PRECISION, String
            .format("Received predicted value %f but expected %f", predictedValue,
                expectedPredictedValue));

    // Assert we received correct standard deviation
    Assertions.assertEquals(expectedStandardDeviation, standardDeviation,
        FeaturePredictionServiceTests.PRECISION, String
            .format("Received standard deviation %f but expected %f", standardDeviation,
                expectedStandardDeviation));

    // Assert we received correct unit type
    Assertions.assertEquals(expectedUnitType, unitType, String
        .format("Received unit type %s but expected %s", unitType, expectedUnitType));
  }


  // Utility method for loading configuration for this integration test class.
  // Priorities configuration in environment variables over configuration in properties file.
  private static Configuration loadConfiguration() throws ConfigurationException {

    CompositeConfiguration config = new CompositeConfiguration();
    config.addConfiguration(new EnvironmentConfiguration());
    config.addConfiguration(new PropertiesConfiguration("integration.properties"));

    // Configuration will throw exception when a key is requested that does not exist instead
    // of returning null
    config.setThrowExceptionOnMissing(true);

    return config;
  }


  // Utility method for resolving the host for which to run integration tests against.  Throws
  // IllegalStateException if "host" key does not exist in configuration.
  private static String resolveHost(Configuration configuration) {

    String host;

    try {

      host = configuration.getString("host");
    } catch (NoSuchElementException e) {

      throw new IllegalStateException(
          "Unable to load required \"host\" property from configuration", e);
    }

    return host;
  }
}
