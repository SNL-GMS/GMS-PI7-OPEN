package gms.core.waveformqc.channelsohqc.plugin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Duration;
import java.util.List;

/**
 * Immutable parameters controlling how {@link ChannelSohQcPlugin} creates {@link
 * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask}s.
 */
@AutoValue
public abstract class ChannelSohPluginParameters {

  public abstract Duration getMergeThreshold();
  public abstract ImmutableList<AcquiredChannelSohType> getExcludedTypes();

  @JsonCreator
  public static ChannelSohPluginParameters from(
      @JsonProperty("mergeThreshold") Duration mergeThreshold,
      @JsonProperty("excludedTypes") List<AcquiredChannelSohType> excludedTypes) {
    return new AutoValue_ChannelSohPluginParameters(mergeThreshold, ImmutableList.copyOf(excludedTypes));
  }
}
