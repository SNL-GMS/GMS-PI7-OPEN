package gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.repository.ChannelSegmentsRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInfoDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.ChannelSegmentDao;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelSegmentsRepositoryJpa implements ChannelSegmentsRepository {

  private final Logger logger = LoggerFactory.getLogger(ChannelSegmentsRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  private final WaveformRepository waveformRepository;
  private final FkSpectraRepository fkSpectraRepository;


  private ChannelSegmentsRepositoryJpa(
      EntityManagerFactory entityManagerFactory,
      WaveformRepository waveformRepository,
      FkSpectraRepository fkSpectraRepository
  ) {
    this.entityManagerFactory = entityManagerFactory;
    this.waveformRepository = waveformRepository;
    this.fkSpectraRepository = fkSpectraRepository;
  }


  /**
   * Creates and returns a new {@link ChannelSegmentsRepositoryJpa} given an {@link
   * EntityManagerFactory}, {@link WaveformRepository}, and a {@link FkSpectraRepository}.
   * The provided {@link EntityManagerFactory} should be the same one that the provided {@link
   * WaveformRepository} and {@link FkSpectraRepository} were created with.
   *
   * @param entityManagerFactory {@link EntityManagerFactory} used by the returned {@link
   * ChannelSegmentsRepositoryJpa}.  Should be the same {@link EntityManagerFactory} used to create
   * the provided {@link WaveformRepository} and {@link FkSpectraRepository}
   * @param waveformRepository {@link WaveformRepository} used by the returned {@link
   * ChannelSegmentsRepositoryJpa} when querying for a {@link ChannelSegment}'s underlying {@link
   * Timeseries}
   * @param fkSpectraRepository {@link FkSpectraRepository} used by the returned {@link
   * ChannelSegmentsRepositoryJpa} when querying for a {@link ChannelSegment}'s underlying {@link
   * Timeseries}
   * @return A new {@link ChannelSegmentsRepositoryJpa}
   */
  public static ChannelSegmentsRepositoryJpa create(
      EntityManagerFactory entityManagerFactory,
      WaveformRepository waveformRepository,
      FkSpectraRepository fkSpectraRepository
  ) {

    return new ChannelSegmentsRepositoryJpa(entityManagerFactory, waveformRepository,
        fkSpectraRepository);
  }


  /**
   * Retrieves the {@link Collection} of {@link ChannelSegment} objects corresponding to the
   * provided {@link Collection} of {@link ChannelSegment} {@link UUID}s.  Optionally returns the
   * underlying {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries}
   * data.
   *
   * @param channelSegmentIds {@link Collection} of {@link ChannelSegment} {@link UUID}s for which
   * to retrieve the associated {@link ChannelSegment}s
   * @param withTimeseries {@link Boolean} denoting whether or not to return the underlying {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries data}
   * @return {@link Collection} of retrieved {@link ChannelSegment}s.  If no {@link ChannelSegment}s
   * are found for the provided {@link ChannelSegment} {@link UUID}s, this {@link Collection} will
   * be empty.
   */
  @Override
  public Collection<ChannelSegment<? extends Timeseries>> retrieveChannelSegmentsByIds(
      Collection<UUID> channelSegmentIds,
      Boolean withTimeseries) throws Exception {

    Objects.requireNonNull(channelSegmentIds,
        "ChannelSegmentsRepositoryJpa::retrieveChannelSegmentsByIds() requires non-null \"channelSegmentIds\" parameter");
    Objects.requireNonNull(withTimeseries,
        "ChannelSegmentsRepositoryJpa::retrieveChannelSegmentsByIds() requires non-null \"withTimeseries\" parameter");

    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    logger.info("Retrieving ChannelSegments by Ids.");

    List<ChannelSegmentDao> channelSegmentDaos = entityManager.createQuery(
        "SELECT cs "
            + "FROM " + ChannelSegmentDao.class.getSimpleName() + " cs "
            + "WHERE cs.id IN :ids",
        ChannelSegmentDao.class
    ).setParameter("ids", channelSegmentIds).getResultList();

    return this.channelSegmentsFromDaos(channelSegmentDaos, withTimeseries);
  }


  /**
   * Retrieves the {@link Collection} of {@link ChannelSegment}s for the provided {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}
   * {@link UUID}s between the time interval defined by the provided start time and end time
   *
   * @param channelIds {@link Collection} of {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}
   * {@link UUID}s for which to retrieve {@link ChannelSegment}s
   * @param startTime {@link Instant} denoting the beginning of the time interval for which to
   * retrieve {@link ChannelSegment}s
   * @param endTime {@link Instant} denoting the end of the time interval for which to retrieve
   * {@link ChannelSegment}s
   */
  @Override
  public Collection<ChannelSegment<? extends Timeseries>> retrieveChannelSegmentsByChannelIds(
      Collection<UUID> channelIds,
      Instant startTime, Instant endTime) throws Exception {

    Objects.requireNonNull(channelIds,
        "ChannelSegmentsRepositoryJpa::retrieveChannelSegmentsByChannelIds() requires non-null \"channelIds\" parameter");
    Objects.requireNonNull(startTime,
        "ChannelSegmentsRepositoryJpa::retrieveChannelSegmentsByChannelIds() requires non-null \"startTime\" parameter");
    Objects.requireNonNull(endTime,
        "ChannelSegmentsRepositoryJpa::retrieveChannelSegmentsByChannelIds() requires non-null \"endTime\" parameter");

    if (startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("\"startTime\" cannot be after \"endTime\"");
    }

    EntityManager entityManager = this.entityManagerFactory.createEntityManager();

    logger.info("Retrieving ChannelSegments by Channel Ids, start time, and end time.");

    TypedQuery<ChannelSegmentDao> query = entityManager
        .createQuery("SELECT cs "
                + "FROM " + ChannelSegmentDao.class.getSimpleName() + " cs "
                + "WHERE cs.channelId IN :channelIds "
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

    List<ChannelSegmentDao> channelSegmentDaos = query
        .setParameter("channelIds", channelIds)
        .setParameter("startTime", startTime)
        .setParameter("endTime", endTime)
        .getResultList();

    return this.channelSegmentsFromDaos(channelSegmentDaos, true);
  }


  // Utility method to reconstruct ChannelSegments from a provided Collection of ChannelSegmentDaos.
  //
  // If withTimeseries is set to TRUE, the Timeseries contained in the reconstructed ChannelSegment
  //   will contain no data values.
  private Collection<ChannelSegment<? extends Timeseries>> channelSegmentsFromDaos(
      Collection<ChannelSegmentDao> channelSegmentDaos,
      Boolean withTimeseries) throws Exception {

    // Create and return List<ChannelSegment> from List<ChannelSegmentDao> WITHOUT timeseries
    Collection<ChannelSegment<? extends Timeseries>> channelSegments = new ArrayList<>();

    for (ChannelSegmentDao csdao : channelSegmentDaos) {
      CreationInfoDao creationInfoDao = csdao.getCreationInfo();

      // Reconstruct SoftwareComponentInfo from Dao
      SoftwareComponentInfo softwareComponentInfo = new SoftwareComponentInfo(
          creationInfoDao.getSoftwareComponentName(),
          creationInfoDao.getSoftwareComponentVersion()
      );

      // Reconstruct CreationInfo from Dao
      CreationInfo creationInfo = new CreationInfo(
          creationInfoDao.getCreatorName(),
          creationInfoDao.getCreationTime(),
          softwareComponentInfo
      );

      List<Timeseries> timeseries = new ArrayList<>();
      if (csdao.getTimeseriesType().equals(Type.FK_SPECTRA)) {

        timeseries.addAll(
            this.fkSpectraRepository.retrieveFkSpectrasByTimeseriesIds(
                csdao.getTimeSeriesIds(),
                withTimeseries
            )
        );
      } else if (csdao.getTimeseriesType().equals(Type.WAVEFORM)) {
        timeseries.addAll(
            this.waveformRepository.retrieveWaveformsByTime(
                csdao.getChannelId(),
                csdao.getStartTime(),
                csdao.getEndTime(),
                withTimeseries
            )
        );
      }

      // Reconstruct ChannelSegment from Dao
      channelSegments.add(
          ChannelSegment.from(
              csdao.getId(),
              csdao.getChannelId(),
              csdao.getName(),
              csdao.getType(),
              csdao.getTimeseriesType(),
              timeseries,
              creationInfo
          )
      );
    }

    return channelSegments;
  }
}
