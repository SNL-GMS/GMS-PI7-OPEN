package gms.core.signalenhancement.fk.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Wrapper class containing all needed data in order to execute {@link FkControl}
 * via spectra.
 */
@AutoValue
public abstract class FkSpectraCommand {

  private static final String LOG_STRING =
      "Output channel ID: %s\n" +
      "Channel IDs: %s\n" +
      "Start time: %s\n" +
      "Slow start: (%s, %s)\n" +
      "Slow delta: (%s, %s)\n" +
      "Slow count: (%s, %s)\n" +
      "Frequency band (low, high): %s, %s\n" +
      "Use channel vertical offset: %s\n\n" + 
      "Window lead: %s\n" +
      "Window length: %s\n" +
      "Phase type: %s\n" +
      "Sample rate: %s\n" +
      "Sample count: %s\n";


  public abstract Instant getStartTime();

  public abstract Double getSampleRate();

  public abstract Long getSampleCount();

  public abstract Set<UUID> getChannelIds();

  public abstract Duration getWindowLead();

  public abstract Duration getWindowLength();

  public abstract Double getLowFrequency();

  public abstract Double getHighFrequency();

  public abstract Boolean getUseChannelVerticalOffset();

  public abstract Boolean getNormalizeWaveforms();

  public abstract PhaseType getPhaseType();

  public abstract Optional<Double> getSlowStartX();

  public abstract Optional<Double> getSlowDeltaX();

  public abstract Optional<Integer> getSlowCountX();

  public abstract Optional<Double> getSlowStartY();

  public abstract Optional<Double> getSlowDeltaY();

  public abstract Optional<Integer> getSlowCountY();

  public abstract UUID getOutputChannelId();

  public static Builder builder() {
    return new AutoValue_FkSpectraCommand.Builder();
  }

  public abstract Builder toBuilder();

  public String logString()
  {
    /*
     * Q: Why did Chris format this string in such a weird way?
     * A: Because each line in the method call now matches the line where
     * those parameters are used.
     */
    return String.format(LOG_STRING,
        getOutputChannelId(),
        getChannelIds(),
                         getStartTime(),
                         getSlowStartX(), getSlowStartY(),
                         getSlowDeltaX(), getSlowDeltaY(),
        getSlowCountX(), getSlowCountY(),
                         getLowFrequency(), getHighFrequency(),
                         getUseChannelVerticalOffset(),
                         getNormalizeWaveforms(),
                         getWindowLead(),
                         getWindowLength(),
                         getPhaseType(),
                         getSampleRate(),
                         getSampleCount());
  }

  @JsonCreator
  public static FkSpectraCommand from(
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("sampleRate") Double sampleRate,
      @JsonProperty("sampleCount") Long sampleCount,
      @JsonProperty("channelIds") Set<UUID> channelIds,
      @JsonProperty("windowLead") Duration windowLead,
      @JsonProperty("windowLength") Duration windowLength,
      @JsonProperty("lowFrequency") Double lowFrequency,
      @JsonProperty("highFrequency") Double highFrequency,
      @JsonProperty("useChannelVerticalOffset") Boolean useChannelVerticalOffset,
      @JsonProperty("normalizeWaveforms") Boolean normalizeWaveforms,
      @JsonProperty("phaseType") PhaseType phaseType,
      @JsonProperty("slowStartX") Optional<Double> slowStartX,
      @JsonProperty("slowDeltaX") Optional<Double> slowDeltaX,
      @JsonProperty("slowCountX") Optional<Integer> slowCountX,
      @JsonProperty("slowStartY") Optional<Double> slowStartY,
      @JsonProperty("slowDeltaY") Optional<Double> slowDeltaY,
      @JsonProperty("slowCountY") Optional<Integer> slowCountY,
      @JsonProperty("outputChannelId") UUID outputChannelId) {

    FkSpectraCommand.Builder command = FkSpectraCommand.builder()
        .setStartTime(startTime)
        .setSampleRate(sampleRate)
        .setSampleCount(sampleCount)
        .setChannelIds(channelIds)
        .setWindowLead(windowLead)
        .setWindowLength(windowLength)
        .setLowFrequency(lowFrequency)
        .setHighFrequency(highFrequency)
        .setUseChannelVerticalOffset(useChannelVerticalOffset)
        .setNormalizeWaveforms(normalizeWaveforms)
        .setPhaseType(phaseType)
        .setOutputChannelId(outputChannelId);

    slowStartX.ifPresent(command::setSlowStartX);
    slowDeltaX.ifPresent(command::setSlowDeltaX);
    slowCountX.ifPresent(command::setSlowCountX);
    slowStartY.ifPresent(command::setSlowStartY);
    slowDeltaY.ifPresent(command::setSlowDeltaY);
    slowCountY.ifPresent(command::setSlowCountY);

    return command.build();
  }

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder setStartTime(Instant startTime);

    public abstract Builder setSampleRate(Double sampleRate);

    public abstract Builder setSampleCount(Long sampleCount);

    public abstract Builder setChannelIds(Set<UUID> channelIds);

    abstract Set<UUID> getChannelIds();

    public abstract Builder setWindowLead(Duration windowLead);

    public abstract Builder setWindowLength(Duration windowLength);

    public abstract Builder setLowFrequency(Double lowFrequency);

    public abstract Builder setHighFrequency(Double highFrequency);

    public abstract Builder setUseChannelVerticalOffset(Boolean useChannelVerticalOffset);

    public abstract Builder setNormalizeWaveforms(Boolean normalizeWaveforms);

    public abstract Builder setPhaseType(PhaseType phaseType);

    public abstract Builder setSlowStartX(Double slowStartX);

    public abstract Builder setSlowDeltaX(Double slowDeltaX);

    public abstract Builder setSlowCountX(Integer slowCountX);

    public abstract Builder setSlowStartY(Double slowStartY);

    public abstract Builder setSlowDeltaY(Double slowDeltaY);

    public abstract Builder setSlowCountY(Integer slowCountY);

    public abstract Builder setOutputChannelId(UUID outputChannelId);

    abstract FkSpectraCommand autoBuild();

    public FkSpectraCommand build() {
      setChannelIds(ImmutableSet.copyOf(getChannelIds()));
      FkSpectraCommand command = autoBuild();

      Preconditions.checkState(command.getSampleRate() > 0.0,
          "Error creating FkSpectraCommand: Sample Rate must be greater than 0");
      Preconditions.checkState(command.getSampleCount() > 0,
          "Error creating FkSpectraCommand: Sample Count must be greater than 0");
      Preconditions.checkState(!command.getChannelIds().isEmpty(),
          "Error creating FkSpectraCommand: Channel IDs cannot be empty");

      return command;
    }

  }
}
