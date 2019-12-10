package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.BeamCreationInfoDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionHypothesisDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.SignalDetectionDaoConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.SignalDetectionHypothesisDaoConverter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository class responsible for storage and retrieval operations on {@link
 * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}s
 * and their related classes via JPA and DAO objects.
 */
public class SignalDetectionRepositoryJpa implements SignalDetectionRepository {

  private static final Logger logger = LoggerFactory.getLogger(SignalDetectionRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Default constructor, requiring a DI'd EntityManagerFactory for generating EntityManagers during
   * a repository call.
   *
   * @param entityManagerFactory Factory to create new {@link EntityManager}s
   */
  private SignalDetectionRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Obtain an instance of {@link SignalDetectionRepositoryJpa} using a default {@link
   * EntityManagerFactory} with a specified persistence url
   *
   * @param persistenceUrl URL for the persistence connection, not null
   * @return SignalDetectionRepositoryJpa, not null
   */
  public static SignalDetectionRepositoryJpa from(String persistenceUrl) {
    Preconditions.checkNotNull(persistenceUrl);

    return new SignalDetectionRepositoryJpa(
        CoiEntityManagerFactory.create(Map.of("hibernate.connection.url", persistenceUrl)));
  }

  /**
   * Obtain a new {@link SignalDetectionRepositoryJpa} which uses the provided {@link
   * EntityManagerFactory}
   *
   * @param entityManagerFactory EntityManagerFactory for the filter definition entity classes, not
   * null
   * @return SignalDetectionRepositoryJpa, not null
   * @throws NullPointerException if entityManagerFactory is null
   */
  public static SignalDetectionRepositoryJpa create(EntityManagerFactory entityManagerFactory) {
    Preconditions.checkNotNull(entityManagerFactory,
        "Cannot create SignalDetectionRepositoryJpa with a null EntityManagerFactory");

    return new SignalDetectionRepositoryJpa(entityManagerFactory);
  }

  /**
   * Convenience method for handling exceptions and resource closing whenever any repository call is
   * made.
   *
   * @param consumer Any repository call returning void (e.g. store calls)
   */
  private void acceptInEntitySession(Consumer<EntityManager> consumer) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      consumer.accept(entityManager);
    } catch (PersistenceException e) {
      throw new IllegalStateException(e);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Convenience method for handling exceptions and resource closing whenever any repository call is
   * made.
   *
   * @param applier Any repository call returning not void (e.g. find calls)
   */
  private <T> T applyInEntitySession(Function<EntityManager, T> applier) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return applier.apply(entityManager);
    } catch (PersistenceException e) {
      throw new IllegalStateException(e);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public void store(SignalDetection detection) {
    Preconditions.checkNotNull(detection, "Cannot store a null SignalDetection");
    acceptInEntitySession(em -> storeInternal(em, detection));
  }

  @Override
  public Map<SignalDetectionHypothesisDescriptor, UpdateStatus> store(
      Collection<SignalDetectionHypothesisDescriptor> hypothesisDescriptors) {
    Preconditions.checkNotNull(hypothesisDescriptors, "Cannot store null hypotheses collection");

    return applyInEntitySession(em -> storeInternal(em, hypothesisDescriptors));
  }

  @Override
  public void store(BeamCreationInfo beamCreationInfo) {
    Objects.requireNonNull(beamCreationInfo, "Cannot store a null BeamCreationInfo");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    acceptInEntitySession(em -> storeInternal(entityManager, beamCreationInfo));
  }

  /**
   * Internal method used to handle storing BeamCreationInfo. *
   *
   * @param entityManager EntityManager used to handle queries and storage.
   * @param beamCreationInfo BeamCreationInfo to store.
   */
  private static void storeInternal(EntityManager entityManager,
      BeamCreationInfo beamCreationInfo) {
    // Store to Postgres.
    try {
      if (beamCreationInfoRecordExists(entityManager, beamCreationInfo)) {
        throw new DataExistsException(
            "BeamCreationInfo record already persisted: " + beamCreationInfo);
      }
      BeamCreationInfoDao beamCreationInfoDao =
          BeamCreationInfoDao.from(beamCreationInfo);
      entityManager.getTransaction().begin();
      entityManager.persist(beamCreationInfoDao);
      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      logger.error("Error storing BeamCreationInfo", ex);
      throw ex;
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  /**
   * Internal method used to handle storing SignalDetections.
   *
   * @param entityManager EntityManager used to handle queries and storage.
   * @param signalDetection SignalDetection to store.
   */
  private static void storeInternal(EntityManager entityManager, SignalDetection signalDetection) {
    SignalDetectionDao signalDetectionDao = getSignalDetectionOrCreate(entityManager,
        signalDetection);

    Function<SignalDetectionHypothesis, SignalDetectionHypothesisDao> converterClosure
        = v -> SignalDetectionHypothesisDaoConverter.toDao(signalDetectionDao, v);

    List<UUID> signalDetectionHypothesisIds = getSignalDetectionHypothesisForOwnerSignalDetectionId(
        entityManager, signalDetection.getId());

    //if empty, means a brand new SignalDetection, so skip the intersections and just persist
    boolean persistSignalDetection;
    List<SignalDetectionHypothesis> hypothesesToStore;

    if (signalDetectionHypothesisIds.isEmpty()) {
      persistSignalDetection = true;
      hypothesesToStore = signalDetection.getSignalDetectionHypotheses();
    } else {
      persistSignalDetection = false;
      hypothesesToStore = getHypothesisToStore(signalDetection, signalDetectionHypothesisIds);
    }

    List<SignalDetectionHypothesisDao> hypothesisToPersist = hypothesesToStore.stream()
        .map(converterClosure)
        .collect(Collectors.toList());

    try {
      entityManager.getTransaction().begin();
      if (persistSignalDetection) {
        entityManager.persist(signalDetectionDao);
      }

      hypothesisToPersist.forEach(entityManager::persist);
      entityManager.getTransaction().commit();
    } catch (IllegalArgumentException | PersistenceException e) {
      logger.error("Error storing SignalDetection", e);
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw e;
    }
  }

  private static Map<SignalDetectionHypothesisDescriptor, UpdateStatus> storeInternal(
      EntityManager entityManager,
      Collection<SignalDetectionHypothesisDescriptor> hypothesisDescriptors) {

    Map<SignalDetectionHypothesisDescriptor, UpdateStatus> updateStatusMap = new HashMap<>();
    entityManager.getTransaction().begin();
    for (SignalDetectionHypothesisDescriptor hypothesisDescriptor : hypothesisDescriptors) {
      TypedQuery<SignalDetectionHypothesisDao> query = entityManager.createQuery(
          "SELECT s FROM SignalDetectionHypothesisDao s WHERE s.signalDetectionHypothesisId = :id",
          SignalDetectionHypothesisDao.class);
      query.setParameter("id", hypothesisDescriptor.getSignalDetectionHypothesis().getId());

      try {
        SignalDetectionHypothesisDao originalHypothesis = query.getSingleResult();
        boolean updated = originalHypothesis
            .update(hypothesisDescriptor.getSignalDetectionHypothesis());
        updateStatusMap
            .put(hypothesisDescriptor, updated ? UpdateStatus.UPDATED : UpdateStatus.UNCHANGED);
      } catch (NoResultException ex) {
        TypedQuery<SignalDetectionDao> parentQuery = entityManager.createQuery(
            "SELECT s FROM SignalDetectionDao s WHERE s.signalDetectionId = :id",
            SignalDetectionDao.class);
        parentQuery.setParameter("id",
            hypothesisDescriptor.getSignalDetectionHypothesis().getParentSignalDetectionId());
        SignalDetectionDao parentSignalDetection = parentQuery.getSingleResult();

        SignalDetectionHypothesisDao hypothesisDao = SignalDetectionHypothesisDaoConverter
            .toDao(parentSignalDetection, hypothesisDescriptor.getSignalDetectionHypothesis());
        entityManager.persist(hypothesisDao);
        updateStatusMap.put(hypothesisDescriptor, UpdateStatus.UPDATED);
      } catch (Exception ex) {
        logger.error("Error update signal detection hypothesis", ex);
        updateStatusMap.put(hypothesisDescriptor, UpdateStatus.FAILED);
      }
    }

    entityManager.getTransaction().commit();
    return updateStatusMap;
  }

  /**
   * Queries the database for a {@link SignalDetectionDao} with an id matching the input {@link
   * SignalDetection}. If one is not found, creates a new SignalDetectionDao from the input
   * SignalDetection.
   *
   * @param entityManager Provides connection to the database.
   * @param signalDetection SignalDetectionDao we are searching for in the database.
   * @return Either a previously persisted or newly created SignalDetectionDao.
   */
  private static SignalDetectionDao getSignalDetectionOrCreate(EntityManager entityManager,
      SignalDetection signalDetection) {
    TypedQuery<SignalDetectionDao> query = entityManager
        .createQuery("SELECT s FROM SignalDetectionDao s WHERE s.signalDetectionId = :id",
            SignalDetectionDao.class);

    query.setParameter("id", signalDetection.getId());

    List<SignalDetectionDao> signalDetectionDaos = query.getResultList();

    return signalDetectionDaos.isEmpty() ? SignalDetectionDaoConverter.toDao(signalDetection)
        : signalDetectionDaos.get(0);
  }

  /**
   * Queries the database for all {@link SignalDetectionHypothesis} ids related to the input {@link
   * SignalDetection} id.
   *
   * @param entityManager Provides connection to the database.
   * @param id SignalDetection id used to search for SignalDetectionHypothesis
   * @return All Identities from SignalDetectionHypothesis that belong to the input SignalDetection.
   */
  private static List<UUID> getSignalDetectionHypothesisForOwnerSignalDetectionId(
      EntityManager entityManager,
      UUID id) {

    TypedQuery<UUID> query = entityManager
        .createQuery("SELECT s.signalDetectionHypothesisId "
                + "FROM SignalDetectionHypothesisDao s "
                + "WHERE s.parentSignalDetection.signalDetectionId = :id",
            UUID.class);

    return query.setParameter("id", id).getResultList();
  }

  /**
   * Filters out {@link SignalDetectionHypothesis} objects that have already been persisted.
   *
   * @param signalDetection SignalDetection containing signalDetectionHypotheses we wish to filter
   * out
   * @param signalDetectionHypothesisIds Ids for {@link SignalDetectionHypothesisDao} already stored
   * in the database
   * @return SignalDetectionHypotheses that need to be persisted
   */
  private static List<SignalDetectionHypothesis> getHypothesisToStore(
      SignalDetection signalDetection,
      List<UUID> signalDetectionHypothesisIds) {

    Set<UUID> hypothesisSet = signalDetection.getSignalDetectionHypotheses().stream()
        .map(SignalDetectionHypothesis::getId).collect(Collectors.toSet());

    hypothesisSet.removeAll(signalDetectionHypothesisIds);

    return signalDetection.getSignalDetectionHypotheses().stream()
        .filter(v -> hypothesisSet.contains((v.getId())))
        .collect(Collectors.toList());
  }

  /**
   * @param em Entity Manager used to query for BeamCreationInfo
   * @param beamCreationInfo Used to verify if the object already exists
   * @return True or false whether BeamCreationInfo exists
   */
  private static boolean beamCreationInfoRecordExists(
      EntityManager em, BeamCreationInfo beamCreationInfo) {
    return !em
        .createQuery("SELECT f.id "
            + "FROM " + BeamCreationInfoDao.class.getSimpleName() + " f "
            + "WHERE f.id = :id ")
        .setParameter("id", beamCreationInfo.getId())
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

  @Override
  public Collection<SignalDetection> retrieveAll() {

    Collection<SignalDetectionHypothesis> signalDetectionHypotheses = retrieveAllSignalDetectionHypothesis();

    TypedQuery<SignalDetectionDao> query = entityManagerFactory.createEntityManager()
        .createQuery("SELECT s FROM SignalDetectionDao s", SignalDetectionDao.class);
    List<SignalDetectionDao> signalDetectionDaos = query.getResultList();

    return mapSignalDetectionHypothesisToSignalDetection(signalDetectionDaos,
        signalDetectionHypotheses);
  }

  private Collection<SignalDetection> mapSignalDetectionHypothesisToSignalDetection(
      List<SignalDetectionDao> signalDetectionDaos,
      Collection<SignalDetectionHypothesis> signalDetectionHypotheses) {

    HashMap<SignalDetectionDao, List<SignalDetectionHypothesis>> signalDetectionHypothesisHashMap =
        new HashMap<>(signalDetectionDaos.size());

    for (SignalDetectionDao signalDetectionDao : signalDetectionDaos) {
      if (!signalDetectionHypothesisHashMap.containsKey(signalDetectionDao)) {
        signalDetectionHypothesisHashMap
            .put(signalDetectionDao, new ArrayList<>());
      }

      for (SignalDetectionHypothesis sdh : signalDetectionHypotheses) {
        if (signalDetectionDao.getSignalDetectionId().equals(sdh.getParentSignalDetectionId())) {
          signalDetectionHypothesisHashMap.get(signalDetectionDao).add(sdh);
        }
      }
    }

    ArrayList<SignalDetection> signalDetections = new ArrayList<>(
        signalDetectionHypothesisHashMap.size());
    for (SignalDetectionDao signalDetectionDao : signalDetectionHypothesisHashMap.keySet()) {
      signalDetections.add(SignalDetectionDaoConverter.fromDao(
          signalDetectionDao, signalDetectionHypothesisHashMap.get(signalDetectionDao)));
    }

    return signalDetections;
  }

  /**
   * Internal method called when searching for all SignalDetections to retrieve the associated
   * hypotheses.
   */
  private Collection<SignalDetectionHypothesis> retrieveAllSignalDetectionHypothesis() {
    final Function<EntityManager, Collection<SignalDetectionHypothesisDao>> findAllSignalDetectionHypothesis =
        em -> em.createQuery("SELECT s FROM SignalDetectionHypothesisDao s",
            SignalDetectionHypothesisDao.class)
            .getResultList();

    return applyInEntitySession(findAllSignalDetectionHypothesis)
        .stream()
        .map(SignalDetectionHypothesisDaoConverter::fromDao)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SignalDetection> findSignalDetectionById(UUID id) {
    Objects.requireNonNull(id, "Cannot query using a null SignalDetection id");

    List<SignalDetectionHypothesis> signalDetectionHypotheses
        = findSignalDetectionHypothesesByParentSignalDetectionId(id);

    TypedQuery<SignalDetectionDao> query = entityManagerFactory.createEntityManager()
        .createQuery("SELECT s FROM SignalDetectionDao s WHERE s.signalDetectionId = :id",
            SignalDetectionDao.class)
        .setParameter("id", id);

    List<SignalDetectionDao> daos = query.getResultList();
    if (daos.isEmpty()) {
      logger.warn("No SignalDetections found for ID = {}", id);
      return Optional.empty();
    } else if (daos.size() > 1) {
      throw new IllegalStateException(
          daos.size() + " SignalDetections returned for ID = " + id);
    }
    return Optional.of(SignalDetectionDaoConverter.fromDao(daos.get(0), signalDetectionHypotheses));
  }

  @Override
  public List<SignalDetection> findSignalDetectionsByIds(Collection<UUID> ids) {
    Objects.requireNonNull(ids, "Cannot find signal detections by null ids");
    TypedQuery<SignalDetectionDao> query = entityManagerFactory.createEntityManager()
        .createQuery("SELECT DISTINCT sd"
                + " FROM SignalDetectionDao sd"
                + " INNER JOIN SignalDetectionHypothesisDao sdh"
                + " ON sd = sdh.parentSignalDetection"
                + " WHERE sd.signalDetectionId IN (:ids)",
            SignalDetectionDao.class)
        .setParameter("ids", ids);
    List<SignalDetectionDao> daos = query.getResultList();

    List<SignalDetection> signalDetections = new ArrayList<>(daos.size());
    for (int i = 0; i < daos.size(); i++) {

      SignalDetectionDao dao = daos.get(i);
      List<SignalDetectionHypothesis> signalDetectionHypotheses
          = findSignalDetectionHypothesesByParentSignalDetectionId(dao.getSignalDetectionId());

      SignalDetection signalDetection = SignalDetectionDaoConverter
          .fromDao(dao, signalDetectionHypotheses);
      signalDetections.add(signalDetection);
    }

    return signalDetections;
  }

  @Override
  public List<SignalDetectionHypothesis> findSignalDetectionHypothesesByIds(Collection<UUID> ids) {
    Objects.requireNonNull(ids, "Cannot find signal detections by null hypothesis ids");
    TypedQuery<SignalDetectionHypothesisDao> query = entityManagerFactory
        .createEntityManager().createQuery(
            "SELECT s FROM SignalDetectionHypothesisDao s WHERE s.signalDetectionHypothesisId IN (:ids)",
            SignalDetectionHypothesisDao.class)
        .setParameter("ids", ids);
    return query.getResultList().stream().map(SignalDetectionHypothesisDaoConverter::fromDao)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SignalDetectionHypothesis> findSignalDetectionHypothesisById(UUID id) {
    Validate.notNull(id, "cannot find signal detection hypothesis by null id");
    TypedQuery<SignalDetectionHypothesisDao> query = entityManagerFactory
        .createEntityManager()
        .createQuery(
            "SELECT s FROM SignalDetectionHypothesisDao s WHERE s.signalDetectionHypothesisId = :id",
            SignalDetectionHypothesisDao.class)
        .setParameter("id", id);
    List<SignalDetectionHypothesisDao> daos = query.getResultList();
    if (daos.isEmpty()) {
      logger.warn("No SignalDetectionHypotheses found for ID = {}", id);
      return Optional.empty();
    } else if (daos.size() > 1) {
      throw new IllegalStateException(
          daos.size() + " SignalDetectionHypotheses returned for ID = " + id);
    }
    return Optional.of(SignalDetectionHypothesisDaoConverter.fromDao(daos.get(0)));
  }

  @Override
  public List<SignalDetection> findSignalDetections(
      Instant start, Instant end) {

    TypedQuery<SignalDetectionDao> query = entityManagerFactory.createEntityManager()
        .createQuery(
            "SELECT DISTINCT sd"
                + " FROM SignalDetectionDao sd"
                + " INNER JOIN SignalDetectionHypothesisDao sdh"
                + " ON sd = sdh.parentSignalDetection"
                + " WHERE sdh.arrivalTimeMeasurement.value.time >= :start"
                + " AND sdh.arrivalTimeMeasurement.value.time <= :end",
            SignalDetectionDao.class)
        .setParameter("start", start)
        .setParameter("end", end);

    List<SignalDetectionDao> daos = query.getResultList();
    return daos.stream()
        .map(d -> SignalDetectionDaoConverter.fromDao(
            d, findSignalDetectionHypothesesByParentSignalDetectionId(d.getSignalDetectionId())))
        .collect(Collectors.toList());
  }

  @Override
  public Map<UUID, List<SignalDetection>> findSignalDetectionsByStationIds(
      Collection<UUID> stationIds, Instant start, Instant end) {
    TypedQuery<SignalDetectionDao> query = entityManagerFactory.createEntityManager()
        .createQuery(
            "SELECT DISTINCT sd"
                + " FROM SignalDetectionDao sd"
                + " INNER JOIN SignalDetectionHypothesisDao sdh"
                + " ON sd = sdh.parentSignalDetection"
                + " WHERE sd.stationId IN (:stationIds)"
                + " AND sdh.arrivalTimeMeasurement.value.time >= :start"
                + " AND sdh.arrivalTimeMeasurement.value.time <= :end",
            SignalDetectionDao.class)
        .setParameter("stationIds", stationIds)
        .setParameter("start", start)
        .setParameter("end", end);

    return query.getResultList().stream()
        .map(d -> SignalDetectionDaoConverter.fromDao(
            d, findSignalDetectionHypothesesByParentSignalDetectionId(d.getSignalDetectionId())))
        .collect(Collectors.groupingBy(SignalDetection::getStationId));
  }

  /**
   * Internal method called when searching for SignalDetections to retrieve the associated
   * hypotheses.
   *
   * @param parentId The ID of the parent SignalDetection
   */
  private List<SignalDetectionHypothesis> findSignalDetectionHypothesesByParentSignalDetectionId(
      UUID parentId) {

    TypedQuery<SignalDetectionHypothesisDao> query = entityManagerFactory.createEntityManager()
        .createQuery(
            "SELECT s FROM SignalDetectionHypothesisDao s WHERE s.parentSignalDetection.signalDetectionId = :id",
            SignalDetectionHypothesisDao.class);
    query.setParameter("id", parentId);

    return query.getResultList()
        .stream()
        .map(SignalDetectionHypothesisDaoConverter::fromDao)
        .collect(Collectors.toList());
  }
}
