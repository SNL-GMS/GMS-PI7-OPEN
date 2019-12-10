package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoiceMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileMetadataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileRawStationDataFrameMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.TransferredFileRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects.TransferredFileDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects.TransferredFileInvoiceDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects.TransferredFileInvoiceMetadataDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects.TransferredFileRawStationDataFrameDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects.TransferredFileRawStationDataFrameMetadataDao;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TransferredFileRepository provides permanent, persistent storage of TransferredFiles with status
 * of either SENT or RECEIVED (i.e. those TransferredFiles appearing in a TransferredFileInvoice
 * where the corresponding data file has not been received; those TransferredFiles referencing
 * received data files that have not appeared in a TransferredFileInvoice).  TransferredFiles with
 * status of SENT are used to populate the "Gap List" display.  TransferredFiles with status of
 * SENT_AND_RECEIVED are regularly purged from the TransferredFileRepository.
 * TransferredFileRepository needs to be accessible from multiple applications (i.e. one or more
 * TransferAuditorUtility instances operating in data acquisition sequences and
 * TransferredFileRepositoryService).
 */

/**
 * Defines a class to provide persistence methods for storing, removing, and retrieving {@link
 * TransferredFile} to/from the relational database.
 */
public class TransferredFileRepositoryJpa implements TransferredFileRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(TransferredFileRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Default constructor
   */
  public TransferredFileRepositoryJpa() {
    this(CoiEntityManagerFactory.create());
  }

  /**
   * Constructor taking in the EntityManagerFactory
   *
   * @param entityManagerFactory entity manager factory
   */
  public TransferredFileRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    Objects.requireNonNull(entityManagerFactory,
        "Cannot instantiate TransferredFileRepositoryJpa with null EntityManagerFactory");
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Close the connection.
   *
   * @return true
   */
  @Override
  public boolean close() {
    if (entityManagerFactory != null) {
      entityManagerFactory.close();
    }
    return true;
  }

  /**
   * Retrieve all TransferredFiles currently stored in the database.
   *
   * @return a List of TransferredFile
   */
  @Override
  public List<TransferredFile> retrieveAll() throws Exception {
    EntityManager em = entityManagerFactory.createEntityManager();
    try {
      return em.createQuery("SELECT t FROM TransferredFileDao AS t ORDER BY transferTime ASC",
          TransferredFileDao.class)
          .getResultList()
          .stream()
          .map(TransferredFileDao::toCoi)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      throw new Exception("Error retrieving all TransferredFiles", ex);
    } finally {
      em.close();
    }

  }

  /**
   * Retrieve all TransferredFiles within a time range currently stored in the database.
   *
   * @return a List of TransferredFile
   */
  @Override
  public List<TransferredFile> retrieveByTransferTime(Instant startTime, Instant endTime)
      throws Exception {
    Objects.requireNonNull(startTime,
        "Cannot find TransferredFiles by time with null start time");
    Objects.requireNonNull(endTime,
        "Cannot find TransferredFiles by time with null end time");
    EntityManager em = entityManagerFactory.createEntityManager();
    try {
      return em.createQuery(
          "SELECT t FROM " + TransferredFileDao.class.getSimpleName()
              + " t WHERE t.transferTime >= :startTime AND t.transferTime <= :endTime",
          TransferredFileDao.class)
          .setParameter("startTime", startTime)
          .setParameter("endTime", endTime)
          .getResultList()
          .stream()
          .map(TransferredFileDao::toCoi)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      throw new Exception("Error retrieving TransferredFiles in time range", ex);
    } finally {
      em.close();
    }
  }

  @Override
  public <T extends TransferredFile> Optional<TransferredFile> find(T file) throws Exception {
    Objects.requireNonNull(file, "Cannot find from null file");
    return findDao(file).map(TransferredFileDao::toCoi);
  }

  /**
   * Finds the DAO for the given transferred file.  If it's not present in storage, empty is
   * returned.  This method only queries by comparing the 'metadata' attribute.
   *
   * @param tf the file to search for
   * @return the DAO or empty if not found
   * @throws Exception datbase errors, etc.
   */
  private Optional<TransferredFileDao> findDao(TransferredFile<?> tf) throws Exception {
    final TransferredFileMetadataType type = tf.getMetadataType();
    final Class<? extends TransferredFileDao> daoClass;
    final Object metadataDao;
    if (type.equals(TransferredFileMetadataType.RAW_STATION_DATA_FRAME)) {
      daoClass = TransferredFileRawStationDataFrameDao.class;
      metadataDao = new TransferredFileRawStationDataFrameMetadataDao(
          (TransferredFileRawStationDataFrameMetadata) tf.getMetadata());
    } else if (type.equals(TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE)) {
      daoClass = TransferredFileInvoiceDao.class;
      metadataDao = new TransferredFileInvoiceMetadataDao(
          (TransferredFileInvoiceMetadata) tf.getMetadata());
    } else {
      throw new RuntimeException("Unknown/unsupported metadata type " + type);
    }

    final EntityManager em = entityManagerFactory.createEntityManager();
    try {
      final List<? extends TransferredFileDao> daos = em.createQuery(
          "SELECT t FROM " + daoClass.getSimpleName()
              + " t WHERE t.metadata = :metadata", daoClass)
          .setParameter("metadata", metadataDao)
          .getResultList();
      if (daos.size() != 1) {
        return Optional.empty();
      }
      return Optional.of(daos.get(0));
    } catch (Exception ex) {
      throw new Exception("Error retrieving dao by metadata", ex);
    } finally {
      em.close();
    }
  }

  /**
   * Remove all TransferredFiles older than the number of seconds specified by olderThan that have
   * status SENT_AND_RECEIVED
   */
  @Override
  public void removeSentAndReceived(Duration olderThan) throws Exception {
    Objects.requireNonNull(olderThan,
        "Cannot removeSentAndReceived files when a null duration is provided");
    if (olderThan.isNegative()) {
      throw new IllegalArgumentException("olderThan must be a positive duration");
    }
    EntityManager em = entityManagerFactory.createEntityManager();
    Instant endTime = Instant.now().minus(olderThan);
    try {
      em.getTransaction().begin();
      final List<TransferredFileDao> filesToDelete = em.createQuery(
          "SELECT t FROM " + TransferredFileDao.class.getSimpleName()
              + " t WHERE t.transferTime < :endTime AND t.status = :status",
          TransferredFileDao.class)
          .setParameter("endTime", endTime)
          .setParameter("status", TransferredFileStatus.SENT_AND_RECEIVED)
          .getResultList();
      filesToDelete.forEach(em::remove);
      em.getTransaction().commit();
    } catch (Exception ex) {
      logger.error("Error removing TransferredFiles in time range", ex);
      em.getTransaction().rollback();
      throw new Exception(ex);
    } finally {
      em.close();
    }
  }

  /**
   * Store TransferredFiles to the database or update their fields.
   */
  @Override
  public void store(Collection<TransferredFile<?>> transferredFiles) throws Exception {
    Objects.requireNonNull(transferredFiles, "Cannot store null Transferred Files");
    EntityManager em = entityManagerFactory.createEntityManager();
    try {
      em.getTransaction().begin();
      for (TransferredFile<?> t : transferredFiles) {
        final TransferredFileDao dao;
        final Optional<TransferredFileDao> existing = findDao(t);
        // if present, update existing and store.
        if (existing.isPresent()) {
          dao = existing.get();
          updateDao(dao, t);
          em.merge(dao);
        } else { // not present, store anew.
          dao = makeDao(t);
        }
        em.merge(dao);
      }
      em.getTransaction().commit();
    } catch (Exception ex) {
      logger.error("Exception trying to store TransferredFile", ex);
      em.getTransaction().rollback();
      throw new Exception(ex);
    } finally {
      em.close();
    }
  }

  /**
   * Helper method for store that updates fields of TransferredFileDao
   */
  private <DaoClass extends TransferredFileDao> void updateDao(DaoClass dao,
      TransferredFile<?> tf) {
    dao.setFilename(tf.getFileName());
    dao.setPriority(tf.getPriority().orElse(null));
    dao.setStatus(tf.getStatus());
    dao.setReceptionTime(tf.getReceptionTime().orElse(null));
    dao.setTransferTime(tf.getTransferTime().orElse(null));
  }

  @SuppressWarnings("unchecked")
  private TransferredFileDao makeDao(TransferredFile<?> tf) {
    final TransferredFileMetadataType type = tf.getMetadataType();
    if (type.equals(TransferredFileMetadataType.RAW_STATION_DATA_FRAME)) {
      return new TransferredFileRawStationDataFrameDao(
          (TransferredFile<TransferredFileRawStationDataFrameMetadata>) tf);
    } else if (type.equals(TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE)) {
      return new TransferredFileInvoiceDao((TransferredFile<TransferredFileInvoiceMetadata>) tf);
    } else {
      throw new RuntimeException("Unknown/unsupported metadata type " + type);
    }
  }
}

