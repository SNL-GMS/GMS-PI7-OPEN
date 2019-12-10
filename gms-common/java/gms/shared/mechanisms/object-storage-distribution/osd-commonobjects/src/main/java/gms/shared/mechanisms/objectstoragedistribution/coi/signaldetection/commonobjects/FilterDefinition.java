package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

/**
 * Describes an IIR or FIR filter.
 */
@AutoValue
@JsonSerialize(as = FilterDefinition.class)
@JsonDeserialize(builder = AutoValue_FilterDefinition.Builder.class)
public abstract class FilterDefinition {

  /**
   * Obtains the filter name
   *
   * @return String, not null
   */
  public abstract String getName();

  /**
   * Obtains the filter description
   *
   * @return String, not null
   */
  public abstract String getDescription();

  /**
   * Obtains the {@link FilterType}
   *
   * @return FilterType, not null
   */
  public abstract FilterType getFilterType();

  /**
   * Obtains the {@link FilterPassBandType}
   *
   * @return FilterPassBandType, not null
   */
  public abstract FilterPassBandType getFilterPassBandType();

  /**
   * Obtains the filter's low frequency
   *
   * @return {@code double, >= 0.0 }
   */
  public abstract double getLowFrequencyHz();

  /**
   * Obtains the filter's high frequency
   *
   * @return {@code double, >= 0.0 }
   */
  public abstract double getHighFrequencyHz();

  /**
   * Obtains the filter's order
   *
   * @return {@code integer, >= 1 }
   */
  public abstract int getOrder();

  /**
   * Obtains the filter's {@link FilterSource}
   *
   * @return FilterSource, not null
   */
  public abstract FilterSource getFilterSource();

  /**
   * Obtains the filter's {@link FilterCausality}
   *
   * @return FilterCausality, not null
   */
  public abstract FilterCausality getFilterCausality();

  /**
   * Obtains whether the filter is a zero phase filter
   *
   * @return true if this is a zero phase filter
   */
  public abstract boolean isZeroPhase();

  /**
   * Obtains the filter's sample rate in samples per second
   *
   * @return {@code double, > 0.0 }
   */
  public abstract double getSampleRate();

  /**
   * Obtains the filter's sample rate tolerance in samples per second
   *
   * @return {@code double, >= 0.0 }
   */
  public abstract double getSampleRateTolerance();

  /**
   * Obtains the filter'a aCoefficients (i.e. the feedback coefficients). See {@link java.beans.Introspector#decapitalize(String)}
   * for discrepency in camel-case
   *
   * @return double array, not null; returns a defensive copy of the aCoefficients (updating the
   * returned array does not affect this Object's aCoefficients)
   */
  public abstract double[] getaCoefficients();

  /**
   * Obtains the filter'a bCoefficients (i.e. the feedforward coefficients). See {@link java.beans.Introspector#decapitalize(String)}
   * for discrepency in camel-case
   *
   * @return double array, not null; returns a defensive copy of the bCoefficients (updating the
   * returned array does not affect this Object's bCoefficients)
   */
  public abstract double[] getbCoefficients();

  /**
   * Obtains the group delay in seconds
   *
   * @return double
   */
  public abstract double getGroupDelaySecs();

  public static Builder builder() {
    return new AutoValue_FilterDefinition.Builder();
  }

  public abstract Builder toBuilder();

  public static Builder firBuilder() {
    return builder()
        .setFilterType(FilterType.FIR_HAMMING)
        .setaCoefficients(new double[]{1.0});
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setName(String name);

    public abstract Builder setDescription(String description);

    public abstract Builder setFilterType(FilterType filterType);

    public abstract Builder setFilterPassBandType(FilterPassBandType filterPassBandType);

    public abstract Builder setLowFrequencyHz(double lowFrequencyHz);

    public abstract Builder setHighFrequencyHz(double highFrequencyHz);

    public abstract Builder setOrder(int order);

    public abstract Builder setFilterSource(FilterSource filterSource);

    public abstract Builder setFilterCausality(FilterCausality filterCausality);

    public abstract Builder setZeroPhase(boolean zeroPhase);

    public abstract Builder setSampleRate(double sampleRate);

    public abstract Builder setSampleRateTolerance(double sampleRateTolerance);

    public abstract Builder setaCoefficients(double[] aCoefficients);

    public abstract Builder setbCoefficients(double[] bCoefficients);

    public abstract Builder setGroupDelaySecs(double groupDelaySecs);

    public abstract FilterDefinition autoBuild();

    public FilterDefinition build() {
      FilterDefinition filterDefinition = autoBuild();

      checkState(filterDefinition.getLowFrequencyHz() >= 0.0,
          "FilterDefinition requires low frequency >= 0.0");
      checkState(filterDefinition.getHighFrequencyHz() >= 0.0,
          "FilterDefinition requires high frequency >= 0.0");
      checkState(filterDefinition.getLowFrequencyHz() < filterDefinition.getHighFrequencyHz(),
          "FilterDefinition requires low frequency < high frequency");
      checkState(filterDefinition.getOrder() > 0, "FilterDefinition requires order > 0");
      checkState(filterDefinition.getSampleRate() > 0.0,
          "FilterDefinition requires sampleRate > 0");
      checkState(filterDefinition.getSampleRateTolerance() >= 0.0,
          "FilterDefinition requires sampleRateTolerance >= 0");
      checkState(filterDefinition.getaCoefficients().length > 0,
          "FilterDefinition requires at least 1 aCoefficient");
      checkState(filterDefinition.getbCoefficients().length > 0,
          "FilterDefinition requires at least 1 bCoefficient");

      return filterDefinition;
    }

  }

}
