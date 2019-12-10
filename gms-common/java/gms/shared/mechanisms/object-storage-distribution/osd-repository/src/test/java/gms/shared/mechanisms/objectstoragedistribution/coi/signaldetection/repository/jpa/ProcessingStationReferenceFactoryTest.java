package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ProcessingStationReferenceFactoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import java.util.List;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Perform unit tests on classes related to the acquisition of channel information.
 */
public class ProcessingStationReferenceFactoryTest {

  private static ProcessingStationReferenceFactoryInterface stationRefFactory;
  // we don't expect any site or channel to be named this
  private static String BAD_NAME = "ZZZ999-000";


  @BeforeClass
  public static void initialize() throws Exception {
    StationReferenceRepositoryInterface referenceRepository
        = mock(StationReferenceRepositoryInterface.class);
    stationRefFactory = new ProcessingStationReferenceFactory(referenceRepository);

    // below: set Mockito.when on every repository operation
    ///////////////////////////////////////////////////////////////////////////
    // set Mockito response on retrieve channel by ID
    when(referenceRepository.retrieveChannelsByEntityId(TestFixtures.refChannel.getEntityId()))
        .thenReturn(List.of(TestFixtures.refChannel));
    // set Mockito response on retrieve response by channel ID
    when(
        referenceRepository.retrieveResponsesByChannelId(TestFixtures.refChannel.getVersionId()))
        .thenReturn(List.of(TestFixtures.refResponse));
    // set Mockito response on retrieve Calibration by channel ID
    when(referenceRepository.retrieveCalibrationsByChannelId(TestFixtures.refChannel.getVersionId()))
        .thenReturn(List.of(TestFixtures.refCalibration));
    // set Mockito response on retrieve digitizer by ID
    when(referenceRepository.retrieveDigitizersByEntityId(TestFixtures.DIGITIZER_ID))
        .thenReturn(List.of(TestFixtures.refDigitizer));
    // set Mockito response on retrieve slimNetwork by ID
    when(referenceRepository.retrieveNetworksByEntityId(TestFixtures.refNetwork.getEntityId()))
        .thenReturn(List.of(TestFixtures.refNetwork));
    // set Mockito response on retrieve slimNetwork by name
    when(referenceRepository.retrieveNetworksByName(TestFixtures.refNetwork.getName()))
        .thenReturn(List.of(TestFixtures.refNetwork));
    // set Mockito response on retrieve slimSite by id
    when(referenceRepository.retrieveSitesByEntityId(TestFixtures.refSite.getEntityId()))
        .thenReturn(List.of(TestFixtures.refSite));
    // set Mockito response on retrieve slimSite by name
    when(referenceRepository.retrieveSitesByName(TestFixtures.refSite.getName()))
        .thenReturn(List.of(TestFixtures.refSite));
    // set Mockito response on retrieve slimStation by id
    when(referenceRepository.retrieveStationsByEntityId(TestFixtures.refStation.getEntityId()))
        .thenReturn(List.of(TestFixtures.refStation));
    // set Mockito response on retrieve slimStation by name
    when(referenceRepository.retrieveStationsByName(TestFixtures.refStation.getName()))
        .thenReturn(List.of(TestFixtures.refStation));
    // set Mockito response on retrieve network memberships by network id
    when(referenceRepository.retrieveNetworkMembershipsByNetworkId(
            TestFixtures.refNetwork.getEntityId()))
        .thenReturn(List.of(TestFixtures.networkMembership));
    // set Mockito response on retrieve station memberships by station id
    when(referenceRepository.retrieveStationMembershipsByStationId(
            TestFixtures.refStation.getEntityId()))
        .thenReturn(List.of(TestFixtures.stationMembership));
    // set Mockito response on retrieve site memberships by site id
    when(referenceRepository.retrieveSiteMembershipsBySiteId(
            TestFixtures.refSite.getEntityId()))
        .thenReturn(List.of(TestFixtures.siteMembership));

  }

  @Test
  public void testNetworkFromNameSlim() {
    Optional<Network> network = stationRefFactory.networkFromName(
        TestFixtures.networkName, true);
    assertNotNull(network);
    assertTrue(network.isPresent());
    assertEquals(TestFixtures.slimNetwork, network.get());
  }

  @Test
  public void testNetworkFromNameFat() {
    Optional<Network> network = stationRefFactory.networkFromName(
        TestFixtures.networkName, false);
    assertNotNull(network);
    assertTrue(network.isPresent());
    assertEquals(TestFixtures.fatNetwork, network.get());
  }

  @Test
  public void testNetworkFromBadNameSlim() {
    Optional<Network> network = stationRefFactory.networkFromName(
        BAD_NAME, true);
    assertNotNull(network);
    assertFalse(network.isPresent());
  }

  @Test
  public void testNetworkFromBadNameFat() {
    Optional<Network> network = stationRefFactory.networkFromName(
        BAD_NAME, false);
    assertNotNull(network);
    assertFalse(network.isPresent());
  }

  @Test
  public void testNetworkFromNullNameSlim() {
    Optional<Network> network = stationRefFactory.networkFromName(
        null, true);
    assertNotNull(network);
    assertFalse(network.isPresent());
  }

  @Test
  public void testNetworkFromNullNameFat() {
    Optional<Network> network = stationRefFactory.networkFromName(
        null, false);
    assertNotNull(network);
    assertFalse(network.isPresent());
  }

  @Test
  public void testSiteFromBadNameSlim() {
    Optional<Site> site = stationRefFactory.siteFromName(
        BAD_NAME, true);
    assertNotNull(site);
    assertFalse(site.isPresent());
  }

  @Test
  public void testSiteFromBadNameFat() {
    Optional<Site> site = stationRefFactory.siteFromName(
        BAD_NAME, false);
    assertNotNull(site);
    assertFalse(site.isPresent());
  }

  @Test
  public void testSiteFromNameSlim() {
    Optional<Site> site = stationRefFactory.siteFromName(
        TestFixtures.siteName, true);
    assertNotNull(site);
    assertTrue(site.isPresent());
    assertEquals(TestFixtures.slimSite, site.get());
  }

  @Test
  public void testSiteFromNameFat() {
    Optional<Site> site = stationRefFactory.siteFromName(
        TestFixtures.siteName, false);
    assertNotNull(site);
    assertTrue(site.isPresent());
    assertEquals(TestFixtures.fatSite, site.get());
  }

  @Test
  public void testStationFromNameSlim() {
    Optional<Station> sta = stationRefFactory.stationFromName(
        TestFixtures.stationName, true);
    assertNotNull(sta);
    assertTrue(sta.isPresent());
    assertEquals(TestFixtures.slimStation, sta.get());
  }

  @Test
  public void testStationFromNameFat() {
    Optional<Station> sta = stationRefFactory.stationFromName(
        TestFixtures.stationName, false);
    assertNotNull(sta);
    assertTrue(sta.isPresent());
    assertEquals(TestFixtures.fatStation, sta.get());
  }

  @Test
  public void testStationFromBadNameSlim() {
    Optional<Station> sta = stationRefFactory.stationFromName(
        BAD_NAME, true);
    assertNotNull(sta);
    assertFalse(sta.isPresent());
  }

  @Test
  public void testStationFromBadNameFat() {
    Optional<Station> sta = stationRefFactory.stationFromName(
        BAD_NAME, false);
    assertNotNull(sta);
    assertFalse(sta.isPresent());
  }

  @Test
  public void testStationFromNullNameSlim() {
    Optional<Station> sta = stationRefFactory.stationFromName(
        null, true);
    assertNotNull(sta);
    assertFalse(sta.isPresent());
  }

  @Test
  public void testStationFromNullNameFat() {
    Optional<Station> sta = stationRefFactory.stationFromName(
        null, false);
    assertNotNull(sta);
    assertFalse(sta.isPresent());
  }
}
