package gms.core.featureprediction.plugins.implementations.ellipticitycorrection;

import gms.core.featureprediction.plugins.DziewanskiGilbertEllipticityCorrectionPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import gms.shared.utilities.geomath.GeoMath;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;

public class DziewonskiGilbertEllipticityCorrectionDelegate {

  private DziewanskiGilbertEllipticityCorrectionPlugin correctionModel;

  private PluginRegistry pluginRegistry = PluginRegistry.getRegistry();

  /**
   * Initializes the plugin via the provided {@link Map} representing the configuration values for
   * this plugin.  This function should be called before the plugin is used.
   *
   * @param earthModelNames Associates {@link String}s representing names of earth models with
   * {@link URL}s pointing to ellipticity correction files containing ellipticity correction values
   * for that earth model.
   */
  public void initialize(Set<String> earthModelNames) {
    Objects.requireNonNull(earthModelNames,
        "DziewonskiGilbertEllipticityCorrection::initialize() requires non-null config parameter.");

    pluginRegistry.loadAndRegister();
    correctionModel = pluginRegistry
        .lookup(PluginInfo.from("dziewonskiGilbertEllipticityModel", "1.0.0"),
            DziewanskiGilbertEllipticityCorrectionPlugin.class).get();
    try {
      correctionModel.initialize(earthModelNames);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Calculates an ellipticity correction value
   *
   * @param modelName the name of the earth model for which to calculate an ellipticity correction
   * @param sourceLocation the {@link EventLocation} of the event
   * @param receiverLocation the {@link Location} of the receiver
   * @param phaseType the {@link PhaseType} for which to calculate an ellipticity correction
   * @return the ellipticity correction value
   */
  public FeaturePredictionComponent correct(String modelName, EventLocation sourceLocation,
      Location receiverLocation,
      PhaseType phaseType) {

    double depth = sourceLocation.getDepthKm();
    double colatitude = GeoMath.toColatitudeDeg(sourceLocation.getLatitudeDegrees());

    double distance = GeoMath.greatCircleAngularSeparation(
        receiverLocation.getLatitudeDegrees(),
        receiverLocation.getLongitudeDegrees(),
        sourceLocation.getLatitudeDegrees(),
        sourceLocation.getLongitudeDegrees()
    );

    double azimuth = GeoMath.azimuth(
        sourceLocation.getLatitudeDegrees(),
        sourceLocation.getLongitudeDegrees(),
        receiverLocation.getLatitudeDegrees(),
        receiverLocation.getLongitudeDegrees()
    );

    Validate.isTrue(colatitude >= 0.0,
        "DziewonskiGilbertEllipticityCorrection::correct() requires colatitude >= 0.0");
    Validate.isTrue(colatitude <= 180.0,
        "DziewonskiGilbertEllipticityCorrection::correct() requires colatitude <= 180.0");
    Validate
        .isTrue(distance >= 0.0,
            "DziewonskiGilbertEllipticityCorrection::correct() requires distance >= 0.0");

    PiecewiseBicubicSplineInterpolator interpolator = new PiecewiseBicubicSplineInterpolator();

    double[] distances = correctionModel.getDistancesDeg(modelName, phaseType);
    double[] depths = correctionModel.getDepthsKm(modelName, phaseType);
    Triple<double[][], double[][], double[][]> taus =
        correctionModel.getValues(modelName, phaseType);

    double[][] tau0Table = taus.getLeft();
    double[][] tau1Table = taus.getMiddle();
    double[][] tau2Table = taus.getRight();

    PiecewiseBicubicSplineInterpolatingFunction tau0InterpolatingFunction = interpolator
        .interpolate(distances, depths, tau0Table);

    PiecewiseBicubicSplineInterpolatingFunction tau1InterpolatingFunction = interpolator
        .interpolate(distances, depths, tau1Table);

    PiecewiseBicubicSplineInterpolatingFunction tau2InterpolatingFunction = interpolator
        .interpolate(distances, depths, tau2Table);

    double tau0 = tau0InterpolatingFunction.value(distance, depth);
    double tau1 = tau1InterpolatingFunction.value(distance, depth);
    double tau2 = tau2InterpolatingFunction.value(distance, depth);

    double correctionNumber = this
        .travelTimeEllipticityCorrection(colatitude, azimuth, tau0, tau1, tau2);
    DoubleValue correctionValue = DoubleValue.from(correctionNumber, 0.0, Units.SECONDS);

    FeaturePredictionComponent featurePredictionComponent = FeaturePredictionComponent.from(
        correctionValue,
        false,
        FeaturePredictionCorrectionType.ELLIPTICITY_CORRECTION
    );

    return featurePredictionComponent;
  }

  /**
   * Calculate travel time correction given tau values, colatitude and azimuth
   *
   * @param colatitudeDegrees colatitude in degrees
   * @param azimuthDegrees azimuth in degrees
   * @param tau0 first value in correction table
   * @param tau1 second value in correction table
   * @param tau2 third value in correction table
   * @return value to add to predicted travel time
   */
  private static double travelTimeEllipticityCorrection(double colatitudeDegrees,
      double azimuthDegrees, double tau0,
      double tau1, double tau2) {
    double colatitudeRadians = Math.toRadians(colatitudeDegrees);
    double azimuthRadians = Math.toRadians(azimuthDegrees);

    double sqrt3over2 = Math.sqrt(0.75);
    double sinColat = Math.sin(colatitudeRadians);

    return 0.25 * (1.0 + 3.0 * Math.cos(2.0 * colatitudeRadians)) * tau0
        + sqrt3over2 * Math.sin(2.0 * colatitudeRadians) * Math.cos(azimuthRadians) * tau1
        + sqrt3over2 * sinColat * sinColat * Math.cos(2.0 * azimuthRadians) * tau2;
  }
}
