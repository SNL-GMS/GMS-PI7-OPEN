package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.util.Objects;

@AutoValue
public abstract class PhaseInfo implements Comparable<PhaseInfo> {

  /**
   * Implements {@link Comparable#compareTo(Object)} by returning the value of {@link
   * Double#compare(double, double)}
   *
   * {@inheritDoc}
   */
  @Override
  public int compareTo(PhaseInfo otherPhaseInfo) {

    Objects.requireNonNull(otherPhaseInfo, "Null o");

    return Double.compare(this.getTravelTimeSeconds(), otherPhaseInfo.getTravelTimeSeconds());
  }

  /**
   * @return phase type that this set of information is associated with
   */
  public abstract PhaseType getPhaseType();

  /**
   * @return TODO: not clear in given document
   */
  public abstract boolean isPrimary();

  /**
   * @return Travel time in seconds
   */
  public abstract double getTravelTimeSeconds();

  /**
   * @return Azimuth in degrees
   */
  public abstract double getAzimuthDegrees();

  /**
   * @return Back azimuth in degrees
   */
  public abstract double getBackAzimuthDegrees();

  /**
   * @return Minimum travel time in seconds
   */
  public abstract double getTravelTimeMinimum();

  /**
   * @return Maximum travel time in seconds
   */
  public abstract double getTravelTimeMaximum();

  /**
   * @return radial travel time derivative (d/dr)
   */
  public abstract double getRadialTravelTimeDerivative();

  /**
   * @return vertical travel time derivative (d/dz)
   */
  public abstract double getVerticalTravelTimeDerivative();

  /**
   * @return Cell width in slowness vector space. TODO: clarify name/purpose of this field:
   */
  public abstract double getSlownessCellWidth();

  /**
   * @return slowness a cell center
   */
  public abstract double getSlowness();
  /**
   * @return Minimum detectable magnitude at station
   */
  public abstract double getMinimumMagnitude();

  /**
   * @return magnitude correction at center of cell
   */
  public abstract double getMagnitudeCorrection();

  /**
   * @return radial derivative of magnitude correction (d/dr)
   */
  public abstract double getRadialMagnitudeCorrectionDerivative();

  /**
   * @return vertical derivative of magnitude correction (d/dz)
   */
  public abstract double getVerticalMagnitudeCorrectionDerivative();

  public static Builder builder() {
    return new AutoValue_PhaseInfo.Builder();
  }

  public abstract Builder toBuilder();

  @JsonCreator
  public static PhaseInfo from(
      @JsonProperty("phaseType") PhaseType phaseType,
      @JsonProperty("isPrimary") boolean primary,
      @JsonProperty("travelTimeSeconds") double travelTimeSeconds,
      @JsonProperty("azimuthDegrees") double azimuthDegrees,
      @JsonProperty("backAzimuthDegrees") double backAzimuthDegrees,
      @JsonProperty("travelTimeMinimum") double travelTimeMinimum,
      @JsonProperty("travelTimeMaximum") double travelTimeMaximum,
      @JsonProperty("radialTravelTimeDerivative") double radialTravelTimeDerivative,
      @JsonProperty("verticalTravelTimeDerivative") double verticalTravelTimeDerivative,
      @JsonProperty("slownessCellWidth") double slownessCellWidth,
      @JsonProperty("slowness") double slowness,
      @JsonProperty("minimumMagnitude") double minimumMagnitude,
      @JsonProperty("magnitudeCorrection") double magnitudeCorrection,
      @JsonProperty("radialMagnitudeCorrectionDerivative") double radialMagnitudeCorrectionDerivative,
      @JsonProperty("verticalMagnitudeCorrectionDerivative") double verticalMagnitudeCorrectionDerivative
  ) {

    return builder()
        .setPhaseType(phaseType)
        .setPrimary(primary)
        .setTravelTimeSeconds(travelTimeSeconds)
        .setAzimuthDegrees(azimuthDegrees)
        .setBackAzimuthDegrees(backAzimuthDegrees)
        .setTravelTimeMinimum(travelTimeMinimum)
        .setTravelTimeMaximum(travelTimeMaximum)
        .setRadialTravelTimeDerivative(radialTravelTimeDerivative)
        .setVerticalTravelTimeDerivative(verticalTravelTimeDerivative)
        .setSlownessCellWidth(slownessCellWidth)
        .setSlowness(slowness)
        .setMinimumMagnitude(minimumMagnitude)
        .setMagnitudeCorrection(magnitudeCorrection)
        .setRadialMagnitudeCorrectionDerivative(radialMagnitudeCorrectionDerivative)
        .setVerticalMagnitudeCorrectionDerivative(verticalMagnitudeCorrectionDerivative)
        .build();
  }

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder setPhaseType(PhaseType phaseType);

    public abstract Builder setPrimary(boolean primary);

    public abstract Builder setTravelTimeSeconds(double travelTimeSeconds);

    public abstract Builder setAzimuthDegrees(double azimuthDegrees);

    public abstract Builder setBackAzimuthDegrees(double backAzimuthDegrees);

    public abstract Builder setTravelTimeMinimum(double travelTimeMinimum);

    public abstract Builder setTravelTimeMaximum(double travelTimeMaximum);

    public abstract Builder setRadialTravelTimeDerivative(double radialTravelTimeDerivative);

    public abstract Builder setVerticalTravelTimeDerivative(double verticalTravelTimeDerivative);

    public abstract Builder setSlownessCellWidth(double slownessCellWidth);

    public abstract Builder setSlowness(double slowness);

    public abstract Builder setMinimumMagnitude(double minimumMagnitude);

    public abstract Builder setMagnitudeCorrection(double magnitudeCorrection);

    public abstract Builder setRadialMagnitudeCorrectionDerivative(
        double radialMagnitudeCorrectionDerivative);

    public abstract Builder setVerticalMagnitudeCorrectionDerivative(
        double verticalMagnitudeCorrectionDerivative);

    abstract PhaseInfo autoBuild();

    public PhaseInfo build() {
      return autoBuild();
    }

  }

}
