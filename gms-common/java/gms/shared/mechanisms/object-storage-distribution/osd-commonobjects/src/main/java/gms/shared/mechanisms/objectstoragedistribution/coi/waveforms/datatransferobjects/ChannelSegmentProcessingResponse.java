package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import java.util.Collection;

/**
 * A Response DTO class describing which {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment}s stored successfully, and which failed to store. References these segments via {@link ChannelSegmentDescriptor}s.
 * @see gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment
 * @see ChannelSegmentDescriptor
 */
@AutoValue
@JsonSerialize(as = ChannelSegmentProcessingResponse.class)
@JsonDeserialize(builder = AutoValue_ChannelSegmentProcessingResponse.Builder.class)
public abstract class ChannelSegmentProcessingResponse {

  public abstract ImmutableList<ChannelSegmentDescriptor> getStored();

  public abstract ImmutableList<ChannelSegmentDescriptor> getFailed();

  public abstract ImmutableList<ChannelSegmentDescriptor> getUnprocessed();

  public static Builder builder(){
    return new AutoValue_ChannelSegmentProcessingResponse.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    abstract ImmutableList.Builder<ChannelSegmentDescriptor> storedBuilder();

    public Builder addStored(ChannelSegmentDescriptor stored) {
      storedBuilder().add(stored);
      return this;
    }

    public Builder addAllStored(Collection<ChannelSegmentDescriptor> stored) {
      storedBuilder().addAll(stored);
      return this;
    }

    abstract Builder setStored(ImmutableList<ChannelSegmentDescriptor> stored);

    public Builder setStored(Collection<ChannelSegmentDescriptor> stored) {
      return setStored(ImmutableList.copyOf(stored));
    }

    abstract ImmutableList.Builder<ChannelSegmentDescriptor> failedBuilder();

    public Builder addFailed(ChannelSegmentDescriptor failed) {
      failedBuilder().add(failed);
      return this;
    }

    public Builder addAllFailed(Collection<ChannelSegmentDescriptor> failed) {
      failedBuilder().addAll(failed);
      return this;
    }

    abstract Builder setFailed(ImmutableList<ChannelSegmentDescriptor> failed);

    public Builder setFailed(Collection<ChannelSegmentDescriptor> failed) {
      return setFailed(ImmutableList.copyOf(failed));
    }

    abstract ImmutableList.Builder<ChannelSegmentDescriptor> unprocessedBuilder();

    public Builder addUnprocessed(ChannelSegmentDescriptor unprocessed) {
      unprocessedBuilder().add(unprocessed);
      return this;
    }

    abstract Builder setUnprocessed(ImmutableList<ChannelSegmentDescriptor> unprocessed);

    public Builder setUnprocessed(Collection<ChannelSegmentDescriptor> unprocessed) {
      return setFailed(ImmutableList.copyOf(unprocessed));
    }

    public abstract ChannelSegmentProcessingResponse build();
  }

}
