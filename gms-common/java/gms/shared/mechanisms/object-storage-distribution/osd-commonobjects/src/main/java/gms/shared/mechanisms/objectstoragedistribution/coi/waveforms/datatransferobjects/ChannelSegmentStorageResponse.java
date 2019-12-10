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
@JsonSerialize(as = ChannelSegmentStorageResponse.class)
@JsonDeserialize(builder = AutoValue_ChannelSegmentStorageResponse.Builder.class)
public abstract class ChannelSegmentStorageResponse {

  public abstract ImmutableList<ChannelSegmentDescriptor> getStored();

  public abstract ImmutableList<ChannelSegmentDescriptor> getFailed();

  public static Builder builder(){
    return new AutoValue_ChannelSegmentStorageResponse.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    abstract ImmutableList.Builder<ChannelSegmentDescriptor> storedBuilder();

    public Builder addStored(ChannelSegmentDescriptor succeeded) {
      storedBuilder().add(succeeded);
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

    abstract Builder setFailed(ImmutableList<ChannelSegmentDescriptor> failed);

    public Builder setFailed(Collection<ChannelSegmentDescriptor> failed) {
      return setFailed(ImmutableList.copyOf(failed));
    }

    public abstract ChannelSegmentStorageResponse build();
  }

}
