package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.primitives.Doubles;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.RepositoryException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.WaveformUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.configuration.CassandraConfig;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.ChannelSegmentDao;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class JpaCassandraWaveformRepository implements WaveformRepository {

  private static final Logger logger = LoggerFactory.getLogger(JpaCassandraWaveformRepository.class);

  private final EntityManagerFactory entityManagerFactory;
  private final CassandraConfig cassandraConfig;
  private Session session;
  private PreparedStatement preparedWaveformsInsert;
  private PreparedStatement preparedWaveformsQueryWithWaveforms;
  private PreparedStatement preparedWaveformsQueryNoWaveforms;

  static final long DOUBLES_PER_BLOCK = 100_000;

  private static final double MERGE_SAMPLE_RATE_TOLERANCE = 2.0;
  private static final double MERGE_MIN_GAP_SAMPLE_COUNT_LIMIT = 0.9;
  private static final long BILLION = 1_000_000_000L;

  public JpaCassandraWaveformRepository(EntityManagerFactory entityManagerFactory) {
    this(entityManagerFactory, CassandraConfig.builder().build());
  }

  /**
   * Default constructor.
   */
  public JpaCassandraWaveformRepository(EntityManagerFactory entityManagerFactory,
      CassandraConfig cassandraConfig) {
    this.entityManagerFactory = entityManagerFactory;
    this.cassandraConfig = cassandraConfig;
  }

  @Override
  public ChannelSegmentStorageResponse store(
      Collection<ChannelSegment<Waveform>> channelSegments) {
    Preconditions.checkNotNull(channelSegments, "Error storing channel segments: null");

    logger.info("Storing {} waveform channel segments", channelSegments.size());
    EntityManager em = entityManagerFactory.createEntityManager();
    try {
      ChannelSegmentStorageResponse.Builder response = ChannelSegmentStorageResponse
          .builder();

      ChannelSegmentDescriptor descriptor;
      for (ChannelSegment<Waveform> channelSegment : channelSegments) {
        descriptor = ChannelSegmentDescriptor.from(channelSegment);

        em.getTransaction().begin();
        try {
          em.persist(new ChannelSegmentDao(channelSegment));
          storeWaveform(channelSegment);
          em.getTransaction().commit();
          response.addStored(descriptor);
        } catch (RollbackException | RepositoryException e) {
          logger.error("Error storing channel segment", e);
          em.getTransaction().rollback();
          response.addFailed(descriptor);
        }
      }

      return response.build();
    } finally {
      em.close();
    }
  }

  private void storeWaveform(ChannelSegment<Waveform> channelSegment) {
    UUID channelId = channelSegment.getChannelId();

    List<ResultSetFuture> futures = new ArrayList<>();
    for (Waveform waveform : channelSegment.getTimeseries()) {
      futures.addAll(storeWaveformInternal(waveform, channelId));
    }
    pollFutures(futures);
  }

  private static void pollFutures(Collection<ResultSetFuture> futures) {
    Collection<ResultSetFuture> futuresToRemove = new ArrayList<>();
    while (!futures.isEmpty()) {
      for (ResultSetFuture f : futures) {
        if (f.isDone()) {
          try {
            f.getUninterruptibly();
          } catch (NoHostAvailableException e) {
            throw new StorageUnavailableException(e);
          } catch (QueryExecutionException | QueryValidationException e) {
            throw new RepositoryException(e);
          }
          futuresToRemove.add(f);
        }
      }
      futures.removeAll(futuresToRemove);
    }
  }

  private Collection<ResultSetFuture> storeWaveformInternal(Waveform waveform, UUID channelId) {
    logger.debug("Calling initializeSession in storeWaveformInternal");
    initializeSession();

    Collection<ResultSetFuture> futures = new ArrayList<>();
    List<Waveform> brokenDownWaveforms = breakIntoBlocks(waveform);
    for (Waveform wf : brokenDownWaveforms) {
      LocalDate waveformDate = LocalDate
          .fromMillisSinceEpoch(wf.getStartTime().toEpochMilli());
      List<Double> samples = Doubles.asList(wf.getValues());

      // Write the data points.
      logger.debug("storeWaveformInternal's preparedWaveformsInsert preparedId {}, routingKey {}",
          preparedWaveformsInsert.getPreparedId(), preparedWaveformsInsert.getRoutingKey());
      BoundStatement bs = preparedWaveformsInsert.bind()
          .setUUID("c", channelId)
          .setDate("d", waveformDate)
          .setLong("st", CassandraDbUtility.toEpochNano(wf.getStartTime()))
          .setLong("e", CassandraDbUtility.toEpochNano(wf.getEndTime()))
          .setLong("sc", wf.getSampleCount())
          .setDouble("sr", wf.getSampleRate())
          .setList("s", samples);

      futures.add(session.executeAsync(bs));
    }
    return futures;
  }

  /**
   * Get a list of Waveforms based on the passed parameters.
   *
   * @param channelId the id of the processing channel the waveform is for.
   * @param startTime Starting time for the time-series.
   * @param endTime Ending time for the time-series.
   * @param includeWaveformValues Flag to indicate if sample values are to be included.
   * @return A list of Waveforms.
   * @throws Exception if invalid parameters.
   */
  @Override
  public List<Waveform> retrieveWaveformsByTime(UUID channelId, Instant startTime,
      Instant endTime, boolean includeWaveformValues) throws Exception {
    Collection<UUID> uuidCollection = new ArrayList<>();
    uuidCollection.add(channelId);
    Map<UUID, List<Waveform>> result = retrieveWaveformsByTime(uuidCollection, startTime, endTime, includeWaveformValues);
    return result.containsKey(channelId) ? result.get(channelId) : List.of();
  }

  /**
   * Get a Map from UUID to Waveform objects from the database. Makes a single call to
   * Postgres and Cassandra for faster performance
   *
   * @param channelIds the ids of the processing channel the waveform is for.
   * @param startTime Starting time for the time-series.
   * @param endTime Ending time for the time-series.
   * @return A list of Waveform objects.  The list may be empty.
   * @throws Exception if invalid parameters or exception thrown by database.
   */
  @Override
  public Map<UUID, List<Waveform>> retrieveWaveformsByTime(Collection<UUID> channelIds,
      Instant startTime, Instant endTime, boolean includeWaveformValues) throws Exception {
    Validate.notNull(channelIds);
    Validate.notNull(startTime);
    Validate.notNull(endTime);

    logger.debug("Calling initializeSession in retrieveWaveformsByTime");
    initializeSession();
    final PreparedStatement ps = includeWaveformValues ?
        preparedWaveformsQueryWithWaveforms : preparedWaveformsQueryNoWaveforms;
    logger.debug("retrieveWaveformsByTime's preparedStatement preparedId {}, routingKey {}",
        ps.getPreparedId(), ps.getRoutingKey());

    final List<ResultSetFuture> callbacks = new ArrayList<>();

    // The data within Cassandra is partitioned
    // using the channel ID and the date, so we have to issue queries for each day in the
    // time range.
    for (LocalDate date : CassandraDbUtility.getCassandraDays(startTime, endTime)) {
      /* Use the start time and our ending inequality, because that is our clustering column.
      Because we always grab full blocks, this will give us all the blocks until the start
      time no longer matches the criteria
      */
      ArrayList<UUID> channelIdsList = new ArrayList<>(channelIds);
      BoundStatement bs = ps.bind()
          .setList("c", channelIdsList)
          .setDate("d", date)
          .setLong("s", CassandraDbUtility.toEpochNano(startTime))
          .setLong("e", CassandraDbUtility.toEpochNano(endTime));
      callbacks.add(session.executeAsync(bs));
    }

    ListMultimap<UUID, Waveform> uuidWaveformListMultimap = ArrayListMultimap.create();
    // Wait for the queries to finish and process the results.
    while (!callbacks.isEmpty()) {
      List<ResultSetFuture> callbacksToRemove = new ArrayList<>();
      for (ResultSetFuture callback : callbacks) {
        if (callback.isDone()) {
          callbacksToRemove.add(callback);

          for (Row r : callback.get()) {
            Instant start = CassandraDbUtility.fromEpochNano(r.getLong("start_epoch_nano"));
            Instant end = CassandraDbUtility.fromEpochNano(r.getLong("end_epoch_nano"));
            double rate = r.getDouble("sample_rate");

            if (!intersects(Pair.of(start, end), Pair.of(startTime, endTime))) {
              continue;
            }

            // Convert the Cassandra waveform points list to an array of doubles.
            Waveform wf;
            if (includeWaveformValues) {
              double[] values = r.getList("samples", Double.class).stream()
                  .mapToDouble(d -> d).toArray();
              wf = Waveform
                  .withValues(start, rate, values);
            } else {
              wf = Waveform.withoutValues(start, rate, r.getLong("sample_count"));
            }
            //Add UUID, waveform key pair to map
            uuidWaveformListMultimap.put(r.getUUID("channel_id"), wf.trim(startTime, endTime));

          } // end for rows loop
        } // end if query is done
      } // end for results loop
      callbacks.removeAll(callbacksToRemove);
    } // end while still futures to process

    //Convert to a map, merge the waveforms for each UUID
    Map<UUID, List<Waveform>> uuidWaveformListMap = new HashMap<>();
    for(UUID uuid : uuidWaveformListMultimap.keySet()){
      List<Waveform> wfs = uuidWaveformListMultimap.get(uuid);
      if (wfs.isEmpty()) {
        logger.warn("No waveforms found for query on channelId {}, start time {}, end time {}",
            channelIds, startTime, endTime);
      }
      else{
        List<Waveform> merged = WaveformUtility.mergeWaveforms(
            uuidWaveformListMultimap.get(uuid), MERGE_SAMPLE_RATE_TOLERANCE, MERGE_MIN_GAP_SAMPLE_COUNT_LIMIT);
        uuidWaveformListMap.put(uuid, merged);
      }
    }
    return uuidWaveformListMap;
  }

  @Override
  public Map<UUID, List<ChannelSegment<Waveform>>> segmentsForProcessingChannel(
      Collection<UUID> channelIds, Instant rangeStart, Instant rangeEnd,
      boolean includeWaveformValues) throws Exception {

    Validate.notEmpty(channelIds);
    Validate.notNull(rangeStart);
    Validate.notNull(rangeEnd);

    Map<UUID, List<ChannelSegmentDao>> daosByChannelId = segmentDaosByChannelIds(
        channelIds, rangeStart, rangeEnd);
    Map<UUID, List<ChannelSegment<Waveform>>> segmentsByChannelId = new HashMap<>();
    for (Entry<UUID, List<ChannelSegmentDao>> e : daosByChannelId.entrySet()) {
      List<ChannelSegment<Waveform>> segments = createChannelSegments(e.getValue(), includeWaveformValues);
      segmentsByChannelId.put(e.getKey(), segments);
    }
    return segmentsByChannelId;
  }

  @Override
  public List<ChannelSegment<Waveform>> segmentsForProcessingChannel(UUID channelId,
      Instant rangeStart, Instant rangeEnd, boolean includeWaveformValues) throws Exception {

    Map<UUID, List<ChannelSegment<Waveform>>> segmentsById = segmentsForProcessingChannel(
        List.of(channelId), rangeStart, rangeEnd, includeWaveformValues);
    return segmentsById.containsKey(channelId) ? segmentsById.get(channelId) : List.of();
  }

  @Override
  public Map<UUID, ChannelSegment<Waveform>> retrieveChannelSegments(Collection<UUID> channelIds,
      Instant rangeStart, Instant rangeEnd, boolean includeWaveformValues) throws Exception {

    Map<UUID, List<ChannelSegmentDao>> daosByChanId = segmentDaosByChannelIds(
        channelIds, rangeStart, rangeEnd);
    Map<UUID, List<Waveform>> waveformsByChanId = retrieveWaveformsByTime(
        channelIds, rangeStart, rangeEnd, includeWaveformValues);

    Map<UUID, ChannelSegment<Waveform>> newSegmentByChanId = new HashMap<>();
    for (Entry<UUID, List<ChannelSegmentDao>> e : daosByChanId.entrySet()) {
      UUID channelId = e.getKey();
      List<ChannelSegmentDao> daos = e.getValue();
      if (!daos.isEmpty()) {
        if (!ofSameTypeAndName(daos)) {
          logger.error(
              "Retrieved ChannelSegment's for channelId {} and time range [{}, {}] are of different segment types or have different names",
              channelId, rangeStart, rangeEnd);
        }
        else if (!waveformsByChanId.containsKey(channelId)) {
          logger.error(
              "Could not find waveforms for channelId {} and time range [{}, {}] even though ChannelSegments are in storage",
              channelId, rangeStart, rangeEnd);
        }
        else {
          ChannelSegment<Waveform> newSegment = ChannelSegment.create(channelId,
              daos.get(0).getName(),
              daos.get(0).getType(), waveformsByChanId.get(channelId),
              new CreationInfo("waveforms-repository", Instant.now(),
                  new SoftwareComponentInfo("waveforms-repository", "0.0.1")));
          newSegmentByChanId.put(channelId, newSegment);
        }
      }
    }
    return newSegmentByChanId;
  }

  @Override
  public Optional<ChannelSegment<Waveform>> retrieveChannelSegment(UUID channelId,
      Instant rangeStart, Instant rangeEnd, boolean includeWaveformValues) throws Exception {

    Map<UUID, ChannelSegment<Waveform>> segmentsById = retrieveChannelSegments(
        List.of(channelId), rangeStart, rangeEnd, includeWaveformValues);
    return Optional.ofNullable(segmentsById.get(channelId));
  }

  @Override
  public Map<UUID, ChannelSegment<Waveform>> retrieveChannelSegments(Collection<UUID> segmentIds,
      boolean includeWaveformValues) throws Exception {

    Validate.notNull(segmentIds);

    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    try {
      TypedQuery<ChannelSegmentDao> query = entityManager
          .createQuery("select cs from ChannelSegmentDao cs where cs.id IN (:ids)",
              ChannelSegmentDao.class);

      Map<UUID, List<ChannelSegmentDao>> daosById = query.setParameter("ids", segmentIds)
          .getResultList()
          .stream()
          .collect(Collectors.groupingBy(ChannelSegmentDao::getId));
      Map<UUID, ChannelSegment<Waveform>> segmentsById = new HashMap<>();
      for (Entry<UUID, List<ChannelSegmentDao>> e : daosById.entrySet()) {
        List<ChannelSegmentDao> daos = e.getValue();
        if (daos.size() > 1) {
          logger.error("More than one channel segment found with id {}", e.getKey());
        }
        else {
          Optional<ChannelSegment<Waveform>> segment = createChannelSegment(daos.get(0), includeWaveformValues);
          segment.ifPresent(s -> segmentsById.put(e.getKey(), s));
        }
      }
      return segmentsById;
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public Optional<ChannelSegment> retrieveChannelSegment(UUID segmentId,
      boolean includeWaveformValues) throws Exception {

    Map<UUID, ChannelSegment<Waveform>> segmentsById = retrieveChannelSegments(
        List.of(segmentId), includeWaveformValues);
    return Optional.ofNullable(segmentsById.get(segmentId));
  }

  @Override
  public Map<UUID, Double> calculateChannelAvailability(Collection<UUID> channelIdSet,
      Instant rangeStart,
      Instant rangeEnd) {

    return channelIdSet.stream()
        .collect(Collectors.toMap(Function.identity(),
            id -> calculateChannelAvailability(id, rangeStart, rangeEnd)));
  }

  private double calculateChannelAvailability(UUID channelId, Instant rangeStart,
      Instant rangeEnd) {
    // Get all channel segments for this id.
    final List<Waveform> waveforms;
    try {
      waveforms = retrieveWaveformsByTime(channelId, rangeStart, rangeEnd, false);
    } catch (Exception e) {
      logger.error("Channel Segments could not be read, defaulting availability to 0.",
          e);
      return 0.0;
    }

    Duration totalTime = Duration.between(rangeStart, rangeEnd);

    // Loop through the channel's waveforms and add up how much data we actually have.
    Duration calculatedTime = waveforms.stream()
        .reduce(Duration.ZERO,
            (d, wf) -> d.plus(Duration.between(wf.getStartTime(), wf.getEndTime())),
            Duration::plus);

    // Convert to a percent.
    return (double) calculatedTime.toMillis() / (double) totalTime.toMillis();
  }

  /**
   * Create a channel segment with its associated waveforms retrieved from the OSD.
   */
  private Optional<ChannelSegment<Waveform>> createChannelSegment(
      ChannelSegmentDao dao, boolean includeWaveformValues) throws Exception {
    List<ChannelSegment<Waveform>> segments = createChannelSegments(List.of(dao), includeWaveformValues);
    return segments.isEmpty() ? Optional.empty() : Optional.of(segments.get(0));
  }

  /**
   * Creates ChannelSegments with their associated waveforms retrieved from the OSD.
   *
   * @param segmentDaos The collection of ChannelSegmentDao objects to retrieve waveform data.
   */
  private List<ChannelSegment<Waveform>> createChannelSegments(List<ChannelSegmentDao> segmentDaos,
      boolean includeWaveformValues) throws Exception {

    List<ChannelSegment<Waveform>> segments = new ArrayList<>();

    for (ChannelSegmentDao segmentDao : segmentDaos) {
      if (segmentDao != null) {
        UUID channelId = segmentDao.getChannelId();
        Instant start = segmentDao.getStartTime(), end = segmentDao.getEndTime();
        Collection<Waveform> waveforms = this.retrieveWaveformsByTime(
            channelId, start, end, includeWaveformValues);

        if (waveforms.isEmpty()) {
          logger.warn("No waveforms found for query on channelId {}, start time {}, end time {}",
              channelId, start, end);
        }
        else {
          // Build the ChannelSegment.
          ChannelSegment<Waveform> segment = ChannelSegment.from(
              segmentDao.getId(), segmentDao.getChannelId(), segmentDao.getName(),
              segmentDao.getType(), waveforms, CreationInfo.DEFAULT);
          segments.add(segment);
        }
      }
    }
    Collections.sort(segments);  // sorts by Comparator in ChannelSegment (by start time)
    return segments;
  }

  private Map<UUID, List<ChannelSegmentDao>> segmentDaosByChannelIds(
      Collection<UUID> channelIds, Instant rangeStart, Instant rangeEnd) throws Exception {

    Validate.notEmpty(channelIds);
    Validate.notNull(rangeStart);
    Validate.notNull(rangeEnd);

    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    try {
      TypedQuery<ChannelSegmentDao> query = entityManager
          .createQuery("select cs from ChannelSegmentDao cs where cs.channelId IN (?1)"
                  + " and cs.endTime >= ?2 and cs.startTime <= ?3",
              ChannelSegmentDao.class);

      return query.setParameter(1, channelIds)
          .setParameter(2, rangeStart)
          .setParameter(3, rangeEnd)
          .getResultList()
          .stream()
          .collect(Collectors.groupingBy(ChannelSegmentDao::getChannelId));
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      entityManager.close();
    }
  }

  private boolean intersects(Pair<Instant, Instant> range1, Pair<Instant, Instant> range2) {
    return isBeforeOrEqual(range1.getLeft(), range2.getRight()) &&
        isAfterOrEqual(range1.getRight(), range2.getLeft());
  }

  private boolean isBeforeOrEqual(Instant time1, Instant time2) {
    return time1.isBefore(time2) || time1.equals(time2);
  }

  private boolean isAfterOrEqual(Instant time1, Instant time2) {
    return time1.isAfter(time2) || time1.equals(time2);
  }

  private static boolean ofSameTypeAndName(Collection<ChannelSegmentDao> segments) {
    if (segments.isEmpty()) {
      return true;
    }
    ChannelSegmentDao first = segments.iterator().next();
    ChannelSegment.Type firstType = first.getType();
    String firstName = first.getName();
    for (ChannelSegmentDao cs : segments) {
      if (!cs.getType().equals(firstType) || !cs.getName().equals(firstName)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @throws StorageUnavailableException if a connection cannot be retrieved
   */
  private void initializeSession() {
    if (this.session == null || this.session.isClosed()) {
      this.session = this.cassandraConfig.getConnection();
      // initialize all prepared statements.
      /*
        We have to use start time as our inequality criteria for these queries
        because that is the clustering column of our db. To achieve this we will manually have to:
        1) subtract the blocking size from the start time (leading to possible overfetching),
           but will guarantee that we grab the block containing our desired start time
        2) use the start time and our ending inequality. Because we always grab full blocks,
           this will give us all the blocks until the start time no longer matches the criteria
      */
      logger.debug("initializeSession\nstate: { }\ncluster: { } ", session.getState(), session.getCluster());
      this.preparedWaveformsInsert = this.session.prepare(
          "INSERT INTO "
              + cassandraConfig.timeseriesKeySpace + "." + cassandraConfig.waveformTable + " "
              + "(channel_id, date, start_epoch_nano, end_epoch_nano, sample_count, sample_rate, samples) "
              + "VALUES (:c, :d, :st, :e, :sc, :sr, :s)")
          .setConsistencyLevel(ConsistencyLevel.QUORUM);
      logger.debug("initializeSession: created preparedWaveformsInsert");

      //TODO: Remove ALLOW FILTERING ONCE DB image is optimized
      this.preparedWaveformsQueryWithWaveforms = this.session.prepare(
          "SELECT channel_id, start_epoch_nano, end_epoch_nano, sample_count, sample_rate, samples "
              + "FROM " + cassandraConfig.timeseriesKeySpace + "." + cassandraConfig.waveformTable
              + " "
              + "WHERE channel_id IN :c AND date = :d AND start_epoch_nano <= :e AND end_epoch_nano >= :s"
              + " "
              + "ALLOW FILTERING")
          .setConsistencyLevel(ConsistencyLevel.QUORUM);
      logger.debug("initializeSession: created preparedWaveformsQueryWithWaveforms");

      //TODO: Remove ALLOW FILTERING ONCE DB image is optimized
      this.preparedWaveformsQueryNoWaveforms = this.session.prepare(
          "SELECT channel_id, start_epoch_nano, end_epoch_nano, sample_count, sample_rate "
              + "FROM " + cassandraConfig.timeseriesKeySpace + "." + cassandraConfig.waveformTable
              + " "
              + "WHERE channel_id IN :c AND date = :d AND start_epoch_nano <= :e AND end_epoch_nano >= :s"
              + " "
              + "ALLOW FILTERING")
          .setConsistencyLevel(ConsistencyLevel.QUORUM);
      logger.debug("initializeSession: created preparedWaveformsQueryNoWaveforms");

    }
  }

  static List<Waveform> breakIntoBlocks(Waveform waveform) {

    long nanosPerBlock = (long) ((DOUBLES_PER_BLOCK) / waveform.getSampleRate() * BILLION);
    //since start time is included, we add 1 less than block size to get the correct amount of samples
    long blockEndNanos = (long) ((DOUBLES_PER_BLOCK - 1) / waveform.getSampleRate() * BILLION);

    Instant end;
    List<Waveform> blockWaveforms = new ArrayList<>();
    for (Instant start = waveform.getStartTime(); !start.isAfter(waveform.getEndTime());
        start = start.plusNanos(nanosPerBlock)) {

      //end time check required due to validation exceptions in Waveform.window
      end = start.plusNanos(blockEndNanos);
      if (end.isAfter(waveform.getEndTime())) {
        end = waveform.getEndTime();
      }

      blockWaveforms.add(waveform.window(start, end));
    }

    return blockWaveforms;
  }

}
