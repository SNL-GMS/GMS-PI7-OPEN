package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.time.Instant;
import java.util.UUID;

/**
 * An Object used to describe a segment of Channel data. Used in place of return the actual segment
 * data itself.
 */
@AutoValue
public abstract class ChannelSegmentDescriptor {

  public abstract UUID getChannelId();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  @JsonCreator
  public static ChannelSegmentDescriptor from(
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("endTime") Instant endTime) {
    return new AutoValue_ChannelSegmentDescriptor(channelId, startTime, endTime);
  }

  public static ChannelSegmentDescriptor from(ChannelSegment<? extends Timeseries> channelSegment) {
    return new AutoValue_ChannelSegmentDescriptor(channelSegment.getChannelId(),
        channelSegment.getStartTime(), channelSegment.getEndTime());
  }
}
