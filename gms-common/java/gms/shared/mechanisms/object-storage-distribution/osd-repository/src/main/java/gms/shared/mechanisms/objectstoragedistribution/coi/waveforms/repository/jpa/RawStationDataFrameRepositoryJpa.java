package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.RawStationDataFrameDao;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RawStationDataFrameRepositoryJpa implements RawStationDataFrameRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(RawStationDataFrameRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  private static final String byStationIdPayloadStartTime
          = " ORDER BY stationId ASC, payloadDataStartTime ASC ";

  /**
   * Default constructor.
   */
  public RawStationDataFrameRepositoryJpa() {
    this(CoiEntityManagerFactory.create());
  }

  public RawStationDataFrameRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  @Override
  public void storeRawStationDataFrame(RawStationDataFrame frame) throws Exception {
    Validate.notNull(frame);
    EntityManager entityManager = this.entityManagerFactory.createEntityManager();
    try {
      RawStationDataFrameDao frameDao = new RawStationDataFrameDao(frame);
      entityManager.getTransaction().begin();
      entityManager.persist(frameDao);
      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public List<RawStationDataFrame> retrieveAll(Instant start, Instant end) throws Exception {
    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    try {
      TypedQuery<RawStationDataFrameDao> query = entityManager
          .createQuery("select f from " + RawStationDataFrameDao.class.getSimpleName()
                  + " f where f.payloadDataEndTime >= :start and f.payloadDataStartTime <= :end "
                          + byStationIdPayloadStartTime,
              RawStationDataFrameDao.class);

      List<RawStationDataFrameDao> frameDaos = query
          .setParameter("start", start)
          .setParameter("end", end)
          .getResultList();
      return frameDaos.stream().map(RawStationDataFrameDao::toCoi)
          .collect(Collectors.toList());
    }
    catch(Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally{
      entityManager.close();
    }
  }

  @Override
  public List<RawStationDataFrame> retrieveByStationId(UUID stationId, Instant start,
                                                       Instant end) throws Exception {
    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    try {
      TypedQuery<RawStationDataFrameDao> query = entityManager
          .createQuery("select f from " + RawStationDataFrameDao.class.getSimpleName()
                          + " f where f.stationId = :staId and f.payloadDataEndTime >= :start"
                          + " and f.payloadDataStartTime <= :end " + byStationIdPayloadStartTime,
              RawStationDataFrameDao.class);

      List<RawStationDataFrameDao> frameDaos = query
              .setParameter("staId", stationId)
          .setParameter("start", start)
          .setParameter("end", end)
          .getResultList();
      return frameDaos.stream().map(RawStationDataFrameDao::toCoi)
          .collect(Collectors.toList());
    }
    catch(Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally{
      entityManager.close();
    }
  }
}
