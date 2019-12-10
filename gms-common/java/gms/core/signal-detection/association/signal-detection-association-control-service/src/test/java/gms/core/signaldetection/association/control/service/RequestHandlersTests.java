package gms.core.signaldetection.association.control.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signaldetection.association.control.EventHypothesisClaimCheck;
import gms.core.signaldetection.association.control.SignalDetectionAssociationControl;
import gms.core.signaldetection.association.control.SignalDetectionAssociationResult;
import gms.core.signaldetection.association.control.SignalDetectionHypothesisClaimCheck;
import gms.core.signaldetection.association.control.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.utilities.service.ContentType;
import gms.shared.utilities.service.HttpStatus.Code;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RequestHandlersTests {

  private final ObjectMapper jsonObjectMapper;
  private final ObjectMapper msgPackObjectMapper;

  private final RequestHandlers requestHandlers;

  private final SignalDetectionAssociationControl associationControl;
  private final Request request;

  public RequestHandlersTests() {
    this.jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    this.msgPackObjectMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();
    this.associationControl = mock(SignalDetectionAssociationControl.class);
    this.requestHandlers = RequestHandlers.create(associationControl);
    this.request = mock(Request.class);
  }

  @Test
  public void testAssociateToLocationInteractiveJson() throws IOException {

    Event event = TestFixtures.associatedEvent;

    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventLocation", TestFixtures.eventLocation),
        Map.entry("signalDetectionHypotheses", TestFixtures.signalDetectionHypotheses)
    );

    // Mock that the control class returns an Event
    given(this.associationControl
        .associateToLocationInteractive(TestFixtures.signalDetectionHypotheses,
            TestFixtures.eventLocation))
        .willReturn(event);

    // Mock that the request body returns the correct contents
    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertTrue(response.getBody().isPresent());
    Event constructedEvent = response.getBody().get();
    Assertions.assertEquals(event, constructedEvent);
  }

  @Test
  public void testAssociateToLocationsInteractiveMsgPack() throws IOException {
    EventLocation eventLocation = TestFixtures.eventLocation;
    List<SignalDetectionHypothesis> signalDetectionHypotheses = TestFixtures.signalDetectionHypotheses;
    Event event = TestFixtures.associatedEvent;

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("eventLocation", eventLocation),
        Map.entry("signalDetectionHypotheses", signalDetectionHypotheses)
    );

    // Mock that the control class returns an event
    given(this.associationControl
        .associateToLocationInteractive(signalDetectionHypotheses, eventLocation))
        .willReturn(event);

    // Mock that the request body returns MessagePack
    // Mock that the request body returns MessagePack
    given(this.request.clientSentMsgpack()).willReturn(true);
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the request body returns the correct contents
    given(this.request.getRawBody())
        .willReturn(this.msgPackObjectMapper.writeValueAsBytes(requestParams));

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.msgPackObjectMapper);

    Assertions.assertTrue(response.getBody().isPresent());
    Event constructedEvent = response.getBody().get();
    Assertions.assertEquals(event, constructedEvent);
  }

  @Test
  public void testAssociateToLocationJson() throws IOException {

    EventLocation eventLocation = TestFixtures.eventLocation;
    List<SignalDetectionHypothesisClaimCheck> signalDetectionHypothesisClaimChecks
        = TestFixtures.signalDetectionHypothesisClaimChecks;
    EventHypothesis hypothesis = TestFixtures.associatedEventHypothesis;

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("eventLocation", eventLocation),
        Map.entry("signalDetectionHypothesisClaimChecks", signalDetectionHypothesisClaimChecks)
    );

    // Mock that the control class returns an event hypothesis claim check
    given(this.associationControl
        .associateToLocation(signalDetectionHypothesisClaimChecks, eventLocation))
        .willReturn(EventHypothesisClaimCheck.from(hypothesis.getId(), UUID.randomUUID()));

    // Mock that the request body returns JSON
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_JSON));

    // Mock that the request body returns the correct contents
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.jsonObjectMapper);

    verify(this.associationControl)
        .associateToLocation(signalDetectionHypothesisClaimChecks,
            eventLocation);

    Assertions.assertTrue(response.getBody().isPresent());
    EventHypothesisClaimCheck constructedHypothesis = response.getBody().get();
    Assertions.assertEquals(hypothesis.getId(), constructedHypothesis.getEventHypothesisId());
  }

  @Test
  public void testAssociateToLocationMsgPack() throws IOException {

    EventLocation eventLocation = TestFixtures.eventLocation;
    List<SignalDetectionHypothesisClaimCheck> signalDetectionHypothesisClaimChecks
        = TestFixtures.signalDetectionHypothesisClaimChecks;
    EventHypothesis hypothesis = TestFixtures.associatedEventHypothesis;

    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("eventLocation", eventLocation),
        Map.entry("signalDetectionHypothesisClaimChecks", signalDetectionHypothesisClaimChecks)
    );

    // Mock that the control class returns an event hypothesis claim check
    given(this.associationControl
        .associateToLocation(signalDetectionHypothesisClaimChecks, eventLocation))
        .willReturn(EventHypothesisClaimCheck.from(hypothesis.getId(), UUID.randomUUID()));

    // Mock that the request body returns MessagePack
    given(this.request.clientSentMsgpack()).willReturn(true);
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the request body returns the correct contents
    given(this.request.getRawBody())
        .willReturn(this.msgPackObjectMapper.writeValueAsBytes(requestParams));

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.msgPackObjectMapper);

    verify(this.associationControl)
        .associateToLocation(signalDetectionHypothesisClaimChecks,
            eventLocation);

    Assertions.assertTrue(response.getBody().isPresent());
    EventHypothesisClaimCheck constructedHypothesis = response.getBody().get();
    Assertions.assertEquals(hypothesis.getId(), constructedHypothesis.getEventHypothesisId());
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveJson() throws IOException {

    EventHypothesis eventHypothesis = TestFixtures.unassociatedEventHypothesis;
    EventHypothesis associatedEventHypothesis = TestFixtures.associatedEventHypothesis;
    List<SignalDetectionHypothesis> signalDetectionHypotheses = TestFixtures.signalDetectionHypotheses;

    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventHypothesis", eventHypothesis),
        Map.entry("signalDetectionHypotheses", signalDetectionHypotheses)
    );

    // Mock that the control class returns an EventHypothesis
    given(this.associationControl
        .associateToEventHypothesisInteractive(signalDetectionHypotheses, eventHypothesis))
        .willReturn(associatedEventHypothesis);

    // Mock that the request body returns JSON
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_JSON));

    // Mock that the request body returns the correct contents
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesis> responseEventHypothesis = this.requestHandlers
        .associateToEventHypothesisInteractive(this.request, this.msgPackObjectMapper);

    Assertions.assertEquals(associatedEventHypothesis, responseEventHypothesis.getBody().get());
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveMsgPack() throws IOException {

    EventHypothesis eventHypothesis = TestFixtures.unassociatedEventHypothesis;
    EventHypothesis associatedEventHypothesis = TestFixtures.associatedEventHypothesis;
    List<SignalDetectionHypothesis> signalDetectionHypotheses = TestFixtures.signalDetectionHypotheses;

    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventHypothesis", eventHypothesis),
        Map.entry("signalDetectionHypotheses", signalDetectionHypotheses)
    );

    // Mock that the control class returns an EventHypothesis
    given(this.associationControl
        .associateToEventHypothesisInteractive(signalDetectionHypotheses, eventHypothesis))
        .willReturn(associatedEventHypothesis);

    // Mock that the request body returns MessagePack
    given(this.request.clientSentMsgpack()).willReturn(true);
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the request body returns the correct contents
    given(this.request.getRawBody())
        .willReturn(this.msgPackObjectMapper.writeValueAsBytes(params));

    Response<EventHypothesis> responseEventHypothesis = this.requestHandlers
        .associateToEventHypothesisInteractive(this.request, this.msgPackObjectMapper);

    Assertions.assertEquals(associatedEventHypothesis, responseEventHypothesis.getBody().get());
  }

  @Test
  public void testAssociateToLocationInteractiveMissingRequestBody() throws IOException {

    given(this.request.getBody()).willReturn(null);

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  public void testAssociateToLocationInteractiveMissingEventLocation() throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("signalDetectionHypotheses", TestFixtures.signalDetectionHypotheses)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertEquals("Json field \"eventLocation\" does not exist in the request body.",
        response.getErrorMessage().get());
  }

  @Test
  public void testAssociateToLocationInteractiveMissingSignalDetectionHypotheses()
      throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventLocation", TestFixtures.eventLocation)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertEquals(
        "List field \"signalDetectionHypotheses\" does not exist in the request body.",
        response.getErrorMessage().get());
  }

  @Test
  public void testAssociateToLocationInteractiveBadJson() throws IOException {

    given(this.request.getBody()).willReturn("asdf");

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get()
        .contains("Could not deserialize request body into JsonNode"));
  }

  @Test
  public void testAssociateToLocationInteractiveBadMsgPack() throws IOException {

    // Mock that the request body returns MessagePack
    given(this.request.clientSentMsgpack()).willReturn(true);
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));
    given(this.request.getRawBody()).willReturn(this.msgPackObjectMapper.writeValueAsBytes("asdf"));

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get()
        .contains("Could not deserialize request body into JsonNode"));
  }

  @Test
  public void testAssociateToLocationInteractiveBadEventLocation() throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventLocation", TestFixtures.unassociatedEvent),
        Map.entry("signalDetectionHypotheses", TestFixtures.signalDetectionHypotheses)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get().contains(
        "Could not deserialize contents of JSON field \"eventLocation\" into EventLocation object"));
  }

  @Test
  public void testAssociateToLocationInteractiveBadSignalDetectionHypotheses() throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventLocation", TestFixtures.eventLocation),
        Map.entry("signalDetectionHypotheses", TestFixtures.unassociatedEvent)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get().contains(
        "Could not deserialize contents of JSON field \"signalDetectionHypotheses\""));
  }

  @Test
  public void testAssociateToLocationMissingRequestBody() throws IOException {

    given(this.request.getBody()).willReturn(null);

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  public void testAssociateToLocationMissingEventLocation() throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("signalDetectionHypothesisClaimChecks",
            TestFixtures.signalDetectionHypothesisClaimChecks)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertEquals("Json field \"eventLocation\" does not exist in the request body.",
        response.getErrorMessage().get());
  }

  @Test
  public void testAssociateToLocationMissingSignalDetectionHypothesisClaimChecks()
      throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventLocation", TestFixtures.eventLocation)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertEquals(
        "List field \"signalDetectionHypothesisClaimChecks\" does not exist in the request body.",
        response.getErrorMessage().get());
  }

  @Test
  public void testAssociateToLocationBadJson() throws IOException {

    given(this.request.getBody()).willReturn("asdf");

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get()
        .contains("Could not deserialize request body into JsonNode"));
  }

  @Test
  public void testAssociateToLocationBadMsgPack() throws IOException {

    // Mock that the request body returns MessagePack
    given(this.request.clientSentMsgpack()).willReturn(true);
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));
    given(this.request.getRawBody()).willReturn(this.msgPackObjectMapper.writeValueAsBytes("asdf"));

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get()
        .contains("Could not deserialize request body into JsonNode"));
  }

  @Test
  public void testAssociateToLocationBadEventLocation() throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventLocation", TestFixtures.unassociatedEvent),
        Map.entry("signalDetectionHypothesisClaimChecks", TestFixtures.signalDetectionHypotheses)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get().contains(
        "Could not deserialize contents of JSON field \"eventLocation\" into EventLocation object"));
  }

  @Test
  public void testAssociateToLocationBadSignalDetectionHypotheses() throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventLocation", TestFixtures.eventLocation),
        Map.entry("signalDetectionHypothesisClaimChecks", TestFixtures.unassociatedEvent)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesisClaimCheck> response = this.requestHandlers
        .associateToLocation(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get().contains(
        "Could not deserialize contents of JSON field \"signalDetectionHypothesisClaimChecks\" into list"));
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveMissingRequestBody() throws IOException {

    given(this.request.getBody()).willReturn(null);

    Response<Event> response = this.requestHandlers
        .associateToLocationInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveMissingEventHypothesis() throws IOException {

    Map<String, Object> params = Map.ofEntries(
        Map.entry("signalDetectionHypotheses", TestFixtures.signalDetectionHypothesisClaimChecks)
    );

    // Mock that the request body returns the correct contents
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesis> response = this.requestHandlers
        .associateToEventHypothesisInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertEquals("Json field \"eventHypothesis\" does not exist in the request body.",
        response.getErrorMessage().get());
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveMissingSignalDetectionHypotheses()
      throws IOException {

    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventHypothesis", TestFixtures.unassociatedEventHypothesis)
    );

    // Mock that the request body returns the correct contents
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesis> response = this.requestHandlers
        .associateToEventHypothesisInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertEquals(
        "List field \"signalDetectionHypotheses\" does not exist in the request body.",
        response.getErrorMessage().get());
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveBadJson() throws IOException {

    given(this.request.getBody()).willReturn("asdf");

    Response<EventHypothesis> response = this.requestHandlers
        .associateToEventHypothesisInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get()
        .contains("Could not deserialize request body into JsonNode"));
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveBadMsgPack() throws IOException {

    // Mock that the request body returns MessagePack
    given(this.request.clientSentMsgpack()).willReturn(true);
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));
    given(this.request.getRawBody()).willReturn(this.msgPackObjectMapper.writeValueAsBytes("asdf"));

    Response<EventHypothesis> response = this.requestHandlers
        .associateToEventHypothesisInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get()
        .contains("Could not deserialize request body into JsonNode"));
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveBadEventHypothesis() throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventHypothesis", TestFixtures.unassociatedEvent),
        Map.entry("signalDetectionHypotheses", TestFixtures.signalDetectionHypotheses)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesis> response = this.requestHandlers
        .associateToEventHypothesisInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
    Assertions.assertTrue(response.getErrorMessage().get().contains(
        "Could not deserialize contents of JSON field \"eventHypothesis\" into EventHypothesis object"));
  }

  @Test
  public void testAssociateToEventHypothesisInteractiveBadSignalDetectionHypotheses()
      throws IOException {
    Map<String, Object> params = Map.ofEntries(
        Map.entry("eventHypothesis", TestFixtures.unassociatedEventHypothesis),
        Map.entry("signalDetectionHypotheses", TestFixtures.unassociatedEvent)
    );

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));

    Response<EventHypothesis> response = this.requestHandlers
        .associateToEventHypothesisInteractive(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  public void testAssociateDetectionBadJson() throws IOException
  {
    given(this.request.getBody()).willReturn("badjson");
    // Mock that the request body returns JSON
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_JSON));

    Response<SignalDetectionAssociationResult> response =
        this.requestHandlers.associateDetections(this.request, this.jsonObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    // TODO: Need to change this to isEmpty when moved to JDK 11
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  public void testAssociateDetectionsBadMsgPack() throws IOException
  {
    given(this.request.clientSentMsgpack()).willReturn(true);
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));
    given(this.request.getRawBody()).willReturn(this.msgPackObjectMapper.writeValueAsBytes("asdf"));

    Response<SignalDetectionAssociationResult> response = this.requestHandlers
        .associateDetections(this.request, this.msgPackObjectMapper);

    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    // TODO: Need to change this to isEmpty when moved to JDK 11
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  public void testAssociateDetectionsBadRequestBody() throws IOException {
    given(this.request.getBody()).willReturn(null);
    Response<SignalDetectionAssociationResult> response = this.requestHandlers
        .associateDetections(this.request, this.jsonObjectMapper);
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  public void testAssociateDetectionsMissingSignalDetectionHypothesesThrowsBadRequest()
    throws IOException {
    ProcessingContext context = ProcessingContext.from(
        Optional.empty(),
        Optional.empty(),
        StorageVisibility.PUBLIC
    );

    Map<String, Object> params = Map.of("processingContext", this.jsonObjectMapper.writeValueAsString(context));
    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));
    Response<SignalDetectionAssociationResult> response = this.requestHandlers
        .associateDetections(this.request, this.jsonObjectMapper);
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  public void testAssociateDetectionsMissingOptionalParametersDoesNotThrowError()
    throws IOException {
    Map<String, Object> params = Map.of(
        "signalDetectionHypotheses", TestFixtures.signalDetectionHypothesesDescriptors
    );

    try {
      given(this.associationControl.associate(TestFixtures.signalDetectionHypothesesDescriptors))
          .willReturn(SignalDetectionAssociationResult.from(Set.of(), Set.of()));
    } catch(Exception e) {

    }
    
    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));
    Response<SignalDetectionAssociationResult> response = this.requestHandlers
        .associateDetections(this.request, this.jsonObjectMapper);
    Assertions.assertEquals(Code.OK, response.getHttpStatus());
  }

  @Test
  public void testAssociateDetectionsBadParametersObjectThrowsError()
    throws IOException {
    Map<String, Object> params = Map.of(
        "signalDetectionHypotheses", TestFixtures.signalDetectionHypothesesDescriptors,
        "parameters", "badparams"
    );

    try {
      given(this.associationControl.associate(TestFixtures.signalDetectionHypothesesDescriptors))
          .willReturn(SignalDetectionAssociationResult.from(Set.of(), Set.of()));
    } catch (Exception e) {

    }

    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));
    Response<SignalDetectionAssociationResult> response = this.requestHandlers
        .associateDetections(this.request, this.jsonObjectMapper);
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertTrue(response.getErrorMessage().isPresent());
    Assertions.assertTrue(!response.getBody().isPresent());
  }

  @Test
  void testAssociateDetectionsUserParametersAndSignalDetectionHypothesisGivenValidResults() throws IOException {
    Map<String, Object> params = Map.of(
        "signalDetectionHypotheses", TestFixtures.signalDetectionHypothesesDescriptors,
        "parameters", TestFixtures.signalDetectionAssociationParameters
    );
    try {
      given(this.associationControl.associate(TestFixtures.signalDetectionHypothesesDescriptors,
          TestFixtures.signalDetectionAssociationParameters))
          .willReturn(SignalDetectionAssociationResult.from(Set.of(), Set.of()));
    } catch (Exception e) {

    }
    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString(params));
    Response<SignalDetectionAssociationResult> response = this.requestHandlers.associateDetections(this.request,
        this.jsonObjectMapper);
    Assertions.assertEquals(Code.OK, response.getHttpStatus());
    Assertions.assertTrue(response.getBody().isPresent());
  }

}
