package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Test station reference interactions with relational database via JPA.
 */
public class StationReferenceRepositoryJpaTest {

  private static StationReferenceRepositoryJpa stationReferenceJpa;
  private static EntityManagerFactory entityManagerFactory;

  private static final String UNKNOWN_NAME = "someFakeName";
  private static final UUID UNKNOWN_ID = UUID.fromString("e2a78dbc-97d6-466b-9dd4-4e3fdf6dd95b");

  private static boolean removeEntries = false;

  @BeforeClass
  public static void setUp() throws Exception {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
    stationReferenceJpa = new StationReferenceRepositoryJpa(entityManagerFactory);

    // Load some initial objects.
    stationReferenceJpa.storeReferenceNetwork(TestFixtures.net_ims_aux);
    stationReferenceJpa.storeReferenceNetwork(TestFixtures.net_idc_da);
    for (ReferenceStation sta : TestFixtures.jnuVersions) {
      stationReferenceJpa.storeReferenceStation(sta);
    }
    for (ReferenceSite site : TestFixtures.jnuSiteVersions) {
      stationReferenceJpa.storeReferenceSite(site);
    }
    for (ReferenceChannel chan : TestFixtures.allChannels) {
      stationReferenceJpa.storeReferenceChannel(chan);
    }
    for (ReferenceDigitizer digi : TestFixtures.allDigitizers) {
      stationReferenceJpa.storeReferenceDigitizer(digi);
    }
    for (ReferenceCalibration calib : TestFixtures.allCalibrations) {
      stationReferenceJpa.storeCalibration(calib);
    }
    for (ReferenceResponse resp : TestFixtures.allResponses) {
      stationReferenceJpa.storeResponse(resp);
    }
    for (ReferenceSensor sensor : TestFixtures.allSensors) {
      stationReferenceJpa.storeSensor(sensor);
    }
    // store memberships

    stationReferenceJpa.storeNetworkMemberships(TestFixtures.networkMemberships);

    stationReferenceJpa.storeStationMemberships(TestFixtures.stationMemberships);

    stationReferenceJpa.storeSiteMemberships(TestFixtures.siteMemberships);

    for (ReferenceDigitizerMembership m : TestFixtures.digitizerMemberships) {
      stationReferenceJpa.storeDigitizerMembership(m);
    }
  }

  @AfterClass
  public static void tearDown() throws Exception {
    if (removeEntries) {
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      try {
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM " + ReferenceSiteDao.class.getSimpleName())
            .executeUpdate();
        entityManager.createQuery("DELETE FROM " + ReferenceStationDao.class.getSimpleName())
            .executeUpdate();
        entityManager.createQuery("DELETE FROM " + ReferenceNetworkDao.class.getSimpleName())
            .executeUpdate();
        entityManager
            .createQuery("DELETE FROM " + ReferenceNetworkMembershipDao.class.getSimpleName())
            .executeUpdate();
        entityManager
            .createQuery("DELETE FROM " + ReferenceStationMembershipDao.class.getSimpleName())
            .executeUpdate();
        entityManager.createQuery("DELETE FROM " + ReferenceDigitizerDao.class.getSimpleName())
            .executeUpdate();
        entityManager.createQuery("DELETE FROM " + ReferenceChannelDao.class.getSimpleName())
            .executeUpdate();
        entityManager.createQuery("DELETE FROM " + ReferenceSiteMembershipDao.class.getSimpleName())
            .executeUpdate();
        entityManager
            .createQuery("DELETE FROM " + ReferenceDigitizerMembershipDao.class.getSimpleName())
            .executeUpdate();
        entityManager.createQuery("DELETE FROM " + ReferenceAliasDao.class.getSimpleName())
            .executeUpdate();
        entityManager.getTransaction().commit();
      } finally {
        entityManager.close();
        entityManagerFactory.close();
      }
    }
  }

  @Test
  public void retrieveNetworksTest() throws Exception {
    List<ReferenceNetwork> allNets = stationReferenceJpa.retrieveNetworks();
    assertNotNull(allNets);
    assertEquals(2, allNets.size());
    assertTrue(allNets.contains(TestFixtures.net_ims_aux));
    assertTrue(allNets.contains(TestFixtures.net_idc_da));
  }

  @Test
  public void retrieveNetworksByNameTest() throws Exception {
    // query for network imx_aux
    List<ReferenceNetwork> networks = stationReferenceJpa.retrieveNetworksByName(
        TestFixtures.net_ims_aux.getName());
    assertNotNull(networks);
    assertEquals(1, networks.size());
    assertTrue(networks.contains(TestFixtures.net_ims_aux));
    // query for network idc_da
    networks = stationReferenceJpa.retrieveNetworksByName(
        TestFixtures.net_idc_da.getName());
    assertNotNull(networks);
    assertEquals(1, networks.size());
    assertTrue(networks.contains(TestFixtures.net_idc_da));
    // query for networks with a bad name (that shouldn't exist)
    networks = stationReferenceJpa.retrieveNetworksByName(UNKNOWN_NAME);
    assertNotNull(networks);
    assertTrue(networks.isEmpty());
  }

  @Test
  public void retrieveNetworksByEntityIdTest() throws Exception {
    // query for network imx_aux
    List<ReferenceNetwork> refNets = stationReferenceJpa.retrieveNetworksByEntityId(
        TestFixtures.net_ims_aux.getEntityId());
    assertNotNull(refNets);
    assertEquals(1, refNets.size());
    assertTrue(refNets.contains(TestFixtures.net_ims_aux));
    // query for idc_da
    refNets = stationReferenceJpa.retrieveNetworksByEntityId(
        TestFixtures.net_idc_da.getEntityId());
    assertNotNull(refNets);
    assertEquals(1, refNets.size());
    assertTrue(refNets.contains(TestFixtures.net_idc_da));
    // query for networks with a bad id (that shouldn't exist)
    refNets = stationReferenceJpa.retrieveNetworksByEntityId(UNKNOWN_ID);
    assertNotNull(refNets);
    assertTrue(refNets.isEmpty());
  }

  @Test
  public void retrieveStationsTest() throws Exception {
    List<ReferenceStation> stations = stationReferenceJpa.retrieveStations();
    assertEquals(TestFixtures.jnuVersions, stations);
  }

  @Test
  public void retrieveStationsByNameTest() throws Exception {
    // assert that all three s of jnu have the same name
    assertEquals(TestFixtures.jnu_v1.getName(), TestFixtures.jnu_v2.getName());
    assertEquals(TestFixtures.jnu_v2.getName(), TestFixtures.jnu_v3.getName());
    List<ReferenceStation> stations
        = stationReferenceJpa.retrieveStationsByName(TestFixtures.jnu_v1.getName());
    assertNotNull(stations);
    assertEquals(TestFixtures.jnuVersions.size(), stations.size());
    assertEquals(TestFixtures.jnuVersions, stations);
    // query for stations with with a bad name (that shouldn't exist)
    stations = stationReferenceJpa.retrieveStationsByName(UNKNOWN_NAME);
    assertNotNull(stations);
    assertTrue(stations.isEmpty());
  }

  @Test
  public void retrieveStationsByEntityIdTest() throws Exception {
    // assert that all three s of jnu have the same entity id
    assertEquals(TestFixtures.jnu_v1.getEntityId(), TestFixtures.jnu_v2.getEntityId());
    assertEquals(TestFixtures.jnu_v2.getEntityId(), TestFixtures.jnu_v3.getEntityId());
    List<ReferenceStation> stations = stationReferenceJpa.retrieveStationsByEntityId(
        TestFixtures.jnu_v1.getEntityId());
    assertNotNull(stations);
    assertEquals(TestFixtures.jnuVersions.size(), stations.size());
    assertEquals(TestFixtures.jnuVersions, stations);
    // query for stations with with a bad ID (that shouldn't exist)
    stations = stationReferenceJpa.retrieveStationsByEntityId(UNKNOWN_ID);
    assertNotNull(stations);
    assertTrue(stations.isEmpty());
  }

  @Test
  public void retrieveSitesTest() throws Exception {
    List<ReferenceSite> sites = stationReferenceJpa.retrieveSites();
    assertEquals(TestFixtures.jnuSiteVersions, sites);
  }

  @Test
  public void retrieveSitesByNameTest() throws Exception {
    // assert that all three s of jnu have the same name
    assertEquals(TestFixtures.jnu_site_v1.getName(), TestFixtures.jnu_site_v2.getName());
    assertEquals(TestFixtures.jnu_site_v2.getName(), TestFixtures.jnu_site_v3.getName());
    List<ReferenceSite> sites = stationReferenceJpa.retrieveSitesByName(
        TestFixtures.jnu_site_v1.getName());
    assertNotNull(sites);
    assertEquals(TestFixtures.jnuSiteVersions.size(), sites.size());
    assertEquals(TestFixtures.jnuSiteVersions, sites);
    // query for sites with with a bad name (that shouldn't exist)
    sites = stationReferenceJpa.retrieveSitesByName(UNKNOWN_NAME);
    assertNotNull(sites);
    assertTrue(sites.isEmpty());
  }

  @Test
  public void retrieveSitesByEntityIdTest() throws Exception {
    // assert that all three s of jnu have the same entity id
    assertEquals(TestFixtures.jnu_site_v1.getEntityId(), TestFixtures.jnu_site_v2.getEntityId());
    assertEquals(TestFixtures.jnu_site_v2.getEntityId(), TestFixtures.jnu_site_v3.getEntityId());
    List<ReferenceSite> sites = stationReferenceJpa.retrieveSitesByEntityId(
        TestFixtures.jnu_site_v1.getEntityId());
    assertNotNull(sites);
    assertEquals(TestFixtures.jnuSiteVersions.size(), sites.size());
    assertEquals(TestFixtures.jnuSiteVersions, sites);
    // query for sites with with a bad id (that shouldn't exist)
    sites = stationReferenceJpa.retrieveSitesByEntityId(UNKNOWN_ID);
    assertNotNull(sites);
    assertTrue(sites.isEmpty());
  }

  @Test
  public void retrieveChannelsTest() throws Exception {
    List<ReferenceChannel> channels = stationReferenceJpa.retrieveChannels();
    assertEquals(TestFixtures.allChannels, channels);
  }

  @Test
  public void retrieveChannelsByEntityIdTest() throws Exception {
    // assert that all three s of the channel have the same entity id
    assertEquals(TestFixtures.chan_jnu_bhe_v1.getEntityId(),
        TestFixtures.chan_jnu_bhe_v2.getEntityId());
    assertEquals(TestFixtures.chan_jnu_bhe_v2.getEntityId(),
        TestFixtures.chan_jnu_bhe_v3.getEntityId());
    List<ReferenceChannel> channels = stationReferenceJpa.retrieveChannelsByEntityId(
        TestFixtures.chan_jnu_bhe_v1.getEntityId());
    List<ReferenceChannel> expecteds = List.of(
        TestFixtures.chan_jnu_bhe_v1, TestFixtures.chan_jnu_bhe_v2, TestFixtures.chan_jnu_bhe_v3);
    assertNotNull(channels);
    assertEquals(expecteds.size(), channels.size());
    assertEquals(expecteds, channels);
    // query for channels with with a bad id (that shouldn't exist)
    channels = stationReferenceJpa.retrieveChannelsByEntityId(UNKNOWN_ID);
    assertNotNull(channels);
    assertTrue(channels.isEmpty());
  }


  @Test
  public void retrieveChannelsByVersionIdsTest() throws Exception {
    // assert that all three of the channels have different version ids
    assertNotEquals(TestFixtures.chan_jnu_bhe_v1.getVersionId(),
        TestFixtures.chan_jnu_bhe_v2.getVersionId());
    assertNotEquals(TestFixtures.chan_jnu_bhe_v2.getVersionId(),
        TestFixtures.chan_jnu_bhe_v3.getVersionId());
    assertNotEquals(TestFixtures.chan_jnu_bhe_v1.getVersionId(),
        TestFixtures.chan_jnu_bhe_v3.getVersionId());

    // we should retrieve the 3 unique channels when querying by their version ids
    List<ReferenceChannel> channels = stationReferenceJpa.retrieveChannelsByVersionIds(
        List.of(TestFixtures.chan_jnu_bhe_v1.getVersionId(),
            TestFixtures.chan_jnu_bhe_v2.getVersionId(),
            TestFixtures.chan_jnu_bhe_v3.getVersionId()));
    List<ReferenceChannel> expecteds = List.of(
        TestFixtures.chan_jnu_bhe_v1, TestFixtures.chan_jnu_bhe_v2, TestFixtures.chan_jnu_bhe_v3);
    assertNotNull(channels);
    assertEquals(expecteds.size(), channels.size());
    assertTrue(channels.containsAll(expecteds));
  }


  @Test
  public void retrieveStationsByVersionIdsTest() throws Exception {
    // assert that all three of the stations have different version ids
    assertNotEquals(TestFixtures.jnu_v1.getVersionId(), TestFixtures.jnu_v2.getVersionId());
    assertNotEquals(TestFixtures.jnu_v2.getVersionId(), TestFixtures.jnu_v3.getVersionId());
    assertNotEquals(TestFixtures.jnu_v2.getVersionId(), TestFixtures.jnu_v3.getVersionId());

    // we should retrieve the 3 unique stations when querying by their version ids
    List<ReferenceStation> stations = stationReferenceJpa.retrieveStationsByVersionIds(
        List.of(TestFixtures.jnu_v1.getVersionId(), TestFixtures.jnu_v2.getVersionId(),
            TestFixtures.jnu_v3.getVersionId()));
    List<ReferenceStation> expecteds = List.of(
        TestFixtures.jnu_v1, TestFixtures.jnu_v2, TestFixtures.jnu_v3);
    assertNotNull(stations);
    assertEquals(expecteds.size(), stations.size());
    assertTrue(stations.containsAll(expecteds));
  }

  @Test
  public void retrieveChannelByNameTest() throws Exception {
    List<ReferenceChannel> bhe_channels = stationReferenceJpa.retrieveChannelsByName(
        TestFixtures.chan_jnu_bhe_v1.getName());
    List<ReferenceChannel> expected = List.of(TestFixtures.chan_jnu_bhe_v1,
        TestFixtures.chan_jnu_bhe_v2, TestFixtures.chan_jnu_bhe_v3);
    assertEquals(expected, bhe_channels);
  }

  @Test
  public void retrieveDigitizersTest() throws Exception {
    List<ReferenceDigitizer> digitizers = stationReferenceJpa.retrieveDigitizers();
    assertEquals(TestFixtures.allDigitizers, digitizers);
  }

  @Test
  public void retrieveDigitizersByEntityIdTest() throws Exception {
    List<ReferenceDigitizer> digitizers = stationReferenceJpa.retrieveDigitizersByEntityId(
        TestFixtures.jnu_digitizer_v1.getEntityId());
    List<ReferenceDigitizer> expected = List.of(TestFixtures.jnu_digitizer_v1);
    assertNotNull(digitizers);
    assertEquals(expected.size(), digitizers.size());
    assertEquals(expected, digitizers);
    // query for digitizers with with a bad name (that shouldn't exist)
    digitizers = stationReferenceJpa.retrieveDigitizersByEntityId(UNKNOWN_ID);
    assertNotNull(digitizers);
    assertTrue(digitizers.isEmpty());
  }

  @Test
  public void retrieveDigitizersByNameTest() throws Exception {
    // assert that all three digitizer have the same name
    assertEquals(TestFixtures.jnu_digitizer_v1.getName(),
        TestFixtures.jnu_digitizer_v2.getName());
    assertEquals(TestFixtures.jnu_digitizer_v2.getName(),
        TestFixtures.jnu_digitizer_v3.getName());
    List<ReferenceDigitizer> digitizers
        = stationReferenceJpa.retrieveDigitizersByName(
        TestFixtures.jnu_digitizer_v1.getName());
    assertNotNull(digitizers);
    assertEquals(TestFixtures.allDigitizers.size(), digitizers.size());
    assertEquals(TestFixtures.allDigitizers, digitizers);
    // query for stations with with a bad name (that shouldn't exist)
    digitizers = stationReferenceJpa.retrieveDigitizersByName(UNKNOWN_NAME);
    assertNotNull(digitizers);
    assertTrue(digitizers.isEmpty());
  }

  @Test
  public void retrieveCalibrationsTest() throws Exception {
    List<ReferenceCalibration> calibrations
        = stationReferenceJpa.retrieveCalibrations();
    assertNotNull(calibrations);
    assertEquals(new HashSet<>(TestFixtures.allCalibrations),
        new HashSet<>(calibrations));
  }

  @Test
  public void retrieveCalibrationsByChannelIdTest() throws Exception {
    List<ReferenceCalibration> calibrations
        = stationReferenceJpa.retrieveCalibrationsByChannelId(
        TestFixtures.chan_jnu_bhe_v1.getEntityId());
    assertNotNull(calibrations);
    assertEquals(List.of(
        TestFixtures.calibration_bhe_v1, TestFixtures.calibration_bhe_v2,
        TestFixtures.calibration_bhe_v3),
        calibrations);
    calibrations = stationReferenceJpa.retrieveCalibrationsByChannelId(UNKNOWN_ID);
    assertNotNull(calibrations);
    assertTrue(calibrations.isEmpty());
  }

  @Test
  public void retrieveResponsesTest() throws Exception {
    List<ReferenceResponse> responses
        = stationReferenceJpa.retrieveResponses();
    assertNotNull(responses);
    assertEquals(new HashSet<>(TestFixtures.allResponses),
        new HashSet<>(responses));
  }

  @Test
  public void retrieveResponsesByChannelIdTest() throws Exception {
    List<ReferenceResponse> responses
        = stationReferenceJpa.retrieveResponsesByChannelId(
        TestFixtures.chan_jnu_bhe_v1.getEntityId());
    assertNotNull(responses);
    assertEquals(List.of(TestFixtures.response_bhe_v1, TestFixtures.response_bhe_v2,
        TestFixtures.response_bhe_v3),
        responses);
    responses = stationReferenceJpa.retrieveResponsesByChannelId(UNKNOWN_ID);
    assertNotNull(responses);
    assertTrue(responses.isEmpty());
  }

  @Test
  public void retrieveSensorsTest() throws Exception {
    List<ReferenceSensor> sensors
        = stationReferenceJpa.retrieveSensors();
    assertNotNull(sensors);
    assertEquals(new HashSet<>(TestFixtures.allSensors),
        new HashSet<>(sensors));
  }

  @Test
  public void retrieveSensorsByChannelIdTest() throws Exception {
    List<ReferenceSensor> sensors
        = stationReferenceJpa.retrieveSensorsByChannelId(
        TestFixtures.chan_jnu_bhe_v1.getEntityId());
    assertNotNull(sensors);
    assertEquals(List.of(TestFixtures.sensor_bhe_v1, TestFixtures.sensor_bhe_v2,
        TestFixtures.sensor_bhe_v3),
        sensors);
    sensors = stationReferenceJpa.retrieveSensorsByChannelId(UNKNOWN_ID);
    assertNotNull(sensors);
    assertTrue(sensors.isEmpty());
  }

  @Test
  public void retrieveNetworkMembershipsTest() throws Exception {
    List<ReferenceNetworkMembership> memberships
        = stationReferenceJpa.retrieveNetworkMemberships();
    assertEquals(TestFixtures.networkMemberships, new HashSet<>(memberships));
  }

  @Test
  public void retrieveNetworkMembershipsByNetworkIdTest() throws Exception {
    UUID netId = TestFixtures.net_ims_aux.getEntityId();
    List<ReferenceNetworkMembership> memberships
        = stationReferenceJpa.retrieveNetworkMembershipsByNetworkId(netId);
    Set<ReferenceNetworkMembership> expectedMemberships = TestFixtures.networkMemberships
        .stream()
        .filter(m -> m.getNetworkId().equals(netId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID, expect no results
    memberships = stationReferenceJpa.retrieveNetworkMembershipsByNetworkId(UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveNetworkMembershipsByStationIdTest() throws Exception {
    UUID stationId = TestFixtures.jnu_v1.getEntityId();
    List<ReferenceNetworkMembership> memberships
        = stationReferenceJpa.retrieveNetworkMembershipsByStationId(stationId);
    Set<ReferenceNetworkMembership> expectedMemberships = TestFixtures.networkMemberships
        .stream()
        .filter(m -> m.getStationId().equals(stationId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID, expect no results
    memberships = stationReferenceJpa.retrieveNetworkMembershipsByStationId(UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveNetworkMembershipsByNetworkAndStationIdTest() throws Exception {
    UUID netId = TestFixtures.net_ims_aux.getEntityId();
    UUID stationId = TestFixtures.jnu_v1.getEntityId();
    List<ReferenceNetworkMembership> memberships
        = stationReferenceJpa.retrieveNetworkMembershipsByNetworkAndStationId(
        netId, stationId);
    Set<ReferenceNetworkMembership> expectedMemberships = TestFixtures.networkMemberships
        .stream()
        .filter(m -> m.getNetworkId().equals(netId))
        .filter(m -> m.getStationId().equals(stationId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID's, expect no results
    memberships = stationReferenceJpa
        .retrieveNetworkMembershipsByNetworkAndStationId(UNKNOWN_ID, UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
    memberships = stationReferenceJpa
        .retrieveNetworkMembershipsByNetworkAndStationId(netId, UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
    memberships = stationReferenceJpa
        .retrieveNetworkMembershipsByNetworkAndStationId(UNKNOWN_ID, stationId);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveStationMembershipsTest() throws Exception {
    List<ReferenceStationMembership> memberships
        = stationReferenceJpa.retrieveStationMemberships();
    assertEquals(TestFixtures.stationMemberships, new HashSet<>(memberships));
  }

  @Test
  public void retrieveStationMembershipsByStationIdTest() throws Exception {
    UUID staId = TestFixtures.jnu_v1.getEntityId();
    List<ReferenceStationMembership> memberships
        = stationReferenceJpa.retrieveStationMembershipsByStationId(staId);
    Set<ReferenceStationMembership> expectedMemberships = TestFixtures.stationMemberships
        .stream()
        .filter(m -> m.getStationId().equals(staId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID, expect no results
    memberships = stationReferenceJpa.retrieveStationMembershipsByStationId(UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveStationMembershipsBySiteIdTest() throws Exception {
    UUID siteId = TestFixtures.jnu_site_v1.getEntityId();
    List<ReferenceStationMembership> memberships
        = stationReferenceJpa.retrieveStationMembershipsBySiteId(siteId);
    Set<ReferenceStationMembership> expectedMemberships = TestFixtures.stationMemberships
        .stream()
        .filter(m -> m.getSiteId().equals(siteId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID, expect no results
    memberships = stationReferenceJpa.retrieveStationMembershipsBySiteId(UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveStationMembershipsByStationAndSiteIdTest() throws Exception {
    UUID stationId = TestFixtures.jnu_v1.getEntityId();
    UUID siteId = TestFixtures.jnu_site_v1.getEntityId();
    List<ReferenceStationMembership> memberships
        = stationReferenceJpa.retrieveStationMembershipsByStationAndSiteId(stationId, siteId);
    Set<ReferenceStationMembership> expectedMemberships = TestFixtures.stationMemberships
        .stream()
        .filter(m -> m.getStationId().equals(stationId))
        .filter(m -> m.getSiteId().equals(siteId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID's, expect no results
    memberships = stationReferenceJpa
        .retrieveStationMembershipsByStationAndSiteId(UNKNOWN_ID, UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
    memberships = stationReferenceJpa
        .retrieveStationMembershipsByStationAndSiteId(stationId, UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
    memberships = stationReferenceJpa
        .retrieveStationMembershipsByStationAndSiteId(UNKNOWN_ID, siteId);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveSiteMembershipsTest() throws Exception {
    List<ReferenceSiteMembership> memberships
        = stationReferenceJpa.retrieveSiteMemberships();
    assertEquals(TestFixtures.siteMemberships, new HashSet<>(memberships));
  }

  @Test
  public void retrieveSiteMembershipsBySiteIdTest() throws Exception {
    UUID siteId = TestFixtures.jnu_site_v1.getEntityId();
    List<ReferenceSiteMembership> memberships
        = stationReferenceJpa.retrieveSiteMembershipsBySiteId(siteId);
    Set<ReferenceSiteMembership> expectedMemberships = TestFixtures.siteMemberships
        .stream()
        .filter(m -> m.getSiteId().equals(siteId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID, expect no results
    memberships = stationReferenceJpa.retrieveSiteMembershipsBySiteId(UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveSiteMembershipsByChannelIdTest() throws Exception {
    UUID channelId = TestFixtures.chan_jnu_bhe_v1.getEntityId();
    List<ReferenceSiteMembership> memberships
        = stationReferenceJpa.retrieveSiteMembershipsByChannelId(channelId);
    Set<ReferenceSiteMembership> expectedMemberships = TestFixtures.siteMemberships
        .stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID, expect no results
    memberships = stationReferenceJpa.retrieveSiteMembershipsByChannelId(UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveSiteMembershipsBySiteAndChannelIdTest() throws Exception {
    UUID siteId = TestFixtures.jnu_site_v1.getEntityId();
    UUID channelId = TestFixtures.chan_jnu_bhe_v1.getEntityId();
    List<ReferenceSiteMembership> memberships
        = stationReferenceJpa.retrieveSiteMembershipsBySiteAndChannelId(siteId, channelId);
    Set<ReferenceSiteMembership> expectedMemberships = TestFixtures.siteMemberships
        .stream()
        .filter(m -> m.getSiteId().equals(siteId))
        .filter(m -> m.getChannelId().equals(channelId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID's, expect no results
    memberships = stationReferenceJpa
        .retrieveSiteMembershipsBySiteAndChannelId(UNKNOWN_ID, UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
    memberships = stationReferenceJpa.retrieveSiteMembershipsBySiteAndChannelId(siteId, UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
    memberships = stationReferenceJpa
        .retrieveSiteMembershipsBySiteAndChannelId(UNKNOWN_ID, channelId);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveDigitizerMembershipsTest() throws Exception {
    List<ReferenceDigitizerMembership> memberships
        = stationReferenceJpa.retrieveDigitizerMemberships();
    assertEquals(TestFixtures.digitizerMemberships, new HashSet<>(memberships));
  }

  @Test
  public void retrieveDigitizerMembershipsByDigitizerIdTest() throws Exception {
    UUID digitizerId = TestFixtures.jnu_digitizer_v1.getEntityId();
    List<ReferenceDigitizerMembership> memberships
        = stationReferenceJpa.retrieveDigitizerMembershipsByDigitizerId(digitizerId);
    Set<ReferenceDigitizerMembership> expectedMemberships = TestFixtures.digitizerMemberships
        .stream()
        .filter(m -> m.getDigitizerId().equals(digitizerId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID, expect no results
    memberships = stationReferenceJpa.retrieveDigitizerMembershipsByDigitizerId(UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveDigitizerMembershipsByChannelIdTest() throws Exception {
    UUID channelId = TestFixtures.chan_jnu_bhe_v1.getEntityId();
    List<ReferenceDigitizerMembership> memberships
        = stationReferenceJpa.retrieveDigitizerMembershipsByChannelId(channelId);
    Set<ReferenceDigitizerMembership> expectedMemberships = TestFixtures.digitizerMemberships
        .stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID, expect no results
    memberships = stationReferenceJpa.retrieveDigitizerMembershipsByChannelId(UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

  @Test
  public void retrieveDigitizerMembershipsByDigitizerAndChannelIdTest() throws Exception {
    UUID digitizerId = TestFixtures.jnu_digitizer_v1.getEntityId();
    UUID channelId = TestFixtures.chan_jnu_bhe_v1.getEntityId();
    List<ReferenceDigitizerMembership> memberships
        = stationReferenceJpa.retrieveDigitizerMembershipsByDigitizerAndChannelId(
        digitizerId, channelId);
    Set<ReferenceDigitizerMembership> expectedMemberships = TestFixtures.digitizerMemberships
        .stream()
        .filter(m -> m.getDigitizerId().equals(digitizerId))
        .filter(m -> m.getChannelId().equals(channelId))
        .collect(Collectors.toSet());
    assertEquals(expectedMemberships, new HashSet<>(memberships));
    // query for bad ID's, expect no results
    memberships = stationReferenceJpa
        .retrieveDigitizerMembershipsByDigitizerAndChannelId(UNKNOWN_ID, UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
    memberships = stationReferenceJpa
        .retrieveDigitizerMembershipsByDigitizerAndChannelId(digitizerId, UNKNOWN_ID);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
    memberships = stationReferenceJpa
        .retrieveDigitizerMembershipsByDigitizerAndChannelId(UNKNOWN_ID, channelId);
    assertNotNull(memberships);
    assertTrue(memberships.isEmpty());
  }

}
