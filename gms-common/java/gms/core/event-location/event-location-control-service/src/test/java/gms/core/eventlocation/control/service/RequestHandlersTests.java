package gms.core.eventlocation.control.service;

import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.eventlocation.control.EventHypothesisClaimCheck;
import gms.core.eventlocation.control.EventLocationControl;
import gms.core.eventlocation.control.EventLocationControlParameters;
import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionGeigers;
import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.utilities.service.HttpStatus.Code;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.Response;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RequestHandlersTests {

  private Request request;

  private final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private final EventLocationControl eventLocationControl = Mockito
      .mock(EventLocationControl.class);

  private final RequestHandlers requestHandlers = RequestHandlers.create(eventLocationControl);

  private final EventLocationDefinition eventLocationDefinition = EventLocationDefinition
      .create(100, 0.01,
          12.34,
          "TESTMODEL",
          false,
          ScalingFactorType.CONFIDENCE,
          4,
          0.3,
          4,
          List.of(LocationRestraint.from(
              RestraintType.UNRESTRAINED,
              0.0,
              RestraintType.UNRESTRAINED,
              0.0,
              DepthRestraintType.UNRESTRAINED,
              0.0,
              RestraintType.UNRESTRAINED,
              Instant.EPOCH)));

  private final LocateValidationInput locateValidationInput = LocateValidationInput.create(
      TestFixtures.associatedEventHypothesis,
      TestFixtures.signalDetections,
      TestFixtures.stations
  );

  private final EventLocationControlParameters eventLocationControlParameters = EventLocationControlParameters
      .create(PluginInfo.from("myCoolPlugin", "myCoolPluginVersion"), eventLocationDefinition);

  private final Collection<EventHypothesisClaimCheck> eventHypothesisClaimChecks = List.of(
      TestFixtures.eventHypothesisClaimCheck
  );

  private final Collection<EventHypothesis> eventHypotheses = List.of(
      TestFixtures.associatedEventHypothesis
  );

  private static final String EVENT_HYPOTHESIS_CLAIM_CHECKS = "eventHypothesisClaimChecks";
  private static final String EVENT_HYPOTHESES = "eventHypotheses";
  private static final String EVENT_HYPOTHESIS = "eventHypothesis";
  private static final String EVENT_LOCATION_CONTROL_PARAMETERS = "parameters";
  private static final String SIGNAL_DETECTIONS = "signalDetections";
  private static final String REFERENCE_STATIONS = "referenceStations";

  private static final String MISSING_PARAMETER_MSG = "Json field \"%s\" does not exist in the request body";
  private static final String BAD_PARAMETER_MSG = "Could not deserialize contents of JSON field \"%s\"";
  private static final String BAD_REQUEST_BODY_MSG = "Could not deserialize request body into JsonNode";

  @BeforeEach
  void init() {
    this.request = Mockito.mock(Request.class);
  }

  @AfterEach
  void teardown() {
    this.request = null;
  }

  // * * * * * * * *
  // Test isAlive()
  // * * * * * * * *

  @Test
  void testIsAlive() {

    // Execute request

    Response<String> response = this.requestHandlers.isAlive(this.request, this.jsonObjectMapper);

    // Assert we got 200 response

    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert we got back a response body

    Assertions.assertTrue(response.getBody().isPresent());
  }

  // * * * * * * *
  // Test locate()
  // * * * * * * *

  @Test
  void testLocate() throws JsonProcessingException, TooManyRestraintsException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("eventHypothesisClaimChecks", this.eventHypothesisClaimChecks)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the control class returns EventHypothesisClaimChecks

    given(this.eventLocationControl.locate(this.eventHypothesisClaimChecks))
        .willReturn(this.eventHypothesisClaimChecks);

    // Execute the request

    Response<Collection<EventHypothesisClaimCheck>> response = this.requestHandlers
        .locate(this.request, this.jsonObjectMapper);

    // Assert we got 200 response code

    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert response body is present

    Assertions.assertTrue(response.getBody().isPresent());

    // Assert we got the correct response body

    Assertions.assertEquals(this.eventHypothesisClaimChecks, response.getBody().get());
  }

  @Test
  void testLocateOverrideParameters() throws JsonProcessingException, TooManyRestraintsException {

    // Create request body parameters

    EventLocationControlParameters parameters = EventLocationControlParameters.create(
        PluginInfo.from("eventLocationPluginGeigers", "1.0.0"),
        EventLocationDefinitionGeigers.create(
            1,
            2.3,
            6.7,
            "ak135",
            true,
            ScalingFactorType.CONFIDENCE,
            6,
            1.0,
            4,
            2,
            true,
            0.1,
            10.0,
            0.0001,
            0.01,
            1.0e5,
            4.5,
            0.1,
            8,
            List.of(LocationRestraint.from(
                RestraintType.UNRESTRAINED,
                0.0,
                RestraintType.UNRESTRAINED,
                0.0,
                DepthRestraintType.UNRESTRAINED,
                0.0,
                RestraintType.UNRESTRAINED,
                Instant.EPOCH))
        )
    );

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("eventHypothesisClaimChecks", this.eventHypothesisClaimChecks),
        Map.entry("parameters", parameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the control class returns EventHypothesisClaimChecks

    given(
        this.eventLocationControl.locate(this.eventHypothesisClaimChecks, parameters))
        .willReturn(this.eventHypothesisClaimChecks);

    // Execute the request

    Response<Collection<EventHypothesisClaimCheck>> response = this.requestHandlers
        .locate(this.request, this.jsonObjectMapper);

    // Assert we got 200 response code

    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert response body is present

    Assertions.assertTrue(response.getBody().isPresent());

    // Assert we got the correct response body

    Assertions.assertEquals(this.eventHypothesisClaimChecks, response.getBody().get());
  }

  @Test
  void testLocateBadRequestBodyJson() {

    // Mock that the request body returns bad Json

    given(this.request.getBody()).willReturn("asdf");

    // Execute the request

    Response<Collection<EventHypothesisClaimCheck>> response = this.requestHandlers
        .locate(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(
        response.getErrorMessage().get().contains(RequestHandlersTests.BAD_REQUEST_BODY_MSG));
  }

  @Test
  void testLocateMissingEventHypothesisClaimChecks() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS,
            this.eventLocationControlParameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Collection<EventHypothesisClaimCheck>> response = this.requestHandlers
        .locate(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertEquals(
        String.format(RequestHandlersTests.MISSING_PARAMETER_MSG,
            RequestHandlersTests.EVENT_HYPOTHESIS_CLAIM_CHECKS),
        response.getErrorMessage().get()
    );
  }

  @Test
  void testLocateBadEventHypothesisClaimChecks() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESIS_CLAIM_CHECKS, UUID.randomUUID()),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS,
            this.eventLocationControlParameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Collection<EventHypothesisClaimCheck>> response = this.requestHandlers
        .locate(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(response.getErrorMessage().get()
        .contains(String.format(RequestHandlersTests.BAD_PARAMETER_MSG,
            RequestHandlersTests.EVENT_HYPOTHESIS_CLAIM_CHECKS)));
  }

  @Test
  void testLocateBadParameters() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESIS_CLAIM_CHECKS,
            this.eventHypothesisClaimChecks),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS, UUID.randomUUID())
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Collection<EventHypothesisClaimCheck>> response = this.requestHandlers
        .locate(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(response.getErrorMessage().get()
        .contains(String.format(RequestHandlersTests.BAD_PARAMETER_MSG,
            RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS)));
  }

  // * * * * * * * * * * * * *
  // Test locateInteractive()
  // * * * * * * * * * * * * *

  @Test
  void testLocateInteractive() throws JsonProcessingException, TooManyRestraintsException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESES, this.eventHypotheses),
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, TestFixtures.signalDetections)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the control class returns LocationSolutions map

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = Map.ofEntries(
        Map.entry(UUID.randomUUID(), Set.of(
            this.eventHypotheses.iterator().next().getLocationSolutions().iterator().next()))
    );

    given(this.eventLocationControl
        .locateInteractive(this.eventHypotheses, TestFixtures.signalDetections))
        .willReturn(locationSolutionsMap);

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateInteractive(this.request, this.jsonObjectMapper);

    // Assert we got 200 response code

    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert response body is present

    Assertions.assertTrue(response.getBody().isPresent());

    // Assert we got the correct response body

    Assertions.assertEquals(locationSolutionsMap, response.getBody().get());
  }

  @Test
  void testLocateInteractiveOverrideParameters()
      throws JsonProcessingException, TooManyRestraintsException {

    // Create request body parameters

    EventLocationControlParameters parameters = EventLocationControlParameters.create(
        PluginInfo.from("eventLocationPluginGeigers", "1.0.0"),
        EventLocationDefinitionGeigers.create(
            1,
            2.3,
            6.7,
            "ak135",
            true,
            ScalingFactorType.CONFIDENCE,
            6,
            1.0,
            4,
            2,
            true,
            0.1,
            10.0,
            0.0001,
            0.01,
            1.0e5,
            4.5,
            0.1,
            8,
            List.of(LocationRestraint.from(
                RestraintType.UNRESTRAINED,
                0.0,
                RestraintType.UNRESTRAINED,
                0.0,
                DepthRestraintType.UNRESTRAINED,
                0.0,
                RestraintType.UNRESTRAINED,
                Instant.EPOCH))
        )
    );

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESES, this.eventHypotheses),
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, TestFixtures.signalDetections),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS, parameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the control class returns LocationSolutions map

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = Map.ofEntries(
        Map.entry(UUID.randomUUID(), Set.of(
            this.eventHypotheses.iterator().next().getLocationSolutions().iterator().next()))
    );

    given(this.eventLocationControl
        .locateInteractive(this.eventHypotheses, TestFixtures.signalDetections, parameters))
        .willReturn(locationSolutionsMap);

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateInteractive(this.request, this.jsonObjectMapper);

    // Assert we got 200 response code

    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert response body is present

    Assertions.assertTrue(response.getBody().isPresent());

    // Assert we got the correct response body

    Assertions.assertEquals(locationSolutionsMap, response.getBody().get());
  }

  @Test
  void testLocateInteractiveBadRequestBodyJson() {

    // Mock that the request body returns bad Json

    given(this.request.getBody()).willReturn("asdf");

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateInteractive(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(
        response.getErrorMessage().get().contains(RequestHandlersTests.BAD_REQUEST_BODY_MSG));
  }

  @Test
  void testLocateInteractiveMissingEventHypotheses() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS,
            this.eventLocationControlParameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateInteractive(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertEquals(
        String.format(RequestHandlersTests.MISSING_PARAMETER_MSG,
            RequestHandlersTests.EVENT_HYPOTHESES),
        response.getErrorMessage().get()
    );
  }

  @Test
  void testLocateInteractiveMissingSignalDetections() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESES, this.eventHypotheses),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS,
            this.eventLocationControlParameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateInteractive(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertEquals(
        String.format(RequestHandlersTests.MISSING_PARAMETER_MSG,
            RequestHandlersTests.SIGNAL_DETECTIONS),
        response.getErrorMessage().get()
    );
  }

  @Test
  void testLocateInteractiveBadEventHypotheses() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESES, UUID.randomUUID()),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS,
            this.eventLocationControlParameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateInteractive(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(response.getErrorMessage().get()
        .contains(String.format(RequestHandlersTests.BAD_PARAMETER_MSG,
            RequestHandlersTests.EVENT_HYPOTHESES)));
  }

  @Test
  void testLocateInteractiveBadSignalDetections() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESES, this.eventHypotheses),
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, UUID.randomUUID()),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS,
            this.eventLocationControlParameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateInteractive(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(response.getErrorMessage().get()
        .contains(String.format(RequestHandlersTests.BAD_PARAMETER_MSG,
            RequestHandlersTests.SIGNAL_DETECTIONS)));
  }

  @Test
  void testLocateInteractiveBadParameters() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESES, this.eventHypotheses),
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, TestFixtures.signalDetections),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS, UUID.randomUUID())
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateInteractive(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(response.getErrorMessage().get()
        .contains(String.format(RequestHandlersTests.BAD_PARAMETER_MSG,
            RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS)));
  }

  // * * * * * * * * * * * * *
  // Test locateValidation()
  // * * * * * * * * * * * * *

  @Test
  void testLocateValidation() throws JsonProcessingException, TooManyRestraintsException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESIS,
            TestFixtures.associatedEvent.getHypotheses().iterator().next()),
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, TestFixtures.signalDetections),
        Map.entry(RequestHandlersTests.REFERENCE_STATIONS, TestFixtures.stations)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the control class returns LocationSolutions map

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = Map.ofEntries(
        Map.entry(UUID.randomUUID(), Set.of(
            this.eventHypotheses.iterator().next().getLocationSolutions().iterator().next()))
    );

    given(this.eventLocationControl.locateValidation(this.locateValidationInput))
        .willReturn(locationSolutionsMap);

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateValidation(this.request, this.jsonObjectMapper);

    // Assert we got 200 response code

    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert response body is present

    Assertions.assertTrue(response.getBody().isPresent());

    // Assert we got the correct response body

    Assertions.assertEquals(locationSolutionsMap, response.getBody().get());
  }

  @Test
  void testLocateValidationOverrideParameters()
      throws JsonProcessingException, TooManyRestraintsException {

    // Create request body parameters

    EventLocationControlParameters parameters = EventLocationControlParameters.create(
        PluginInfo.from("eventLocationPluginGeigers", "1.0.0"),
        EventLocationDefinitionGeigers.create(
            1,
            2.3,
            6.7,
            "ak135",
            true,
            ScalingFactorType.CONFIDENCE,
            6,
            1.0,
            4,
            2,
            true,
            0.1,
            10.0,
            0.0001,
            0.01,
            1.0e5,
            4.5,
            0.1,
            8,
            List.of(LocationRestraint.from(
                RestraintType.UNRESTRAINED,
                0.0,
                RestraintType.UNRESTRAINED,
                0.0,
                DepthRestraintType.UNRESTRAINED,
                0.0,
                RestraintType.UNRESTRAINED,
                Instant.EPOCH))
        )
    );

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESIS,
            TestFixtures.associatedEvent.getHypotheses().iterator().next()),
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, TestFixtures.signalDetections),
        Map.entry(RequestHandlersTests.REFERENCE_STATIONS, TestFixtures.stations),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS, parameters)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the control class returns LocationSolutions map

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = Map.ofEntries(
        Map.entry(UUID.randomUUID(), Set.of(
            this.eventHypotheses.iterator().next().getLocationSolutions().iterator().next()))
    );

    given(this.eventLocationControl.locateValidation(this.locateValidationInput, parameters))
        .willReturn(locationSolutionsMap);

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateValidation(this.request, this.jsonObjectMapper);

    // Assert we got no error msg

    Assertions.assertEquals(Optional.empty(), response.getErrorMessage());

    // Assert we got 200 response code

    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert response body is present

    Assertions.assertTrue(response.getBody().isPresent());

    // Assert we got the correct response body

    Assertions.assertEquals(locationSolutionsMap, response.getBody().get());
  }

  @Test
  void testLocateValidationBadRequestBodyJson() {

    // Mock that the request body returns bad Json

    given(this.request.getBody()).willReturn("asdf");

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateValidation(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(
        response.getErrorMessage().get().contains(RequestHandlersTests.BAD_REQUEST_BODY_MSG));
  }

  @Test
  void testLocateValidationMissingEventHypothesis() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, TestFixtures.signalDetections),
        Map.entry(RequestHandlersTests.REFERENCE_STATIONS, TestFixtures.signalDetections)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateValidation(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertEquals(
        String.format(RequestHandlersTests.MISSING_PARAMETER_MSG,
            RequestHandlersTests.EVENT_HYPOTHESIS),
        response.getErrorMessage().get()
    );
  }

  @Test
  void testLocateValidationMissingSignalDetections() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESIS,
            TestFixtures.associatedEvent.getHypotheses().iterator().next()),
        Map.entry(RequestHandlersTests.REFERENCE_STATIONS, TestFixtures.signalDetections)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateValidation(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertEquals(
        String.format(RequestHandlersTests.MISSING_PARAMETER_MSG,
            RequestHandlersTests.SIGNAL_DETECTIONS),
        response.getErrorMessage().get()
    );
  }

  @Test
  void testLocateValidationMissingReferenceStations() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESIS,
            TestFixtures.associatedEvent.getHypotheses().iterator().next()),
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, TestFixtures.signalDetections)
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateValidation(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertEquals(
        String.format(RequestHandlersTests.MISSING_PARAMETER_MSG,
            RequestHandlersTests.REFERENCE_STATIONS),
        response.getErrorMessage().get()
    );
  }

  @Test
  void testLocateValidationBadParameters() throws JsonProcessingException {

    // Create request body parameters

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry(RequestHandlersTests.EVENT_HYPOTHESIS,
            TestFixtures.associatedEvent.getHypotheses().iterator().next()),
        Map.entry(RequestHandlersTests.SIGNAL_DETECTIONS, TestFixtures.signalDetections),
        Map.entry(RequestHandlersTests.REFERENCE_STATIONS, TestFixtures.stations),
        Map.entry(RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS, UUID.randomUUID())
    );

    // Mock that the request body returns request parameters as Json

    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Execute the request

    Response<Map<UUID, Set<LocationSolution>>> response = this.requestHandlers
        .locateValidation(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response code

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert we got an error message

    Assertions.assertTrue(response.getErrorMessage().isPresent());

    // Assert we got the correct error message

    Assertions.assertTrue(response.getErrorMessage().get()
        .contains(String.format(RequestHandlersTests.BAD_PARAMETER_MSG,
            RequestHandlersTests.EVENT_LOCATION_CONTROL_PARAMETERS)));
  }
}
