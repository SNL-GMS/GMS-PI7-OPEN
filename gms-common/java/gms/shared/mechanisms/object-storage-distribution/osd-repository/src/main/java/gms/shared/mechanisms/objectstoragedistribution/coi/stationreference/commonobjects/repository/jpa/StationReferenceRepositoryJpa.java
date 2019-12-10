package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.Sorters;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationRefRepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects.*;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Define a class to provide persistence methods for reading and writing to the relational database
 * for station reference information.
 */
public class StationReferenceRepositoryJpa implements StationReferenceRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(StationReferenceRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Default constructor.
   */
  public StationReferenceRepositoryJpa() {
    this(CoiEntityManagerFactory.create());
  }

  /**
   * Constructor taking in the EntityManagerFactory
   *
   * @param entityManagerFactory entity manager factory
   */
  public StationReferenceRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    Validate.notNull(entityManagerFactory);
    this.entityManagerFactory = entityManagerFactory;
  }

  private static void saveObjectAndCommit(EntityManager entityManager, Object obj)
      throws Exception {

    try {
      entityManager.getTransaction().begin();
      entityManager.persist(obj);
      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      throw StationRefRepositoryExceptionUtils.wrap(ex);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public boolean close() {
    if (entityManagerFactory != null) {
      entityManagerFactory.close();
    }
    return true;
  }

  @Override
  public void storeReferenceNetwork(ReferenceNetwork network) throws Exception {
    Validate.notNull(network);
    EntityManager em = entityManagerFactory.createEntityManager();
    if (exists(em, ReferenceNetworkDao.class, network.getName(), network.getComment(),
        network.getActualChangeTime(), network.getSystemChangeTime())) {
      throw new DataExistsException("Attempt to store network, already present: " + network);
    } else {
      saveObjectAndCommit(entityManagerFactory.createEntityManager(),
          new ReferenceNetworkDao(network));
    }
  }

  @Override
  public List<ReferenceNetwork> retrieveNetworks() throws Exception {
    return retrieveAll(ReferenceNetwork.class, ReferenceNetworkDao.class,
        ReferenceNetworkDao::toCoi, Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceNetwork> retrieveNetworksByEntityId(UUID id) throws Exception {
    return retrieveAllByField("entityId", id,
        ReferenceNetwork.class, ReferenceNetworkDao.class,
        ReferenceNetworkDao::toCoi, Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceNetwork> retrieveNetworksByName(String name) throws Exception {
    Validate.notEmpty(name);
    return retrieveAllByField("name", name, ReferenceNetwork.class,
        ReferenceNetworkDao.class, ReferenceNetworkDao::toCoi,
        Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public void storeReferenceStation(ReferenceStation station) throws Exception {
    Validate.notNull(station);
    EntityManager em = entityManagerFactory.createEntityManager();
    if (exists(em, ReferenceStationDao.class, station.getName(), station.getComment(),
        station.getActualChangeTime(), station.getSystemChangeTime())) {
      logger.warn("Attempt to store station, already present: " + station);
    } else {
      saveObjectAndCommit(entityManagerFactory.createEntityManager(),
          new ReferenceStationDao(station));
    }
  }

  @Override
  public List<ReferenceStation> retrieveStations() throws Exception {
    return retrieveAll(ReferenceStation.class, ReferenceStationDao.class,
        ReferenceStationDao::toCoi, Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceStation> retrieveStationsByName(String name) throws Exception {
    Validate.notEmpty(name);
    return retrieveAllByField("name", name, ReferenceStation.class,
        ReferenceStationDao.class, ReferenceStationDao::toCoi,
        Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceStation> retrieveStationsByEntityId(UUID id) throws Exception {
    return retrieveAllByField("entityId", id, ReferenceStation.class,
        ReferenceStationDao.class, ReferenceStationDao::toCoi,
        Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceStation> retrieveStationsByVersionIds(Collection<UUID> stationVersionIds) throws Exception {
    Objects.requireNonNull(stationVersionIds, "Cannot find ReferenceStations by null or empty version ids");
    final EntityManager em = entityManagerFactory.createEntityManager();
    try {
      return em.createQuery("SELECT DISTINCT s FROM " + ReferenceStationDao.class.getSimpleName() +
              " s WHERE s.versionId IN :ids", ReferenceStationDao.class)
              .setParameter("ids", stationVersionIds)
              .getResultList()
              .stream()
              .map(ReferenceStationDao::toCoi)
              .collect(Collectors.toList());
    } catch (Exception ex) {
      throw new Exception("Exception trying to find Reference Stations by versionIds");
    } finally {
      em.close();
    }
  }

  @Override
  public void storeReferenceSite(ReferenceSite site) throws Exception {
    Validate.notNull(site);
    EntityManager em = entityManagerFactory.createEntityManager();
    if (exists(em, ReferenceSiteDao.class, site.getName(), site.getComment(),
        site.getActualChangeTime(), site.getSystemChangeTime())) {
      throw new DataExistsException("Attempt to store site, already present: " + site);
    } else {
      saveObjectAndCommit(entityManagerFactory.createEntityManager(),
          new ReferenceSiteDao(site));
    }
  }

  @Override
  public List<ReferenceSite> retrieveSites() throws Exception {
    return retrieveAll(ReferenceSite.class, ReferenceSiteDao.class,
        ReferenceSiteDao::toCoi, Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceSite> retrieveSitesByName(String name) throws Exception {
    Validate.notEmpty(name);
    return retrieveAllByField("name", name,
        ReferenceSite.class, ReferenceSiteDao.class,
        ReferenceSiteDao::toCoi, Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceSite> retrieveSitesByEntityId(UUID id) throws Exception {
    return retrieveAllByField("entityId", id,
        ReferenceSite.class, ReferenceSiteDao.class,
        ReferenceSiteDao::toCoi, Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public void storeReferenceChannel(ReferenceChannel channel) throws Exception {
    Validate.notNull(channel);
    EntityManager em = entityManagerFactory.createEntityManager();
    if (channelExists(em, channel)) {
      throw new DataExistsException("Attempt to store channel, already present: " + channel);
    } else {
      saveObjectAndCommit(entityManagerFactory.createEntityManager(),
          new ReferenceChannelDao(channel));
    }
  }

  @Override
  public List<ReferenceChannel> retrieveChannels() throws Exception {
    return retrieveAll(ReferenceChannel.class, ReferenceChannelDao.class,
        ReferenceChannelDao::toCoi, Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceChannel> retrieveChannelsByEntityId(UUID id) throws Exception {
    return retrieveAllByField("entityId", id, ReferenceChannel.class,
        ReferenceChannelDao.class, ReferenceChannelDao::toCoi,
        Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceChannel> retrieveChannelsByVersionIds(Collection<UUID> channelVersionIds) throws Exception {
    Objects.requireNonNull(channelVersionIds, "Cannot find ReferenceChannels by null or empty version ids");
    final EntityManager em = entityManagerFactory.createEntityManager();
    try {
      return em.createQuery("SELECT DISTINCT c FROM " + ReferenceChannelDao.class.getSimpleName() +
              " c WHERE c.versionId IN :ids", ReferenceChannelDao.class)
              .setParameter("ids", channelVersionIds)
              .getResultList()
              .stream()
              .map(ReferenceChannelDao::toCoi)
              .collect(Collectors.toList());
    } catch (Exception ex) {
      throw new Exception("Exception trying to find Reference Channels by versionIds");
    } finally {
      em.close();
    }
  }

  @Override
  public List<ReferenceChannel> retrieveChannelsByName(String name) throws Exception {
    return retrieveAllByField("name", name, ReferenceChannel.class,
        ReferenceChannelDao.class, ReferenceChannelDao::toCoi,
        Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public void storeReferenceDigitizer(ReferenceDigitizer digitizer) throws Exception {
    Validate.notNull(digitizer);
    EntityManager em = entityManagerFactory.createEntityManager();
    if (digitizerExists(em, digitizer)) {
      throw new DataExistsException("Attempt to store digitizer, already present: " + digitizer);
    } else {
      saveObjectAndCommit(entityManagerFactory.createEntityManager(),
          new ReferenceDigitizerDao(digitizer));
    }
  }

  @Override
  public List<ReferenceDigitizer> retrieveDigitizers() throws Exception {
    return retrieveAll(ReferenceDigitizer.class, ReferenceDigitizerDao.class,
        ReferenceDigitizerDao::toCoi, Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceDigitizer> retrieveDigitizersByEntityId(UUID id) throws Exception {
    return retrieveAllByField("entityId", id, ReferenceDigitizer.class,
        ReferenceDigitizerDao.class, ReferenceDigitizerDao::toCoi,
        Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public List<ReferenceDigitizer> retrieveDigitizersByName(String name) throws Exception {
    return retrieveAllByField("name", name, ReferenceDigitizer.class,
        ReferenceDigitizerDao.class, ReferenceDigitizerDao::toCoi,
        Sorters.byNameActualTimeSystemTime);
  }

  @Override
  public void storeCalibration(ReferenceCalibration calibration) throws Exception {
    saveObjectAndCommit(entityManagerFactory.createEntityManager(),
        new ReferenceCalibrationDao(calibration));
  }

  @Override
  public List<ReferenceCalibration> retrieveCalibrations() throws Exception {
    return retrieveAll(ReferenceCalibration.class, ReferenceCalibrationDao.class,
        ReferenceCalibrationDao::toCoi, Sorters.byActualTimeSystemTime);
  }

  @Override
  public List<ReferenceCalibration> retrieveCalibrationsByChannelId(UUID channelId)
      throws Exception {
    return retrieveAllByField("channelId", channelId, ReferenceCalibration.class,
        ReferenceCalibrationDao.class, ReferenceCalibrationDao::toCoi,
        Sorters.byActualTimeSystemTime);
  }

  @Override
  public void storeResponse(ReferenceResponse response) throws Exception {
    saveObjectAndCommit(entityManagerFactory.createEntityManager(),
        new ReferenceResponseDao(response));
  }

  @Override
  public List<ReferenceResponse> retrieveResponses() throws Exception {
    return retrieveAll(ReferenceResponse.class, ReferenceResponseDao.class,
        ReferenceResponseDao::toCoi, Sorters.byActualTimeSystemTime);
  }

  @Override
  public List<ReferenceResponse> retrieveResponsesByChannelId(UUID channelId) throws Exception {
    return retrieveAllByField("channelId", channelId, ReferenceResponse.class,
        ReferenceResponseDao.class, ReferenceResponseDao::toCoi,
        Sorters.byActualTimeSystemTime);
  }

  @Override
  public void storeSensor(ReferenceSensor sensor) throws Exception {
    saveObjectAndCommit(entityManagerFactory.createEntityManager(),
        new ReferenceSensorDao(sensor));
  }

  @Override
  public List<ReferenceSensor> retrieveSensors() throws Exception {
    return retrieveAll(ReferenceSensor.class, ReferenceSensorDao.class,
        ReferenceSensorDao::toCoi, Sorters.byActualTimeSystemTime);
  }

  @Override
  public List<ReferenceSensor> retrieveSensorsByChannelId(UUID channelId) throws Exception {
    return retrieveAllByField("channelId", channelId, ReferenceSensor.class,
        ReferenceSensorDao.class, ReferenceSensorDao::toCoi,
        Sorters.byActualTimeSystemTime);
  }

  @Override
  public void storeNetworkMemberships(Collection<ReferenceNetworkMembership> memberships)
      throws Exception {
    Validate.notNull(memberships);
    for (ReferenceNetworkMembership membership : memberships) {
      EntityManager em = entityManagerFactory.createEntityManager();
      if (networkMembershipExists(em, membership)) {
        throw new DataExistsException(
            "Attempt to store network membership, already present: " + membership);
      } else {
        saveObjectAndCommit(entityManagerFactory.createEntityManager(),
            new ReferenceNetworkMembershipDao(membership));
      }
    }

  }

  @Override
  public List<ReferenceNetworkMembership> retrieveNetworkMemberships() throws Exception {
    return retrieveAll(ReferenceNetworkMembership.class,
        ReferenceNetworkMembershipDao.class,
        ReferenceNetworkMembershipDao::toCoi, Sorters.byActualTimeSystemTime);
  }

  @Override
  public List<ReferenceNetworkMembership> retrieveNetworkMembershipsByNetworkId(UUID id)
      throws Exception {
    return retrieveAllByField("networkId", id, ReferenceNetworkMembership.class,
        ReferenceNetworkMembershipDao.class, ReferenceNetworkMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public List<ReferenceNetworkMembership> retrieveNetworkMembershipsByStationId(UUID id)
      throws Exception {
    return retrieveAllByField("stationId", id, ReferenceNetworkMembership.class,
        ReferenceNetworkMembershipDao.class, ReferenceNetworkMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public List<ReferenceNetworkMembership> retrieveNetworkMembershipsByNetworkAndStationId(
      UUID networkId, UUID stationId) throws Exception {
    return retrieveAllByFields(
        Map.of("networkId", networkId, "stationId", stationId),
        ReferenceNetworkMembership.class,
        ReferenceNetworkMembershipDao.class, ReferenceNetworkMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public void storeStationMemberships(Collection<ReferenceStationMembership> memberships)
      throws Exception {
    Validate.notNull(memberships);
    for (ReferenceStationMembership membership : memberships) {

      EntityManager em = entityManagerFactory.createEntityManager();
      if (stationMembershipExists(em, membership)) {
        throw new DataExistsException(
            "Attempt to store station membership, already present: " + membership);
      } else {
        saveObjectAndCommit(entityManagerFactory.createEntityManager(),
            new ReferenceStationMembershipDao(membership));
      }
    }

  }

  @Override
  public List<ReferenceStationMembership> retrieveStationMemberships() throws Exception {
    return retrieveAll(ReferenceStationMembership.class,
        ReferenceStationMembershipDao.class,
        ReferenceStationMembershipDao::toCoi, Sorters.byActualTimeSystemTime);
  }

  @Override
  public List<ReferenceStationMembership> retrieveStationMembershipsByStationId(UUID id)
      throws Exception {
    return retrieveAllByField("stationId", id, ReferenceStationMembership.class,
        ReferenceStationMembershipDao.class, ReferenceStationMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public List<ReferenceStationMembership> retrieveStationMembershipsBySiteId(UUID id)
      throws Exception {
    return retrieveAllByField("siteId", id, ReferenceStationMembership.class,
        ReferenceStationMembershipDao.class, ReferenceStationMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public List<ReferenceStationMembership> retrieveStationMembershipsByStationAndSiteId(
      UUID stationId, UUID siteId) throws Exception {
    return retrieveAllByFields(
        Map.of("stationId", stationId, "siteId", siteId),
        ReferenceStationMembership.class,
        ReferenceStationMembershipDao.class, ReferenceStationMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public void storeSiteMemberships(Collection<ReferenceSiteMembership> memberships)
      throws Exception {
    Validate.notNull(memberships);
    for (ReferenceSiteMembership membership : memberships) {
      EntityManager em = entityManagerFactory.createEntityManager();
      if (siteMembershipExists(em, membership)) {
        throw new DataExistsException(
            "Attempt to store site membership, already present: " + membership);
      } else {
        saveObjectAndCommit(entityManagerFactory.createEntityManager(),
            new ReferenceSiteMembershipDao(membership));
      }
    }
  }

  @Override
  public List<ReferenceSiteMembership> retrieveSiteMemberships() throws Exception {
    return retrieveAll(ReferenceSiteMembership.class,
        ReferenceSiteMembershipDao.class,
        ReferenceSiteMembershipDao::toCoi, Sorters.byActualTimeSystemTime);
  }

  @Override
  public List<ReferenceSiteMembership> retrieveSiteMembershipsBySiteId(UUID id) throws Exception {
    return retrieveAllByField("siteId", id, ReferenceSiteMembership.class,
        ReferenceSiteMembershipDao.class, ReferenceSiteMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public List<ReferenceSiteMembership> retrieveSiteMembershipsByChannelId(UUID id)
      throws Exception {
    return retrieveAllByField("channelId", id, ReferenceSiteMembership.class,
        ReferenceSiteMembershipDao.class, ReferenceSiteMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);
  }

  @Override
  public List<ReferenceSiteMembership> retrieveSiteMembershipsBySiteAndChannelId(UUID siteId,
      UUID channelId) throws Exception {
    return retrieveAllByFields(
        Map.of("siteId", siteId, "channelId", channelId),
        ReferenceSiteMembership.class,
        ReferenceSiteMembershipDao.class, ReferenceSiteMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);
  }

  @Override
  public void storeDigitizerMembership(ReferenceDigitizerMembership membership) throws Exception {
    saveObjectAndCommit(entityManagerFactory.createEntityManager(),
        new ReferenceDigitizerMembershipDao(membership));
  }

  @Override
  public List<ReferenceDigitizerMembership> retrieveDigitizerMemberships() throws Exception {
    return retrieveAll(ReferenceDigitizerMembership.class,
        ReferenceDigitizerMembershipDao.class,
        ReferenceDigitizerMembershipDao::toCoi, Sorters.byActualTimeSystemTime);
  }

  @Override
  public List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByDigitizerId(UUID id)
      throws Exception {
    return retrieveAllByField("digitizerId", id, ReferenceDigitizerMembership.class,
        ReferenceDigitizerMembershipDao.class, ReferenceDigitizerMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByChannelId(UUID id)
      throws Exception {
    return retrieveAllByField("channelId", id, ReferenceDigitizerMembership.class,
        ReferenceDigitizerMembershipDao.class, ReferenceDigitizerMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  @Override
  public List<ReferenceDigitizerMembership> retrieveDigitizerMembershipsByDigitizerAndChannelId(
      UUID digitizerId, UUID channelId) throws Exception {
    return retrieveAllByFields(
        Map.of("digitizerId", digitizerId, "channelId", channelId),
        ReferenceDigitizerMembership.class,
        ReferenceDigitizerMembershipDao.class, ReferenceDigitizerMembershipDao::toCoi,
        Sorters.byActualTimeSystemTime);

  }

  private <COI, DAO> List<COI> retrieveAll(
      Class<COI> coiClass, Class<DAO> daoClass,
      Function<DAO, COI> converter, List<String> orderBys) throws Exception {

    EntityManager entityManager = null;
    try {
      entityManager = this.entityManagerFactory.createEntityManager();

      String query = "SELECT x FROM " + daoClass.getSimpleName()
          + " x " + formatOrderBys(orderBys);
      List<DAO> daos = entityManager.createQuery(query, daoClass)
          .getResultList();

      return daos.stream()
          .map(converter)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      logger.error("Error retrieving " + coiClass + "'s", ex);
      throw StationRefRepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  private <COI, DAO> List<COI> retrieveAllByField(
      String fieldName, Object value,
      Class<COI> coiClass, Class<DAO> daoClass, Function<DAO, COI> converter,
      List<String> orderBys) throws Exception {

    EntityManager entityManager = null;
    try {
      entityManager = this.entityManagerFactory.createEntityManager();

      String query = String.format("SELECT x FROM %s x where x.%s = :value"
              + " " + formatOrderBys(orderBys),
          daoClass.getSimpleName(), fieldName);
      List<DAO> daos = entityManager.createQuery(query, daoClass)
          .setParameter("value", value)
          .getResultList();

      return daos.stream()
          .map(converter)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      logger.error("Error retrieving " + coiClass + "'s by value" + value
          + " for field " + fieldName, ex);
      throw StationRefRepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  private <COI, DAO> List<COI> retrieveAllByFields(
      Map<String, Object> fieldToValue,
      Class<COI> coiClass, Class<DAO> daoClass, Function<DAO, COI> converter,
      List<String> orderBys) throws Exception {

    EntityManager entityManager = null;
    try {
      entityManager = this.entityManagerFactory.createEntityManager();

      StringBuilder queryString = new StringBuilder(
          String.format("SELECT x FROM %s x where ", daoClass.getSimpleName()));
      Iterator<String> paramNames = fieldToValue.keySet().iterator();
      while (paramNames.hasNext()) {
        String paramName = paramNames.next();
        queryString.append(String.format("x.%s = :%s", paramName, paramName));
        // only append AND if there is another clause to add.
        if (paramNames.hasNext()) {
          queryString.append(" AND ");
        }
      }
      queryString.append(" ");
      queryString.append(formatOrderBys(orderBys));
      TypedQuery<DAO> query = entityManager.createQuery(queryString.toString(), daoClass);
      for (Entry<String, Object> param : fieldToValue.entrySet()) {
        query = query.setParameter(param.getKey(), param.getValue());
      }
      List<DAO> daos = query.getResultList();

      return daos.stream()
          .map(converter)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      logger.error("Error retrieving " + coiClass + "'s by params " + fieldToValue, ex);
      throw StationRefRepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  private static String formatOrderBys(List<String> orderBys) {
    return "ORDER BY " +
        orderBys.stream()
            .collect(Collectors.joining(", "));
  }

  private static boolean exists(EntityManager em, Class<?> daoClass,
      String name, String comment, Instant actualTime, Instant systemTime) {
    return !em
        .createQuery("SELECT x FROM " + daoClass.getSimpleName()
            + " x where x.name = :name and x.comment = :comment"
            + " AND x.actualTime = :actualTime AND x.systemTime = :systemTime")
        .setParameter("name", name)
        .setParameter("comment", comment)
        .setParameter("actualTime", actualTime)
        .setParameter("systemTime", systemTime)
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

  private static boolean channelExists(EntityManager em, ReferenceChannel chan) {
    return !em
        .createQuery("SELECT x FROM " + ReferenceChannelDao.class.getSimpleName()
            + " x WHERE x.name = :name AND x.type = :type AND x.dataType = :dataType"
            + " AND x.locationCode = :locationCode AND x.latitude = :lat AND x.longitude = :lon"
            + " AND x.elevation = :elev AND x.depth = :depth AND x.verticalElevation = :vertElev"
            + " AND x.horizontalElevation = :horizElev AND x.nominalSampleRate = :sampRate"
            + " AND x.comment = :comment"
            + " AND x.actualTime = :actualTime AND x.systemTime = :systemTime")
        .setParameter("name", chan.getName())
        .setParameter("type", chan.getType())
        .setParameter("dataType", chan.getDataType())
        .setParameter("locationCode", chan.getLocationCode())
        .setParameter("lat", chan.getLatitude())
        .setParameter("lon", chan.getLongitude())
        .setParameter("elev", chan.getElevation())
        .setParameter("depth", chan.getDepth())
        .setParameter("vertElev", chan.getVerticalAngle())
        .setParameter("horizElev", chan.getHorizontalAngle())
        .setParameter("sampRate", chan.getNominalSampleRate())
        .setParameter("comment", chan.getComment())
        .setParameter("actualTime", chan.getActualTime())
        .setParameter("systemTime", chan.getSystemTime())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

  private static boolean digitizerExists(EntityManager em, ReferenceDigitizer digi) {
    return !em
        .createQuery("SELECT x FROM " + ReferenceDigitizerDao.class.getSimpleName()
            + " x where x.name = :name AND x.model = :model AND x.manufacturer = :manufacturer"
            + " AND x.serialNumber = :serial"
            + " AND x.actualTime = :actualTime AND x.systemTime = :systemTime")
        .setParameter("name", digi.getName())
        .setParameter("model", digi.getModel())
        .setParameter("manufacturer", digi.getManufacturer())
        .setParameter("serial", digi.getSerialNumber())
        .setParameter("actualTime", digi.getActualChangeTime())
        .setParameter("systemTime", digi.getSystemChangeTime())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

  private static boolean networkMembershipExists(EntityManager em,
      ReferenceNetworkMembership membership) {
    return !em
        .createQuery("SELECT m FROM " + ReferenceNetworkMembershipDao.class.getSimpleName()
            + " m where m.networkId = :netId AND m.stationId = :staId AND m.status = :status"
            + " AND m.actualTime = :actualTime AND m.systemTime = :systemTime")
        .setParameter("netId", membership.getNetworkId())
        .setParameter("staId", membership.getStationId())
        .setParameter("actualTime", membership.getActualChangeTime())
        .setParameter("systemTime", membership.getSystemChangeTime())
        .setParameter("status", membership.getStatus())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

  private static boolean stationMembershipExists(EntityManager em,
      ReferenceStationMembership membership) {
    return !em
        .createQuery("SELECT m FROM " + ReferenceStationMembershipDao.class.getSimpleName()
            + " m where m.stationId = :staId AND m.siteId = :siteId AND m.status = :status"
            + " AND m.actualTime = :actualTime AND m.systemTime = :systemTime")
        .setParameter("staId", membership.getStationId())
        .setParameter("siteId", membership.getSiteId())
        .setParameter("actualTime", membership.getActualChangeTime())
        .setParameter("systemTime", membership.getSystemChangeTime())
        .setParameter("status", membership.getStatus())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

  private static boolean siteMembershipExists(EntityManager em,
      ReferenceSiteMembership membership) {
    return !em
        .createQuery("SELECT m FROM " + ReferenceSiteMembershipDao.class.getSimpleName()
            + " m where m.siteId = :siteId AND m.channelId = :chanId AND m.status = :status"
            + " AND m.actualTime = :actualTime AND m.systemTime = :systemTime")
        .setParameter("siteId", membership.getSiteId())
        .setParameter("chanId", membership.getChannelId())
        .setParameter("actualTime", membership.getActualChangeTime())
        .setParameter("systemTime", membership.getSystemChangeTime())
        .setParameter("status", membership.getStatus())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

}
