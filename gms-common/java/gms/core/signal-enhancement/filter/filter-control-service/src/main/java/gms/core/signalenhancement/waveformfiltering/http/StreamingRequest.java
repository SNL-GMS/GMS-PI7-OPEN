package gms.core.signalenhancement.waveformfiltering.http;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for the request body used in streaming invocations of {@link
 * gms.core.signalenhancement.waveformfiltering.control.FilterControl} via {@link
 * FilterControlRouteHandler#streaming(ContentType, byte[], ContentType)}
 */
@AutoValue
@JsonSerialize(as = StreamingRequest.class)
@JsonDeserialize(builder = AutoValue_StreamingRequest.Builder.class)
public abstract class StreamingRequest {

  public abstract ImmutableList<ChannelSegment<Waveform>> getChannelSegments();

  public abstract ImmutableMap<UUID, UUID> getInputToOutputChannelIds();

  public abstract ImmutableMap<String, Object> getPluginParams();

  public static Builder builder(){
    return new AutoValue_StreamingRequest.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    abstract Builder setChannelSegments(ImmutableList<ChannelSegment<Waveform>> channelSegments);

    public Builder setChannelSegments(Collection<ChannelSegment<Waveform>> channelSegments) {
      return setChannelSegments(ImmutableList.copyOf(channelSegments));
    }

    abstract ImmutableList.Builder<ChannelSegment<Waveform>> channelSegmentsBuilder();

    public Builder addChannelSegment(ChannelSegment<Waveform> channelSegment) {
      channelSegmentsBuilder().add(channelSegment);
      return this;
    }

    abstract Builder setInputToOutputChannelIds(ImmutableMap<UUID, UUID> inputToOutputChannelIds);

    public Builder setInputToOutputChannelIds(Map<UUID, UUID> inputToOutputChannelIds) {
      return setInputToOutputChannelIds(ImmutableMap.copyOf(inputToOutputChannelIds));
    }

    abstract ImmutableMap.Builder<UUID, UUID> inputToOutputChannelIdsBuilder();

    public Builder putChannelIds(UUID inputChannelId, UUID outputChannelId) {
      inputToOutputChannelIdsBuilder().put(inputChannelId, outputChannelId);
      return this;
    }

    abstract Builder setPluginParams(ImmutableMap<String, Object> pluginParams);

    public Builder setPluginParams(Map<String, Object> pluginParams) {
      return setPluginParams(ImmutableMap.copyOf(pluginParams));
    }

    abstract ImmutableMap.Builder<String, Object> pluginParamsBuilder();

    public Builder putPluginParam(String key, Object parameter) {
      pluginParamsBuilder().put(key, parameter);
      return this;
    }

    public abstract StreamingRequest build();
  }

}
