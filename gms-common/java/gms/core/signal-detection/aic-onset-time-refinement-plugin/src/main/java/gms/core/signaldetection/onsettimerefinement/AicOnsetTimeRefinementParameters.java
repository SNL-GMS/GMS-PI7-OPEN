package gms.core.signaldetection.onsettimerefinement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.time.Duration;

@AutoValue
public abstract class AicOnsetTimeRefinementParameters {

  public abstract Duration getNoiseWindowSize();

  public abstract Duration getSignalWindowSize();

  public abstract int getOrder();

  @JsonCreator
  public static AicOnsetTimeRefinementParameters from(
      @JsonProperty("noiseWindowSize") Duration noiseWindowSize,
      @JsonProperty("signalWindowSize") Duration signalWindowSize,
      @JsonProperty("order") int order) {
    return new AutoValue_AicOnsetTimeRefinementParameters(noiseWindowSize, signalWindowSize,
        order);
  }
}
