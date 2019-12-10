package gms.dataacquisition.css.stationrefconverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.dataacquisition.cssreader.utilities.Utility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;

public class CssReferenceReaderTest {

  private static CssReferenceReader reader;

  @BeforeClass
  public static void setup() throws Exception {
    final String baseName = "referencefiles/test_config.";
    final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    reader = new CssReferenceReader(
        classLoader.getResource(baseName + "affiliation").getPath(),
        classLoader.getResource(baseName + "instrument").getPath(),
        classLoader.getResource(baseName + "network").getPath(),
        classLoader.getResource(baseName + "sensor").getPath(),
        classLoader.getResource(baseName + "site").getPath(),
        classLoader.getResource(baseName + "sitechan").getPath());
  }

  @Test
  public void testExpectedNetworks() {
    // assert there are X networks present.
    final Collection<ReferenceNetwork> networks = reader.getReferenceNetworks();
    assertNotNull(networks);
    // assert that the list has only unique elements
    assertEquals(networks.size(), new HashSet<>(networks).size());
    assertEquals(5, networks.size());
    // assert expected properties of networks
    assertAllElementsMatched(networks, List.of(
        networkMatcher("test", "network of all stations in the test network"),
        networkMatcher("IMS_PRI", "IMS Primary Seismic Stations"),
        networkMatcher("IMS_AUX", "IMS Auxiliary Seismic Stations"),
        networkMatcher("IMS_HYD", "IMS Hydroacoustic Stations"),
        networkMatcher("IMS_INF", "IMS Infrasound Stations")));
  }

  @Test
  public void testExpectedStations() {
    // assert there are X stations present.
    final Collection<ReferenceStation> stations = reader.getReferenceStations();
    assertNotNull(stations);
    // assert that the list has only unique elements
    assertEquals(stations.size(), new HashSet<>(stations).size());
    assertEquals(7, stations.size());

    // assert that some particular stations are present - one each of the different kinds
    final List<Consumer<Collection<ReferenceStation>>> stationMatchers = List.of(
        // mkar
        stationMatcher(
            "MKAR", "Makanchi_Array,_Kazakhstan,_IMS",
            46.79368, 82.29057 , 0.618, Utility.jdToInstant("2000092")),
        // kdak
        stationMatcher(
            "KDAK", "Kodiak_Island,_Alaska_USA",
            57.78280, -152.58350 , 0.152, Utility.jdToInstant("1997160")),
        // H06E
        stationMatcher(
            "H06E", "Socorro,_Mexico",
            18.7805, -110.9253, 0.3160, Utility.jdToInstant("2005333")),
        // I51GB
        stationMatcher("I51GB", "Bermuda,_United_Kingdom",
            32.36154, -64.69874, -0.035, Utility.jdToInstant("2008315")));
    assertAllElementsMatched(stations, stationMatchers);
  }

  @Test
  public void testExpectedSites() {
    // assert there are X sites present.
    final Collection<ReferenceSite> sites = reader.getReferenceSites();
    assertNotNull(sites);
    // assert that the list has only unique elements
    assertEquals(sites.size(), new HashSet<>(sites).size());
    assertEquals(25, sites.size());
    final Location mkarLocation = Location.from(46.79368, 82.29057, 0, 0.618);
    final List<Consumer<Collection<ReferenceSite>>> siteMatchers = List.of(
        // MK01
        siteMatcher("MK01", "Makanchi_Array,_Kazakhstan,_IMS",
            46.76897, 82.30066, 0.608, Utility.jdToInstant("2000092"),
            RelativePosition.from(-2.749, 0.771, 0)),
        // MK32
        siteMatcher("MK32", "Makanchi_Array,_Kazakhstan,_IMS",
            46.79369, 82.29060, 0.617, Utility.jdToInstant("2000254"),
            RelativePosition.from(0.000, 0.002, 0)),
        // KDAK
        siteMatcher("KDAK", "Kodiak_Island,_Alaska_USA",
            57.78280, -152.58350 , 0.152, Utility.jdToInstant("1997160"),
            RelativePosition.from(0, 0, 0)));
    assertAllElementsMatched(sites, siteMatchers);
  }

  @Test
  public void testExpectedChannels() {
    // assert there are X channels present.
    final Collection<ReferenceChannel> channels = reader.getReferenceChannels();
    assertNotNull(channels);
    // assert that the list has only unique elements
    assertEquals(channels.size(), new HashSet<>(channels).size());
    final double mk01Lat = 46.76897, mk01Lon = 82.30066, mk01Elev = 0.608;
    final double kdakLat = 57.78280, kdakLon = -152.58350, kdakElev = 0.152;
    assertEquals(65, channels.size());
    final List<Consumer<Collection<ReferenceChannel>>> channelMatchers = List.of(
        // MK01/sz
        channelMatcher("MK01/sz", mk01Lat, mk01Lon, mk01Elev, 0.029,
            -1, 0, 40, Utility.jdToInstant("2000092")),
        // MK01/SHZ
        channelMatcher("MK01/SHZ", mk01Lat, mk01Lon, mk01Elev, 0.03,
            -1, 0, 40, Utility.jdToInstant("2004289")),
        // KDAK/BH1
        channelMatcher("KDAK/BH1/00", "00", kdakLat, kdakLon, kdakElev, 0.088,
            0, 90, 20, Utility.jdToInstant("2014243")),
        // KDAK/BH2
        channelMatcher("KDAK/BH2", kdakLat, kdakLon, kdakElev, 0.088,
            90, 90, 20, Utility.jdToInstant("2014243")));
    assertAllElementsMatched(channels, channelMatchers);
  }

  @Test
  public void testExpectedNetworkMemberships() {
    final Set<ReferenceNetworkMembership> networkMemberships = reader.getReferenceNetworkMemberships();
    assertNotNull(networkMemberships);
    assertEquals(14, networkMemberships.size());
    assertNoDuplicatesByComparator(networkMemberships,
        CssReferenceReaderTest::isNetworkMembershipDuplicate);
    final UUID testNetworkId = getNetworkEntityId("test");
    final UUID imsAuxNetworkId = getNetworkEntityId("IMS_AUX");
    final UUID mkarStationId = getStationEntityId("MKAR");
    final UUID kdakStationId = getStationEntityId("KDAK");
    final List<Consumer<Collection<ReferenceNetworkMembership>>> membershipMatchers = List.of(
        // network 'test' to station 'MKAR'
        networkMembershipMatcher(testNetworkId, mkarStationId, Instant.ofEpochSecond(954547200)),
        // network 'IMS_AUX' to station 'KDAK'
        networkMembershipMatcher(imsAuxNetworkId, kdakStationId, Instant.ofEpochSecond(865814400))
    );
    assertAllElementsMatched(networkMemberships, membershipMatchers);
  }

  @Test
  public void testExpectedStationMemberships() {
    final Set<ReferenceStationMembership> stationMemberships = reader.getReferenceStationMemberships();
    assertNotNull(stationMemberships);
    assertEquals(26, stationMemberships.size());
    assertNoDuplicatesByComparator(stationMemberships,
        CssReferenceReaderTest::isStationMembershipDuplicate);
    final UUID mkarStationId = getStationEntityId("MKAR");
    final UUID kdakStationId = getStationEntityId("KDAK");
    final UUID mk01SiteId = getSiteEntityId("MK01");
    final UUID mk32SiteId = getSiteEntityId("MK32");
    final UUID kdakSiteId = getSiteEntityId("KDAK");
    final List<Consumer<Collection<ReferenceStationMembership>>> membershipMatchers = List.of(
      stationMembershipMatcher(mkarStationId, mk01SiteId, StatusType.ACTIVE, Utility.jdToInstant("2000092")),
      stationMembershipMatcher(mkarStationId, mk32SiteId, StatusType.ACTIVE, Utility.jdToInstant("2000254")),
      stationMembershipMatcher(kdakStationId, kdakSiteId, StatusType.ACTIVE, Utility.jdToInstant("1997160"))
    );
    assertAllElementsMatched(stationMemberships, membershipMatchers);
  }

  @Test
  public void testExpectedSiteMemberships() {
    final Set<ReferenceSiteMembership> siteMemberships = reader.getReferenceSiteMemberships();
    assertNotNull(siteMemberships);
    // TODO: correct with input from SME's on right number
    assertEquals(91, siteMemberships.size());
    assertNoDuplicatesByComparator(siteMemberships,
        CssReferenceReaderTest::isSiteMembershipDuplicate);
    final UUID mk01SiteId = getSiteEntityId("MK01");
    final UUID kdakSiteId = getSiteEntityId("KDAK");
    final UUID mk01_sz_channelId = getChannelEntityId("MK01/sz");
    final UUID mk01_SHZ_channelId = getChannelEntityId("MK01/SHZ");
    final UUID kdak_BH1_channelId = getChannelEntityId("KDAK/BH1/00");
    final UUID kdak_sz_channelId = getChannelEntityId("KDAK/sz");
    final List<Consumer<Collection<ReferenceSiteMembership>>> membershipMatchers = List.of(
        // MK01 associated to channel sz
        siteMembershipMatcher(mk01SiteId, mk01_sz_channelId, StatusType.ACTIVE, Utility.jdToInstant("2000092")),
        // MK01 unassociated to channel sz
        siteMembershipMatcher(mk01SiteId, mk01_sz_channelId, StatusType.INACTIVE, Utility.jdToInstant("2004288")),
        // MK01 associated to channel SHZ
        siteMembershipMatcher(mk01SiteId, mk01_SHZ_channelId, StatusType.ACTIVE, Utility.jdToInstant("2004289")),
        // kdak associated to channel sz
        siteMembershipMatcher(kdakSiteId, kdak_sz_channelId, StatusType.ACTIVE, Utility.jdToInstant("1997160")),
        // kdak unassociated to channel sz
        siteMembershipMatcher(kdakSiteId, kdak_sz_channelId, StatusType.INACTIVE, Utility.jdToInstant("2003013")),
        // kdak associated to channel BH1
        siteMembershipMatcher(kdakSiteId, kdak_BH1_channelId, StatusType.ACTIVE, Utility.jdToInstant("2014243")));
    assertAllElementsMatched(siteMemberships, membershipMatchers);
  }

  @Test
  public void testExpectedSensors() {
    final Collection<ReferenceSensor> sensors = reader.getSensors();
    assertNotNull(sensors);
    assertEquals(76, sensors.size());
    final List<Consumer<Collection<ReferenceSensor>>> sensorMatchers = List.of(
        sensorMatcher("MK01/SHZ", "Geotech_GS-21", "GS-21",
            Instant.ofEpochSecond(1101772800)),
        sensorMatcher("MK32/sz", "Geotech_GS-13", "GS-13",
            Instant.ofEpochSecond(968544000)),
        sensorMatcher("KDAK/BH1/00", "Geotech_KS-54000_Borehole_Seismometer",
            "KS5400", Instant.ofEpochSecond(1409443200))
    );
    assertAllElementsMatched(sensors, sensorMatchers);
  }

  @Test
  public void testExpectedCalibrations() {
    final Collection<ReferenceCalibration> calibrations = reader.getCalibrations();
    assertNotNull(calibrations);
    assertEquals(76, calibrations.size());
    final List<Consumer<Collection<ReferenceCalibration>>> calibrationMatchers = List.of(
        calibrationMatcher("MK01/SHZ", 0.012083, 1,
            Instant.ofEpochSecond(1101772800)),
        calibrationMatcher("MK32/sz", 0.015, 1,
            Instant.ofEpochSecond(968544000)),
        calibrationMatcher("KDAK/BH1/00", 0.086901, 1,
            Instant.ofEpochSecond(1409443200)),
        calibrationMatcher("I51H1/BDF", 0.0001, 10,
            Instant.ofEpochSecond(1226338004))
    );
    assertAllElementsMatched(calibrations, calibrationMatchers);
  }

  private static boolean isNetworkMembershipDuplicate(ReferenceNetworkMembership member1,
      ReferenceNetworkMembership member2) {

    return member1.getStationId().equals(member2.getStationId())
        && member1.getNetworkId().equals(member2.getNetworkId())
        && member1.getActualChangeTime().equals(member2.getActualChangeTime())
        && member1.getSystemChangeTime().equals(member2.getSystemChangeTime())
        && member1.getStatus().equals(member2.getStatus());
  }

  private static boolean isStationMembershipDuplicate(ReferenceStationMembership member1,
      ReferenceStationMembership member2) {

    return member1.getStationId().equals(member2.getStationId())
        && member1.getSiteId().equals(member2.getSiteId())
        && member1.getActualChangeTime().equals(member2.getActualChangeTime())
        && member1.getSystemChangeTime().equals(member2.getSystemChangeTime())
        && member1.getStatus().equals(member2.getStatus());
  }

  private static boolean isSiteMembershipDuplicate(ReferenceSiteMembership member1,
      ReferenceSiteMembership member2) {

    return member1.getChannelId().equals(member2.getChannelId())
        && member1.getSiteId().equals(member2.getSiteId())
        && member1.getActualChangeTime().equals(member2.getActualChangeTime())
        && member1.getSystemChangeTime().equals(member2.getSystemChangeTime())
        && member1.getStatus().equals(member2.getStatus());
  }

  private static <T> void assertNoDuplicatesByComparator(Collection<T> elems,
      BiPredicate<T, T> comparisonFunction) {

    for (T t : elems) {
      List<T> duplicateElems = elems.stream()
          .filter(e -> comparisonFunction.test(t, e))
          .collect(Collectors.toList());
      assertEquals(List.of(t), duplicateElems);
    }
  }

  private static <T> boolean containsElementMatchingPredicate(Collection<T> elems, Predicate<T> pred) {
    return elems.stream().anyMatch(pred);
  }

  private static <T> void assertAllElementsMatched(Collection<T> elems, Collection<Consumer<Collection<T>>> matchers) {
    matchers.forEach(m -> m.accept(elems));
  }

  private static Consumer<Collection<ReferenceNetwork>> networkMatcher(String name, String description) {
    final Instant expectedTime = Instant.EPOCH;
    final NetworkOrganization monitoringOrg = NetworkOrganization.CTBTO;
    final Predicate<ReferenceNetwork> predicate = n -> n.getName().equals(name) &&
        n.getDescription().equals(description) && n.getOrganization().equals(monitoringOrg) &&
        n.getActualChangeTime().equals(expectedTime) && n.getSystemChangeTime().equals(Instant.EPOCH);
    return networks -> assertTrue(
        String.format("Expected to find network with name=%s and description=%s and organization=%s"
                + " and actual/systemChangeTime=%s in collection: %s",
            name, description, monitoringOrg, expectedTime, networks),
        containsElementMatchingPredicate(networks, predicate));
  }

  private static Consumer<Collection<ReferenceStation>> stationMatcher(String name, String description,
      double lat, double lon, double elev, Instant changeTime) {

    final Predicate<ReferenceStation> predicate = s -> s.getName().equals(name) && s.getDescription().equals(description) &&
        equal(s.getLatitude(), lat) && equal(s.getLongitude(), lon) && equal(s.getElevation(), elev) &&
        s.getActualChangeTime().equals(changeTime) && s.getSystemChangeTime().equals(changeTime);
    return stations -> assertTrue(
        String.format("Expected to find station with name=%s, description=%s, "
            + "lat=%f, lon=%f, elev=%f, actual/systemChangeTime=%s in collection: " + stations,
            name, description, lat, lon, elev, changeTime),
        containsElementMatchingPredicate(stations, predicate));
  }

  private static Consumer<Collection<ReferenceSite>> siteMatcher(String name, String description,
      double lat, double lon, double elev, Instant changeTime, RelativePosition relativePos) {

    final Predicate<ReferenceSite> predicate = s -> s.getName().equals(name) && s.getDescription().equals(description) &&
        equal(s.getLatitude(), lat) && equal(s.getLongitude(), lon) && equal(s.getElevation(), elev) &&
        s.getActualChangeTime().equals(changeTime) && s.getSystemChangeTime().equals(changeTime) &&
        s.getPosition().equals(relativePos);
    return sites -> assertTrue(
        String.format("Expected to find site with name=%s, description=%s, "
            + "lat=%f, lon=%f, elev=%f, actual/systemChangeTime=%s, relativePosition=%s in collection: " + sites,
            name, description, lat, lon, elev, changeTime, relativePos),
        containsElementMatchingPredicate(sites, predicate));
  }

  private static Consumer<Collection<ReferenceChannel>> channelMatcher(String name, double lat, double lon, double elev,
      double depth, double horizAngle, double vertAngle, double sampleRate, Instant changeTime) {
    // default locationCode to "" since that's what it often is
    return channelMatcher(name, "", lat, lon, elev, depth, horizAngle, vertAngle, sampleRate, changeTime);
  }

  private static Consumer<Collection<ReferenceChannel>> channelMatcher(String name, String locationCode, double lat,
      double lon, double elev, double depth, double horizAngle, double vertAngle, double sampleRate, Instant changeTime) {

    final Predicate<ReferenceChannel> predicate = c -> c.getName().equals(name) &&
        c.getLocationCode().equals(locationCode) &&
        equal(c.getLatitude(), lat) && equal(c.getLongitude(), lon) && equal(c.getElevation(), elev) &&
        equal(c.getDepth(), depth) && equal(c.getVerticalAngle(), vertAngle) && equal(c.getHorizontalAngle(), horizAngle) &&
        equal(c.getNominalSampleRate(), sampleRate) && c.getActualTime().equals(changeTime) && c.getSystemTime().equals(changeTime);
    return channels -> assertTrue(
        String.format("Expected to find channel with name=%s, lat=%f, lon=%f, elev=%f, depth=%f, verticalAngle=%f, "
                + "horizontalAngle=%f, nominalSampleRate=%f, actual/systemChangeTime=%s",
            name, lat, lon, elev, depth, vertAngle, horizAngle, sampleRate, changeTime),
        containsElementMatchingPredicate(channels, predicate));
  }

  private static Consumer<Collection<ReferenceNetworkMembership>> networkMembershipMatcher(
      UUID networkId, UUID stationId, Instant changeTime) {
    final StatusType status = StatusType.ACTIVE;
    final Predicate<ReferenceNetworkMembership> predicate = m -> m.getNetworkId().equals(networkId) &&
        m.getStationId().equals(stationId) && m.getStatus().equals(status) &&
        m.getActualChangeTime().equals(changeTime) && m.getSystemChangeTime().equals(changeTime);
    return memberships -> assertTrue(
        String.format("Expected to find network membership with networkId=%s, stationId=%s, "
            + "status=%s, actual/systemChangeTime=%s", networkId, stationId, status, changeTime),
        containsElementMatchingPredicate(memberships, predicate));
  }

  private static Consumer<Collection<ReferenceStationMembership>> stationMembershipMatcher(
      UUID stationId, UUID siteId, StatusType status, Instant changeTime) {

    final Predicate<ReferenceStationMembership> predicate = m -> m.getStationId().equals(stationId) &&
        m.getSiteId().equals(siteId) && m.getStatus().equals(status) &&
        m.getActualChangeTime().equals(changeTime) && m.getSystemChangeTime().equals(changeTime);
    return memberships -> assertTrue(
        String.format("Expected to find station membership with stationId=%s, siteId=%s, "
            + "status=%s, actual/systemChangeTime=%s", stationId, siteId, status, changeTime),
        containsElementMatchingPredicate(memberships, predicate));
  }

  private static Consumer<Collection<ReferenceSiteMembership>> siteMembershipMatcher(
      UUID siteId, UUID channelId, StatusType status, Instant changeTime) {

    final Predicate<ReferenceSiteMembership> predicate = m -> m.getSiteId().equals(siteId) &&
        m.getChannelId().equals(channelId) && m.getStatus().equals(status) &&
        m.getActualChangeTime().equals(changeTime) && m.getSystemChangeTime().equals(changeTime);
    return memberships -> assertTrue(
        String.format("Expected to find site membership with siteId=%s, channelId=%s"
            + "status=%s, actual/systemChangeTime=%s", siteId, channelId, status, changeTime),
        containsElementMatchingPredicate(memberships, predicate));
  }

  private Consumer<Collection<ReferenceSensor>> sensorMatcher(
      String channelName, String manufacturer, String model, Instant changeTime) {
    final UUID channelId = getChannelVersionId(channelName);
    final Predicate<ReferenceSensor> predicate = s -> s.getChannelId().equals(channelId) &&
        s.getInstrumentManufacturer().equals(manufacturer) &&
        s.getInstrumentModel().equals(model) &&
        s.getActualTime().equals(changeTime) &&
        s.getSystemTime().equals(changeTime);
    return sensors -> assertTrue(
        String.format("Expected to find sensor with channelId=%s, manufacturer=%s, "
                + "model=%s, actual/system change time=%s",
            channelId, manufacturer, model, changeTime),
        containsElementMatchingPredicate(sensors, predicate));
  }

  private Consumer<Collection<ReferenceCalibration>> calibrationMatcher(
      String channelName, double calibrationFactor, double calibrationPeriod, Instant changeTime) {

    final double expectedTimeShift = 0.0, expectedCalibFactorError = 0.0, expectedInterval = 0.0;
    final UUID channelId = getChannelVersionId(channelName);
    final Predicate<ReferenceCalibration> predicate = c -> c.getChannelId().equals(channelId) &&
        equal(c.getCalibrationInterval(), expectedInterval) &&
        equal(c.getCalibrationFactor(), calibrationFactor) &&
        equal(c.getCalibrationPeriod(), calibrationPeriod) &&
        equal(c.getTimeShift(), expectedTimeShift) &&
        equal(c.getCalibrationFactorError(), expectedCalibFactorError) &&
        c.getActualTime().equals(changeTime) &&
        c.getSystemTime().equals(changeTime);
    return calibrations -> assertTrue(
        String.format("Expected to find calibration with channelId=%s, calibrationInterval=%f, "
            + "calibrationFactor=%f, calibrationPeriod=%f, timeShift=%f, calibErrorFactor=%f, "
            + "actual/system change time = %s",
            channelId, expectedInterval, calibrationFactor, calibrationPeriod,
            expectedTimeShift, expectedCalibFactorError, changeTime),
        containsElementMatchingPredicate(calibrations, predicate));
  }

  private static boolean equal(double d1, double d2) {
    return Double.compare(d1, d2) == 0;
  }

  private UUID getNetworkEntityId(String name) {
    return getIdByName(reader.getReferenceNetworks(), name,
        ReferenceNetwork::getName, ReferenceNetwork::getEntityId);
  }

  private UUID getStationEntityId(String name) {
    return getIdByName(reader.getReferenceStations(), name,
        ReferenceStation::getName, ReferenceStation::getEntityId);
  }

  private UUID getSiteEntityId(String name) {
    return getIdByName(reader.getReferenceSites(), name,
        ReferenceSite::getName, ReferenceSite::getEntityId);
  }

  private UUID getChannelEntityId(String name) {
    return getIdByName(reader.getReferenceChannels(), name,
        ReferenceChannel::getName, ReferenceChannel::getEntityId);
  }

  private UUID getChannelVersionId(String name) {
    return getIdByName(reader.getReferenceChannels(), name,
        ReferenceChannel::getName, ReferenceChannel::getVersionId);
  }

  private static <T> UUID getIdByName(Collection<T> elems, String name,
      Function<T, String> nameExtractor, Function<T, UUID> idExtractor) {

    return idExtractor.apply(elems.stream()
        .filter(n -> nameExtractor.apply(n).equals(name))
        .findAny().orElseThrow(IllegalStateException::new));
  }
}
