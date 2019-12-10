package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "beam_creationinfo")
public class BeamCreationInfoDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(nullable = false)
  private UUID id;

  @Column(name = "creation_time")
  private Instant creationTime;

  private String name;

  @Column(name = "processing_group_id", nullable = false)
  private UUID processingGroupId;

  @Column(name="channel_id", nullable = false)
  private UUID channelId;

  @Column(name = "channel_segment_id", nullable = false)
  private UUID channelSegmentId;

  @Column(name = "requested_start_time", nullable = false)
  private Instant requestedStartTime;

  @Column(name = "requested_end_time", nullable = false)
  private Instant requestedEndTime;

  @ManyToOne(cascade = CascadeType.ALL)
  private BeamDefinitionDao beamDefinitionDao;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "beam_creationinfo_used_input_channel_ids")
  @Column(nullable = false)
  private Set<UUID> usedInputChannelIds;

  protected BeamCreationInfoDao() {
  }

  private BeamCreationInfoDao(UUID id, Instant creationTime, String name,
      UUID processingGroupId, UUID channelId, UUID channelSegmentId,
      Instant requestedStartTime, Instant requestedEndTime,
      BeamDefinitionDao beamDefinitionDao, Set<UUID> usedInputChannelIds) {
    this.id = id;
    this.creationTime = creationTime;
    this.name = name;
    this.processingGroupId = processingGroupId;
    this.channelId = channelId;
    this.channelSegmentId = channelSegmentId;
    this.requestedStartTime = requestedStartTime;
    this.requestedEndTime = requestedEndTime;
    this.beamDefinitionDao = beamDefinitionDao;
    this.usedInputChannelIds = usedInputChannelIds;
  }

  /**
   * Obtains a {@link BeamCreationInfoDao} containing the same information as a {@link
   * BeamCreationInfo}
   *
   * @param coi BeamCreationInfo to convert to a BeamCreationInfoDao, not null
   * @return beamCreationInfoDao, not null
   * @throws NullPointerException if beamCreationInfo is null
   */
  public static BeamCreationInfoDao from(BeamCreationInfo coi) {
    Objects.requireNonNull(coi,
        "Cannot create dao from a null BeamCreationInfo");
    return new BeamCreationInfoDao(
        coi.getId(),
        coi.getCreationTime(),
        coi.getName(),
        coi.getProcessingGroupId().orElse(null),
        coi.getChannelId(),
        coi.getChannelSegmentId(),
        coi.getRequestedStartTime(),
        coi.getRequestedEndTime(),
        BeamDefinitionDao.from(coi.getBeamDefinition()),
        coi.getUsedInputChannelIds());
  }

  /**
   * Creates a {@link BeamCreationInfo} containing the same information as this {@link
   * BeamCreationInfoDao}
   *
   * @return BeamCreationInfo, not null
   * @throws NullPointerException if BeamCreationInfoDao is null
   */
  public BeamCreationInfo toCoi() {

    return BeamCreationInfo
        .from(id, creationTime, name, Optional.of(processingGroupId), channelId, channelSegmentId,
            requestedStartTime, requestedEndTime, beamDefinitionDao.toCoi(), usedInputChannelIds);

  }
}
