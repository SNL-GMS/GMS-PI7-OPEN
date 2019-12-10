package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra;

import com.datastax.driver.core.Session;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.ChannelProcessingGroupRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.BeamCreationInfoDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.BeamRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.configuration.CassandraConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the Beam Repository.
 */
public class BeamRepositoryCassandraJpa implements BeamRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(BeamRepositoryCassandraJpa.class);

  private final EntityManagerFactory entityManagerFactory;
  private final CassandraConfig cassandraConfig;
  private Session session;

  ChannelProcessingGroupRepositoryJpa channelProcessingGroupRepositoryJpa;
  JpaCassandraWaveformRepository jpaCassandraWaveformRepository;

  /**
   * Default constructor.
   */
  public BeamRepositoryCassandraJpa() throws Exception {
    this(CoiEntityManagerFactory.create(), CassandraConfig.builder().build());
  }

  /**
   * Constructor.
   */
  public BeamRepositoryCassandraJpa(EntityManagerFactory entityManagerFactory)
      throws Exception {
    this(entityManagerFactory, CassandraConfig.builder().build());
  }

  /**
   * Constructor.
   */
  public BeamRepositoryCassandraJpa(CassandraConfig cassandraConfig)
      throws Exception {
    this(CoiEntityManagerFactory.create(), cassandraConfig);
  }

  /**
   * Constructor.
   */
  public BeamRepositoryCassandraJpa(
      EntityManagerFactory entityManagerFactory, CassandraConfig cassandraConfig)
      throws Exception {
    this.entityManagerFactory = entityManagerFactory;
    this.cassandraConfig = cassandraConfig;
    this.channelProcessingGroupRepositoryJpa =
        ChannelProcessingGroupRepositoryJpa.create(entityManagerFactory);
    this.jpaCassandraWaveformRepository =
        new JpaCassandraWaveformRepository(entityManagerFactory, cassandraConfig);
  }

  @Override
  public void storeBeam(
      ChannelSegment<Waveform> beamChanSeg,
      BeamCreationInfo beamCreateInfo) throws Exception {

    // Check for null inputs.
    Validate.notNull(beamChanSeg);
    Validate.notNull(beamCreateInfo);

    // Check for the correct Type.
    ChannelSegment.Type segType = beamChanSeg.getType();
    Validate.isTrue(segType.equals(ChannelSegment.Type.FK_BEAM) ||
            segType.equals(ChannelSegment.Type.DETECTION_BEAM),
        "Type is incorrect.");

    // Check that the channel segment and creation info objects agree!
    String errMsg = "ChannelSegment and CreationInfo objects are incompatible: " +
        "ChannelSegment.%s must match CreationInfo.%s.";
    Validate.isTrue(beamChanSeg.getChannelId().equals(beamCreateInfo.getChannelId()),
        String.format(errMsg, "channelId", "channelId"));
    Validate.isTrue(beamChanSeg.getId().equals(beamCreateInfo.getChannelSegmentId()),
        String.format(errMsg, "id", "channelSegmentId"));

    for (Waveform ts : beamChanSeg.getTimeseries()) {
      // Check that the correct number of Waveform data points exist in this timeseries.
      Validate.isTrue(ts.getSampleCount() == ts.getValues().length,
          String.format("The number of waveform data points found in the Beam " +
              "does not match the Sample Count specified in the Channel Segment object " +
              "(expected %d, found %d).", ts.getSampleCount(), ts.getValues().length));
    }

    // Store to Postgres.
    EntityManager entityManager = null;
    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      if (beamCreationInfoRecordExists(beamCreateInfo)) {
        throw new DataExistsException(
            "BeamCreationInfo record already persisted: " + beamCreateInfo);
      }
      BeamCreationInfoDao beamCreationInfoDao = BeamCreationInfoDao.from(beamCreateInfo);
      entityManager.getTransaction().begin();
      entityManager.persist(beamCreationInfoDao);
      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }

    // Store to Cassandra.
    jpaCassandraWaveformRepository.store(beamChanSeg);
  }

  @Override
  public boolean beamCreationInfoRecordExists(BeamCreationInfo beamCreationInfo) throws Exception {
    EntityManager entityManager = null;

    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<BeamCreationInfoDao> query = entityManager
          .createQuery("SELECT f "
                  + "FROM " + BeamCreationInfoDao.class.getSimpleName() + " f "
                  + "WHERE f.id = :id ",
              BeamCreationInfoDao.class);

      List<BeamCreationInfoDao> queryResults = query
          .setParameter("id", beamCreationInfo.getId())
          .getResultList();

      if (queryResults.size() > 1) {
        throw new Exception("BeanCreationInfo.id value returned multiple results.");
      }

      return (!queryResults.isEmpty());
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public List<BeamCreationInfo> retrieveCreationInfoByProcessingGroupId(
      UUID processingGroupId) throws Exception {

    Validate.notNull(processingGroupId);
    EntityManager entityManager = null;

    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<BeamCreationInfoDao> query = entityManager
          .createQuery("SELECT f "
                  + "FROM " + BeamCreationInfoDao.class.getSimpleName() + " f "
                  + "WHERE f.processingGroupId = :processingGroupId "
                  + "ORDER BY creationTime ASC ",
              BeamCreationInfoDao.class);

      List<BeamCreationInfoDao> queryResults = query
          .setParameter("processingGroupId", processingGroupId)
          .getResultList();

      return queryResults.stream()
          .map(BeamCreationInfoDao::toCoi)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public Optional<BeamCreationInfo> retrieveCreationInfoByChannelSegmentId(
      UUID channelSegmentId) throws Exception {

    Validate.notNull(channelSegmentId);
    EntityManager entityManager = null;

    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<BeamCreationInfoDao> query = entityManager
          .createQuery("SELECT f "
                  + "FROM " + BeamCreationInfoDao.class.getSimpleName() + " f "
                  + "WHERE f.id = :id "
                  + "ORDER BY creationTime ASC ",
              BeamCreationInfoDao.class);

      List<BeamCreationInfoDao> queryResults = query
          .setParameter("id", channelSegmentId)
          .getResultList();

      if (queryResults.size() > 1) {
        throw new Exception("Channel Segment ID returned multiple results.");
      }

      return (queryResults.isEmpty()) ?
          Optional.empty() :
          Optional.of(queryResults.get(0).toCoi());
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

}
