package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.time.Instant;
import java.util.UUID;

/**
 * Wrapper class containing all needed data in order to execute BeamControl via claim
 * check.
 */
@AutoValue
public abstract class ProcessingGroupDescriptor {

  /**
   * Factory method for creating a standard ProcessingGroupDescriptor. outputChannelId maps
   * describes the relationship between Channels providing the input waveforms to be used as input
   * for the Beam and the output Channels associated with the Beams's results. No validation occurs
   * on this mapping which must be correct and valid when input to this factory.
   *
   * @param processingGroupId Id of the ChannelProcessingGroup aggregating ChannelSegments used for
   * input
   * @param startTime Start of the time range to run Beam, not null
   * @param endTime End of the time range to run Beam, not null
   * @return {@link ProcessingGroupDescriptor} used for executing BeamControl
   */
  @JsonCreator
  public static ProcessingGroupDescriptor create(
      @JsonProperty("processingGroupId") UUID processingGroupId,
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("endTime") Instant endTime) {
    return new AutoValue_ProcessingGroupDescriptor(processingGroupId, startTime, endTime);
  }

  public abstract UUID getProcessingGroupId();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();
}
