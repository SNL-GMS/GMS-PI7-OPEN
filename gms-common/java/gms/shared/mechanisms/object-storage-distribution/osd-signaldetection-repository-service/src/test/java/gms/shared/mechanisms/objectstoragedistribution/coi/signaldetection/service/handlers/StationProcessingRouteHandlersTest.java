package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;
import spark.Response;

@RunWith(MockitoJUnitRunner.class)
public class StationProcessingRouteHandlersTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();


  @Mock
  private ProcessingStationReferenceFactory factory;

  @Mock
  private Request request;

  @Mock
  private Response response;

  private StationProcessingRouteHandlers routeHandlers;

  @Before
  public void setUp() {
    this.routeHandlers = StationProcessingRouteHandlers.create(factory);
  }

  @After
  public void tearDown() {
    this.routeHandlers = null;
  }

  @Test
  public void testCreateNullFactoryThrowsNullPointerException() {
    exception.expect(NullPointerException.class);
    StationProcessingRouteHandlers.create(null);
  }

  @Test
  public void testGetNetworkNullValuesThrowsNullPointerExceptions() throws IllegalAccessException {
    Instant testTime = Instant.ofEpochMilli(0);
    Network testNetwork = Network
        .create("TEST", NetworkOrganization.UNKNOWN, NetworkRegion.UNKNOWN, Set.of());

    given(request.headers("Accept")).willReturn("application/json");
    given(request.queryParams("name")).willReturn("TEST");
    given(request.queryParams("time")).willReturn(testTime.toString());
    given(factory.networkFromName(any(), any(), any())).willReturn(Optional.of(testNetwork));

    TestUtilities.checkMethodValidatesNullArguments(routeHandlers, "getNetwork",
        request, response);
  }

  @Test
  public void testGetNetworkNoNameThrowsIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Error retrieving network, must provide name query param.");
    given(request.queryParams("name")).willReturn(null);
    routeHandlers.getNetwork(request, response);
  }

  @Test
  public void testGetNetworkNoTimeThrowsIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Error retrieving network, must provide time query param.");
    given(request.queryParams("name")).willReturn("TEST");
    given(request.queryParams("time")).willReturn(null);
    routeHandlers.getNetwork(request, response);
  }

  @Test
  public void testGetNetworkMalformedTimeThrowsError() {
    exception.expect(DateTimeParseException.class);
    given(request.queryParams("name")).willReturn("TEST");
    given(request.queryParams("time")).willReturn("BADTIME");
    routeHandlers.getNetwork(request, response);
  }

  @Test
  public void testGetNetworkBadAcceptReturnsError406() {
    Instant testTime = Instant.ofEpochMilli(0);

    given(request.queryParams("name")).willReturn("TEST");
    given(request.queryParams("time")).willReturn(testTime.toString());
    String result = routeHandlers.getNetwork(request, response);

    assertThat(result, CoreMatchers.containsString("406"));
    verify(response, times(1)).status(406);
  }

  @Test
  public void testGetNetwork() {
    Instant testTime = Instant.ofEpochMilli(0);
    Network testNetwork = Network
        .create("TEST", NetworkOrganization.UNKNOWN, NetworkRegion.UNKNOWN, Set.of());

    given(request.headers("Accept")).willReturn("application/json");
    given(request.queryParams("name")).willReturn("TEST");
    given(request.queryParams("time")).willReturn(testTime.toString());
    given(factory.networkFromName(any(), any(), any())).willReturn(Optional.of(testNetwork));

    String result = routeHandlers.getNetwork(request, response);
    assertEquals(ObjectSerialization.writeValue(testNetwork), result);

    verify(factory, times(1))
        .networkFromName("TEST", testTime, testTime);
  }
}
