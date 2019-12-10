package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

/**
 * A container for attributes associated with an FK.
 */
@AutoValue
public abstract class FkAttributes {

  public abstract double getAzimuth();

  public abstract double getSlowness();

  public abstract double getAzimuthUncertainty();

  public abstract double getSlownessUncertainty();

  public abstract double getPeakFStat();


  /**
   * Obtains a new {@link FkAttributes} object. To be used for deserialization.
   *
   * @return {@link FkAttributes}, not null
   * @throws IllegalArgumentException Azimuth uncertainty is less than or equal to zero. Slowness
   * uncertainty is less than zero.
   */
  @JsonCreator
  public static FkAttributes from(
      @JsonProperty("azimuth") double azimuth,
      @JsonProperty("slowness") double slowness,
      @JsonProperty("azimuthUncertainty") double azimuthUncertainty,
      @JsonProperty("slownessUncertainty") double slownessUncertainty,
      @JsonProperty("peakFStat") double peakFStat) {
    Preconditions.checkArgument(azimuthUncertainty >= 0,
        "Azimuth uncertainty must be greater than or equal to zero");
    Preconditions.checkArgument(slownessUncertainty >= 0,
        "Slowness uncertainty must be greater than or equal to zero");

    return new AutoValue_FkAttributes(azimuth, slowness, azimuthUncertainty, slownessUncertainty,
        peakFStat);
  }

}
