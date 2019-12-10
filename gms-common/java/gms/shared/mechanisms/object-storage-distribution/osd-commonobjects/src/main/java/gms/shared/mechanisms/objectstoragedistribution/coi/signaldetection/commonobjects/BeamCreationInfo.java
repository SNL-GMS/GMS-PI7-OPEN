package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AutoValue
public abstract class BeamCreationInfo {

  /**
   * Obtain the UUID of this BeamCreationInfo
   *
   * @return id, not null
   */
  public abstract UUID getId();

  /**
   * Obtain the creation time of beam object
   *
   * @return creationTime, not null
   */
  public abstract Instant getCreationTime();

  /**
   * Obtain the Channel Segment Name
   *
   * @return channel segment name, not null
   */
  public abstract String getName();

  /**
   * Obtain the UUID of the processing group used to calculate the Beam. This id return back empty
   * if the Beam was created via streaming
   *
   * @return processingGroupId, not null
   */
  public abstract Optional<UUID> getProcessingGroupId();

  /**
   * Obtain the Channel ID.
   *
   * @return channel ID, not null
   */
  public abstract UUID getChannelId();

  /**
   * Obtain the Channel Segment ID.
   *
   * @return channel segment ID, not null
   */
  public abstract UUID getChannelSegmentId();

  /**
   * Obtain the start time of the Beam
   *
   * @return reference start time, not null
   */
  public abstract Instant getRequestedStartTime();

  /**
   * Obtain the End time of the Beam
   *
   * @return reference end time, not null
   */
  public abstract Instant getRequestedEndTime();

  /**
   * Obtain the BeamDefinition used to calculate Beam
   *
   * @return beamDefinition, not null
   */
  public abstract BeamDefinition getBeamDefinition();

  /**
   * Obtain the Channel IDs used to generate the Beam
   *
   * @return list of UUIDs, not null
   */
  public abstract Set<UUID> getUsedInputChannelIds();

  public static Builder builder() {
    return new AutoValue_BeamCreationInfo.Builder();
  }

  public abstract Builder toBuilder();

  /**
   * Obtain a new {@link BeamCreationInfo} using an existing BeamCreationInfo UUID
   *
   * @param id The UUID of this BeamCreationInfo
   * @param creationTime The creation time of the waveform data used to calculate beam, not null
   * @param name Channel Segment name, not empty or null
   * @param processingGroupId UUID of Channel Processing Group used to generate the beam, not null
   * @param channelId UUID of Derived Channel containing Waveform data used to generate the beam,
   * not null
   * @param channelSegmentId UUID of Beam Channel Segment, not null
   * @param requestedStartTime Reference start time of the channel segment, not null
   * @param beamDefinition Describes parameters used to calculate beam, not null
   * @param usedInputChannelIds Set of Channel IDs used to generate the beam, not null
   * @return {@link BeamCreationInfo}, not null
   */
  @JsonCreator
  public static BeamCreationInfo from(
      @JsonProperty("id") UUID id,
      @JsonProperty("creationTime") Instant creationTime,
      @JsonProperty("name") String name,
      @JsonProperty("processingGroupId") Optional<UUID> processingGroupId,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("channelSegmentId") UUID channelSegmentId,
      @JsonProperty("requestedStartTime") Instant requestedStartTime,
      @JsonProperty("requestedEndTime") Instant requestedEndTime,
      @JsonProperty("beamDefinition") BeamDefinition beamDefinition,
      @JsonProperty("usedInputChannelIds") Set<UUID> usedInputChannelIds) {

    return builder()
        .setId(id)
        .setCreationTime(creationTime)
        .setName(name)
        .setProcessingGroupId(processingGroupId)
        .setChannelId(channelId)
        .setChannelSegmentId(channelSegmentId)
        .setRequestedStartTime(requestedStartTime)
        .setRequestedEndTime(requestedEndTime)
        .setBeamDefinition(beamDefinition)
        .setUsedInputChannelIds(usedInputChannelIds)
        .build();
  }


  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder setId(UUID id);

    public Builder generatedId() {
      return setId(UUID.randomUUID());
    }

    public abstract Builder setCreationTime(Instant creationTime);

    public abstract Builder setName(String name);

    public abstract Builder setProcessingGroupId(UUID processingGroupId);

    public abstract Builder setProcessingGroupId(Optional<UUID> processingGroupId);

    public abstract Builder setChannelId(UUID channelId);

    public abstract Builder setChannelSegmentId(UUID channelSegmentId);

    public abstract Builder setRequestedStartTime(Instant requestedStartTime);

    public abstract Builder setRequestedEndTime(Instant requestedEndTime);

    public abstract Builder setBeamDefinition(BeamDefinition beamDefinition);

    public abstract Builder setUsedInputChannelIds(Set<UUID> usedInputChannelIds);

    abstract Set<UUID> getUsedInputChannelIds();

    abstract BeamCreationInfo autoBuild();

    public BeamCreationInfo build() {
      setUsedInputChannelIds(ImmutableSet.copyOf(getUsedInputChannelIds()));
      BeamCreationInfo beamCreationInfo = autoBuild();
      Preconditions.checkState(!beamCreationInfo.getName().isEmpty(),
          "BeamCreationInfo requires non-empty name");

      return beamCreationInfo;
    }

  }


}
