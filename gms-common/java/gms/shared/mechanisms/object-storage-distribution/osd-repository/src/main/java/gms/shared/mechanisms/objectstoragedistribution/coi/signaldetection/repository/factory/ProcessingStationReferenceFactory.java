package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ProcessingStationReferenceFactoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters.CalibrationConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters.ChannelConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters.NetworkConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters.ResponseConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters.SiteConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters.StationConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessingStationReferenceFactory implements
    ProcessingStationReferenceFactoryInterface {

  private final StationReferenceRepositoryInterface referenceRepository;

  private static Logger logger = LoggerFactory.getLogger(ProcessingStationReferenceFactory.class);

  private static final InformationSource fakeSource = InformationSource.create("fake org",
      Instant.EPOCH, "fake reference");

  public ProcessingStationReferenceFactory(
      StationReferenceRepositoryInterface referenceRepository) {
    this.referenceRepository = Objects.requireNonNull(referenceRepository);
  }

  @Override
  public Optional<Network> networkFromName(String name, Instant actualChangeTime,
      Instant systemChangeTime, boolean slim) {
    try {
      List<ReferenceNetwork> nets
          = this.referenceRepository.retrieveNetworksByName(name);
      Optional<ReferenceNetwork> latestNet = latest(
          nets, ReferenceNetwork::getActualChangeTime,
          ReferenceNetwork::getSystemChangeTime, actualChangeTime, systemChangeTime);
      if (!latestNet.isPresent()) {
        logger.info("empty result returned at time t = " + Instant.now().toString());
        return Optional.empty();
      }
      ReferenceNetwork net = latestNet.get();
      return Optional.of(slim ? NetworkConverter.withoutStations(net)
          : NetworkConverter.withStations(net,
              getStations(net, actualChangeTime, systemChangeTime)));
    } catch (Exception ex) {
      logger.error("Error in networkFromName", ex);
      return Optional.empty();
    }
  }

  @Override
  public Optional<Station> stationFromName(String name, Instant actualChangeTime,
      Instant systemChangeTime, boolean slim) {
    try {
      List<ReferenceStation> stas
          = this.referenceRepository.retrieveStationsByName(name);
      Optional<ReferenceStation> latestSta = latest(
          stas, ReferenceStation::getActualChangeTime,
          ReferenceStation::getSystemChangeTime, actualChangeTime, systemChangeTime);
      if (!latestSta.isPresent()) {
        logger.info("empty result returned at time t = " + Instant.now().toString());

        return Optional.empty();
      }
      ReferenceStation sta = latestSta.get();
      return Optional.of(slim ? StationConverter.withoutSites(sta)
          : StationConverter.withSites(sta,
              getSites(sta, actualChangeTime, systemChangeTime)));
    } catch (Exception ex) {
      logger.error("Error in stationFromName", ex);
      return Optional.empty();
    }
  }

  @Override
  public Optional<Site> siteFromName(String name, Instant actualChangeTime,
      Instant systemChangeTime, boolean slim) {
    try {
      List<ReferenceSite> sites
          = this.referenceRepository.retrieveSitesByName(name);
      Optional<ReferenceSite> latestSite = latest(
          sites, ReferenceSite::getActualChangeTime,
          ReferenceSite::getSystemChangeTime, actualChangeTime, systemChangeTime);
      if (!latestSite.isPresent()) {
        logger.info("empty result returned at time t = " + Instant.now().toString());

        return Optional.empty();
      }
      ReferenceSite site = latestSite.get();
      return Optional.of(slim ? SiteConverter.withoutChannels(site)
          : SiteConverter.withChannels(site,
              getChannels(site, actualChangeTime, systemChangeTime)));
    } catch (Exception ex) {
      logger.error("Error in siteFromName", ex);
      return Optional.empty();
    }
  }

  private Set<Station> getStations(ReferenceNetwork n, Instant actualChangeTime,
      Instant systemChangeTime) {
    try {
      final Set<Station> stations = new HashSet<>();
      final List<ReferenceNetworkMembership> memberships
          = referenceRepository.retrieveNetworkMembershipsByNetworkId(n.getEntityId());
      final Set<UUID> stationIds = associatedStationIdsAtTime(memberships, actualChangeTime,
          systemChangeTime);
      for (UUID staId : stationIds) {
        final List<ReferenceStation> stas
            = this.referenceRepository.retrieveStationsByEntityId(staId);
        final Optional<ReferenceStation> latestSta = latest(
            stas, ReferenceStation::getActualChangeTime,
            ReferenceStation::getSystemChangeTime, actualChangeTime,
            systemChangeTime);
        if (latestSta.isPresent()) {
          final Set<Site> sites = getSites(latestSta.get(), actualChangeTime,
              systemChangeTime);
          final Station sta = StationConverter.withSites(latestSta.get(), sites);
          stations.add(sta);
        }
      }
      return stations;
    } catch (Exception ex) {
      logger.error("Error in getStations", ex);
      return Set.of();
    }
  }

  private Set<Site> getSites(ReferenceStation sta, Instant actualChangeTime,
      Instant systemChangeTime) {
    try {
      final Set<Site> sites = new HashSet<>();
      final List<ReferenceStationMembership> memberships
          = referenceRepository.retrieveStationMembershipsByStationId(sta.getEntityId());
      final Set<UUID> siteIds = associatedSiteIdsAtTime(memberships, actualChangeTime,
          systemChangeTime);
      for (UUID siteId : siteIds) {
        final List<ReferenceSite> refSites
            = this.referenceRepository.retrieveSitesByEntityId(siteId);
        final Optional<ReferenceSite> latestSite = latest(
            refSites, ReferenceSite::getActualChangeTime,
            ReferenceSite::getSystemChangeTime, actualChangeTime, systemChangeTime);
        if (latestSite.isPresent()) {
          final Set<Channel> channels = getChannels(latestSite.get(),
              actualChangeTime, systemChangeTime);
          final Site site = SiteConverter.withChannels(latestSite.get(), channels);
          sites.add(site);
        }
      }
      return sites;
    } catch (Exception ex) {
      logger.error("Error in getSites", ex);
      return Set.of();
    }
  }

  private Set<Channel> getChannels(ReferenceSite s, Instant actualChangeTime,
      Instant systemChangeTime) {
    try {
      final Set<Channel> channels = new HashSet<>();
      final List<ReferenceSiteMembership> memberships = referenceRepository
          .retrieveSiteMembershipsBySiteId(s.getEntityId());
      final Set<UUID> channelIds = associatedChannelIdsAtTime(memberships, actualChangeTime,
          systemChangeTime);
      for (UUID chanId : channelIds) {
        List<ReferenceChannel> refChans
            = this.referenceRepository.retrieveChannelsByEntityId(chanId);
        Optional<ReferenceChannel> latestChan = latest(
            refChans, ReferenceChannel::getActualTime,
            ReferenceChannel::getSystemTime, actualChangeTime, systemChangeTime);
        if (latestChan.isPresent()) {
          Channel chan = ChannelConverter.from(
              latestChan.get());
          channels.add(chan);
        }
      }
      return channels;
    } catch (Exception ex) {
      logger.error("Error in getChannels", ex);
      return Set.of();
    }
  }

  private static boolean isBeforeOrEqual(Instant time1, Instant time2) {
    return !time1.isAfter(time2);
  }

  /**
   * Gets the Response that is for the proper time frame. If the response cannot be found, a fake
   * one is returned. //TODO: never return fake one; this is only for testing.
   *
   * @param chanId the id of the channel the response is for
   * @param actualChangeTime the actual change time
   * @param systemChangeTime the system change time
   * @return Response for the right time frame
   */
  private Response getResponse(UUID chanId, Instant actualChangeTime,
      Instant systemChangeTime) throws Exception {

    List<ReferenceResponse> responses = this.referenceRepository
        .retrieveResponsesByChannelId(chanId);

    Optional<ReferenceResponse> response = latest(responses,
        ReferenceResponse::getActualTime, ReferenceResponse::getSystemTime,
        actualChangeTime, systemChangeTime);

    if (!response.isPresent()) {
      logger.warn("Could not find response for ref channel " + chanId
          + " and actualTime " + actualChangeTime + " and systemTime "
          + systemChangeTime);
      return ResponseConverter.from(ReferenceResponse.create(chanId,
          "fake", new byte[]{},
          "fake unit", actualChangeTime, systemChangeTime,
          fakeSource, "fake comment"));
    }
    return ResponseConverter.from(response.get());
  }

  /**
   * Gets the Calibration that is for the proper time frame. If the calibration cannot be found, a
   * fake one is returned. //TODO: never return fake one; this is only for testing.
   *
   * @param chanId the id of the channel the calibration is for
   * @param actualChangeTime the actual change time
   * @param systemChangeTime the system change time
   * @return Calibration for the right time frame
   */
  private Calibration getCalibration(UUID chanId, Instant actualChangeTime,
      Instant systemChangeTime) throws Exception {

    List<ReferenceCalibration> calibrations = this.referenceRepository
        .retrieveCalibrationsByChannelId(chanId);

    // Find Calibration for which it's 'system change time' and 'actual change time' is before
    // the requested time, and take the largest such time (most recent).
    Optional<ReferenceCalibration> cal = latest(calibrations,
        ReferenceCalibration::getActualTime, ReferenceCalibration::getSystemTime,
        actualChangeTime, systemChangeTime);

    if (!cal.isPresent()) {
      logger.warn("Could not find calibration for ref channel " + chanId
          + " and actualTime " + actualChangeTime + " and systemTime " + systemChangeTime);
      return CalibrationConverter.from(ReferenceCalibration.create(chanId,
          Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE,
          Double.MIN_VALUE, Double.MIN_VALUE,
          actualChangeTime, systemChangeTime, fakeSource, "fake comment"));
    }
    return CalibrationConverter.from(cal.get());
  }

  private static <T> Optional<T> latest(List<T> elems,
      Function<T, Instant> actualTimeExtractor, Function<T, Instant> systemTimeExtractor,
      Instant actualChangeTime, Instant systemChangeTime) {
    return elems.stream()
        .filter(e -> isBeforeOrEqual(actualTimeExtractor.apply(e), actualChangeTime))
        .filter(e -> isBeforeOrEqual(systemTimeExtractor.apply(e), systemChangeTime))
        .max(Comparator.comparing(systemTimeExtractor));
  }

  private static Set<UUID> associatedStationIdsAtTime(
      List<ReferenceNetworkMembership> memberships, Instant actualChangeTime,
      Instant systemChangeTime) {

    return associatedIdsAtTime(memberships, ReferenceNetworkMembership::getStationId,
        ReferenceNetworkMembership::getActualChangeTime,
        ReferenceNetworkMembership::getSystemChangeTime,
        m -> m.getStatus() == StatusType.ACTIVE, actualChangeTime, systemChangeTime);
  }

  private static Set<UUID> associatedSiteIdsAtTime(
      List<ReferenceStationMembership> memberships, Instant actualChangeTime,
      Instant systemChangeTime) {

    return associatedIdsAtTime(memberships, ReferenceStationMembership::getSiteId,
        ReferenceStationMembership::getActualChangeTime,
        ReferenceStationMembership::getSystemChangeTime,
        m -> m.getStatus() == StatusType.ACTIVE, actualChangeTime, systemChangeTime);
  }

  private static Set<UUID> associatedChannelIdsAtTime(
      List<ReferenceSiteMembership> memberships, Instant actualChangeTime,
      Instant systemChangeTime) {

    return associatedIdsAtTime(memberships, ReferenceSiteMembership::getChannelId,
        ReferenceSiteMembership::getActualChangeTime, ReferenceSiteMembership::getSystemChangeTime,
        m -> m.getStatus() == StatusType.ACTIVE, actualChangeTime, systemChangeTime);
  }

  private static <Membership> Set<UUID> associatedIdsAtTime(List<Membership> memberships,
      Function<Membership, UUID> desiredIdExtractor,
      Function<Membership, Instant> actualTimeExtractor,
      Function<Membership, Instant> systemTimeExtractor,
      Function<Membership, Boolean> activeExtractor,
      Instant actualChangeTime, Instant systemChangeTime) {

    // group the memberships by the desired id
    final ListMultimap<UUID, Membership> membershipsByDesiredId = ArrayListMultimap.create();
    for (Membership membership : memberships) {
      membershipsByDesiredId.put(desiredIdExtractor.apply(membership), membership);
    }
    final Set<UUID> associatedIds = new HashSet<>();
    // look at the latest membership for the channel ID;
    // if it is ACTIVE, that channel is associated to the site at this time.
    for (UUID id : membershipsByDesiredId.keySet()) {
      // get the most recent membership
      final Optional<Membership> latestMembership = latest(membershipsByDesiredId.get(id),
          actualTimeExtractor, systemTimeExtractor, actualChangeTime, systemChangeTime);
      // if the latest membership up to this time is active, the associated ID is included.
      if (latestMembership.isPresent() && activeExtractor.apply(latestMembership.get())) {
        associatedIds.add(desiredIdExtractor.apply(latestMembership.get()));
      }
    }
    return associatedIds;
  }
}
