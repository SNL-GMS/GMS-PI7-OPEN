package gms.core.signalenhancement.beam.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.UUID;

@AutoValue
public abstract class SlownessAzimuthPair {

  @JsonCreator
  public static SlownessAzimuthPair from(
      @JsonProperty("slowness") double slowness,
      @JsonProperty("azimuth") double azimuth) {
    return new AutoValue_SlownessAzimuthPair(slowness, azimuth);
  }

  public abstract double getSlowness();
  public abstract double getAzimuth();
}
