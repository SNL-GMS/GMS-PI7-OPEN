package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.reset;

import com.fasterxml.jackson.databind.JavaType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ChannelProcessingGroupRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ObjectSerialization;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;
import spark.Response;

@RunWith(MockitoJUnitRunner.class)
public class ChannelProcessingGroupRouteHandlerTests {

  private static ArgumentCaptor<ChannelProcessingGroup> sigDetArgumentCaptor =
      ArgumentCaptor.forClass(ChannelProcessingGroup.class);
  private static ChannelProcessingGroupRepository repository
      = Mockito.mock(ChannelProcessingGroupRepository.class);

  private static ChannelProcessingGroupRouteHandlers handlers;

  private static JavaType mapUuidToSigDetsJavaType;
  private static JavaType sigDetsListType;

  static {
    JavaType uuidType = TestFixtures.objectMapper.constructType(UUID.class);
    sigDetsListType = TestFixtures.objectMapper.getTypeFactory().constructCollectionType(
        List.class, ChannelProcessingGroup.class);
    mapUuidToSigDetsJavaType = TestFixtures.objectMapper.getTypeFactory().constructMapType(
        HashMap.class, uuidType, sigDetsListType);
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private Request request;

  @Mock
  private Response response;

  @BeforeClass
  public static void setupHandlers() {
    handlers = ChannelProcessingGroupRouteHandlers.create(repository);
  }

  @Before
  public void setUp() throws Exception {
    reset(repository);
  }

  @Test
  public void testCreateNullParameterThrowsNullPointerException() {
    exception.expect(NullPointerException.class);
    ChannelProcessingGroupRouteHandlers.create(null);
  }

  @Test
  public void testGetChannelProcessingGroupValidatesNullValues() throws IllegalAccessException {
    TestUtilities.checkMethodValidatesNullArguments(handlers, "getChannelProcessingGroup",
        request, response);
  }

  @Test
  public void testGetChannelProcessingGroupBadAcceptReturnsError406() throws Exception{
    given(request.params(":id"))
        .willReturn(UUID.randomUUID().toString());

    String responseBody = handlers.getChannelProcessingGroup(request, response);
    assertThat(responseBody, containsString("406"));

    verify(response, times(1)).status(406);
  }

  @Test
  public void testGetChannelProcessingGroupNoIdParameterReturnsAll() throws Exception {
    UUID testId = UUID.randomUUID();
    List<ChannelProcessingGroup> expected = List.of(
        ChannelProcessingGroup.from(
            UUID.randomUUID(),
            ChannelProcessingGroupType.BEAM,
            Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
            Instant.EPOCH,
            Instant.EPOCH,
            "good",
            "status is good"
        )
    );

    given(request.headers("Accept")).willReturn("application/json");
    given(request.params(":id"))
        .willReturn(null);
    given(repository.retrieveAll()).willReturn(expected);

    String actual = handlers.getChannelProcessingGroup(request, response);

    assertThat(actual, is(both(notNullValue()).and(equalTo(ObjectSerialization.writeValue(expected)))));

    verify(repository, times(1)).retrieveAll();
  }

  @Test
  public void testGetSignalDetectionWithIdNotFoundReturnsNullString() throws Exception {
    given(request.headers("Accept")).willReturn("application/json");
    given(request.params(":id"))
        .willReturn(UUID.randomUUID().toString());
    given(repository.retrieve(any()))
        .willReturn(Optional.empty());

    String actual = handlers.getChannelProcessingGroup(request, response);

    //Jackson serialization converts an empty optional to "null"
    assertThat(actual, is(both(notNullValue()).and(equalTo("null"))));
  }

  @Test
  public void testGetSignalDetectionWithIdReturnsSingleSignalDetection() throws Exception {
    UUID testId = UUID.randomUUID();
    ChannelProcessingGroup expected = ChannelProcessingGroup.from(
        testId,
        ChannelProcessingGroupType.BEAM,
        Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
        Instant.EPOCH,
        Instant.EPOCH,
        "good",
        "status is good"
    );

    given(repository.retrieve(testId))
        .willReturn(Optional.of(expected));

    given(request.headers("Accept")).willReturn("application/json");

    given(request.params(":id"))
        .willReturn(testId.toString());

    String actual = handlers.getChannelProcessingGroup(request, response);

    assertThat(actual,
        is(both(notNullValue()).and(equalTo(ObjectSerialization.writeValue(expected)))));

    verify(repository, times(1)).retrieve(testId);
    verify(repository, never()).retrieveAll();
  }

  @Test
  public void testStoreSignalDetectionsFromArray() throws Exception {
    List<ChannelProcessingGroup> expected = List.of(
        ChannelProcessingGroup.from(
            UUID.randomUUID(),
            ChannelProcessingGroupType.BEAM,
            Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
            Instant.EPOCH,
            Instant.EPOCH,
            "good",
            "status is good"
        ),
        ChannelProcessingGroup.from(
            UUID.randomUUID(),
            ChannelProcessingGroupType.BEAM,
            Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
            Instant.EPOCH,
            Instant.EPOCH,
            "bad",
            "status is bad"
        )
    );
    doNothing().when(repository).createChannelProcessingGroup(sigDetArgumentCaptor.capture());
    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(
        expected));
    when(response.status()).thenReturn(HttpStatus.OK_200);
    String serviceResponse = handlers.storeChannelProcessingGroups(
        request, response);
    assertEquals("", serviceResponse);
    assertEquals(expected, sigDetArgumentCaptor.getAllValues());
    assertEquals(HttpStatus.OK_200, response.status());
  }

  @Test
  public void testNullParametersForSignalDetectionArrayStore() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot store null channel processing groups");
    given(request.body()).willReturn(null);
    handlers.storeChannelProcessingGroups(
        request, response);
  }

}
