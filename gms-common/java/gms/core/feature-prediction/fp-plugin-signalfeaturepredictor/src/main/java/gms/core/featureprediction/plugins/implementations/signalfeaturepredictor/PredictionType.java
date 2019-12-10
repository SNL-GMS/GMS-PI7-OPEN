package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.exceptions.MissingEarthModelOrPhaseException;
import gms.core.featureprediction.plugins.DepthDistance1dModelSet;
import gms.core.featureprediction.plugins.Distance1dModelSet;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.utilities.geomath.CartesianDerivativeCalculation;
import gms.shared.utilities.geomath.GeoMath;
import gms.shared.utilities.geomath.GlobalEarthModelProperties;
import gms.shared.utilities.standardearthmodelformat.StandardEarthModelFormatUtility;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;

/**
 * Contains calculations for predicting different types of measurements (currently slowness, travel
 * time and azimuth (source/reciever and reciever/source)
 */
public enum PredictionType {
  SLOWNESS() {
    @Override
    public PredictionReturn predict(
        PredictionDefinition definition) {
      Validate.notNull(definition.sourceLocation);
      Validate.notNull(definition.receiverLocation);

      // compute baseline
      double distanceDeg = GeoMath
          .greatCircleAngularSeparation(definition.sourceLocation.getLatitudeDegrees(),
              definition.sourceLocation.getLongitudeDegrees(),
              definition.receiverLocation.getLatitudeDegrees(),
              definition.receiverLocation.getLongitudeDegrees());

      Triple<Duration, double[], Boolean> durationBooleanTriple = computeTravelTime(
          definition.extrapolateTravelTime, definition.depthDistanceModelSet,
          definition.earthModel, definition.phase,
          definition.sourceLocation.getDepthKm(), distanceDeg);

      // compute uncertainty
      Validate.isTrue(
          definition.uncertaintyModelSet.getEarthModelNames().contains(definition.earthModel),
          "Earth model, " + definition.earthModel
              + ", not in slowness uncertainty earth model 1D set.");
      double uncertainty;
      try {
        uncertainty = StandardEarthModelFormatUtility.interpolateUncertainties(distanceDeg,
            definition.uncertaintyModelSet
                .getDistancesDeg(definition.earthModel, definition.phase),
            definition.uncertaintyModelSet.getValues(definition.earthModel, definition.phase));
      } catch (IllegalArgumentException e) {
        // Either values were out of range of tables, or phase table does not exist for this earth model
        uncertainty = GlobalEarthModelProperties.getGlobalSlownessUncertainty();
      }

      double azimuth = GeoMath.azimuth(definition.sourceLocation.getLatitudeDegrees(),
          definition.sourceLocation.getLongitudeDegrees(),
          definition.receiverLocation.getLatitudeDegrees(),
          definition.receiverLocation.getLongitudeDegrees());

      return new PredictionReturn(
          durationBooleanTriple.getMiddle()[1], uncertainty,
          CartesianDerivativeCalculation.OF_SLOWNESS.calculate(
              durationBooleanTriple.getMiddle(),
              distanceDeg,
              definition.sourceLocation.getDepthKm(),
              azimuth), //durationBooleanTriple.getMiddle(),
          durationBooleanTriple.getRight(), Units.SECONDS_PER_DEGREE);
    }

    @Override
    public boolean correctionsValid(FeaturePredictionCorrection[] corrections) {
      return Arrays.stream(corrections).anyMatch(fpc ->
          fpc.getCorrectionType().equals(FeaturePredictionCorrectionType.ELLIPTICITY_CORRECTION)
              || fpc.getCorrectionType()
              .equals(FeaturePredictionCorrectionType.ELEVATION_CORRECTION));
    }

  },

  ARRIVAL_TIME() {
    @Override
    public PredictionReturn predict(
        PredictionDefinition definition) {
      Validate.notNull(definition.sourceLocation);
      Validate.notNull(definition.receiverLocation);

      double distanceDeg = GeoMath
          .greatCircleAngularSeparation(definition.sourceLocation.getLatitudeDegrees(),
              definition.sourceLocation.getLongitudeDegrees(),
              definition.receiverLocation.getLatitudeDegrees(),
              definition.receiverLocation.getLongitudeDegrees());

      Triple<Duration, double[], Boolean> durationBooleanTriple = computeTravelTime(false,
          definition.depthDistanceModelSet, definition.earthModel, definition.phase,
          definition.sourceLocation.getDepthKm(),
          distanceDeg);

      Duration travelTime = durationBooleanTriple.getLeft();

      //TODO: For now, we are returning UNIX epoch time.  At some point, we will want another solution such as generalizing the FeaturePrediction class to return an instant in time
      Instant arrivalTime = definition.sourceLocation.getTime().plus(travelTime);

      double azimuth = GeoMath.azimuth(definition.sourceLocation.getLatitudeDegrees(),
          definition.sourceLocation.getLongitudeDegrees(),
          definition.receiverLocation.getLatitudeDegrees(),
          definition.receiverLocation.getLongitudeDegrees());

      return new PredictionReturn(arrivalTime.getEpochSecond() + arrivalTime.getNano() / 1.0E+09,
          1.16,
          CartesianDerivativeCalculation.OF_TRAVEL_TIME.calculate(
              durationBooleanTriple.getMiddle(),
              distanceDeg,
              definition.sourceLocation.getDepthKm(),
              azimuth),
          false, Units.SECONDS);
    }

    @Override
    public boolean correctionsValid(FeaturePredictionCorrection[] corrections) {

      return Arrays.stream(corrections).anyMatch(fpc ->
          fpc.getCorrectionType().equals(FeaturePredictionCorrectionType.ELLIPTICITY_CORRECTION)
              || fpc.getCorrectionType()
              .equals(FeaturePredictionCorrectionType.ELEVATION_CORRECTION));
    }
  },

  SOURCE_TO_RECEIVER_AZIMUTH() {
    @Override
    public PredictionReturn predict(PredictionDefinition definition) {
      // compute baseline
      double azimuth = GeoMath
          .azimuth(definition.sourceLocation.getLatitudeDegrees(),
              definition.sourceLocation.getLongitudeDegrees(),
              definition.receiverLocation.getLatitudeDegrees(),
              definition.receiverLocation.getLongitudeDegrees());

      return new PredictionReturn(
          azimuth,
          computeGlobalAzimuthUncertainty(
              definition.sourceLocation.getLatitudeDegrees(),
              definition.sourceLocation.getLongitudeDegrees(),
              definition.receiverLocation.getLatitudeDegrees(),
              definition.receiverLocation.getLongitudeDegrees()),
          CartesianDerivativeCalculation.OF_AZIMUTH.calculate(
              new double[0], // Azimuth derivatives only use distance and azimuth
              GeoMath.greatCircleAngularSeparation(definition.sourceLocation.getLatitudeDegrees(),
                  definition.sourceLocation.getLongitudeDegrees(),
                  definition.receiverLocation.getLatitudeDegrees(),
                  definition.receiverLocation.getLongitudeDegrees()),
              0.0, // Azimuth derivatives only use distance and azimuth
              azimuth), false, Units.DEGREES);
    }

    @Override
    public boolean correctionsValid(FeaturePredictionCorrection[] corrections) {
      return corrections.length == 0;
    }
  },

  RECEIVER_TO_SOURCE_AZIMUTH() {
    @Override
    public PredictionReturn predict(PredictionDefinition definition) {
      // compute baseline
      double baseline = GeoMath
          .azimuth(definition.receiverLocation.getLatitudeDegrees(),
              definition.receiverLocation.getLongitudeDegrees(),
              definition.sourceLocation.getLatitudeDegrees(),
              definition.sourceLocation.getLongitudeDegrees());

      return new PredictionReturn(
          baseline,
          computeGlobalAzimuthUncertainty(
              definition.sourceLocation.getLatitudeDegrees(),
              definition.sourceLocation.getLongitudeDegrees(),
              definition.receiverLocation.getLatitudeDegrees(),
              definition.receiverLocation.getLongitudeDegrees()),
          //TODO: are 0,0,0,0 the correct derivative values?
          CartesianDerivativeCalculation.OF_AZIMUTH.calculate(
              new double[0], // Azimuth derivatives only use distance and azimuth
              GeoMath.greatCircleAngularSeparation(definition.sourceLocation.getLatitudeDegrees(),
                  definition.sourceLocation.getLongitudeDegrees(),
                  definition.receiverLocation.getLatitudeDegrees(),
                  definition.receiverLocation.getLongitudeDegrees()),
              0.0, // Azimuth derivatives only use distance and azimuth
              GeoMath
                  .azimuth(definition.sourceLocation.getLatitudeDegrees(),
                      definition.sourceLocation.getLongitudeDegrees(),
                      definition.receiverLocation.getLatitudeDegrees(),
                      definition.receiverLocation.getLongitudeDegrees())), false, Units.DEGREES);
    }

    @Override
    public boolean correctionsValid(FeaturePredictionCorrection[] corrections) {
      return corrections.length == 0;
    }
  },

  MAGNITUDE_CORRECTION() {
    @Override
    public PredictionReturn predict(PredictionDefinition definition) {

      double distanceDeg = GeoMath
          .greatCircleAngularSeparation(definition.sourceLocation.getLatitudeDegrees(),
              definition.sourceLocation.getLongitudeDegrees(),
              definition.receiverLocation.getLatitudeDegrees(),
              definition.receiverLocation.getLongitudeDegrees());

      double[] values = computeTravelTime(
          definition.extrapolateTravelTime, definition.depthDistanceModelSet,
          definition.earthModel, definition.phase,
          definition.sourceLocation.getDepthKm(), distanceDeg).getMiddle();

      Optional<double[]> optionalDepthModelingErrors = definition.depthDistanceModelSet
          .getDepthModelingErrors(definition.earthModel, definition.phase);
      Optional<double[]> optionalDistanceModelingErrors = definition.depthDistanceModelSet
          .getDistanceModelingErrors(definition.earthModel, definition.phase);
      Optional<double[][]> optionalModelingErrorValues = definition.depthDistanceModelSet
          .getValueModelingErrors(definition.earthModel, definition.phase);

      double uncertainty;

      if (!optionalDepthModelingErrors.isPresent()
          || !optionalDistanceModelingErrors.isPresent()
          || !optionalModelingErrorValues.isPresent()) {

        uncertainty = 0;
      } else {

        PiecewiseBicubicSplineInterpolator bcsInterpolator = new PiecewiseBicubicSplineInterpolator();

        PiecewiseBicubicSplineInterpolatingFunction bcsInterpolatingFunction = bcsInterpolator
            .interpolate(
                optionalDepthModelingErrors.get(),
                optionalDistanceModelingErrors.get(),
                optionalModelingErrorValues.get()
            );

        uncertainty = bcsInterpolatingFunction
            .value(definition.sourceLocation.getDepthKm(), distanceDeg);
      }

      return new PredictionReturn(values[0], uncertainty, new double[]{0.0, 0.0, 0.0, 0.0},
          definition.extrapolateTravelTime,
          Units.MAGNITUDE);
    }

    @Override
    public boolean correctionsValid(FeaturePredictionCorrection[] corrections) {

      return false;
    }
  };

  /**
   * Calculate a prediction based on parameters inside the PredictionDefinition helper object
   *
   * @param definition Parameters for calculation
   * @return values predicted, wrapped in a PredictionReturn helper object
   */
  public abstract PredictionReturn predict(PredictionDefinition definition);

  /**
   * Check whether a set of corrections is valid for a prediction type
   *
   * @param corrections corecctions to check
   * @return validity of corrections
   */
  public abstract boolean correctionsValid(FeaturePredictionCorrection[] corrections);

  Triple<Duration, double[], Boolean> computeTravelTime(
      boolean extrapolateTravelTimes,
      DepthDistance1dModelSet<double[], double[][]> depthDistanceModelSet,
      String earthModelName, PhaseType phase,
      double depthKm,
      double distanceDeg) {

    if (!depthDistanceModelSet.getEarthModelNames().contains(earthModelName) ||
        !depthDistanceModelSet.getPhaseTypes(earthModelName).contains(phase)) {
      throw new MissingEarthModelOrPhaseException(this.name(), earthModelName, phase);
    }

    BcsTravelTimeInterpolator algorithm = new BcsTravelTimeInterpolator
        .Builder()
        .withEarthModelsPlugin(depthDistanceModelSet)
        .withEarthModelName(earthModelName)
        .withPhaseType(phase)
        .withExtrapolation(extrapolateTravelTimes)
        .build();

    double[] values = algorithm.getPhaseTravelTimeAndDerivatives(depthKm, distanceDeg);

    return Triple.of(Duration
        .ofNanos(Math.round(1.0E+09 * values[0])), values, algorithm.wasExtrapolated());
  }

  static double computeGlobalAzimuthUncertainty(
      double sourceLatitudeDegrees, double sourceLongitudeDegrees,
      double receiverLatidudeDegrees, double receiverLongitudeDegrees) {
    double[] stops = GlobalEarthModelProperties.getGlobalAzimuthUncertaintyStops();
    double[] values = GlobalEarthModelProperties.getGlobalAzimuthUncertaintyValues();

    double distanceDeg = GeoMath
        .greatCircleAngularSeparation(sourceLatitudeDegrees,
            sourceLongitudeDegrees,
            receiverLatidudeDegrees,
            receiverLongitudeDegrees);

    Validate.isTrue(stops.length + 1 == values.length);

    for (int i = 0; i < stops.length; ++i) {
      if (distanceDeg < stops[i]) {
        return values[i];
      }
    }
    return values[values.length - 1];
  }

  public static class PredictionDefinition {

    DepthDistance1dModelSet<double[], double[][]> depthDistanceModelSet;
    Distance1dModelSet uncertaintyModelSet;
    FeatureMeasurementType type;
    String earthModel;
    EventLocation sourceLocation;
    Location receiverLocation;
    PhaseType phase;
    boolean extrapolateTravelTime;

    public PredictionDefinition(
        DepthDistance1dModelSet<double[], double[][]> depthDistanceModelSet,
        Distance1dModelSet uncertaintyModelSet,
        FeatureMeasurementType type, String earthModel,
        EventLocation sourceLocation,
        Location receiverLocation,
        PhaseType phase, boolean extrapolateTravelTime) {
      this.depthDistanceModelSet = depthDistanceModelSet;
      this.uncertaintyModelSet = uncertaintyModelSet;
      this.type = type;
      this.earthModel = earthModel;
      this.sourceLocation = sourceLocation;
      this.receiverLocation = receiverLocation;
      this.phase = phase;
      this.extrapolateTravelTime = extrapolateTravelTime;
    }
  }

  public static class PredictionReturn {

    double value;
    double uncertainty;
    double[] derivatives;
    boolean wasExtrapolated;
    Units units;

    public PredictionReturn(double value, double uncertainty, double[] derivatives,
        boolean wasExtrapolated, Units units) {
      this.value = value;
      this.uncertainty = uncertainty;
      this.derivatives = derivatives;
      this.wasExtrapolated = wasExtrapolated;
      this.units = units;
    }
  }
}