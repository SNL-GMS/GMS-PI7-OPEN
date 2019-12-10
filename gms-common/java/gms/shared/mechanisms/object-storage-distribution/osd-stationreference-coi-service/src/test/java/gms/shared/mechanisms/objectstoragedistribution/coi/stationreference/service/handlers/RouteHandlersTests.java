package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.handlers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ProcessingStationReferenceFactoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.StationReferenceCoiService;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Endpoints;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities.UnirestTestUtilities;
import java.net.ServerSocket;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import junit.framework.TestCase;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class RouteHandlersTests {


  private static String BASE_URL;
  private static String SHORT_BASE_URL;

  private final String NETWORKS_URL = BASE_URL + Endpoints.NETWORKS;
  private final String STATIONS_URL = BASE_URL + Endpoints.STATIONS;
  private final String SITES_URL = BASE_URL + Endpoints.SITES;
  private final String CHANNELS_URL = BASE_URL + Endpoints.CHANNELS;
  private final String DIGITIZERS_URL = BASE_URL + Endpoints.DIGITIZERS;
  private final String SENSORS_URL = BASE_URL + Endpoints.SENSORS;
  private final String RESPONSES_URL = BASE_URL + Endpoints.RESPONSES;
  private final String CALIBRATIONS_URL = BASE_URL + Endpoints.CALIBRATIONS;
  private final String NETWORK_MEMBERSHIPS_URL = BASE_URL + Endpoints.NETWORK_MEMBERSHIPS;
  private final String STATION_MEMBERSHIPS_URL = BASE_URL + Endpoints.STATION_MEMBERSHIPS;
  private final String SITE_MEMBERSHIPS_URL = BASE_URL + Endpoints.SITE_MEMBERSHIPS;
  private final String DIGITIZER_MEMBERSHIPS_URL = BASE_URL + Endpoints.DIGITIZER_MEMBERSHIPS;
  private final String PROCESSING_NETWORKS_URL = BASE_URL + Endpoints.PROCESSING_NETWORKS;
  private final String PROCESSING_STATIONS_URL = BASE_URL + Endpoints.PROCESSING_STATIONS;
  private final String PROCESSING_SITES_URL = BASE_URL + Endpoints.PROCESSING_SITES;

  //STORE endpoints use the short base url
  private final String STORE_NETWORKS_URL = SHORT_BASE_URL + Endpoints.STORE_NETWORKS;
  private final String STORE_STATIONS_URL = SHORT_BASE_URL + Endpoints.STORE_STATIONS;
  private final String STORE_SITES_URL = SHORT_BASE_URL + Endpoints.STORE_SITES;
  private final String STORE_CHANNELS_URL = SHORT_BASE_URL + Endpoints.STORE_CHANNELS;
  private final String STORE_CALIBRATIONS_URL = SHORT_BASE_URL + Endpoints.STORE_CALIBRATIONS;
  private final String STORE_RESPONSES_URL = SHORT_BASE_URL + Endpoints.STORE_RESPONSES;
  private final String STORE_SENSORS_URL = SHORT_BASE_URL + Endpoints.STORE_SENSORS;

  private final String STORE_NETWORK_MEMBERSHIPS_URL =
      SHORT_BASE_URL + Endpoints.STORE_NETWORK_MEMBERSHIPS;
  private final String STORE_STATION_MEMBERSHIPS_URL =
      SHORT_BASE_URL + Endpoints.STORE_STATION_MEMBERSHIPS;
  private final String STORE_SITE_MEMBERSHIPS_URL =
      SHORT_BASE_URL + Endpoints.STORE_SITE_MEMBERSHIPS;

  // New HTTP conventions require use of /coi/ prefix for both query and store ops
  private final String STATIONS_BY_VERSION_IDS_URL =
      SHORT_BASE_URL + Endpoints.STATIONS_BY_VERSION_IDS;
  private final String CHANNELS_BY_VERSION_IDS_URL =
      SHORT_BASE_URL + Endpoints.CHANNELS_BY_VERSION_IDS;

  private final UUID UNKNOWN_UUID = UUID.fromString("611e4cf1-4a3d-40a9-8e88-60a85cf76d67");
  private final String UNKNOWN_NAME = "unknownName";

  private static final StationReferenceRepositoryInterface stationRefRepo
      = mock(StationReferenceRepositoryInterface.class);

  private static final ProcessingStationReferenceFactoryInterface stationRefFactory
      = mock(ProcessingStationReferenceFactoryInterface.class);

  private static ArgumentCaptor<ReferenceNetwork> referenceNetworkArgumentCaptor = ArgumentCaptor
      .forClass(ReferenceNetwork.class);
  private static ArgumentCaptor<ReferenceStation> referenceStationArgumentCaptor = ArgumentCaptor
      .forClass(ReferenceStation.class);
  private static ArgumentCaptor<ReferenceSite> referenceSiteArgumentCaptor = ArgumentCaptor
      .forClass(ReferenceSite.class);
  private static ArgumentCaptor<ReferenceChannel> referenceChannelArgumentCaptor = ArgumentCaptor
      .forClass(ReferenceChannel.class);
  private static ArgumentCaptor<ReferenceCalibration> referenceCalibrationArgumentCaptor = ArgumentCaptor
      .forClass(ReferenceCalibration.class);
  private static ArgumentCaptor<ReferenceResponse> referenceResponseArgumentCaptor = ArgumentCaptor
      .forClass(ReferenceResponse.class);
  private static ArgumentCaptor<ReferenceSensor> referenceSensorArgumentCaptor = ArgumentCaptor
      .forClass(ReferenceSensor.class);

  private static ArgumentCaptor<Collection<ReferenceNetworkMembership>> referenceNetworkMembershipArgumentCaptor = ArgumentCaptor
      .forClass(Collection.class);
  private static ArgumentCaptor<Collection<ReferenceStationMembership>> referenceStationMembershipArgumentCaptor = ArgumentCaptor
      .forClass(Collection.class);
  private static ArgumentCaptor<Collection<ReferenceSiteMembership>> referenceSiteMembershipArgumentCaptor = ArgumentCaptor
      .forClass(Collection.class);

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Before
  public void setUp() throws Exception {
    reset(stationRefRepo);
    //argument captor behavior for storage tests
    doNothing().when(stationRefRepo)
        .storeReferenceNetwork(referenceNetworkArgumentCaptor.capture());
    doNothing().when(stationRefRepo)
        .storeReferenceStation(referenceStationArgumentCaptor.capture());
    doNothing().when(stationRefRepo)
        .storeReferenceSite(referenceSiteArgumentCaptor.capture());
    doNothing().when(stationRefRepo)
        .storeReferenceStation(referenceStationArgumentCaptor.capture());
    doNothing().when(stationRefRepo).storeReferenceSite(referenceSiteArgumentCaptor.capture());
    doNothing().when(stationRefRepo)
        .storeReferenceChannel(referenceChannelArgumentCaptor.capture());
    doNothing().when(stationRefRepo).storeCalibration(referenceCalibrationArgumentCaptor.capture());
    doNothing().when(stationRefRepo).storeResponse(referenceResponseArgumentCaptor.capture());
    doNothing().when(stationRefRepo).storeSensor(referenceSensorArgumentCaptor.capture());
    doNothing().when(stationRefRepo)
        .storeNetworkMemberships(referenceNetworkMembershipArgumentCaptor.capture());
    doNothing().when(stationRefRepo)
        .storeStationMemberships(referenceStationMembershipArgumentCaptor.capture());
    doNothing().when(stationRefRepo)
        .storeSiteMemberships(referenceSiteMembershipArgumentCaptor.capture());

    // networks
    when(stationRefRepo.retrieveNetworksByEntityId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveNetworksByEntityId(TestFixtures.network.getEntityId()))
        .thenReturn(List.of(TestFixtures.network));
    when(stationRefRepo.retrieveNetworksByEntityId(TestFixtures.network2.getEntityId()))
        .thenReturn(List.of(TestFixtures.network2));
    when(stationRefRepo.retrieveNetworks())
        .thenReturn(TestFixtures.allNetworks);
    // name queries
    when(stationRefRepo.retrieveNetworksByName(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveNetworksByName(TestFixtures.network.getName()))
        .thenReturn(List.of(TestFixtures.network));
    when(stationRefRepo.retrieveNetworksByName(TestFixtures.network2.getName()))
        .thenReturn(List.of(TestFixtures.network2));
    // stations
    when(stationRefRepo.retrieveStationsByEntityId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveStationsByEntityId(TestFixtures.station.getEntityId()))
        .thenReturn(List.of(TestFixtures.station));
    when(stationRefRepo.retrieveStationsByEntityId(TestFixtures.station2.getEntityId()))
        .thenReturn(List.of(TestFixtures.station2));
    when(stationRefRepo.retrieveStationsByVersionIds(List.of(TestFixtures.station.getVersionId())))
        .thenReturn(List.of(TestFixtures.station));
    when(stationRefRepo.retrieveStationsByVersionIds(
        List.of(TestFixtures.station.getVersionId(), TestFixtures.station2.getVersionId())))
        .thenReturn(List.of(TestFixtures.station, TestFixtures.station2));
    when(stationRefRepo.retrieveStationsByName(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveStationsByName(TestFixtures.station.getName()))
        .thenReturn(List.of(TestFixtures.station));
    when(stationRefRepo.retrieveStationsByName(TestFixtures.station2.getName()))
        .thenReturn(List.of(TestFixtures.station2));
    when(stationRefRepo.retrieveStations())
        .thenReturn(TestFixtures.allStations);
    // sites
    when(stationRefRepo.retrieveSitesByEntityId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveSitesByEntityId(TestFixtures.site.getEntityId()))
        .thenReturn(List.of(TestFixtures.site));
    when(stationRefRepo.retrieveSitesByEntityId(TestFixtures.site2.getEntityId()))
        .thenReturn(List.of(TestFixtures.site2));
    when(stationRefRepo.retrieveSitesByName(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveSitesByName(TestFixtures.site.getName()))
        .thenReturn(List.of(TestFixtures.site));
    when(stationRefRepo.retrieveSitesByName(TestFixtures.site2.getName()))
        .thenReturn(List.of(TestFixtures.site2));
    when(stationRefRepo.retrieveSites())
        .thenReturn(TestFixtures.allSites);
    // channels
    when(stationRefRepo.retrieveChannelsByEntityId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveChannelsByEntityId(TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.channel));
    when(stationRefRepo.retrieveChannelsByEntityId(TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.channel2));
    when(stationRefRepo.retrieveChannelsByVersionIds(List.of(TestFixtures.channel.getVersionId())))
        .thenReturn(List.of(TestFixtures.channel));
    when(stationRefRepo.retrieveChannelsByVersionIds(
        List.of(TestFixtures.channel.getVersionId(), TestFixtures.channel2.getVersionId())))
        .thenReturn(List.of(TestFixtures.channel, TestFixtures.channel2));
    when(stationRefRepo.retrieveChannelsByName(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveChannelsByName(TestFixtures.channel.getName()))
        .thenReturn(List.of(TestFixtures.channel));
    when(stationRefRepo.retrieveChannelsByName(TestFixtures.channel2.getName()))
        .thenReturn(List.of(TestFixtures.channel2));
    when(stationRefRepo.retrieveChannels())
        .thenReturn(TestFixtures.allChannels);
    // calibrations
    when(stationRefRepo.retrieveCalibrations())
        .thenReturn(TestFixtures.allCalibrations);
    when(stationRefRepo.retrieveCalibrationsByChannelId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveCalibrationsByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(TestFixtures.chan1_calibrations);
    when(stationRefRepo.retrieveCalibrationsByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(TestFixtures.chan2_calibrations);
    // responses
    when(stationRefRepo.retrieveResponses())
        .thenReturn(TestFixtures.allResponses);
    when(stationRefRepo.retrieveResponsesByChannelId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveResponsesByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(TestFixtures.chan1_responses);
    when(stationRefRepo.retrieveResponsesByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(TestFixtures.chan2_responses);
    // sensors
    when(stationRefRepo.retrieveSensors())
        .thenReturn(TestFixtures.allSensors);
    when(stationRefRepo.retrieveSensorsByChannelId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveSensorsByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(TestFixtures.chan1_sensors);
    when(stationRefRepo.retrieveSensorsByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(TestFixtures.chan2_sensors);
    // digitizers
    when(stationRefRepo.retrieveDigitizersByEntityId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveDigitizersByEntityId(TestFixtures.digitizer.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizer));
    when(stationRefRepo.retrieveDigitizersByEntityId(TestFixtures.digitizer2.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizer2));
    when(stationRefRepo.retrieveDigitizersByName(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveDigitizersByName(TestFixtures.digitizer.getName()))
        .thenReturn(List.of(TestFixtures.digitizer));
    when(stationRefRepo.retrieveDigitizersByName(TestFixtures.digitizer2.getName()))
        .thenReturn(List.of(TestFixtures.digitizer2));
    when(stationRefRepo.retrieveDigitizers())
        .thenReturn(TestFixtures.allDigitizers);
    // network memberships
    when(stationRefRepo.retrieveNetworkMembershipsByNetworkId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveNetworkMembershipsByNetworkId(TestFixtures.network.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember));
    when(stationRefRepo.retrieveNetworkMembershipsByNetworkId(TestFixtures.network2.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember2));
    when(stationRefRepo.retrieveNetworkMembershipsByStationId(TestFixtures.station.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember));
    when(stationRefRepo.retrieveNetworkMembershipsByStationId(TestFixtures.station2.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember2));
    when(stationRefRepo.retrieveNetworkMembershipsByNetworkAndStationId(
        TestFixtures.network.getEntityId(), TestFixtures.station.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember));
    when(stationRefRepo.retrieveNetworkMembershipsByNetworkAndStationId(
        TestFixtures.network2.getEntityId(), TestFixtures.station2.getEntityId()))
        .thenReturn(List.of(TestFixtures.netMember2));
    when(stationRefRepo.retrieveNetworkMemberships())
        .thenReturn(TestFixtures.allNetworkMemberships);
    // station memberships
    when(stationRefRepo.retrieveStationMembershipsByStationId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveStationMembershipsByStationId(TestFixtures.station.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember));
    when(stationRefRepo.retrieveStationMembershipsByStationId(TestFixtures.station2.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember2));
    when(stationRefRepo.retrieveStationMembershipsBySiteId(TestFixtures.site.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember));
    when(stationRefRepo.retrieveStationMembershipsBySiteId(TestFixtures.site2.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember2));
    when(stationRefRepo.retrieveStationMembershipsByStationAndSiteId(
        TestFixtures.station.getEntityId(), TestFixtures.site.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember));
    when(stationRefRepo.retrieveStationMembershipsByStationAndSiteId(
        TestFixtures.station2.getEntityId(), TestFixtures.site2.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMember2));
    when(stationRefRepo.retrieveStationMemberships())
        .thenReturn(TestFixtures.allStationMemberships);
    // site memberships
    when(stationRefRepo.retrieveSiteMembershipsBySiteId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveSiteMembershipsBySiteId(TestFixtures.site.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember));
    when(stationRefRepo.retrieveSiteMembershipsBySiteId(TestFixtures.site2.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember2));
    when(stationRefRepo.retrieveSiteMembershipsByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember));
    when(stationRefRepo.retrieveSiteMembershipsByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember2));
    when(stationRefRepo.retrieveSiteMembershipsBySiteAndChannelId(
        TestFixtures.site.getEntityId(), TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember));
    when(stationRefRepo.retrieveSiteMembershipsBySiteAndChannelId(
        TestFixtures.site2.getEntityId(), TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMember2));
    when(stationRefRepo.retrieveSiteMemberships())
        .thenReturn(TestFixtures.allSiteMemberships);
    // digitizer memberships
    when(stationRefRepo.retrieveDigitizerMembershipsByDigitizerId(any()))
        .thenReturn(List.of());
    when(stationRefRepo
        .retrieveDigitizerMembershipsByDigitizerId(TestFixtures.digitizer.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember));
    when(stationRefRepo
        .retrieveDigitizerMembershipsByDigitizerId(TestFixtures.digitizer2.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember2));
    when(stationRefRepo.retrieveDigitizerMembershipsByChannelId(any()))
        .thenReturn(List.of());
    when(stationRefRepo.retrieveDigitizerMembershipsByChannelId(TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember));
    when(
        stationRefRepo.retrieveDigitizerMembershipsByChannelId(TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember2));
    when(stationRefRepo.retrieveDigitizerMembershipsByDigitizerAndChannelId(
        TestFixtures.digitizer.getEntityId(), TestFixtures.channel.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember));
    when(stationRefRepo.retrieveDigitizerMembershipsByDigitizerAndChannelId(
        TestFixtures.digitizer2.getEntityId(), TestFixtures.channel2.getEntityId()))
        .thenReturn(List.of(TestFixtures.digitizerMember2));
    when(stationRefRepo.retrieveDigitizerMemberships())
        .thenReturn(TestFixtures.allDigitizerMemberships);
    // processing views
    reset(stationRefFactory);
    when(stationRefFactory.networkFromName(TestFixtures.fatNetwork.getName()))
        .thenReturn(Optional.of(TestFixtures.fatNetwork));
    when(stationRefFactory.stationFromName(TestFixtures.fatStation.getName()))
        .thenReturn(Optional.of(TestFixtures.fatStation));
    when(stationRefFactory.siteFromName(TestFixtures.fatSite.getName()))
        .thenReturn(Optional.of(TestFixtures.fatSite));
  }

  @BeforeClass
  public static void init() throws Exception {
    final int servicePort = getAvailablePort();
    Configuration configuration = Configuration.builder().setPort(servicePort).build();
    BASE_URL = "http://localhost:" + servicePort + configuration.getBaseUrl();
    SHORT_BASE_URL = "http://localhost:" + servicePort;
    StationReferenceCoiService.startService(configuration, stationRefRepo, stationRefFactory);
  }


  @AfterClass
  public static void teardown() {
    StationReferenceCoiService.stopService();
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForNetworkStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_NETWORKS_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceNetworks. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreNetworks() throws Exception {
    ReferenceNetwork net = TestFixtures.network;
    List<ReferenceNetwork> postBody = List.of(net);
    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_NETWORKS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    UUID[] expectedJson = new UUID[]{net.getEntityId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");
    List<ReferenceNetwork> storedNetworks = referenceNetworkArgumentCaptor.getAllValues();
    assertEquals(postBody, storedNetworks);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForStationStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_STATIONS_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceStations. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreStations() throws Exception {
    ReferenceStation station = TestFixtures.station;
    List<ReferenceStation> postBody = List.of(TestFixtures.station);

    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_STATIONS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{station.getEntityId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");

    List<ReferenceStation> storedStations = referenceStationArgumentCaptor.getAllValues();
    assertEquals(postBody, storedStations);
  }


  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForSiteStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_SITES_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceSites. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreSites() throws Exception {
    ReferenceSite site = TestFixtures.site;
    List<ReferenceSite> postBody = List.of(TestFixtures.site);

    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_SITES_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{site.getEntityId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");

    List<ReferenceSite> storedSites = referenceSiteArgumentCaptor.getAllValues();
    assertEquals(postBody, storedSites);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForChannelStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_CHANNELS_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceChannels. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreChannels() throws Exception {
    ReferenceChannel channel = TestFixtures.channel;
    List<ReferenceChannel> postBody = List.of(TestFixtures.channel);

    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_CHANNELS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{channel.getEntityId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");

    List<ReferenceChannel> storedChannels = referenceChannelArgumentCaptor.getAllValues();
    assertEquals(postBody, storedChannels);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForCalibrationStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_CALIBRATIONS_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceCalibrations. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreCalibrations() throws Exception {
    ReferenceCalibration calibration = TestFixtures.calibration_chan1_v1;
    List<ReferenceCalibration> postBody = List.of(TestFixtures.calibration_chan1_v1);

    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_CALIBRATIONS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{calibration.getId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");

    List<ReferenceCalibration> storedCalibrations = referenceCalibrationArgumentCaptor
        .getAllValues();
    assertEquals(postBody, storedCalibrations);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForResponseStore() throws Exception {
    HttpResponse<String> resp =
        UnirestTestUtilities.postJson(null, STORE_RESPONSES_URL, String.class);
    assertNotNull(resp);
    assertEquals(resp.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceResponses. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreResponses() throws Exception {
    ReferenceResponse resp = TestFixtures.response_chan1_v1;
    List<ReferenceResponse> postBody = List.of(TestFixtures.response_chan1_v1);

    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_RESPONSES_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{resp.getChannelId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");

    List<ReferenceResponse> storedResponses = referenceResponseArgumentCaptor.getAllValues();
    assertEquals(postBody, storedResponses);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForSensorStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_SENSORS_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceSensors. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreSensors() throws Exception {
    ReferenceSensor sensor = TestFixtures.sensor_chan1_v1;
    List<ReferenceSensor> postBody = List.of(TestFixtures.sensor_chan1_v1);

    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_SENSORS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{sensor.getId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");

    List<ReferenceSensor> storedSensors = referenceSensorArgumentCaptor.getAllValues();
    assertEquals(postBody, storedSensors);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForReferenceNetworkMembershipStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_NETWORK_MEMBERSHIPS_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceNetworkMemberships. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreReferenceNetworkMemberships() throws Exception {
    ReferenceNetworkMembership networkMembership = TestFixtures.netMember2;
    Collection<ReferenceNetworkMembership> postBody = new HashSet<ReferenceNetworkMembership>(
        List.of(TestFixtures.netMember2));
    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_NETWORK_MEMBERSHIPS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{networkMembership.getId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");
    Collection<ReferenceNetworkMembership> storedNetworkMemberships = referenceNetworkMembershipArgumentCaptor
        .getAllValues().get(0);
    assertEquals(storedNetworkMemberships, postBody);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForReferenceStationMembershipStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_STATION_MEMBERSHIPS_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceNetworkMemberships. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreReferenceStationMemberships() throws Exception {
    ReferenceStationMembership stationMembership = TestFixtures.stationMember;
    Collection<ReferenceStationMembership> postBody = new HashSet<ReferenceStationMembership>(
        List.of(TestFixtures.stationMember));
    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_STATION_MEMBERSHIPS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{stationMembership.getId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");
    Collection<ReferenceStationMembership> storedStationMemberships = referenceStationMembershipArgumentCaptor
        .getAllValues().get(0);
    assertEquals(storedStationMemberships, postBody);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParametersForReferenceSiteMembershipStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_SITE_MEMBERSHIPS_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests storing ReferenceNetworkMemberships. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreReferenceSiteMemberships() throws Exception {
    ReferenceSiteMembership siteMembership = TestFixtures.siteMember;
    Collection<ReferenceSiteMembership> postBody = new HashSet<ReferenceSiteMembership>(
        List.of(TestFixtures.siteMember));
    // test json request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STORE_SITE_MEMBERSHIPS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    UUID[] expectedJson = new UUID[]{siteMembership.getId()};
    UUID[] receivedJson = objectMapper.readValue(response.getBody(), UUID[].class);
    assertArrayEquals(expectedJson, receivedJson);
    TestCase.assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");
    Collection<ReferenceSiteMembership> storedSiteMemberships = referenceSiteMembershipArgumentCaptor
        .getAllValues().get(0);
    assertEquals(storedSiteMemberships, postBody);
  }


  /**
   * Tests the 'networks/processing' endpoint with a name parameter.
   */
  @Test
  public void testQueryProcessingNetworkByName() throws Exception {
    String url = PROCESSING_NETWORKS_URL + TestFixtures.fatNetwork.getName();
    testGetJson(url, Optional.of(TestFixtures.fatNetwork));
  }

  /**
   * Tests the 'networks/processing' endpoint with a bad name parameter. a bad name will simply
   * return an empty response body.
   */
  @Test
  public void testQueryProcessingNetworkBadName() throws Exception {
    String url = PROCESSING_NETWORKS_URL + UNKNOWN_NAME;
    testGetJsonErrorResponse(url, HttpStatus.NOT_FOUND_404);
  }

  /**
   * Tests the 'networks/processing' endpoint with no name parameter. A 404 not found error is
   * thrown.
   */
  @Test
  public void testQueryProcessingNetworkNoName() throws Exception {
    testGetJsonErrorResponse(PROCESSING_NETWORKS_URL, HttpStatus.NOT_FOUND_404);
  }

  /**
   * Tests the 'stations/processing' endpoint.
   */
  @Test
  public void testQueryProcessingStationByName() throws Exception {
    String url = PROCESSING_STATIONS_URL + TestFixtures.fatStation.getName();
    testGetJson(url, Optional.of(TestFixtures.fatStation));
  }


  /**
   * Tests the 'stations/processing' endpoint with a bad name parameter. a bad name will simply
   * return an empty response body.
   */
  @Test
  public void testQueryProcessingStationBadName() throws Exception {
    String url = PROCESSING_STATIONS_URL + UNKNOWN_NAME;
    testGetJsonErrorResponse(url, HttpStatus.NOT_FOUND_404);
  }

  /**
   * Tests the 'stations/processing' endpoint with no name parameter. A 404 not found error is
   * thrown.
   */
  @Test
  public void testQueryProcessingStationNoName() throws Exception {
    testGetJsonErrorResponse(PROCESSING_STATIONS_URL, HttpStatus.NOT_FOUND_404);
  }


  /**
   * Tests the 'sites/processing' endpoint.
   */
  @Test
  public void testQueryProcessingSiteByName() throws Exception {
    String url = PROCESSING_SITES_URL + TestFixtures.fatSite.getName();
    testGetJson(url, Optional.of(TestFixtures.fatSite));
  }


  /**
   * Tests the 'sites/processing' endpoint with a bad name parameter. a bad name will simply return
   * an empty response body.
   */
  @Test
  public void testQueryProcessingSiteBadName() throws Exception {
    String url = PROCESSING_STATIONS_URL + UNKNOWN_NAME;
    testGetJsonErrorResponse(url, HttpStatus.NOT_FOUND_404);
  }

  /**
   * Tests the 'sites/processing' endpoint with no name parameter. A 404 not found error is thrown.
   */
  @Test
  public void testQueryProcessingSiteNoName() throws Exception {
    testGetJsonErrorResponse(PROCESSING_SITES_URL, HttpStatus.NOT_FOUND_404);
  }

  /**
   * Tests the 'networks' endpoint with no parameters.
   */
  @Test
  public void testQueryNetworksNoParams() throws Exception {
    testGetJson(NETWORKS_URL, TestFixtures.allNetworks);
  }

  @Test
  public void testQueryNetworksStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveNetworks();
    testGetJsonErrorResponse(NETWORKS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'networks' endpoint with an id parameter.
   */
  @Test
  public void testQueryNetworksById() throws Exception {
    String url = NETWORKS_URL + "/id/" + TestFixtures.network.getEntityId();
    testGetJson(url, List.of(TestFixtures.network));
    // make request with bad id, expect empty list and status OK back.
    url = NETWORKS_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryNetworksByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveNetworksByEntityId(any());
    String url = NETWORKS_URL + "/id/" + TestFixtures.network.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'networks' endpoint with a name parameter.
   */
  @Test
  public void testQueryNetworksByName() throws Exception {
    String url = NETWORKS_URL + "/name/" + TestFixtures.network.getName();
    testGetJson(url, List.of(TestFixtures.network));
    url = NETWORKS_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryNetworksByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveNetworksByName(any());
    String url = NETWORKS_URL + "/name/" + TestFixtures.network.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  @Test
  public void testQueryNetworksByStationName() throws Exception {
    String url = NETWORKS_URL + "?station-name=" + TestFixtures.stationName;
    testGetJson(url, List.of(TestFixtures.network));
    url = NETWORKS_URL + "?station-name=" + TestFixtures.stationName2;
    testGetJson(url, List.of(TestFixtures.network2));
    url = NETWORKS_URL + "?station-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'networks' endpoint with start and end time parameters.
   */
  @Test
  public void testQueryNetworksByStartAndEndTime() throws Exception {
    // do a query for all networks, but within a time range that will exclude network 2.
    // assert that the result is only a list of network 1.
    String url = NETWORKS_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.network.getActualChangeTime();
    testGetJson(url, List.of(TestFixtures.network));
  }

  /**
   * Tests the 'networks' endpoint with bad start time format which should return a bad response.
   */
  @Test
  public void testQueryNetworksWithBadStartTime() throws Exception {
    String url = NETWORKS_URL + "?start-time=bad_time";
    HttpResponse<ReferenceNetwork[]> response = UnirestTestUtilities.getJson(
        url, ReferenceNetwork[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceNetwork[] nets = response.getBody();
    assertNotNull(nets);
    assertEquals(0, nets.length);
  }

  /**
   * Tests the 'networks' endpoint with bad end time format which should return a bad response.
   */
  @Test
  public void testQueryNetworksWithBadEndTime() throws Exception {
    String url = NETWORKS_URL + "?end-time=bad_time";
    HttpResponse<ReferenceNetwork[]> response = UnirestTestUtilities.getJson(
        url, ReferenceNetwork[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceNetwork[] nets = response.getBody();
    assertNotNull(nets);
    assertEquals(0, nets.length);
  }

  /**
   * Tests the 'stations' endpoint with no parameters.
   */
  @Test
  public void testQueryStationNoParams() throws Exception {
    testGetJson(STATIONS_URL, TestFixtures.allStations);
  }

  @Test
  public void testQueryStationsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveStations();
    testGetJsonErrorResponse(STATIONS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'stations' endpoint with an entity id parameter.
   */
  @Test
  public void testQueryStationByEntityId() throws Exception {
    String url = STATIONS_URL + "/id/" + TestFixtures.station.getEntityId();
    testGetJson(url, List.of(TestFixtures.station));
    // make request with bad id, expect empty list and status OK back.
    url = STATIONS_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'stations' endpoint with a version id parameter.
   */
  @Test
  public void testQueryStationsByVersionIds() throws Exception {

    // test with 1 station
    Collection<UUID> postBody = List.of(TestFixtures.station.getVersionId());
    // test request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, STATIONS_BY_VERSION_IDS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    String expected = TestFixtures.objectMapper.writeValueAsString(List.of(TestFixtures.station));
    assertEquals(response.getBody(), expected);

    // test with 2 stations
    Collection<UUID> postBody2 = List
        .of(TestFixtures.station.getVersionId(), TestFixtures.station2.getVersionId());
    // test request
    HttpResponse<String> response2 = TestUtilities
        .postResponseFromEndPoint(postBody2, STATIONS_BY_VERSION_IDS_URL);
    assertNotNull(response2);
    assertEquals(HttpStatus.OK_200, response2.getStatus());

    String expected2 = TestFixtures.objectMapper
        .writeValueAsString(List.of(TestFixtures.station, TestFixtures.station2));
    assertEquals(response2.getBody(), expected2);
  }

  @Test
  public void testQueryStationByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveStationsByEntityId(any());
    String url = STATIONS_URL + "/id/" + TestFixtures.station.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'stations' endpoint with a name parameter.
   */
  @Test
  public void testQueryStationsByName() throws Exception {
    String url = STATIONS_URL + "/name/" + TestFixtures.station.getName();
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryStationsByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveStationsByName(any());
    String url = STATIONS_URL + "/name/" + TestFixtures.station.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'stations' endpoint providing argument 'network-name'.
   */
  @Test
  public void testQueryStationsByNetworkName() throws Exception {
    String url = STATIONS_URL + "?network-name=" + TestFixtures.network.getName();
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "?network-name=" + TestFixtures.network2.getName();
    testGetJson(url, List.of(TestFixtures.station2));
    url = STATIONS_URL + "?network-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryStationsBySiteName() throws Exception {
    String url = STATIONS_URL + "?site-name=" + TestFixtures.site.getName();
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "?site-name=" + TestFixtures.site2.getName();
    testGetJson(url, List.of(TestFixtures.station2));
    url = STATIONS_URL + "?site-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryStationsByNetworkAndSiteName() throws Exception {
    String url = STATIONS_URL + "?network-name=" + TestFixtures.network.getName()
        + "&site-name=" + TestFixtures.site.getName();
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "?network-name=" + TestFixtures.network2.getName()
        + "&site-name=" + TestFixtures.site2.getName();
    testGetJson(url, List.of(TestFixtures.station2));
    url = STATIONS_URL + "?network-name=" + UNKNOWN_NAME
        + "&site-name=" + TestFixtures.site2.getName();
    testGetJson(url, List.of());
    url = STATIONS_URL + "?network-name=" + TestFixtures.network2.getName()
        + "&site-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
    url = STATIONS_URL + "?network-name=" + UNKNOWN_NAME + "&site-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'station' endpoint with start and end time parameters.
   */
  @Test
  public void testQueryStationsByStartAndEndTime() throws Exception {
    // do a query for all station, but within a time range that will exclude station 2.
    // assert that the result is only a list of station 1.
    String url = STATIONS_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.station.getActualChangeTime();
    testGetJson(url, List.of(TestFixtures.station));
  }

  /**
   * Tests the 'station' endpoint with bad start time format which should return a bad response.
   */
  @Test
  public void testQueryStationsWithBadStartTime() throws Exception {
    String url = STATIONS_URL + "?start-time=bad_time";
    HttpResponse<ReferenceStation[]> response = UnirestTestUtilities.getJson(
        url, ReferenceStation[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceStation[] stas = response.getBody();
    assertNotNull(stas);
    assertEquals(0, stas.length);
  }

  /**
   * Tests the 'station' endpoint with bad end time format which should return a bad response.
   */
  @Test
  public void testQueryStationsWithBadEndTime() throws Exception {
    String url = STATIONS_URL + "?end-time=bad_time";
    HttpResponse<ReferenceStation[]> response = UnirestTestUtilities.getJson(
        url, ReferenceStation[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceStation[] stas = response.getBody();
    assertNotNull(stas);
    assertEquals(0, stas.length);
  }

  /**
   * Tests the 'sites' endpoint with no parameters.
   */
  @Test
  public void testQuerySitesNoParams() throws Exception {
    testGetJson(SITES_URL, TestFixtures.allSites);
  }

  @Test
  public void testQuerySitesStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveSites();
    testGetJsonErrorResponse(SITES_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'sites' endpoint with an id parameter.
   */
  @Test
  public void testQuerySitesById() throws Exception {
    String url = SITES_URL + "/id/" + TestFixtures.site.getEntityId();
    testGetJson(url, List.of(TestFixtures.site));
    // make request with bad id, expect empty list and status OK back.
    url = SITES_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQuerySitesByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveSitesByEntityId(any());
    String url = SITES_URL + "/id/" + TestFixtures.site.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'sites' endpoint with a name parameter.
   */
  @Test
  public void testQuerySitesByName() throws Exception {
    String url = SITES_URL + "/name/" + TestFixtures.site.getName();
    testGetJson(url, List.of(TestFixtures.site));
    url = SITES_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQuerySitesByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveSitesByName(any());
    String url = SITES_URL + "/name/" + TestFixtures.site.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'sites' endpoint providing argument 'channel-id'.
   */
  @Test
  public void testQuerySitesByChannel() throws Exception {
    String url = SITES_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.site));
    url = SITES_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.site2));
    url = SITES_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'sites' endpoint with start and end time parameters.
   */
  @Test
  public void testQuerySitesByStartAndEndTime() throws Exception {
    // do a query for all sites, but within a time range that will exclude site 2.
    // assert that the result is only a list of site 1.
    String url = SITES_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.site.getActualChangeTime();
    testGetJson(url, List.of(TestFixtures.site));
  }

  /**
   * Tests the 'sites' endpoint with bad start time format which should return a bad response.
   */
  @Test
  public void testQuerySitesWithBadStartTime() throws Exception {
    String url = SITES_URL + "?start-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'sites' endpoint with bad end time format which should return a bad response.
   */
  @Test
  public void testQuerySitesWithBadEndTime() throws Exception {
    String url = SITES_URL + "?end-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'channels' endpoint with no parameters.
   */
  @Test
  public void testQueryChannelsNoParams() throws Exception {
    testGetJson(CHANNELS_URL, TestFixtures.allChannels);
  }

  @Test
  public void testQueryChannelsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveChannels();
    testGetJsonErrorResponse(CHANNELS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'channels' endpoint with an entity id parameter.
   */
  @Test
  public void testQueryChannelsByEntityId() throws Exception {
    String url = CHANNELS_URL + "/id/" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel));
    // make request with bad id, expect empty list and status OK back.
    url = CHANNELS_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels' endpoint with a version id parameter.
   */
  @Test
  public void testQueryChannelsByVersionIds() throws Exception {

    // test with 1 channel
    Collection<UUID> postBody = List.of(TestFixtures.channel.getVersionId());
    // test request
    HttpResponse<String> response = TestUtilities
        .postResponseFromEndPoint(postBody, CHANNELS_BY_VERSION_IDS_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());

    String expected = TestFixtures.objectMapper.writeValueAsString(List.of(TestFixtures.channel));
    assertEquals(response.getBody(), expected);

    // test with 2 channels
    Collection<UUID> postBody2 = List
        .of(TestFixtures.channel.getVersionId(), TestFixtures.channel2.getVersionId());
    // test request
    HttpResponse<String> response2 = TestUtilities
        .postResponseFromEndPoint(postBody2, CHANNELS_BY_VERSION_IDS_URL);
    assertNotNull(response2);
    assertEquals(HttpStatus.OK_200, response2.getStatus());

    String expected2 = TestFixtures.objectMapper
        .writeValueAsString(List.of(TestFixtures.channel, TestFixtures.channel2));
    assertEquals(response2.getBody(), expected2);
  }

  @Test
  public void testQueryChannelsByIdStorageUnavailable() throws Exception {
    doThrow(new
        StorageUnavailableException()).when(stationRefRepo).retrieveChannelsByEntityId(any());
    String
        url = CHANNELS_URL + "/id/" + TestFixtures.channel.getEntityId();
    testGetJsonErrorResponse(url,
        HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'channels' endpoint with a name parameter.
   */
  @Test
  public void testQueryChannelsByName() throws Exception {
    String url = CHANNELS_URL + "/name/" + TestFixtures.channel.getName();
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryChannelsByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveChannelsByName(any());
    String url = CHANNELS_URL + "/name/" + TestFixtures.channel.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'channels' endpoint providing argument 'digitizer-id'.
   */
  @Test
  public void testQueryChannelsByDigitizerId() throws Exception {
    String url = CHANNELS_URL + "?digitizer-id=" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "?digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel2));
    url = CHANNELS_URL + "?digitizer-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels' endpoint providing argument 'site-name'.
   */
  @Test
  public void testQueryChannelsBySite() throws Exception {
    String url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName;
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName2;
    testGetJson(url, List.of(TestFixtures.channel2));
    url = CHANNELS_URL + "?site-name=" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels' endpoint providing arguments 'site-name' and 'digitizer-id'.
   */
  @Test
  public void testQueryChannelsBySiteAndDigitizer() throws Exception {
    String url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName
        + "&digitizer-id=" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName2
        + "&digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of(TestFixtures.channel2));
    url = CHANNELS_URL + "?site-name=" + UNKNOWN_NAME
        + "&digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of());
    url = CHANNELS_URL + "?site-name=" + TestFixtures.siteName2
        + "&digitizer-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    url = CHANNELS_URL + "?site-name=" + UNKNOWN_NAME
        + "&digitizer-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels' endpoint with start and end time parameters.
   */
  @Test
  public void testQueryChannelsByStartAndEndTime() throws Exception {
    // do a query for all channels, but within a time range that will exclude channel 2.
    // assert that the result is only a list of channel 1.
    String url = CHANNELS_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.channel.getActualTime();
    testGetJson(url, List.of(TestFixtures.channel));
  }

  /**
   * Tests the 'channels' endpoint with bad start time format which should return a bad response.
   */
  @Test
  public void testQueryChannelsWithBadStartTime() throws Exception {
    String url = CHANNELS_URL + "?start-time=bad_time";
    HttpResponse<ReferenceChannel[]> response = UnirestTestUtilities.getJson(
        url, ReferenceChannel[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceChannel[] chans = response.getBody();
    assertNotNull(chans);
    assertEquals(0, chans.length);
  }

  /**
   * Tests the 'channels' endpoint with bad end time format which should return a bad response.
   */
  @Test
  public void testQueryChannelsWithBadEndTime() throws Exception {
    String url = CHANNELS_URL + "?end-time=bad_time";
    HttpResponse<ReferenceChannel[]> response = UnirestTestUtilities.getJson(
        url, ReferenceChannel[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceChannel[] chans = response.getBody();
    assertNotNull(chans);
    assertEquals(0, chans.length);
  }

  /**
   * Tests the 'digitizers' endpoint with no parameters.
   */
  @Test
  public void testQueryDigitizersNoParams() throws Exception {
    testGetJson(DIGITIZERS_URL, TestFixtures.allDigitizers);
  }

  @Test
  public void testQueryDigitizersStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveDigitizers();
    testGetJsonErrorResponse(DIGITIZERS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'digitizers' endpoint with an id parameter.
   */
  @Test
  public void testQueryDigitizersById() throws Exception {
    String url = DIGITIZERS_URL + "/id/" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizer));
    // make request with bad id, expect empty list and status OK back.
    url = DIGITIZERS_URL + "/id/" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryDigitizersByIdStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveDigitizersByEntityId(any());
    String url = DIGITIZERS_URL + "/id/" + TestFixtures.digitizer.getEntityId();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'digitizers' endpoint with a name parameter.
   */
  @Test
  public void testQueryDigitizersByName() throws Exception {
    String url = DIGITIZERS_URL + "/name/" + TestFixtures.digitizer.getName();
    testGetJson(url, List.of(TestFixtures.digitizer));
    url = DIGITIZERS_URL + "/name/" + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryDigitizersByNameStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveDigitizersByName(any());
    String url = DIGITIZERS_URL + "/name/" + TestFixtures.digitizer.getName();
    testGetJsonErrorResponse(url, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests the 'digitizers' endpoint providing argument 'channel-id'.
   */
  @Test
  public void testQueryDigitizersByChannelId() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizer));
    url = DIGITIZERS_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizer2));
    url = DIGITIZERS_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'digitizers' endpoint with start and end time parameters.
   */
  @Test
  public void testQueryDigitizersByStartAndEndTime() throws Exception {
    // do a query for all digitizers, but within a time range that will exclude digitizer 2.
    // assert that the result is only a list of digitizer 1.
    String url = DIGITIZERS_URL + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.digitizer.getActualChangeTime();
    testGetJson(url, List.of(TestFixtures.digitizer));
  }

  /**
   * Tests the 'digitizers' endpoint with bad start time format which should return a bad response.
   */
  @Test
  public void testQueryDigitizersWithBadStartTime() throws Exception {
    String url = DIGITIZERS_URL + "?start-time=bad_time";
    HttpResponse<ReferenceDigitizer[]> response = UnirestTestUtilities.getJson(
        url, ReferenceDigitizer[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceDigitizer[] digis = response.getBody();
    assertNotNull(digis);
    assertEquals(0, digis.length);
  }

  /**
   * Tests the 'digitizers' endpoint with bad end time format which should return a bad response.
   */
  @Test
  public void testQueryDigitizersWithBadEndTime() throws Exception {
    String url = DIGITIZERS_URL + "?end-time=bad_time";
    HttpResponse<ReferenceDigitizer[]> response = UnirestTestUtilities.getJson(
        url, ReferenceDigitizer[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceDigitizer[] digis = response.getBody();
    assertNotNull(digis);
    assertEquals(0, digis.length);
  }

  /**
   * Tests the 'stations by network' endpoint with a network known to be linked to one station, and
   * times. First, time range [EPOCH, actualTime] is used (which finds TestFixtures.network), then
   * range [EPOCH, actualTime - 1] is used, which finds nothing.
   */
  @Test
  public void testQueryStationsByNetworkAndTimes() throws Exception {
    String url = STATIONS_URL + "?network-name="
        + TestFixtures.networkName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.station));
    url = STATIONS_URL + "?network-name="
        + TestFixtures.networkName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime.minusSeconds(1);
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'stations by network' endpoint with a bad start time.
   */
  @Test
  public void testQueryStationsByNetworkAndBadStartTime() throws Exception {
    String url = STATIONS_URL + "?network-name=" + TestFixtures.networkName
        + "&start-time=bad_time";
    HttpResponse<ReferenceStation[]> response = UnirestTestUtilities.getJson(
        url, ReferenceStation[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceStation[] stas = response.getBody();
    assertNotNull(stas);
    assertEquals(0, stas.length);
  }

  /**
   * Tests the 'stations by network' endpoint with a bad end time.
   */
  @Test
  public void testQueryStationsByNetworkAndBadEndTime() throws Exception {
    String url = STATIONS_URL + "?network-name=" + TestFixtures.networkName
        + "&end-time=bad_time";
    HttpResponse<ReferenceStation[]> response = UnirestTestUtilities.getJson(
        url, ReferenceStation[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceStation[] stas = response.getBody();
    assertNotNull(stas);
    assertEquals(0, stas.length);
  }

  /**
   * Tests the 'stations by network' endpoint with an unknown name, checks that no results are
   * found.
   */
  @Test
  public void testQueryStationsByBadNetworkName() throws Exception {
    String url = STATIONS_URL + "?network-name="
        + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'sites by station' endpoint with a station known to be linked to one site.
   */
  @Test
  public void testQuerySitesByStation() throws Exception {
    String url = SITES_URL + "?station-name="
        + TestFixtures.stationName;
    testGetJson(url, List.of(TestFixtures.site));
  }

  /**
   * Tests the 'sites by station' endpoint with a station known to be linked to one site, and times.
   * First, time range [EPOCH, actualTime] is used (which finds TestFixtures.site), then range
   * [EPOCH, actualTime - 1] is used, which finds nothing.
   */
  @Test
  public void testQuerySitesByStationAndTimes() throws Exception {
    String url = SITES_URL + "?station-name="
        + TestFixtures.stationName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.site));
    url = SITES_URL + "?station-name="
        + TestFixtures.stationName + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime.minusSeconds(1);
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'sites by station' endpoint with a bad start time.
   */
  @Test
  public void testQuerySitesByStationAndBadStartTime() throws Exception {
    String url = SITES_URL + "?station-name=" + TestFixtures.stationName
        + "&start-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'sites by station' endpoint with a bad end time.
   */
  @Test
  public void testQuerySitesByStationAndBadEndTime() throws Exception {
    String url = SITES_URL + "?station-name=" + TestFixtures.stationName
        + "&end-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'sites by station' endpoint with an unknown name, checks that no results are found.
   */
  @Test
  public void testQuerySitesByBadStationName() throws Exception {
    String url = SITES_URL + "?station-name="
        + UNKNOWN_NAME;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'digitizers by channel' endpoint with a channel known to be linked to one digitizer.
   */
  @Test
  public void testQueryDigitizersByChannel() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id="
        + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizer));
  }

  /**
   * Tests the 'digitizers by channel' endpoint with a channel known to be linked to one digitizer,
   * and times. First, time range [EPOCH, actualTime] is used (which finds TestFixtures.digitizer),
   * then range [EPOCH, actualTime - 1] is used, which finds nothing.
   */
  @Test
  public void testQueryDigitizersByChannelAndTimes() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id="
        + TestFixtures.channel.getEntityId() + "&start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.digitizer));
    url = DIGITIZERS_URL + "?channel-id="
        + TestFixtures.channel.getEntityId() + "&start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime.minusSeconds(1);
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'digitizers by channel' endpoint with a bad start time.
   */
  @Test
  public void testQueryDigitizersByChannelIdAndBadStartTime() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'digitizers by channel' endpoint with a bad end time.
   */
  @Test
  public void testQueryDigitizersByChannelAndBadEndTime() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&end-time=bad_time";
    HttpResponse<ReferenceSite[]> response = UnirestTestUtilities.getJson(
        url, ReferenceSite[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceSite[] sites = response.getBody();
    assertNotNull(sites);
    assertEquals(0, sites.length);
  }

  /**
   * Tests the 'digitizers by channel' endpoint with an unknown id, checks that no results are
   * found.
   */
  @Test
  public void testQueryDigitizersByBadSiteName() throws Exception {
    String url = DIGITIZERS_URL + "?channel-id="
        + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels by digitizer' endpoint with a digitizer known to be linked to one channel,
   * and times. First, time range [EPOCH, actualTime] is used (which finds TestFixtures.channel),
   * then range [EPOCH, actualTime - 1] is used, which finds nothing.
   */
  @Test
  public void testQueryChannelsByDigitizerAndTimes() throws Exception {
    String url = CHANNELS_URL + "?digitizer-name="
        + TestFixtures.digitizer.getName() + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.channel));
    url = CHANNELS_URL + "?digitizer-name="
        + TestFixtures.digitizer.getName() + "&start-time" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime.minusSeconds(1);
    testGetJson(url, List.of());
  }

  /**
   * Tests the 'channels by digitizer' endpoint with a bad start time.
   */
  @Test
  public void testQueryChannelsByDigitizerAndBadStartTime() throws Exception {
    String url = CHANNELS_URL + "?digitizer-name=" + TestFixtures.digitizer.getName()
        + "&start-time=bad_time";
    HttpResponse<ReferenceChannel[]> response = UnirestTestUtilities.getJson(
        url, ReferenceChannel[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceChannel[] chans = response.getBody();
    assertNotNull(chans);
    assertEquals(0, chans.length);
  }

  /**
   * Tests the 'channels by digitizer' endpoint with a bad end time.
   */
  @Test
  public void testQueryChannelsByDigitizerAndBadEndTime() throws Exception {
    String url = CHANNELS_URL + "?digitizer-name=" + TestFixtures.digitizer.getName()
        + "&end-time=bad_time";
    HttpResponse<ReferenceChannel[]> response = UnirestTestUtilities.getJson(
        url, ReferenceChannel[].class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    ReferenceChannel[] chans = response.getBody();
    assertNotNull(chans);
    assertEquals(0, chans.length);
  }

  /**
   * Tests querying for channel calibrations.
   */
  @Test
  public void testQueryCalibrations() throws Exception {
    testGetJson(CALIBRATIONS_URL, TestFixtures.allCalibrations);
    String url = CALIBRATIONS_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, TestFixtures.chan1_calibrations);
    url = CALIBRATIONS_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, TestFixtures.chan2_calibrations);
    // time query: during time of first response, but before time of second.
    url = CALIBRATIONS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime.minusSeconds(60)
        + "&end-time=" + TestFixtures.actualTime2.minusSeconds(1);
    testGetJson(url, List.of(TestFixtures.calibration_chan1_v1));
    // time query: after time of second response, only finds second response.
    url = CALIBRATIONS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime2.plusSeconds(1);
    testGetJson(url, List.of(TestFixtures.calibration_chan1_v2));
    // time query: at time of first response to time of 3rd response, finds responses 1-3.
    url = CALIBRATIONS_URL + "?start-time=" + TestFixtures.actualTime
        + "&end-time=" + TestFixtures.actualTime3;
    testGetJson(url, List.of(TestFixtures.calibration_chan1_v1,
        TestFixtures.calibration_chan1_v2, TestFixtures.calibration_chan2_v1));
    // unknown channel
    url = CALIBRATIONS_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryCalibrationsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveCalibrations();
    testGetJsonErrorResponse(CALIBRATIONS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for channel sensors.
   */
  @Test
  public void testQuerySensors() throws Exception {
    testGetJson(SENSORS_URL, TestFixtures.allSensors);
    String url = SENSORS_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, TestFixtures.chan1_sensors);
    url = SENSORS_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, TestFixtures.chan2_sensors);
    // time query: during time of first sensor, but before time of second.
    url = SENSORS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime.minusSeconds(60)
        + "&end-time=" + TestFixtures.actualTime2.minusSeconds(1);
    testGetJson(url, List.of(TestFixtures.sensor_chan1_v1));
    // time query: after time of second sensor, only finds second sensor.
    url = SENSORS_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime2.plusSeconds(1);
    testGetJson(url, List.of(TestFixtures.sensor_chan1_v2));
    // time query: at time of first sensor to time of 3rd sensor, finds sensor 1-3.
    url = SENSORS_URL + "?start-time=" + TestFixtures.actualTime
        + "&end-time=" + TestFixtures.actualTime3;
    testGetJson(url, List.of(TestFixtures.sensor_chan1_v1,
        TestFixtures.sensor_chan1_v2, TestFixtures.sensor_chan2_v1));
    // unknown channel
    url = SENSORS_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQuerySensorsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveSensors();
    testGetJsonErrorResponse(SENSORS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for channel responses.
   */
  @Test
  public void testQueryResponses() throws Exception {
    testGetJson(RESPONSES_URL, TestFixtures.allResponses);
    String url = RESPONSES_URL + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, TestFixtures.chan1_responses);
    url = RESPONSES_URL + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, TestFixtures.chan2_responses);
    // time query: during time of first response, but before time of second.
    url = RESPONSES_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime.minusSeconds(60)
        + "&end-time=" + TestFixtures.actualTime2.minusSeconds(1);
    testGetJson(url, List.of(TestFixtures.response_chan1_v1));
    // time query: after time of second response, only finds second response.
    url = RESPONSES_URL + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&start-time=" + TestFixtures.actualTime2.plusSeconds(1);
    testGetJson(url, List.of(TestFixtures.response_chan1_v2));
    // time query: at time of first response to time of 3rd response, finds responses 1-3.
    url = RESPONSES_URL + "?start-time=" + TestFixtures.actualTime
        + "&end-time=" + TestFixtures.actualTime3;
    testGetJson(url, List.of(TestFixtures.response_chan1_v1,
        TestFixtures.response_chan1_v2, TestFixtures.response_chan2_v1));
    // unknown channel
    url = RESPONSES_URL + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
  }

  @Test
  public void testQueryResponsesStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveResponses();
    testGetJsonErrorResponse(RESPONSES_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for network memberships.
   */
  @Test
  public void testNetworkMemberships() throws Exception {
    final String baseUrl = NETWORK_MEMBERSHIPS_URL;
    // test getting all memberships
    testGetJson(baseUrl, TestFixtures.allNetworkMemberships);
    // test filter by start time
    String url = baseUrl + "?start-time=" + TestFixtures.actualTime2;
    testGetJson(url, List.of(TestFixtures.netMember2));
    // test filter by end time
    url = baseUrl + "?end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.netMember));
    // test filter by start time and end time
    url = baseUrl + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.netMember));
    // test filter by network-id
    url = baseUrl + "?network-id=" + TestFixtures.network.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember));
    url = baseUrl + "?network-id=" + TestFixtures.network2.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember2));
    url = baseUrl + "?network-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by station-id
    url = baseUrl + "?station-id=" + TestFixtures.station.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember));
    url = baseUrl + "?station-id=" + TestFixtures.station2.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember2));
    url = baseUrl + "?station-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by network-id and station-id
    url = baseUrl + "?network-id=" + TestFixtures.network.getEntityId()
        + "&station-id=" + TestFixtures.station.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember));
    url = baseUrl + "?network-id=" + TestFixtures.network2.getEntityId()
        + "&station-id=" + TestFixtures.station2.getEntityId();
    testGetJson(url, List.of(TestFixtures.netMember2));
    // test with bad network-id
    url = baseUrl + "?network-id=1234";
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    // test with bad station-id
    url = baseUrl + "?station-id=1234";
    response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  @Test
  public void testQueryNetworkMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveNetworkMemberships();
    testGetJsonErrorResponse(NETWORK_MEMBERSHIPS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for station memberships.
   */
  @Test
  public void testStationMemberships() throws Exception {
    final String baseUrl = STATION_MEMBERSHIPS_URL;
    // test getting all memberships
    testGetJson(baseUrl, TestFixtures.allStationMemberships);
    // test filter by start time
    String url = baseUrl + "?start-time=" + TestFixtures.actualTime2;
    testGetJson(url, List.of(TestFixtures.stationMember2));
    // test filter by end time
    url = baseUrl + "?end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.stationMember));
    // test filter by start time and end time
    url = baseUrl + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.stationMember));
    // test filter by station-id
    url = baseUrl + "?station-id=" + TestFixtures.station.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember));
    url = baseUrl + "?station-id=" + TestFixtures.station2.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember2));
    url = baseUrl + "?station-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by site-id
    url = baseUrl + "?site-id=" + TestFixtures.site.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember));
    url = baseUrl + "?site-id=" + TestFixtures.site2.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember2));
    url = baseUrl + "?site-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by station-id and site-id
    url = baseUrl + "?station-id=" + TestFixtures.station.getEntityId()
        + "&site-id=" + TestFixtures.site.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember));
    url = baseUrl + "?station-id=" + TestFixtures.station2.getEntityId()
        + "&site-id=" + TestFixtures.site2.getEntityId();
    testGetJson(url, List.of(TestFixtures.stationMember2));
    // test with bad station-id
    url = baseUrl + "?station-id=1234";
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    // test with bad site-id
    url = baseUrl + "?site-id=1234";
    response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  @Test
  public void testQueryStationMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveStationMemberships();
    testGetJsonErrorResponse(STATION_MEMBERSHIPS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for site memberships.
   */
  @Test
  public void testSiteMemberships() throws Exception {
    final String baseUrl = SITE_MEMBERSHIPS_URL;
    // test getting all memberships
    testGetJson(baseUrl, TestFixtures.allSiteMemberships);
    // test filter by start time
    String url = baseUrl + "?start-time=" + TestFixtures.actualTime2;
    testGetJson(url, List.of(TestFixtures.siteMember2));
    // test filter by end time
    url = baseUrl + "?end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.siteMember));
    // test filter by start time and end time
    url = baseUrl + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.siteMember));
    // test filter by site-id
    url = baseUrl + "?site-id=" + TestFixtures.site.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember));
    url = baseUrl + "?site-id=" + TestFixtures.site2.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember2));
    url = baseUrl + "?site-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by channel-id
    url = baseUrl + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember));
    url = baseUrl + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember2));
    url = baseUrl + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by site-id and channel-id
    url = baseUrl + "?site-id=" + TestFixtures.site.getEntityId()
        + "&channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember));
    url = baseUrl + "?site-id=" + TestFixtures.site2.getEntityId()
        + "&channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.siteMember2));
    // test with bad site-id
    url = baseUrl + "?site-id=1234";
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    // test with bad channel-id
    url = baseUrl + "?channel-id=1234";
    response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  @Test
  public void testQuerySiteMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveSiteMemberships();
    testGetJsonErrorResponse(SITE_MEMBERSHIPS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /**
   * Tests querying for digitizer memberships.
   */
  @Test
  public void testDigitizerMemberships() throws Exception {
    final String baseUrl = DIGITIZER_MEMBERSHIPS_URL;
    // test getting all memberships
    testGetJson(baseUrl, TestFixtures.allDigitizerMemberships);
    // test filter by start time
    String url = baseUrl + "?start-time=" + TestFixtures.actualTime2;
    testGetJson(url, List.of(TestFixtures.digitizerMember2));
    // test filter by end time
    url = baseUrl + "?end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    // test filter by start time and end time
    url = baseUrl + "?start-time=" + Instant.EPOCH
        + "&end-time=" + TestFixtures.actualTime;
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    // test filter by channel-id
    url = baseUrl + "?channel-id=" + TestFixtures.channel.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    url = baseUrl + "?channel-id=" + TestFixtures.channel2.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember2));
    url = baseUrl + "?channel-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by digitizer-id
    url = baseUrl + "?digitizer-id=" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    url = baseUrl + "?digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember2));
    url = baseUrl + "?digitizer-id=" + UNKNOWN_UUID;
    testGetJson(url, List.of());
    // test filter by channel-id and digitizer-id
    url = baseUrl + "?channel-id=" + TestFixtures.channel.getEntityId()
        + "&digitizer-id=" + TestFixtures.digitizer.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember));
    url = baseUrl + "?channel-id=" + TestFixtures.channel2.getEntityId()
        + "&digitizer-id=" + TestFixtures.digitizer2.getEntityId();
    testGetJson(url, List.of(TestFixtures.digitizerMember2));
    // test with bad channel-id
    url = baseUrl + "?channel-id=1234";
    HttpResponse<String> response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    // test with bad channel-id
    url = baseUrl + "?digitizer-id=1234";
    response = UnirestTestUtilities.getJson(
        url, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  @Test
  public void testQueryDigitizerMembershipsStorageUnavailable() throws Exception {
    doThrow(new StorageUnavailableException())
        .when(stationRefRepo).retrieveDigitizerMemberships();
    testGetJsonErrorResponse(DIGITIZER_MEMBERSHIPS_URL, HttpStatus.SERVICE_UNAVAILABLE_503);
  }

  /////////////////////////////////////////////////

  /**
   * Tests the 'alive' endpoint.
   */
  @Test
  public void testGetAlive() throws Exception {
    String url = BASE_URL + "alive";

    HttpResponse<String> response = TestUtilities
        .getResponseFromEndPoint(url);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    assertEquals(true, response.getBody().contains("alive at"));
  }

  private static void testGetJsonErrorResponse(String url, int expectedStatus) throws Exception {
    HttpResponse<String> response = TestUtilities
        .getResponseFromEndPoint(url);
    assertNotNull(response);
    assertEquals(expectedStatus, response.getStatus());
    assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    //assertEquals(response.getHeaders().get("Content-Type").get(0), "application/text");
  }

  private static void testGetJson(String url, Object expectedResponse) throws Exception {
    testGetJson(url, expectedResponse, HttpStatus.OK_200);
  }

  private static void testGetJson(String url, Object expectedResponse,
      int expectedStatus) throws Exception {
    HttpResponse<String> response = TestUtilities
        .getResponseFromEndPoint(url);
    assertNotNull(response);
    assertEquals(expectedStatus, response.getStatus());
    String expectedJson = TestFixtures.objectMapper.writeValueAsString(expectedResponse);
    assertEquals(expectedJson, response.getBody());
    assertTrue(response.getHeaders().keySet().contains("Content-Type"));
    assertEquals(response.getHeaders().get("Content-Type").size(), 1);
    assertEquals(response.getHeaders().get("Content-Type").get(0), "application/json");
  }

  private static int getAvailablePort() throws Exception {
    ServerSocket ephemeralSocket = new ServerSocket(0);
    final int port = ephemeralSocket.getLocalPort();
    ephemeralSocket.close();
    return port;
  }

}