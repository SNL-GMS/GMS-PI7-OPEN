package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.ParameterValidation;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskVersionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.QcMaskDaoConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.QcMaskVersionDaoConverter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository class responsible for storage and retrieval operations on {@link QcMask}s and their
 * related classes via JPA and DAO objects.
 */
public class QcMaskRepositoryJpa implements QcMaskRepository {

  private static final Logger logger = LoggerFactory.getLogger(QcMaskRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Obtain an instance of {@link QcMaskRepositoryJpa} using a default {@link EntityManagerFactory}
   */
  public QcMaskRepositoryJpa() {
    this(CoiEntityManagerFactory.create());
  }

  /**
   * Default constructor, requiring a DI'd EntityManagerFactory for generating EntityManagers during
   * a repository call.
   *
   * @param entityManagerFactory Factory to create new {@link EntityManager}s
   */
  private QcMaskRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public static QcMaskRepositoryJpa create(EntityManagerFactory entityManagerFactory) {
    Objects.requireNonNull(entityManagerFactory,
        "Cannot create QcMaskRepositoryJpa with a null EntityManagerFactory");

    return new QcMaskRepositoryJpa(entityManagerFactory);
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
      throw new RuntimeException(e);
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
      throw new RuntimeException(e);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public void store(QcMask qcMask) {
    Objects.requireNonNull(qcMask, "Cannot store a null QcMask");
    acceptInEntitySession(em -> storeInternal(em, qcMask));
  }

  /**
   * Internal method used to handle storing QcMasks.
   *
   * @param entityManager EntityManager used to handle queries and storage.
   * @param qcMask QcMask to store.
   */
  private static void storeInternal(EntityManager entityManager, QcMask qcMask) {
    QcMaskDao qcMaskDao = getQcMaskOrCreate(entityManager, qcMask);
    Function<QcMaskVersion, QcMaskVersionDao> converterClosure
        = v -> QcMaskVersionDaoConverter.toDao(qcMaskDao, v);

    List<Long> versionDaoIds = getQcMaskVersionVersionsForOwnerQcMaskId(entityManager,
        qcMask.getId());

    //if empty, means a brand new qcmask, so skip the intersections and just persist
    final boolean persistQcMask;
    final List<QcMaskVersion> versionsToStore;

    if (versionDaoIds.isEmpty()) {
      persistQcMask = true;
      versionsToStore = qcMask.qcMaskVersions().collect(Collectors.toList());
    } else {
      persistQcMask = false;
      versionsToStore = getVersionsToStore(qcMask, versionDaoIds);
    }

    List<QcMaskVersionDao> versionsToPersist = versionsToStore.stream()
        .map(converterClosure)
        .collect(Collectors.toList());

    try {
      entityManager.getTransaction().begin();
      if (persistQcMask) {
        entityManager.persist(qcMaskDao);
      }

      versionsToPersist.forEach(entityManager::persist);
      entityManager.getTransaction().commit();
    } catch (IllegalArgumentException | PersistenceException e) {
      logger.error("Error storing QcMasks", e);
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw e;
    }
  }

  @Override
  public List<QcMask> findCurrentByChannelIdAndTimeRange(UUID channelId,
      Instant startTime, Instant endTime) {
    Objects.requireNonNull(channelId, "Cannot query by null ProcessingChannel Id");
    Objects.requireNonNull(startTime, "Cannot query by null start time");
    Objects.requireNonNull(endTime, "Cannot query by null end time");
    ParameterValidation.requireFalse(Instant::isAfter, startTime, endTime,
        "Cannot query for invalid time range: start must be less than or equal to end");
    return applyInEntitySession(
        em -> findByChannelIdAndTimeRangeInternal(em, channelId, startTime,
            endTime));
  }

  /**
   * Internal method used to handle retrieving QcMasks by ProcessingChannel Id and valid within the
   * given time range.
   *
   * @param entityManager EntityManager used to handle queries and storage.
   * @param channelId ProcessingChannel id used as part of our search criteria.
   * @param startTime Start of the time range we are searching in.
   * @param endTime End of the time range we are searching in.
   * @return All QcMasks meeting our query requirements.
   */
  private static List<QcMask> findByChannelIdAndTimeRangeInternal(
      EntityManager entityManager, UUID channelId, Instant startTime,
      Instant endTime) {

    List<QcMaskVersionDao> latestQcMaskVersionDaos =
        getLatestQcMaskVersionsByChannelIdWithinTimeRange(
            entityManager, channelId, startTime, endTime);

    //The versions should be unique per QcMask, so we can just iterate instead from first grouping
    List<QcMask> qcMasks = new ArrayList<>();
    for (QcMaskVersionDao qcMaskVersionDao : latestQcMaskVersionDaos) {
      QcMask qcMask = QcMaskDaoConverter.fromDao(qcMaskVersionDao.getOwnerQcMask(),
          qcMaskVersionDao);
      qcMasks.add(qcMask);
    }

    return qcMasks;
  }

  /**
   * Selects the latest version of a {@link QcMaskVersionDao} from the database for each, {@link
   * QcMaskDao} for the corresponding {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}
   * id and time range.
   *
   * @param entityManager Provides connection to the database.
   * @param channelId Id for the processing channel the mask was made on.
   * @param startTime Inclusive start time for the range we are searching for qc masks.
   * @param endTime Inclusive end time for the range we are searching for qc masks.
   * @return All QcMaskVersionDaos meeting our search criteria.
   */
  private static List<QcMaskVersionDao> getLatestQcMaskVersionsByChannelIdWithinTimeRange(
      EntityManager entityManager, UUID channelId, Instant startTime,
      Instant endTime) {
    TypedQuery<QcMaskVersionDao> query = entityManager
        .createQuery(
            "SELECT v FROM QcMaskVersionDao v "
                + "WHERE v.ownerQcMask.channelId = :id "
                + "AND v.endTime >= :start AND v.startTime <= :end "
                + "AND v.version = (SELECT MAX(vv.version) from QcMaskVersionDao vv "
                + "WHERE vv.ownerQcMask = v.ownerQcMask)",
            QcMaskVersionDao.class);

    query.setParameter("id", channelId);
    query.setParameter("start", startTime);
    query.setParameter("end", endTime);

    return query.getResultList();
  }

  /**
   * Queries the database for a {@link QcMaskDao} with an id matching the input {@link QcMask}. If
   * one is not found, creates a new QcMaskDao from the input QcMask.
   *
   * @param entityManager Provides connection to the database.
   * @param qcMask QcMask we are searching for in the database.
   * @return Either a previously persisted or newly created QcMaskDao.
   */
  private static QcMaskDao getQcMaskOrCreate(EntityManager entityManager, QcMask qcMask) {
    TypedQuery<QcMaskDao> query = entityManager
        .createQuery("SELECT m FROM QcMaskDao m WHERE m.id = :id", QcMaskDao.class);

    query.setParameter("id", qcMask.getId());

    List<QcMaskDao> qcMaskDaos = query.getResultList();

    return qcMaskDaos.isEmpty() ? QcMaskDaoConverter.toDao(qcMask) : qcMaskDaos.get(0);
  }

  /**
   * Queries the database for all {@link QcMaskVersion} version ids related to the input {@link
   * QcMask} id.
   *
   * @param entityManager Provides connection to the database.
   * @param id QcMask id used to search for QcMaskVersions
   * @return All version Identities from QcMaskVersions that belong to the input QcMask.
   */
  private static List<Long> getQcMaskVersionVersionsForOwnerQcMaskId(
      EntityManager entityManager,
      UUID id) {

    TypedQuery<Long> query = entityManager
        .createQuery("SELECT v.version FROM QcMaskVersionDao v WHERE v.ownerQcMask.id = :id",
            Long.class);

    return query.setParameter("id", id).getResultList();
  }

  /**
   * Filters out {@link QcMaskVersion} objects that have already been persisted.
   *
   * @param qcMask QcMask containing version we wish to filter out
   * @param versionDaoIds Ids for {@link QcMaskVersionDao} already stored in the database
   * @return QcMaskVersions that need to be persisted
   */
  private static List<QcMaskVersion> getVersionsToStore(QcMask qcMask,
      List<Long> versionDaoIds) {

    Set<Long> versionSet = qcMask.qcMaskVersions()
        .map(QcMaskVersion::getVersion)
        .collect(Collectors.toSet());

    versionSet.removeAll(versionDaoIds);

    return qcMask.qcMaskVersions()
        .filter(v -> versionSet.contains((v.getVersion())))
        .collect(Collectors.toList());
  }

}
