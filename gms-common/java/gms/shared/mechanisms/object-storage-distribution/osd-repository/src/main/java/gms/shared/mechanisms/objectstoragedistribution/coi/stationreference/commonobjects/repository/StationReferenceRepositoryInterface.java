package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Define an interface for storing and retrieving reference network, station,
 * site, digitizer, channel, calibration, sensor, and response classes.
 */
public interface StationReferenceRepositoryInterface {

  /**
   * Close persistence databases and perform other shutdown tasks.
   *
   * @return True if successful, otherwise false.
   */
  boolean close();

  /**
   * Retrieve all networks.
   * @return all networks
   */
  List<ReferenceNetwork> retrieveNetworks() throws Exception;

  /**
   * Retrieve all networks by entity id.
   * @param id the id
   * @return all network versions with that entity id
   */
  List<ReferenceNetwork> retrieveNetworksByEntityId(UUID id) throws Exception;

  /**
   * Retrieve all networks by name.
   * @param name the name
   * @return all network versions with that name
   */
  List<ReferenceNetwork> retrieveNetworksByName(String name) throws Exception;

  /**
   * Retrieves all stations
   * @return all stations
   */
  List<ReferenceStation> retrieveStations() throws Exception;

  /**
   * Retrieve all stations by entity id.
   * @param id the id
   * @return all station versions with that entity id
   */
  List<ReferenceStation> retrieveStationsByEntityId(UUID id) throws Exception;

  /**
   * Retrieve all stations by version ids.
   *
   * @param stationVersionIds the version ids
   * @return all station versions with those version ids
   */
  List<ReferenceStation> retrieveStationsByVersionIds(Collection<UUID> stationVersionIds) throws Exception;

  /**
   * Retrieve all stations by name.
   * @param name the name
   * @return all station versions with that name
   */
  List<ReferenceStation> retrieveStationsByName(String name) throws Exception;

  /**
   * Retrieve all site.
   * @return all site
   */
  List<ReferenceSite> retrieveSites() throws Exception;

  /**
   * Retrieve all sites by entity id.
   * @param id the id
   * @return all site versions with that entity id
   */
  List<ReferenceSite> retrieveSitesByEntityId(UUID id) throws Exception;

  /**
   * Retrieve all sites by name.
   * @param name the name
   * @return all site versions with that name
   */
  List<ReferenceSite> retrieveSitesByName(String name) throws Exception;

  /**
   * Finds all channels.
   * @return all channels
   */
  List<ReferenceChannel> retrieveChannels() throws Exception;

  /**
   * Retrieve all channels by entity id.
   * @param id the id
   * @return all channel versions with that entity id
   */
  List<ReferenceChannel> retrieveChannelsByEntityId(UUID id) throws Exception;

  /**
   * Retrieve all channels by version id.
   *
   * @param channelVersionIds the version ids
   * @return all channel versions with those version ids
   */
  List<ReferenceChannel> retrieveChannelsByVersionIds(Collection<UUID> channelVersionIds) throws Exception;

  /**
   * Retrieve all channels by name.
   * @param name the name
   * @return all channel versions with that name
   */
  List<ReferenceChannel> retrieveChannelsByName(String name) throws Exception;

  /**
   * Store ReferenceDigitizer to the relational database.
   * @param digitizer the digitizer
   */
  void storeReferenceDigitizer(ReferenceDigitizer digitizer) throws Exception;

  /**
   * Gets all digitizers
   * @return all digitizers
   */
  List<ReferenceDigitizer> retrieveDigitizers() throws Exception;

  /**
   * Retrieve all digitizers by entity id.
   * @param id the id
   * @return all digitizer versions with that entity id
   */
  List<ReferenceDigitizer> retrieveDigitizersByEntityId(UUID id) throws Exception;

  /**
   * Retrieve all digitizers by name.
   * @param name the name
   * @return all digitizer versions with that name
   */
  List<ReferenceDigitizer> retrieveDigitizersByName(String name) throws Exception;

  /**
   * Retrieves all calibrations.
   * @return list of calibrations; may be empty.
   */
  List<ReferenceCalibration> retrieveCalibrations() throws Exception;

  /**
   * Retrieves calibrations by the channel they are associated with.
   * @param channelId the id of the channel
   * @return list of calibrations; may be empty.
   */
  List<ReferenceCalibration> retrieveCalibrationsByChannelId(UUID channelId) throws Exception;

  /**
   * Retrieves all responses.
   * @return list of responses; may be empty.
   */
  List<ReferenceResponse> retrieveResponses() throws Exception;

  /**
   * Retrieves responses by the channel they are associated with.
   * @param channelId the id of the channel
   * @return list of responses; may be empty.
   */
  List<ReferenceResponse> retrieveResponsesByChannelId(UUID channelId) throws Exception;

  /**
   * Retrieves all sensors.
   * @return list of sensors; may be empty.
   */
  List<ReferenceSensor> retrieveSensors() throws Exception;

  /**
   * Retrieves sensors by the channel they are associated with.
   * @param channelId the id of the channel
   * @return list of sensors; may be empty.
   */
  List<ReferenceSensor> retrieveSensorsByChannelId(UUID channelId) throws Exception;

  /**
   * Retrieves all network memberships
   * @return the memberships
   */
  List<ReferenceNetworkMembership> retrieveNetworkMemberships() throws Exception;

  /**
   * Retrieves network memberships with the given network entity id.
   * @return the memberships
   */
  List<ReferenceNetworkMembership> retrieveNetworkMembershipsByNetworkId(UUID id) throws Exception;

  /**
   * Retrieves network memberships with the given station entity id
   * @return the memberships
   */
  List<ReferenceNetworkMembership> retrieveNetworkMembershipsByStationId(UUID id) throws Exception;

  /**
   * Retrieves network memberships with the given network entity id and station entity id.
   * @return the memberships
   */
  List<ReferenceNetworkMembership> retrieveNetworkMembershipsByNetworkAndStationId(
      UUID networkId, UUID stationId) throws Exception;

  /**
   * Retrieves all station memberships
   * @return the memberships
   */
  List<ReferenceStationMembership> retrieveStationMemberships() throws Exception;

  /**
   * Retrieves station memberships with the given station entity id.
   * @return the memberships
   */
  List<ReferenceStationMembership> retrieveStationMembershipsByStationId(UUID id) throws Exception;

  /**
   * Retrieves station memberships with the given site entity id.
   * @return the memberships
   */
  List<ReferenceStationMembership> retrieveStationMembershipsBySiteId(UUID id) throws Exception;

  /**
   * Retrieves station memberships with the given station entity id and site entity id.
   * @return the memberships
   */
  List<ReferenceStationMembership> retrieveStationMembershipsByStationAndSiteId(
      UUID stationId, UUID siteId) throws Exception;

  /**
   * Retrieves all site memberships
   * @return the memberships
   */
  List<ReferenceSiteMembership> retrieveSiteMemberships() throws Exception;

  /**
   * Retrieves site memberships with the given site entity id.
   * @return the memberships
   */
  List<ReferenceSiteMembership> retrieveSiteMembershipsBySiteId(UUID id) throws Exception;

  /**
   * Retrieves site memberships with the given channel entity id.
   * @return the memberships
   */
  List<ReferenceSiteMembership> retrieveSiteMembershipsByChannelId(UUID id) throws Exception;

  /**
   * Retrieves site memberships with the given site entity id and channel entity id.
   * @return the memberships
   */
  List<ReferenceSiteMembership> retrieveSiteMembershipsBySiteAndChannelId(
      UUID siteId, UUID channelId) throws Exception;


  /**
   * Store a digitizer membership to the database.
   * @param membership the object to store
   */
  void storeDigitizerMembership(ReferenceDigitizerMembership membership) throws Exception;

  /**
   * Retrieves all digitizer memberships
   * @return the memberships
   */
  List<ReferenceDigitizerMembership> retrieveDigitizerMemberships() throws Exception;

  /**
   * Retrieves digitizer memberships with the given digitizer entity id.
   * @return the memberships
   */
  List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByDigitizerId(UUID id) throws Exception;

  /**
   * Retrieves digitizer memberships with the given channel entity id.
   * @return the memberships
   */
  List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByChannelId(UUID id) throws Exception;

  /**
   * Retrieves digitizer memberships with the given digitizer entity id and channel entity id.
   * @return the memberships
   */
  List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByDigitizerAndChannelId(
      UUID digitizerId, UUID channelId) throws Exception;

  /**
   * Stores a ReferenceNetwork.
   *
   * @param network the network
   */
  void storeReferenceNetwork(ReferenceNetwork network) throws Exception;

  /**
   * Stores a ReferenceStation.
   *
   * @param station the station
   */
  void storeReferenceStation(ReferenceStation station) throws Exception;

  /**
   * Stores a ReferenceSite.
   *
   * @param site the site
   */
  void storeReferenceSite(ReferenceSite site) throws Exception;

  /**
   * Stores a ReferenceChannel.
   *
   * @param channel the channel
   */
  void storeReferenceChannel(ReferenceChannel channel) throws Exception;

  /**
   * Stores a calibration
   *
   * @param calibration the calibration
   */
  void storeCalibration(ReferenceCalibration calibration) throws Exception;

  /**
   * Stores a response
   *
   * @param response the response
   */
  void storeResponse(ReferenceResponse response) throws Exception;

  /**
   * Stores a sensor
   *
   * @param sensor the sensor
   */
  void storeSensor(ReferenceSensor sensor) throws Exception;

  /**
   * Stores network memberships
   *
   * @param memberships the memberships
   */
  void storeNetworkMemberships(Collection<ReferenceNetworkMembership> memberships)
      throws Exception;

  /**
   * Stores station memberships
   *
   * @param memberships the memberships
   */
  void storeStationMemberships(Collection<ReferenceStationMembership> memberships)
      throws Exception;

  /**
   * Stores site memberships
   *
   * @param memberships the memberships
   */
  void storeSiteMemberships(Collection<ReferenceSiteMembership> memberships)
      throws Exception;



}
