package gms.dataacquisition.css.stationrefconverter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import gms.dataacquisition.cssreader.data.AffiliationRecord;
import gms.dataacquisition.cssreader.data.InstrumentRecord;
import gms.dataacquisition.cssreader.data.NetworkRecord;
import gms.dataacquisition.cssreader.data.SensorRecord;
import gms.dataacquisition.cssreader.data.SiteChannelRecord;
import gms.dataacquisition.cssreader.data.SiteRecord;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Read flat files and convert their contents to Reference COI objects.
 */
public class CssReferenceReader {

  private static final Logger logger = LoggerFactory.getLogger(CssReferenceReader.class);

  private final Set<ReferenceNetworkMembership> referenceNetworkMemberships;
  private final Set<ReferenceStationMembership> referenceStationMemberships;
  private final Set<ReferenceSiteMembership> referenceSiteMemberships;
  private final Collection<ReferenceNetwork> networks;
  private final Collection<ReferenceStation> stations;
  private final Collection<ReferenceSite> sites;
  private final Collection<ReferenceSensor> sensors;
  private final Collection<ReferenceResponse> responses;
  private final Collection<ReferenceCalibration> calibrations;
  private final Multimap<UUID, ReferenceChannel> siteEntityIdToChannels;

  // Map by instrument ID.
  private final Map<Integer, InstrumentRecord> instrumentRecordsByInid;

  // In the CSS files this date (or close to it) is used to indicate no end date.
  private static final Instant MAX_DATE = Instant.parse("2200-01-01T00:00:00Z");

  /**
   * Pass in the text file paths which are parsed and converted into COI objects.
   *
   * @param affiliationFilePath path to the CSS .affiliation file
   * @param instrumentFilePath path to the CSS .instrument file
   * @param networkFilePath path to the CSS .network file
   * @param sensorFilePath path to the CSS .sensor file
   * @param siteFilePath path to the CSS .site file
   * @param siteChannelFilePath path to the CSS .sitechan file
   */
  public CssReferenceReader(
      String affiliationFilePath,
      String instrumentFilePath,
      String networkFilePath,
      String sensorFilePath,
      String siteFilePath,
      String siteChannelFilePath) throws Exception {

    this.instrumentRecordsByInid = ReaderUtility
        .readInstrumentRecordsIntoMapByInid(instrumentFilePath);
    this.networks = processNetworkRecords(ReaderUtility.readNetworkRecords(networkFilePath));
    final Collection<SiteRecord> siteRecords = ReaderUtility.readSiteRecords(siteFilePath);
    this.stations = processSiteRecordsToMakeStations(siteRecords);
    final SetMultimap<String, ReferenceNetwork> networksByName = HashMultimap.create();
    for (ReferenceNetwork net : networks) {
      networksByName.put(net.getName(), net);
    }
    final SetMultimap<String, ReferenceStation> stationsByName = HashMultimap.create();
    for (ReferenceStation station : stations) {
      stationsByName.put(station.getName(), station);
    }
    Pair<Collection<ReferenceSite>, Set<ReferenceStationMembership>> r = processSiteRecordsToMakeSites(
        siteRecords, stationsByName);
    this.sites = r.getLeft();
    final SetMultimap<String, ReferenceSite> sitesByName = HashMultimap.create();
    for (ReferenceSite site : sites) {
      sitesByName.put(site.getName(), site);
    }
    this.referenceStationMemberships = r.getRight();
    this.referenceNetworkMemberships = processAffiliationRecords(
        ReaderUtility.readAffiliations(affiliationFilePath),
        networksByName, stationsByName);
    final Multimap<Integer, SensorRecord> sensorRecordMap = ReaderUtility
        .readSensorRecordsIntoMultimapByChannelId(sensorFilePath);
    final Pair<Multimap<UUID, ReferenceChannel>, Set<ReferenceSiteMembership>> siteChanResults = processSiteChannelRecords(
        ReaderUtility.readSitechanRecords(siteChannelFilePath), sitesByName, sensorRecordMap, this.instrumentRecordsByInid);
    this.siteEntityIdToChannels = siteChanResults.getLeft();
    this.referenceSiteMemberships = siteChanResults.getRight();
    final SetMultimap<String, ReferenceChannel> channelsByName = HashMultimap.create();
    for (ReferenceChannel chan : this.siteEntityIdToChannels.values()) {
      channelsByName.put(chan.getName(), chan);
    }
    final SensorRecordResults sensorRecordResults = processSensorRecords(
        sensorRecordMap.values(), channelsByName);
    this.sensors = sensorRecordResults.sensors;
    this.responses = sensorRecordResults.responses;
    this.calibrations = sensorRecordResults.calibrations;
  }

  /**
   * Process all network records and convert into COI objects.
   */
  private static Collection<ReferenceNetwork> processNetworkRecords(
      Collection<NetworkRecord> records) {
    final NetworkOrganization org = NetworkOrganization.CTBTO;
    final Collection<ReferenceNetwork> result = new ArrayList<>();
    for (NetworkRecord record : records) {
      final NetworkRegion region;
      switch (record.getType().toUpperCase()) {
        case "WW":
          region = NetworkRegion.GLOBAL;
          break;
        case "AR":
          region = NetworkRegion.REGIONAL;
          break;
        case "LO":
          region = NetworkRegion.LOCAL;
          break;
        default:
          logger.warn(
              "processNetworkRecordsUnknown() - network region detected: " + record.getType());
          region = NetworkRegion.GLOBAL;
      }
      final InformationSource source = InformationSource.create("External",
          record.getLddate(), "Loaded from CSS file");
      final ReferenceNetwork network = ReferenceNetwork.create(record.getName(),
          record.getDesc(), org, region, source,
          "Loaded from CSS network file", Instant.EPOCH, Instant.EPOCH);
      result.add(network);
    }
    return Collections.unmodifiableCollection(result);
  }


  /**
   * Process all affiliation records and map the stations to networks.  This table also may indicate
   * a relationship between stations and sites.
   */
  private static Set<ReferenceNetworkMembership> processAffiliationRecords(
      Collection<AffiliationRecord> records,
      Multimap<String, ReferenceNetwork> networksByName,
      Multimap<String, ReferenceStation> stationsByName) {

    final Set<ReferenceNetworkMembership> results = new HashSet<>();
    final List<AffiliationRecord> sortedRecords = records.stream().sorted(
        Comparator.comparing(AffiliationRecord::getNet)
            .thenComparing(AffiliationRecord::getSta)
            .thenComparing(AffiliationRecord::getTime))
        .collect(Collectors.toList());
    // Loop over all the records in the affiliation file.
    for (int i = 0; i < sortedRecords.size(); i++) {
      final AffiliationRecord record = sortedRecords.get(i);
      final String netName = record.getNet();
      final String staName = record.getSta();
      final Instant end = record.getEndtime();

      final Optional<ReferenceNetwork> network = getFirst(networksByName, record.getNet());
      if (!network.isPresent()) {
        logger.warn("Could not find network " + netName + " referenced in affiliation file");
        continue;
      }
      final Optional<ReferenceStation> station = getFirst(stationsByName, staName);
      if (!station.isPresent()) {
        logger.warn("Could not find station " + staName + " referenced in affiliation file");
        continue;
      }
      final Instant actualDate = record.getTime() == null ? Instant.EPOCH : record.getTime();
      final String comment = "Relationship for network " + netName + " and station " + staName;
      final UUID netId = network.get().getEntityId();
      final UUID staId = station.get().getEntityId();
      final ReferenceNetworkMembership membership = ReferenceNetworkMembership.create(
          comment, actualDate, actualDate, netId, staId, StatusType.ACTIVE);
      if (!isNetworkMembershipDuplicate(results, membership)) {
        results.add(membership);
      }
      // add inactive membership if required
      if (inactiveMembershipRequired(record, i, sortedRecords)) {
        final ReferenceNetworkMembership inactiveMembership = ReferenceNetworkMembership.create(
            comment, end, end, netId, staId, StatusType.INACTIVE);
        if (!isNetworkMembershipDuplicate(results, inactiveMembership)) {
          results.add(inactiveMembership);
        }
      }
    }
    return Collections.unmodifiableSet(results);
  }

  /**
   * Process all site-channel records and convert into COI objects.
   */
  private static Pair<Multimap<UUID, ReferenceChannel>, Set<ReferenceSiteMembership>> processSiteChannelRecords(
      List<SiteChannelRecord> records,
      Multimap<String, ReferenceSite> sitesByName,
      Multimap<Integer, SensorRecord> sensorRecordsByChannelId,
      Map<Integer, InstrumentRecord> instrumentRecordsByInid) {
    final Multimap<UUID, ReferenceChannel> siteIdToChannels = ArrayListMultimap.create();
    final Set<ReferenceSiteMembership> siteMemberships = new HashSet<>();
    final ChannelDataType dataType = ChannelDataType.UNKNOWN;
    final List<SiteChannelRecord> sortedRecords = records.stream().sorted(
        Comparator.comparing(SiteChannelRecord::getSta)
            .thenComparing(SiteChannelRecord::getChan)
            .thenComparing(SiteChannelRecord::getOndate))
        .collect(Collectors.toList());

    // Loop over all the SiteChannelRecords read from the flat file.
    for (int i = 0; i < sortedRecords.size(); i++) {
      final SiteChannelRecord record = sortedRecords.get(i);
      // Get the station or site name.  Could be either!
      final String entityName = record.getSta();
      final Instant onDate = record.getOndate();
      final Instant offDate = record.getOffdate();

      final Optional<ReferenceSite> associatedSite = sitesByName.get(entityName).stream()
          .filter(s -> s.getActualChangeTime().isBefore(onDate) ||
              s.getActualChangeTime().equals(onDate))
          .max(Comparator.comparing(ReferenceSite::getActualChangeTime));
      if (!associatedSite.isPresent()) {
        logger.warn("Could not find site associated to channel record: " + record);
        continue;
      }
      final ReferenceSite site = associatedSite.get();

      // Create a information source object.
      final InformationSource source = InformationSource.create("External",
          record.getOndate(), "OffDate: " + offDate + ", Loaded from CSS file");

      // Get some details from other records.
      final Optional<SensorRecord> sensorRecord = getFirst(sensorRecordsByChannelId, record.getChanid());
      final Optional<InstrumentRecord> instrumentRecord = sensorRecord.map(
          s -> instrumentRecordsByInid.get(s.getInid()));
      final double sampleRate = instrumentRecord.map(InstrumentRecord::getSamprate).orElse(0.0);
      // create channel
      final RelativePosition position = RelativePosition.from(
          0, 0, 0);
      final Pair<String, String> chanAndLocCode = ChannelNameAndLocCodeParser.parse(record.getChan());
      final ReferenceChannel channel = ReferenceChannel.create(
          makeChannelName(record.getSta(), record.getChan()),
          ChannelTypeConverter.getChannelType(record.getChan()),
          dataType, chanAndLocCode.getRight(), site.getLatitude(),
          site.getLongitude(), site.getElevation(),
          record.getEdepth(), record.getVang(), record.getHang(),
          sampleRate, onDate, onDate,
          source, "Channel is associated with site " + entityName,
          position, List.of());

      if (!isChannelDuplicate(siteIdToChannels.values(), channel)) {
        siteIdToChannels.put(site.getEntityId(), channel);
      } else {
        logger.warn("processSiteChannelRecords() - found duplicate channel: " + channel);
      }
      // create site membership, associating the site and channel.
      final ReferenceSiteMembership activeMember = ReferenceSiteMembership.create(
          "Channel " + channel.getName() + " is associated with site " + record.getSta(),
          onDate, onDate, site.getEntityId(),
          channel.getEntityId(), StatusType.ACTIVE);
      if (!isSiteMembershipDuplicate(siteMemberships, activeMember)) {
        siteMemberships.add(activeMember);
      }
      // add inactive membership if required
      if (inactiveMembershipRequired(record, i, sortedRecords)) {
        final ReferenceSiteMembership inactiveMember = ReferenceSiteMembership.create(
            "Channel " + channel.getName() + " is un-associated with site " + record.getSta(),
            offDate, offDate, site.getEntityId(),
            channel.getEntityId(), StatusType.INACTIVE);
        if (!isSiteMembershipDuplicate(siteMemberships, inactiveMember)) {
          siteMemberships.add(inactiveMember);
        }
      }
    }
    return Pair.of(siteIdToChannels, siteMemberships);
  }

  private static Collection<ReferenceStation> processSiteRecordsToMakeStations(
      Collection<SiteRecord> records) {
    final Collection<ReferenceStation> results = new ArrayList<>();
    final Collection<SiteRecord> siteRecordsThatMakeStations = records
        .stream().filter(CssReferenceReader::recordRepresentsStation)
        .collect(Collectors.toList());
    for (SiteRecord record : siteRecordsThatMakeStations) {
      final InformationSource source = InformationSource.create(
          "External", record.getLddate(),
          "OffDate: " + record.getOffdate() + ", Loaded from CSS file");

      // TODO: this mapping may not be correct.
      StationType type = StationType.UNKNOWN;
      switch (record.getStatype().toUpperCase()) {
        case "SS":
          type = StationType.Seismic3Component;
          break;
        case "AR":
          type = StationType.SeismicArray;
          break;
        default:
          logger.warn("processSiteRecordsToMakeSites() - Unknown site type detected: "
              + record.getStatype() + " for record: " + record);
      }
      final String name = record.getSta();
      final ReferenceStation station = ReferenceStation.create(name, record.getStaname(),
          type, source, "Loaded from site file.", record.getLat(),
          record.getLon(), record.getElev(), record.getOndate(), record.getOndate(),
          List.of());
      if (!isStationDuplicate(results, station)) {
        results.add(station);
      } else {
        logger.warn("processSiteRecordsToMakeSites() - found duplicate station: " + station);
      }
    }
    return Collections.unmodifiableCollection(results);
  }

  /**
   * Process all site records and convert into COI objects.
   */
  private static Pair<Collection<ReferenceSite>, Set<ReferenceStationMembership>> processSiteRecordsToMakeSites(
      Collection<SiteRecord> records,
      Multimap<String, ReferenceStation> stationsByName) {
    final Collection<ReferenceSite> sites = new ArrayList<>();
    final Set<ReferenceStationMembership> memberships = new HashSet<>();
    final List<SiteRecord> siteRecordsThatMakeSites = records
        .stream().filter(CssReferenceReader::recordRepresentsSite)
        // sort by sta and then ondate
        .sorted(Comparator.comparing(SiteRecord::getSta).thenComparing(SiteRecord::getOndate))
        .collect(Collectors.toList());

    for (int i = 0; i < siteRecordsThatMakeSites.size(); i++) {
      final SiteRecord record = siteRecordsThatMakeSites.get(i);
      final InformationSource source = InformationSource.create("External",
          record.getLddate(),
          "OffDate: " + record.getOffdate() + ", Loaded from CSS file");

      final String siteName = record.getSta();
      final String stationName = record.getRefsta();
      final Optional<ReferenceStation> parentStation = getFirst(stationsByName, stationName);
      if (!parentStation.isPresent()) {
        logger.error(
            "Could not find parent station for site " + siteName + "; ref sta is " + stationName);
        continue;
      }
      final ReferenceStation station = parentStation.get();
      final RelativePosition relativePosition = RelativePosition.from(
          record.getDnorth(), record.getDeast(), 0);
      // make the site
      final ReferenceSite site = ReferenceSite.create(record.getSta(),
          record.getStaname(), source,
          "Site is associated with station " + record.getRefsta(),
          record.getLat(), record.getLon(), record.getElev(),
          record.getOndate(), record.getOndate(), relativePosition, List.of());

      // Save the site in various structures for later processing.  But first make sure this
      // isn't a duplicate entry in the input file.
      if (!isSiteDuplicate(sites, site)) {
        sites.add(site);
      } else {
        logger.warn("processSiteRecordsToMakeSites() - found duplicate site: " + site);
      }
      // make the station membership (relates site and it's station)
      final ReferenceStationMembership member = ReferenceStationMembership.create(
          "Relationship for station "
              + station.getName() + " and site " + site.getName(),
          record.getOndate(), record.getOndate(),
          station.getEntityId(),
          site.getEntityId(),
          StatusType.ACTIVE);
      if (!isStationMembershipDuplicate(memberships, member)) {
        memberships.add(member);
      }
      if (inactiveMembershipRequired(record, i, siteRecordsThatMakeSites)) {
        final ReferenceStationMembership inactiveMembership = ReferenceStationMembership.create(
            "Relationship for station "
                + station.getName() + " and site " + site.getName(),
            record.getOffdate(), record.getOffdate(),
            station.getEntityId(), site.getEntityId(), StatusType.INACTIVE);
        if (!isStationMembershipDuplicate(memberships, inactiveMembership)) {
          memberships.add(inactiveMembership);
        }
      }
    }
    return Pair
        .of(Collections.unmodifiableCollection(sites), Collections.unmodifiableSet(memberships));
  }

  /**
   * Process all sensor records and convert into COI objects.
   */
  private SensorRecordResults processSensorRecords(
      Collection<SensorRecord> records,
      Multimap<String, ReferenceChannel> channelsByName) {

    final Collection<ReferenceSensor> sensors = new ArrayList<>();
    final Collection<ReferenceResponse> responses = new ArrayList<>();
    final Collection<ReferenceCalibration> calibrations = new ArrayList<>();
    final Set<String> missingResponseFiles = new HashSet<>();
    for (SensorRecord record : records) {
      final Instant offDate = record.getEndTime();
      final int inid = record.getInid();
      if (!this.instrumentRecordsByInid.containsKey(inid)) {
        logger.error("Could not find instrument record with inid "
            + inid + " referenced in sensor record " + record);
        continue;
      }
      final InstrumentRecord instr = this.instrumentRecordsByInid.get(inid);
      final InformationSource source = InformationSource.create("External",
          record.getLddate(), "OffDate: " + offDate + ", Loaded from CSS file");

      // Get the channel associated with this record
      final String channelName = makeChannelName(record.getSta(), record.getChan());
      final Optional<ReferenceChannel> channel = getFirst(channelsByName, channelName);
      if (!channel.isPresent()) {
        logger.warn("Could not find channel for sensor record by name " + channelName);
        continue;
      }
      final UUID channelId = channel.get().getVersionId();
      sensors.add(ReferenceSensor.create(
          channelId, instr.getInsname(), instr.getInstype(), "SNxxxx",
          1, 1, 0, 0,
          record.getTime(), record.getTime(),
          source, "Sensor is associated with channel " + channel.get().getName()));

      calibrations.add(ReferenceCalibration.create(
          channelId, 0,
          instr.getNcalib(),
          0,
          record.getCalper(),
          record.getTshift(),
          record.getTime(), record.getTime(),
          source,
          "Calibration is associated with channel " + channel.get().getName()));

      String dir = instr.getDir();
      if (!dir.endsWith(File.separator)) {
        dir = dir + File.separator;
      }
      final String responsePath = dir + instr.getDfile();

      try {
        final byte[] responseData = Files.readAllBytes(Paths.get(responsePath));
        responses.add(ReferenceResponse.create(
            channelId, instr.getRsptype(), responseData, "UNKNOWN",
            record.getTime(), record.getTime(), source,
            "Response associated with channel "
                + channel.get().getName()));
      } catch (IOException e) {
        missingResponseFiles.add(responsePath);
      }
    }
    if (!missingResponseFiles.isEmpty()) {
      logger.warn("Could not find response files: " + missingResponseFiles);
    }
    return new SensorRecordResults(sensors, responses, calibrations);
  }

  /**
   * Get the list of ReferenceNetwork objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public Collection<ReferenceNetwork> getReferenceNetworks() {
    return this.networks;
  }

  /**
   * Get the list of ReferenceStation objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public Collection<ReferenceStation> getReferenceStations() {
    return this.stations;
  }

  /**
   * Get the list of ReferenceSite objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public Collection<ReferenceSite> getReferenceSites() {
    return this.sites;
  }

  /**
   * Get the list of ReferenceChannel objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public Collection<ReferenceChannel> getReferenceChannels() {
    return this.siteEntityIdToChannels.values();
  }

  /**
   * Get the list of ReferenceResponse objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public Collection<ReferenceResponse> getResponses() {
    return Collections.unmodifiableCollection(this.responses);
  }

  /**
   * Get the list of ReferenceCalibration objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public Collection<ReferenceCalibration> getCalibrations() {
    return Collections.unmodifiableCollection(this.calibrations);
  }

  /**
   * Get the list of ReferenceSensor objects created.
   *
   * @return A list of objects, it may be empty.
   */
  public Collection<ReferenceSensor> getSensors() {
    return Collections.unmodifiableCollection(this.sensors);
  }

  /**
   * Get the set of ReferenceNetworkMembership objects.
   *
   * @return A set of objects, it may be empty.
   */
  public Set<ReferenceNetworkMembership> getReferenceNetworkMemberships() {
    return this.referenceNetworkMemberships;
  }

  /**
   * Get the set of ReferenceStationMembership objects.
   *
   * @return A set of objects, it may be empty.
   */
  public Set<ReferenceStationMembership> getReferenceStationMemberships() {
    return this.referenceStationMemberships;
  }

  /**
   * Get the set of ReferenceSiteMembership objects.
   *
   * @return A set of objects, it may be empty.
   */
  public Set<ReferenceSiteMembership> getReferenceSiteMemberships() {
    return this.referenceSiteMemberships;
  }

  private static <K, T> Optional<T> getFirst(Multimap<K, T> m, K key) {
    if (!m.containsKey(key)) {
      return Optional.empty();
    }
    Collection<T> l = m.get(key);
    return l.isEmpty() ? Optional.empty() : Optional.of(l.iterator().next());
  }

  private static boolean isNetworkMembershipDuplicate(
      Collection<ReferenceNetworkMembership> memberships,
      ReferenceNetworkMembership member) {
    for (ReferenceNetworkMembership item : memberships) {
      if (member.getStationId().equals(item.getStationId())
          && member.getNetworkId().equals(item.getNetworkId())
          && member.getActualChangeTime().equals(item.getActualChangeTime())
          && member.getSystemChangeTime().equals(item.getSystemChangeTime())
          && member.getStatus().equals(item.getStatus())
      ) {
        return true;
      }
    }
    return false;
  }

  private static boolean isStationMembershipDuplicate(
      Collection<ReferenceStationMembership> memberships,
      ReferenceStationMembership member) {
    for (ReferenceStationMembership item : memberships) {
      if (member.getStationId().equals(item.getStationId())
          && member.getSiteId().equals(item.getSiteId())
          && member.getActualChangeTime().equals(item.getActualChangeTime())
          && member.getSystemChangeTime().equals(item.getSystemChangeTime())
          && member.getStatus().equals(item.getStatus())
      ) {
        return true;
      }
    }
    return false;
  }

  private static boolean isSiteMembershipDuplicate(Collection<ReferenceSiteMembership> memberships, ReferenceSiteMembership member) {
    for (ReferenceSiteMembership item : memberships) {
      if (member.getChannelId().equals(item.getChannelId())
          && member.getSiteId().equals(item.getSiteId())
          && member.getActualChangeTime().equals(item.getActualChangeTime())
          && member.getSystemChangeTime().equals(item.getSystemChangeTime())
          && member.getStatus().equals(item.getStatus())
      ) {
        return true;
      }
    }
    return false;
  }

  private static boolean isSiteDuplicate(Collection<ReferenceSite> sites, ReferenceSite site) {
    for (ReferenceSite item : sites) {
      if (site.getName().equals(item.getName())
          && site.getActualChangeTime().equals(item.getActualChangeTime())
          && site.getElevation() == item.getElevation()
          && site.getLatitude() == item.getLatitude()
          && site.getLongitude() == item.getLongitude()
      ) {
        return true;
      }

    }
    return false;
  }


  private static boolean isStationDuplicate(Collection<ReferenceStation> stations,
      ReferenceStation station) {
    for (ReferenceStation item : stations) {
      if (station.getName().equals(item.getName())
          && station.getActualChangeTime().equals(item.getActualChangeTime())
          && station.getElevation() == item.getElevation()
          && station.getLatitude() == item.getLatitude()
          && station.getLongitude() == item.getLongitude()
          && station.getStationType().equals(item.getStationType())
      ) {
        return true;
      }

    }
    return false;
  }

  private static boolean isChannelDuplicate(Collection<ReferenceChannel> channels, ReferenceChannel channel) {
    for (ReferenceChannel item : channels) {
      if (channel.getName().equals(item.getName())
          && channel.getActualTime().equals(item.getActualTime())
          && channel.getElevation() == item.getElevation()
          && channel.getLatitude() == item.getLatitude()
          && channel.getLongitude() == item.getLongitude()
          && channel.getDepth() == item.getDepth()
          && channel.getVerticalAngle() == item.getVerticalAngle()
          && channel.getHorizontalAngle() == item.getHorizontalAngle()
          && channel.getNominalSampleRate() == item.getNominalSampleRate()
          && channel.getType().equals(item.getType())
          && channel.getDataType().equals(item.getDataType())
          && channel.getComment().equals(item.getComment())
      ) {
        return true;
      }

    }
    return false;
  }

  private static Location getLocation(ReferenceStation station) {
    return Location.from(station.getLatitude(), station.getLongitude(),
        0, station.getElevation());
  }

  private static boolean recordRepresentsStation(SiteRecord record) {
    return record.getSta().equals(record.getRefsta());
  }

  private static boolean recordRepresentsSite(SiteRecord record) {
    return !record.getSta().equals(record.getRefsta()) ||
        (record.getSta().equals(record.getRefsta()) && record.getStatype().equals("ss"));
  }

  private static final BiPredicate<SiteRecord, SiteRecord> SITE_REC_EQUALITY_FUNC = (r1, r2) ->
      r1.getSta().equals(r2.getSta()) && r1.getRefsta().equals(r2.getRefsta());

  private static final BiPredicate<SiteChannelRecord, SiteChannelRecord> SITECHAN_REC_EQUALITY_FUNC = (r1, r2) ->
      r1.getSta().equals(r2.getSta()) && r1.getChan().equals(r2.getChan());

  private static final BiPredicate<AffiliationRecord, AffiliationRecord> AFFILIATION_EQUALITY_FUNC = (r1, r2) ->
      r1.getNet().equals(r2.getNet()) && r1.getSta().equals(r2.getSta());

  private static boolean inactiveMembershipRequired(
      SiteRecord record, int index, List<SiteRecord> records) {
    return inactiveMembershipRequired(record, index, records,
        SiteRecord::getOndate, SiteRecord::getOffdate, SITE_REC_EQUALITY_FUNC);
  }

  private static boolean inactiveMembershipRequired(
      SiteChannelRecord record, int index, List<SiteChannelRecord> records) {

    return inactiveMembershipRequired(record, index, records,
        SiteChannelRecord::getOndate, SiteChannelRecord::getOffdate,
        SITECHAN_REC_EQUALITY_FUNC);
  }

  private static boolean inactiveMembershipRequired(
      AffiliationRecord record, int index, List<AffiliationRecord> records) {

    return inactiveMembershipRequired(record, index, records,
        AffiliationRecord::getTime, AffiliationRecord::getEndtime,
        AFFILIATION_EQUALITY_FUNC);
  }

  private static <Record> boolean inactiveMembershipRequired(
      Record record, int index, List<Record> records,
      Function<Record, Instant> ondateExtractor,
      Function<Record, Instant> offdateExtractor,
      BiPredicate<Record, Record> equalityFunc) {

    // if offdate is not before the special value, return false.
    if (!offdateExtractor.apply(record).isBefore(MAX_DATE)) {
      return false;
    }
    // if last record, return true.
    else if (!(index + 1 < records.size())) {
      return true;
    }

    // Check if the next record has ondate = to this records' offdate.
    // If not, create an inactive membership record.
    final Record nextRecord = records.get(index + 1);
    final boolean nextRecordIsSameEntity = equalityFunc.test(record, nextRecord);
    final boolean nextRecordStartsOnSameDay = offdateExtractor.apply(record).equals(
        ondateExtractor.apply(nextRecord));
    return !nextRecordIsSameEntity || !nextRecordStartsOnSameDay;
  }

  private class SensorRecordResults {
    public final Collection<ReferenceSensor> sensors;
    public final Collection<ReferenceResponse> responses;
    public final Collection<ReferenceCalibration> calibrations;

    public SensorRecordResults(Collection<ReferenceSensor> sensors,
        Collection<ReferenceResponse> responses,
        Collection<ReferenceCalibration> calibrations) {
      this.sensors = Collections.unmodifiableCollection(sensors);
      this.responses = Collections.unmodifiableCollection(responses);
      this.calibrations = Collections.unmodifiableCollection(calibrations);
    }
  }

  // populate name of channel with convention '{siteName}/{channelName}'.  Add location code if present.
  private static String makeChannelName(String sta, String chan) {
    final Pair<String, String> chanAndLocCode = ChannelNameAndLocCodeParser.parse(chan);
    String channelName = sta + "/" + chanAndLocCode.getLeft();
    if (chanAndLocCode.getRight() != null && !chanAndLocCode.getRight().isEmpty()) {
      channelName += "/" + chanAndLocCode.getRight();
    }
    return channelName;
  }
}
