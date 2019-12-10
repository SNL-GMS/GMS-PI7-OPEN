package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;


/**
 * An FkSpectrum contains both the Fk power spectrum values
 * and its fstat values for ever power pixel.
 */
@AutoValue
public abstract class FkSpectrum {

  public static FkSpectrum from(double[][] power, double[][] fstat, int quality) {
    return builder().setPower(power).setFstat(fstat).setQuality(quality).setAttributes(List.of())
        .build();
  }

  public static Builder builder() {
    return new AutoValue_FkSpectrum.Builder();
  }

  public abstract Builder toBuilder();

  @JsonProperty("power")
  public double[][] getPowerMutable() {
    return getPower().copyOf();
  }

  //flattened 2d power array
  @JsonIgnore
  public abstract Immutable2dDoubleArray getPower();

  @JsonProperty("fstat")
  public double[][] getFstatMutable() {
    return getFstat().copyOf();
  }

  //flattened 2d fstat array
  @JsonIgnore
  public abstract Immutable2dDoubleArray getFstat();

  public abstract int getQuality();

  //List of calculated attributes for the power and fstat arrays
  public abstract List<FkAttributes> getAttributes();

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder setPower(Immutable2dDoubleArray value);

    public Builder setPower(double[][] value) {
      setPower(Immutable2dDoubleArray.from(value));
      return this;
    }

    abstract Immutable2dDoubleArray getPower();

    public abstract Builder setFstat(Immutable2dDoubleArray value);

    public Builder setFstat(double[][] value) {
      setFstat(Immutable2dDoubleArray.from(value));
      return this;
    }

    abstract Immutable2dDoubleArray getFstat();

    public abstract Builder setQuality(int value);

    public abstract Builder setAttributes(List<FkAttributes> value);

    public Builder setAttributes(FkAttributes... value) {
      setAttributes(Arrays.asList(value));
      return this;
    }

    abstract List<FkAttributes> getAttributes();

    abstract FkSpectrum autobuild();

    public FkSpectrum build() {
      setAttributes(ImmutableList.copyOf(getAttributes()));
      FkSpectrum fkSpectrum = autobuild();

      Preconditions.checkState(getPower().rowCount() == getFstat().rowCount(),
          "Power and Fstat must have same row count");

      Preconditions.checkState(getPower().columnCount() == getFstat().columnCount(),
          "Power and Fstat must have same column count");

      return fkSpectrum;
    }
  }

  @JsonCreator
  public static FkSpectrum from(
      @JsonProperty("power") double[][] power,
      @JsonProperty("fstat") double[][] fstat,
      @JsonProperty("quality") int quality,
      @JsonProperty("attributes") List<FkAttributes> attributes) {
    return builder().setPower(power).setFstat(fstat).setQuality(quality).setAttributes(attributes)
        .build();
  }
}
