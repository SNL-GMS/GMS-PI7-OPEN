package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A type of {@link Timeseries} where each element in the series is an {@link FkSpectrum}.
 * Each FkSpectrum in a FkSpectra is computed in the same way, so the {@link Metadata} is stored
 * at the FkSpectra level.  {@link Timeseries#getSampleRate()} determines
 * the separation in FkSpectrum times.
 */
@AutoValue
public abstract class FkSpectra extends Timeseries {

  public static Builder builder() {
    return new AutoValue_FkSpectra.Builder();
  }

  public abstract Builder toBuilder();

  /**
   * An immutable {@link List} of Fk Spectrum with the same metadata, incrementing in time continuously given
   * this {@link Timeseries}'s start time and sample rate.
   */
  public abstract List<FkSpectrum> getValues();

  /**
   * Obtain the {@link Metadata} associated with the FkSpectra. This metadata should be assumed to
   * associate with each {@link FkSpectrum} of this FkSpectra.
   *
   * @return The metadata for this FkSpectra
   */
  public abstract Metadata getMetadata();

  @AutoValue.Builder
  public static abstract class Builder {

    abstract Builder setType(Type value);

    public abstract Builder setStartTime(Instant value);

    public abstract Builder setSampleRate(double value);

    abstract Builder setSampleCount(long value);

    abstract Builder setValues(List<FkSpectrum> values);

    abstract List<FkSpectrum> getValues();

    public Builder withValues(List<FkSpectrum> values) {
      return setValues(values).setSampleCount(values.size());
    }

    public Builder withoutValues(long value) {
      return setSampleCount(value).setValues(List.of());
    }

    public abstract Builder setMetadata(Metadata value);

    public abstract Metadata.Builder metadataBuilder();

    abstract FkSpectra autobuild();

    public FkSpectra build() {
      setType(Type.FK_SPECTRA).setValues(ImmutableList.copyOf(getValues()));
      FkSpectra fkSpectra = autobuild();

      Preconditions.checkState(fkSpectra.getValues().stream().noneMatch(Objects::isNull),
          "cannot contain null FkSpectrum values");

      Preconditions.checkState(fkSpectra.getValues().stream().map(FkSpectrum::getPower)
              .mapToInt(Immutable2dDoubleArray::rowCount).distinct().limit(2).count() <= 1,
          "Power must contain the same number of rows");
      Preconditions.checkState(fkSpectra.getValues().stream().map(FkSpectrum::getPower)
              .mapToInt(Immutable2dDoubleArray::columnCount).distinct().limit(2).count() <= 1,
          "Power must contain the same number of columns");

      Preconditions.checkState(fkSpectra.getValues().stream().map(FkSpectrum::getFstat)
              .mapToInt(Immutable2dDoubleArray::rowCount).distinct().limit(2).count() <= 1,
          "FStat must contain the same number of rows");
      Preconditions.checkState(fkSpectra.getValues().stream().map(FkSpectrum::getFstat)
              .mapToInt(Immutable2dDoubleArray::columnCount).distinct().limit(2).count() <= 1,
          "FStat must contain the same number of columns");

      return fkSpectra;
    }

  }

  @AutoValue
  public static abstract class Metadata {

    public static Metadata.Builder builder() {
      return new AutoValue_FkSpectra_Metadata.Builder();
    }

    public abstract Metadata.Builder toBuilder();


    /**
     * The assumed phase used to calculate this FkSpectra
     */
    public abstract PhaseType getPhaseType();

    /**
     * The start of the slowness grid in the X (East/West) direction
     */
    public abstract double getSlowStartX();

    /**
     * The start of the slowness grid in the Y (North/South) direction
     */
    public abstract double getSlowStartY();

    /**
     * The step size of the slowness grid in the X (East/West) direction
     */
    public abstract double getSlowDeltaX();

    /**
     * The step size of the slowness grid in the Y (North/South) direction
     */
    public abstract double getSlowDeltaY();

    @AutoValue.Builder
    public static abstract class Builder {

      public abstract Builder setPhaseType(PhaseType value);

      public abstract Builder setSlowStartX(double value);

      public abstract Builder setSlowStartY(double value);

      public abstract Builder setSlowDeltaX(double value);

      public abstract Builder setSlowDeltaY(double value);

      public abstract Metadata build();

    }

    @JsonCreator
    public static Metadata from(
        @JsonProperty("phaseType") PhaseType phaseType,
        @JsonProperty("slowStartX") double slowStartX,
        @JsonProperty("slowStartY") double slowStartY,
        @JsonProperty("slowDeltaX") double slowDeltaX,
        @JsonProperty("slowDeltaY") double slowDeltaY) {
      return builder().setPhaseType(phaseType)
          .setSlowStartX(slowStartX)
          .setSlowStartY(slowStartY)
          .setSlowDeltaX(slowDeltaX)
          .setSlowDeltaY(slowDeltaY)
          .build();
    }

  }

  @JsonCreator
  public static FkSpectra from(
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("sampleRate") double sampleRate,
      @JsonProperty("sampleCount") long sampleCount,
      @JsonProperty("values") List<FkSpectrum> values,
      @JsonProperty("metadata") Metadata metadata) {
    return builder().setStartTime(startTime).setSampleRate(sampleRate)
        .setSampleCount(sampleCount).setValues(values)
        .setMetadata(metadata).build();
  }
}

