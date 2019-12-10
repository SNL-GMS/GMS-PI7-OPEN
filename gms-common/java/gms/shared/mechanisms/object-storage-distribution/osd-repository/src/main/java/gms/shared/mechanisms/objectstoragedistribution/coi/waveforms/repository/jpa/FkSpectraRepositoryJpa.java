package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.extras.codecs.arrays.DoubleArrayCodec;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.ChannelProcessingGroupRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkAttributes;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.FkSpectraUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.JpaCassandraWaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.configuration.CassandraConfig;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.ChannelSegmentDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.FkSpectraDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.FkSpectrumDao;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FkSpectraRepositoryJpa implements FkSpectraRepository {

  private static final Logger logger =
      LogManager.getLogger(FkSpectraRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;
  private final CassandraConfig cassandraConfig;
  private Session cassandraSession;

  ChannelProcessingGroupRepositoryJpa channelProcessingGroupRepositoryJpa;
  JpaCassandraWaveformRepository jpaCassandraWaveformRepository;

  private PreparedStatement preparedFkSampleDataInsert;
  private PreparedStatement preparedFkSampleDataQueryByStorageId;

  //TODO: Fix these factory generations to follow new pattern
  public FkSpectraRepositoryJpa() throws Exception {
    this(CoiEntityManagerFactory.create(), CassandraConfig.builder().build());
  }

  public FkSpectraRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this(entityManagerFactory, CassandraConfig.builder().build());
  }

  public FkSpectraRepositoryJpa(CassandraConfig cassandraConfig) {
    this(CoiEntityManagerFactory.create(), cassandraConfig);
  }

  public FkSpectraRepositoryJpa(EntityManagerFactory entityManagerFactory,
      CassandraConfig cassandraConfig) {
    this.entityManagerFactory = entityManagerFactory;
    this.cassandraConfig = cassandraConfig;
    this.channelProcessingGroupRepositoryJpa =
        ChannelProcessingGroupRepositoryJpa.create(entityManagerFactory);
    this.jpaCassandraWaveformRepository =
        new JpaCassandraWaveformRepository(entityManagerFactory, cassandraConfig);
  }

  public static FkSpectraRepositoryJpa create(EntityManagerFactory entityManagerFactory) {
    Objects.requireNonNull(entityManagerFactory,
        "Cannot create FkSpectraRepositoryJpa with a null EntityManagerFactory");
    return new FkSpectraRepositoryJpa(entityManagerFactory);
  }

  @Override
  public void storeFkSpectra(ChannelSegment<FkSpectra> fkChanSeg) throws Exception {
    // Check for null inputs.
    Validate.notNull(fkChanSeg);

    // Check for the correct ChannelSegmentType.
    Validate.isTrue(fkChanSeg.getType()
            .equals(ChannelSegment.Type.FK_SPECTRA),
        "ChannelSegmentType must be of type 'FkSpectra'");

    // Check that only one FkSpectra object exists in the ChannelSegment.series list.
    Validate.isTrue(fkChanSeg.getTimeseries().size() == 1,
        String.format(
            "A ChannelSegment<FkSpectra> may only contain a single timeseries element " +
                "(expected 1, found %d).", fkChanSeg.getTimeseries().size()));

    for (FkSpectra ts : fkChanSeg.getTimeseries()) {
      // Check that the correct number of FK Spectrum objects exist in this FK Spectra.
      Validate.isTrue(ts.getSampleCount() == ts.getValues().size(),
          String.format("The number of FkSpectrum objects found in the FkSpectra " +
              "does not match the Sample Count specified in the Channel Segment object " +
              "(expected %d, found %d).", ts.getSampleCount(), ts.getValues().size()));
    }

    ChannelSegmentDao channelSegmentDao = new ChannelSegmentDao(fkChanSeg);
    List<UUID> timeSeriesIds = new ArrayList<>();
    List<FkSpectraDao> fkSpectraDaos = new ArrayList<>();

    // Store to Cassandra.
    initializeCassandraSession();
    DoubleArrayCodec codec = new DoubleArrayCodec();
    for (FkSpectra spectra : fkChanSeg.getTimeseries()) {
      FkSpectraDao fkSpectraDao = FkSpectraDao.fromCoi(spectra);
      UUID timeSeriesId = UUID.randomUUID();
      fkSpectraDao.getTimeSeries().setId(timeSeriesId);
      timeSeriesIds.add(timeSeriesId);
      for (int i = 0; i < spectra.getValues().size(); i++) {
        FkSpectrum fk = spectra.getValues().get(i);

        double[] flattenedPower = flattenArray(fk.getPower().copyOf());
        double[] flattenedFstat = flattenArray(fk.getFstat().copyOf());

        UUID storageId = UUID.randomUUID();
        fkSpectraDao.getValues().get(i).setSampleStorageId(storageId);

        BoundStatement bs = preparedFkSampleDataInsert.bind()
            .setUUID("id", storageId)
            .setBytes("pow", codec.serialize(flattenedPower, ProtocolVersion.NEWEST_SUPPORTED))
            .setBytes("fst", codec.serialize(flattenedFstat, ProtocolVersion.NEWEST_SUPPORTED))
            .setInt("d1", fk.getPower().rowCount())
            .setInt("d2", fk.getPower().columnCount());
        cassandraSession.execute(bs);
      }
      fkSpectraDaos.add(fkSpectraDao);
    }

    channelSegmentDao.setTimeSeriesIds(timeSeriesIds);

    // Store to Postgres.
    EntityManager entityManager = null;
    try {
      entityManager = this.entityManagerFactory.createEntityManager();

      if (fkChannelSegmentRecordExists(fkChanSeg)) {
        throw new DataExistsException("ChannelSegment record already persisted: " + fkChanSeg);
      }

      entityManager.getTransaction().begin();

      entityManager.persist(channelSegmentDao);
      fkSpectraDaos.forEach(entityManager::persist);

      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public boolean fkChannelSegmentRecordExists(ChannelSegment<FkSpectra> fkChannelSegment)
      throws Exception {
    EntityManager entityManager = null;

    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<ChannelSegmentDao> query = entityManager.createQuery(
          "SELECT cs FROM " + ChannelSegmentDao.class.getSimpleName() + " cs WHERE cs.id = :id ",
          ChannelSegmentDao.class);

      List<ChannelSegmentDao> queryResults = query.setParameter("id", fkChannelSegment.getId())
          .getResultList();

      if (queryResults.size() > 1) {
        throw new Exception("ChannelSegment.id value returned multiple results.");
      }

      return (!queryResults.isEmpty());
    } catch (Exception e) {
      throw RepositoryExceptionUtils.wrap(e);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public boolean fkChannelSegmentRecordsExist(
      List<ChannelSegment<FkSpectra>> fkChannelSegments) throws Exception {

    // Check that the channel segments we constructed from Fk creation info are present in the ChannelSegment table
    TypedQuery<ChannelSegmentDao> csQuery = entityManagerFactory.createEntityManager().createQuery(
        "SELECT cs FROM " + ChannelSegmentDao.class.getSimpleName() + " cs WHERE id IN :ids",
        ChannelSegmentDao.class);
    List<ChannelSegmentDao> storedFkChannelSegments = csQuery.setParameter("ids",
        fkChannelSegments.stream().map(fk -> fk.getId()).collect(Collectors.toList()))
        .getResultList();

    if (storedFkChannelSegments.size() >= fkChannelSegments.size()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Optional<ChannelSegment<FkSpectra>> retrieveFkChannelSegment(UUID channelSegmentId,
      boolean withFkSpectra)
      throws Exception {
    Validate.notNull(channelSegmentId);
    EntityManager entityManager = null;

    // Query the FK creation info object.
    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      ChannelSegmentDao channelSegmentDao = entityManager
          .createQuery("SELECT cs "
                  + "FROM " + ChannelSegmentDao.class.getSimpleName() + " cs "
                  + "WHERE cs.id = :id ",
              ChannelSegmentDao.class)
          .setParameter("id", channelSegmentId)
          .getSingleResult();

      return Optional.of(createFkSegmentFromDao(channelSegmentDao, withFkSpectra));

    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public List<ChannelSegment<FkSpectra>> segmentsForProcessingChannel(UUID channelId,
      Instant fkReferenceStartTime, Instant fkReferenceEndTime) throws Exception {
    return retrieveFkSegments(channelId, fkReferenceStartTime, fkReferenceEndTime, true);
  }

  @Override
  public Optional<ChannelSegment<FkSpectra>> retrieveFkSpectraByTime(UUID channelId,
      Instant fkReferenceStartTime, Instant fkReferenceEndTime, boolean includeSpectrum)
      throws Exception {
    List<ChannelSegment<FkSpectra>> channelSegments =
        retrieveFkSegments(channelId, fkReferenceStartTime, fkReferenceEndTime, includeSpectrum);

    if (channelSegments.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(FkSpectraUtility.mergeChannelSegments(channelSegments));
  }

  /**
   * Retrieve {@link ChannelSegment} information from PostGres and optionally from Cassandra and
   * return it as a {@link List} of segments spanning a time range
   *
   * @param channelId Channel ID associated with the desired {@link ChannelSegment}
   * @param fkReferenceStartTime Desired start time of retrieved ChannelSegments
   * @param fkReferenceEndTime Desired end time of retrieved ChannelSegments
   * @param withSpectra Flag stating whether FK power/fstat data should be retrieved
   * @return A {@link List} of {@link ChannelSegment}s spanning the time range
   * @throws Exception if there was an issue retrieving the {@link ChannelSegment}s
   */
  private List<ChannelSegment<FkSpectra>> retrieveFkSegments(UUID channelId,
      Instant fkReferenceStartTime, Instant fkReferenceEndTime, boolean withSpectra)
      throws Exception {
    Validate.notNull(channelId);
    Validate.notNull(fkReferenceStartTime);
    Validate.notNull(fkReferenceEndTime);
    Validate.isTrue(fkReferenceStartTime.compareTo(fkReferenceEndTime) < 0,
        "Start time must come before the end time.");

    EntityManager entityManager = null;

    try {

      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<ChannelSegmentDao> query = entityManager
          .createQuery("SELECT cs "
                  + "FROM " + ChannelSegmentDao.class.getSimpleName() + " cs "
                  + "WHERE cs.channelId = :channelId "
                  + "AND ("
                  //ChannelSegment bounds lie strictly within start/endTime bounds
                  + " (cs.startTime >= :startTime AND cs.endTime <= :endTime) "
                  + "OR"
                  //startTime lies within ChannelSegment bounds
                  + " (cs.startTime <= :startTime AND cs.endTime >= :startTime) "
                  + "OR"
                  //endTime lies within ChannelSegment bounds
                  + " (cs.startTime <= :endTime AND cs.endTime >= :endTime) "
                  + ")"
                  + " ORDER BY startTime ASC ",
              ChannelSegmentDao.class);

      List<ChannelSegmentDao> queryResults = query
          .setParameter("channelId", channelId)
          .setParameter("startTime", fkReferenceStartTime)
          .setParameter("endTime", fkReferenceEndTime)
          .getResultList();

      List<ChannelSegment<FkSpectra>> fkChannelSegments = new ArrayList<>();
      for (ChannelSegmentDao csDao : queryResults) {
        fkChannelSegments.add(createFkSegmentFromDao(csDao, withSpectra));
      }

      if (!this.fkChannelSegmentRecordsExist(fkChannelSegments)) {
        throw new IllegalStateException(
            "ChannelSegments constructed from calls to createFkSegmentFromDao() do not match those stored in ChannelSegments table");
      }

      return fkChannelSegments;

    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      entityManager.close();
    }
  }


  /**
   * Retrieves a {@link List} of {@link FkSpectra} objects associated with the provided {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.TimeseriesDao}
   * {@link UUID}s.  Optionally includes the underlying {@link FkSpectrum} values.
   *
   * @param ids {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.TimeseriesDao}
   * {@link UUID}s for which to retrieve the associated {@link FkSpectra}s
   * @param withValues {@link Boolean} denoting whether or not to include the underlying {@link
   * FkSpectrum} values
   * @return {@link List} of {@link FkSpectra}. Optionally includes the underlying {@link
   * FkSpectrum} values.
   */
  public List<FkSpectra> retrieveFkSpectrasByTimeseriesIds(Collection<UUID> ids, Boolean withValues)
      throws Exception {

    Objects.requireNonNull(ids,
        "FkSpectraRepositoryJpa::retrieveFkSpectrasByIds() requires non-null \"ids\" parameter");
    Objects.requireNonNull(withValues,
        "FkSpectraRepositoryJpa::retrieveFkSpectrasByIds() requires non-null \"withValues\" parameter");

    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    TypedQuery<FkSpectraDao> query = entityManager.createQuery(
        "SELECT fk FROM " + FkSpectraDao.class.getSimpleName()
            + " fk WHERE fk.timeSeries.id IN :ids ",
        FkSpectraDao.class);

    List<FkSpectraDao> retrievedFkSpectraDaos = query.setParameter("ids", ids).getResultList();

    if (withValues) {
      this.populateSpectraDaos(retrievedFkSpectraDaos);
    }

    return retrievedFkSpectraDaos.stream().map(fkSpectraDao -> {

          FkSpectra.Builder fkBuilder = FkSpectra.builder()
              .setMetadata(
                  FkSpectra.builder().metadataBuilder()
                      .setPhaseType(fkSpectraDao.getMetadata().getPhaseType())
                      .setSlowDeltaX(fkSpectraDao.getMetadata().getSlowDeltaX())
                      .setSlowDeltaY(fkSpectraDao.getMetadata().getSlowDeltaY())
                      .setSlowStartX(fkSpectraDao.getMetadata().getSlowStartX())
                      .setSlowStartY(fkSpectraDao.getMetadata().getSlowStartY())
                      .build()
              )
              .setSampleRate(fkSpectraDao.getTimeSeries().getSampleRate())
              .setStartTime(fkSpectraDao.getTimeSeries().getStartTime());

          if (withValues) {

            List<FkSpectrum> values = fkSpectraDao.getValues().stream().map(fkSpectrumDao ->
                FkSpectrum.builder()
                    .setFstat(fkSpectrumDao.getFstat())
                    .setPower(fkSpectrumDao.getPower())
                    .setQuality(fkSpectrumDao.getQuality())
                    .setAttributes(
                        fkSpectrumDao.getAttributes().stream().map(fkAttributesDao ->
                            FkAttributes.from(
                                fkAttributesDao.getAzimuth(),
                                fkAttributesDao.getSlowness(),
                                fkAttributesDao.getAzimuthUncertainty(),
                                fkAttributesDao.getSlownessUncertainty(),
                                fkAttributesDao.getPeakFStat()
                            )
                        ).collect(Collectors.toList())
                    )
                    .build()
            ).collect(Collectors.toList());

            fkBuilder.withValues(values);

          } else {

            fkBuilder.withoutValues(fkSpectraDao.getTimeSeries().getSampleCount());
          }

          return fkBuilder.build();
        }
    ).collect(Collectors.toList());
  }

  /**
   * Constructs a {@link ChannelSegment} COI object based on retrieved {@link FkSpectra}
   * information
   *
   * @param channelSegmentDao DAO object to be converted to COI
   * @param withFkSpectra Flag stating whether FK power/fstat data should be retrieved for the
   * {@link FkSpectra}
   * @return A {@link ChannelSegment} COI object populated with retrieved {@link FkSpectra}
   * information
   * @throws Exception if there was an issue retrieving the {@link FkSpectra}s or its related
   * information
   */
  private ChannelSegment<FkSpectra> createFkSegmentFromDao(ChannelSegmentDao channelSegmentDao,
      boolean withFkSpectra) throws Exception {
    List<FkSpectraDao> fkSpectraDaos = new ArrayList<>();

    EntityManager entityManager = null;
    try {

      entityManager = this.entityManagerFactory.createEntityManager();

      for (UUID id : channelSegmentDao.getTimeSeriesIds()) {
        fkSpectraDaos.add(entityManager
            .createQuery("SELECT s "
                    + "FROM " + FkSpectraDao.class.getSimpleName() + " s "
                    + "WHERE s.timeSeries.id = :id ",
                FkSpectraDao.class)
            .setParameter("id", id)
            .getSingleResult());
      }

      if (withFkSpectra) {
        populateSpectraDaos(fkSpectraDaos);
      }
      ChannelSegment<FkSpectra> channelSegment = ChannelSegment.from(
          channelSegmentDao.getId(),
          channelSegmentDao.getChannelId(),
          channelSegmentDao.getName(),
          channelSegmentDao.getType(),
          fkSpectraDaos.stream().map(FkSpectraDao::toCoi).collect(Collectors.toList()),
          CreationInfo.DEFAULT
      );
      return channelSegment;
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Populates the passed in {@link FkSpectraDao}s' {@link FkSpectrumDao}s with power and fstat data
   * from Cassandra
   *
   * @param fkSpectraDaos DAOs to be populated with power and fstat data
   * @throws Exception if there was an issue retrieving the
   */
  private void populateSpectraDaos(List<FkSpectraDao> fkSpectraDaos) throws Exception {
    initializeCassandraSession();

    try {
      // Query Cassandra for the Fk Spectrum data.
      List<Map.Entry<ResultSetFuture, FkSpectrumDao>> callbacks = new ArrayList<>();
      for (FkSpectraDao spectraDao : fkSpectraDaos) {
        for (FkSpectrumDao fk : spectraDao.getValues()) {
          final ResultSetFuture callback;
          BoundStatement bs = preparedFkSampleDataQueryByStorageId.bind()
              .setUUID("id", fk.getSampleStorageId());
          callbacks.add(Map.entry(cassandraSession.executeAsync(bs), fk));
        }
      }

      DoubleArrayCodec codec = new DoubleArrayCodec();
      List<Map.Entry<ResultSetFuture, FkSpectrumDao>> toRemove;

      while (!callbacks.isEmpty()) {
        logger.debug("Waiting for requests to complete");
        toRemove = new ArrayList<>();
        for (Map.Entry<ResultSetFuture, FkSpectrumDao> callback : callbacks) {
          if (callback.getKey().isDone()) {
            callback.getKey().get().forEach(r -> {
              callback.getValue().setPower(unflattenArray(
                  codec.deserialize(
                      r.getBytes("power"),
                      ProtocolVersion.NEWEST_SUPPORTED),
                  r.getInt("samples_d1_size"),
                  r.getInt("samples_d2_size")));
              callback.getValue().setFstat(unflattenArray(
                  codec.deserialize(
                      r.getBytes("fstat"),
                      ProtocolVersion.NEWEST_SUPPORTED),
                  r.getInt("samples_d1_size"),
                  r.getInt("samples_d2_size")));
            });
            toRemove.add(callback);
          }
        }
        callbacks.removeAll(toRemove);
      }
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    }

    //TODO: Check that the correct number of FK Spectrum samples were returned by Cassandra.
  }

  /**
   * Set up a connection to Cassandra and define the prepared queries to store/retrieve FK data
   *
   * @throws Exception if there was an issue establishing the Cassandra connection
   */
  private void initializeCassandraSession() throws Exception {
    if (this.cassandraSession == null || this.cassandraSession.isClosed()) {
      this.cassandraSession = this.cassandraConfig.getConnection();

    /*
    We have to use start time as our inequality criteria for these queries because that is the clustering column of our db
    To achieve this we will manually have to:
     1) subtract the blocking size from the start time (leading to possible overfetching), but will guarantee that we grab
        the block containing our desired start time
     2) use the start time and our ending inequality. Because we always grab full blocks, this will give us all the blocks
        until the start time no longer matches the criteria
    */
      String cassandraFkSpectraTable =
          cassandraConfig.timeseriesKeySpace + "." + cassandraConfig.fkSpectraTable;

      preparedFkSampleDataInsert = cassandraSession.prepare(
          "INSERT INTO " + cassandraFkSpectraTable + " "
              + "(id, power, fstat, samples_d1_size, samples_d2_size) "
              + "VALUES (:id, :pow, :fst, :d1, :d2)")
          .setConsistencyLevel(ConsistencyLevel.QUORUM);

      preparedFkSampleDataQueryByStorageId = cassandraSession.prepare(
          "SELECT "
              + "id, power, fstat, samples_d1_size, samples_d2_size "
              + "FROM " + cassandraFkSpectraTable + " "
              + "WHERE "
              + "id = :id ");
    }
  }

  /**
   * 'Flatten' a 2d double array into a 1d double array to be stored into Cassandra
   *
   * @param array Array to be 'flattened'
   * @return A 'flattened' 1d double array
   */
  public static double[] flattenArray(double[][] array) {
    double[] newArray = new double[array.length * array[0].length];
    int index = 0;

    for (int i = 0; i < array.length; i++) {
      System.arraycopy(array[i], 0, newArray, index, array[i].length);
      index += array[i].length;
    }

    return newArray;
  }

  /**
   * 'Unflatten' a 1d double array read from Cassandra into a 2d double array
   *
   * @param array Array to be 'unflattened'
   * @param d1Size The size of the outer dimension of the resulting 2d array
   * @param d2Size The size of the inner dimension of the resulting 2d array
   * @return An 'unflattened' 2d double array
   */
  public static double[][] unflattenArray(double[] array, int d1Size, int d2Size) {
    double[][] newArray = new double[d1Size][d2Size];

    for (int i = 0; i < d1Size; i++) {
      for (int j = 0; j < d2Size; j++) {
        newArray[i][j] = array[j + i * d2Size];
      }
    }

    return newArray;
  }
}