package gms.core.signalenhancement.fk.service.fkspectra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import gms.core.signalenhancement.fk.FkTestUtility;
import gms.core.signalenhancement.fk.control.FkControl;
import gms.core.signalenhancement.fk.control.FkSpectraCommand;
import gms.core.signalenhancement.fk.service.ContentType;
import gms.core.signalenhancement.fk.service.ObjectSerialization;
import gms.core.signalenhancement.fk.service.StandardResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.ProcessingResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FkRouteHandlerTests {

  private FkRouteHandler routeHandler;

  @Mock
  private FkControl mockFkControl;

  @BeforeEach
  public void setUp() {
    routeHandler = FkRouteHandler.create(mockFkControl);
  }

  @Test
  public void testCreateValidation() {
    assertThrows(NullPointerException.class, () -> FkRouteHandler.create(null));
    assertNotNull(assertDoesNotThrow(() -> FkRouteHandler.create(mockFkControl)));
  }

  @Test
  public void testSpectraRequestContentTypeJson() {
    final StandardResponse response = routeHandler
        .interactiveSpectra(ContentType.APPLICATION_JSON,
            ObjectSerialization.writeJson(FkTestUtility.defaultSpectraCommand()),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testSpectraRequestContentTypeMessagePack() {
    final StandardResponse response = routeHandler
        .interactiveSpectra(ContentType.APPLICATION_MSGPACK,
            ObjectSerialization.writeMessagePack(FkTestUtility.defaultSpectraCommand()),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testSpectraRequestContentTypeAny() {
    final StandardResponse response = routeHandler
        .interactiveSpectra(ContentType.APPLICATION_ANY,
            ObjectSerialization.writeMessagePack(FkTestUtility.defaultSpectraCommand()),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, response.getHttpStatus());
  }

  @Test
  public void testSpectraAcceptTypeJson() {
    final StandardResponse response = routeHandler
        .interactiveSpectra(ContentType.APPLICATION_JSON,
            ObjectSerialization.writeJson(FkTestUtility.defaultSpectraCommand()),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testSpectraAcceptTypeMessagePack() {
    final StandardResponse response = routeHandler
        .interactiveSpectra(ContentType.APPLICATION_JSON,
            ObjectSerialization.writeJson(FkTestUtility.defaultSpectraCommand()),
            ContentType.APPLICATION_MSGPACK);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testSpectraAcceptTypeAny() {
    final StandardResponse response = routeHandler
        .interactiveSpectra(ContentType.APPLICATION_JSON,
            ObjectSerialization.writeJson(FkTestUtility.defaultSpectraCommand()),
            ContentType.APPLICATION_ANY);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testSpectraRequestContentTypeUnsupportedExpect415() {
    FkTestUtility.getContentTypeStream(FkTestUtility::invalidSpectraRequestTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, routeHandler
                .interactiveSpectra(c, new byte[0], ContentType.APPLICATION_JSON).getHttpStatus())
        );
  }

  @Test
  public void testSpectraAcceptTypeUnsupportedExpect406() {
    FkTestUtility.getContentTypeStream(
        FkTestUtility::invalidSpectraResponseTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.NOT_ACCEPTABLE_406, routeHandler
                .interactiveSpectra(ContentType.APPLICATION_JSON, new byte[0], c).getHttpStatus())
        );
  }

  @Test
  public void testSpectraResponseSerialization() {
    FkSpectraCommand command = FkTestUtility.defaultSpectraCommand();
    byte[] jsonCommand = ObjectSerialization.writeJson(command);

    List<ChannelSegment<FkSpectra>> expected = List.of(FkTestUtility.defaultSpectraSegment(
        UUID.randomUUID(), Instant.EPOCH, 1.0));
    byte[] expectedJson = ObjectSerialization.writeJson(expected);
    byte[] expectedMsgPack = ObjectSerialization.writeMessagePack(expected);

    given(mockFkControl.generateFkSpectra(command)).willReturn(expected);

    StandardResponse jsonResponse = routeHandler.interactiveSpectra(ContentType.APPLICATION_JSON,
        jsonCommand, ContentType.APPLICATION_JSON);

    assertAll("Spectra Response (Accept: application/json",
        () -> assertNotNull(jsonResponse),
        () -> assertEquals(HttpStatus.OK_200, jsonResponse.getHttpStatus()),
        () -> assertArrayEquals(expectedJson, (byte[]) jsonResponse.getResponseBody()));

    StandardResponse msgPackResponse = routeHandler
        .interactiveSpectra(ContentType.APPLICATION_JSON, jsonCommand, ContentType.APPLICATION_MSGPACK);

    assertAll("Spectra Response (Accept: application/msg-pack",
        () -> assertNotNull(msgPackResponse),
        () -> assertEquals(HttpStatus.OK_200, msgPackResponse.getHttpStatus()),
        () -> assertArrayEquals(expectedMsgPack, (byte[]) msgPackResponse.getResponseBody()));

    StandardResponse anyResponse = routeHandler.interactiveSpectra(ContentType.APPLICATION_JSON, jsonCommand,
            ContentType.APPLICATION_ANY);

    assertAll("Spectra Response (Accept: any)",
        () -> assertNotNull(anyResponse),
        () -> assertEquals(HttpStatus.OK_200, anyResponse.getHttpStatus()),
        () -> assertArrayEquals(expectedJson, (byte[]) anyResponse.getResponseBody()));
  }

  @Test
  public void testSpectraValidation() {
    FkSpectraCommand command = FkTestUtility.defaultSpectraCommand();
    given(mockFkControl.generateFkSpectra(any(FkSpectraCommand.class))).willReturn(List.of());
    byte[] serializedCommand = ObjectSerialization.writeJson(command);

    assertAll("Spectra Validation",
        () -> assertThrows(NullPointerException.class,
            () -> routeHandler.interactiveSpectra(null, serializedCommand, ContentType.APPLICATION_JSON)),
        () -> assertThrows(NullPointerException.class,
            () -> routeHandler
                .interactiveSpectra(ContentType.APPLICATION_JSON, null, ContentType.APPLICATION_JSON)),
        () -> assertThrows(NullPointerException.class,
            () -> routeHandler.interactiveSpectra(ContentType.APPLICATION_JSON, serializedCommand, null)),
        () -> assertDoesNotThrow(
            () -> routeHandler.interactiveSpectra(ContentType.APPLICATION_JSON, serializedCommand,
                ContentType.APPLICATION_JSON)));
  }

  @Test
  public void testSpectraInvokesExecute() {
    FkSpectraCommand command = FkTestUtility.defaultSpectraCommand();
    final byte[] requestBody = ObjectSerialization.writeMessagePack(command);
    given(mockFkControl.generateFkSpectra(any(FkSpectraCommand.class))).willReturn(List.of());

    routeHandler
        .interactiveSpectra(ContentType.APPLICATION_MSGPACK, requestBody, ContentType.APPLICATION_JSON);

    then(mockFkControl).should().generateFkSpectra(command);
  }

  @Test
  public void testFeatureMeasurementsRequestTypeUnsupportedExpect415() {
    FkTestUtility.getContentTypeStream(
        FkTestUtility::invalidAnalysisRequestTypeFilter)
        .forEach(c -> assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, routeHandler
            .featureMeasurements(c, new byte[0], ContentType.APPLICATION_JSON).getHttpStatus()));
  }

  @Test
  public void testFeatureMeasurementsResponseTypeUnsupportedExpect406() {
    FkTestUtility.getContentTypeStream(
        FkTestUtility::invalidAnalysisResponseTypeFilter)
        .forEach(c -> assertEquals(HttpStatus.NOT_ACCEPTABLE_406, routeHandler
            .featureMeasurements(ContentType.APPLICATION_JSON, new byte[0], c).getHttpStatus()));
  }

  @Test
  public void testMeasureFkFeaturesValidation() {
    List<SignalDetectionHypothesisDescriptor> expected = FkTestUtility.defaultInputDescriptors();
    ProcessingResponse processingResponse = FkTestUtility.defaultProcessingResponse();
    byte[] serializedInterval = ObjectSerialization.writeJson(expected);

    given(mockFkControl.measureFkFeatures(any()))
        .willReturn(processingResponse);

    assertAll("Analysis null check",
        () -> assertThrows(NullPointerException.class,
            () -> routeHandler.featureMeasurements(null, serializedInterval, ContentType.APPLICATION_JSON)),
        () -> assertThrows(NullPointerException.class,
            () -> routeHandler
                .featureMeasurements(ContentType.APPLICATION_JSON, null, ContentType.APPLICATION_MSGPACK)),
        () -> assertThrows(NullPointerException.class,
            () -> routeHandler.featureMeasurements(ContentType.APPLICATION_JSON, serializedInterval, null)),
        () -> assertDoesNotThrow(() -> routeHandler
            .featureMeasurements(ContentType.APPLICATION_JSON, serializedInterval,
                ContentType.APPLICATION_MSGPACK))
    );
  }

  @Test
  public void testMeasureFkFeaturesResponseSerialization() {
    List<SignalDetectionHypothesisDescriptor> inputDescriptors = FkTestUtility
        .defaultInputDescriptors();
    ProcessingResponse expected = FkTestUtility.defaultProcessingResponse();
    byte[] intervalJson = ObjectSerialization.writeJson(inputDescriptors);

    byte[] expectedJson = ObjectSerialization.writeJson(expected);
    byte[] expectedMsgPack = ObjectSerialization.writeMessagePack(expected);

    given(mockFkControl.measureFkFeatures(inputDescriptors)).willReturn(expected);

    StandardResponse jsonResponse = routeHandler
        .featureMeasurements(ContentType.APPLICATION_JSON, intervalJson, ContentType.APPLICATION_JSON);

    assertAll("Analysis Serialization (application/json)",
        () -> assertNotNull(jsonResponse),
        () -> assertEquals(HttpStatus.OK_200, jsonResponse.getHttpStatus()),
        () -> assertArrayEquals(expectedJson, (byte[]) jsonResponse.getResponseBody()));

    StandardResponse msgPackResponse = routeHandler
        .featureMeasurements(ContentType.APPLICATION_JSON, intervalJson, ContentType.APPLICATION_MSGPACK);

    assertAll("Analysis Serialization (application/msgpack)",
        () -> assertNotNull(msgPackResponse),
        () -> assertEquals(HttpStatus.OK_200, msgPackResponse.getHttpStatus()),
        () -> assertArrayEquals(expectedMsgPack, (byte[]) msgPackResponse.getResponseBody()));

    StandardResponse anyResponse = routeHandler
        .featureMeasurements(ContentType.APPLICATION_JSON, intervalJson, ContentType.APPLICATION_ANY);

    assertAll("Analysis Serialization (any)",
        () -> assertNotNull(anyResponse),
        () -> assertEquals(HttpStatus.OK_200, anyResponse.getHttpStatus()),
        () -> assertArrayEquals(expectedJson, (byte[]) anyResponse.getResponseBody()));
  }

  @Test
  public void testMeasureFkFeaturesInvokesExecute() {
    List<SignalDetectionHypothesisDescriptor> inputDes = FkTestUtility.defaultInputDescriptors();
    ProcessingResponse processingResponse = FkTestUtility.defaultProcessingResponse();
    final byte[] requestBody = ObjectSerialization.writeJson(inputDes);

    given(mockFkControl.measureFkFeatures(any()))
        .willReturn(processingResponse);
    routeHandler.featureMeasurements(ContentType.APPLICATION_JSON, requestBody, ContentType.APPLICATION_JSON);
    then(mockFkControl).should().measureFkFeatures(inputDes);
  }
}
