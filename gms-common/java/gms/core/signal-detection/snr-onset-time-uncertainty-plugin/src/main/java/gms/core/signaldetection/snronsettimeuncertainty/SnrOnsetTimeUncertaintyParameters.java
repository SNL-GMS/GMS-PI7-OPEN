package gms.core.signaldetection.snronsettimeuncertainty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.utilities.signalprocessing.normalization.Transform;
import java.time.Duration;

@AutoValue
@JsonSerialize(as = SnrOnsetTimeUncertaintyParameters.class)
@JsonDeserialize(builder = AutoValue_SnrOnsetTimeUncertaintyParameters.Builder.class)
public abstract class SnrOnsetTimeUncertaintyParameters {

  public abstract Duration getNoiseWindowOffset();

  public abstract Duration getNoiseWindowSize();

  public abstract Duration getSignalWindowOffset();

  public abstract Duration getSignalWindowSize();

  public abstract Duration getSlidingWindowSize();

  public abstract double getMinTimeUncertainty();

  public abstract double getMaxTimeUncertainty();

  public abstract double getMinSnr();

  public abstract double getMaxSnr();

  public abstract Transform getTransform();

  public static Builder builder() {
    return new AutoValue_SnrOnsetTimeUncertaintyParameters.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setNoiseWindowOffset(Duration noiseWindowOffset);

    public abstract Builder setNoiseWindowSize(Duration noiseWindowSize);

    public abstract Builder setSignalWindowOffset(Duration signalWindowOffset);

    public abstract Builder setSignalWindowSize(Duration signalWindowSize);

    public abstract Builder setSlidingWindowSize(Duration slidingWindowSize);

    public abstract Builder setMinTimeUncertainty(double minTimeUncertainty);

    public abstract Builder setMaxTimeUncertainty(double maxTimeUncertainty);

    public abstract Builder setMinSnr(double minSnr);

    public abstract Builder setMaxSnr(double maxSnr);

    public abstract Builder setTransform(Transform transform);

    public abstract SnrOnsetTimeUncertaintyParameters autoBuild();

    public SnrOnsetTimeUncertaintyParameters build() {
      return autoBuild();
    }
  }
}

