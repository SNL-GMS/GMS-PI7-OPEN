package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.core.featureprediction.plugins.EllipticityCorrectionPlugin;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ElevationCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionVisitor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import gms.shared.utilities.geomath.ElevationCorrectionUtility;
import gms.shared.utilities.geomath.MediumVelocities;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Implementation of the {@link FeaturePredictionCorrectionVisitor}, which visits the various
 * feature prediction corrections implementing {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection},
 * marking them as "visitable" classes.
 */
public class FeaturePredictionCorrection1dApplier implements FeaturePredictionCorrectionVisitor {

  private static final Logger logger = LogManager.getLogger(
      FeaturePredictionCorrection1dApplier.class);

  private String earthModelName;
  private EventLocation sourceLocation;
  private Location receiverLocation;
  private PhaseType phase;
  private static PluginRegistry pluginRegistry = PluginRegistry.getRegistry();

  // Maps earth model names to ElevationCorrectionUtility instances so only one
  // such utility ever need be instantiated for a given earth model. This assumes
  // that ElevationCorrectionUtility is a thread-safe class.
  private static final Map<String, ElevationCorrectionUtility> elevationCorrectionUtilityMap =
      new ConcurrentHashMap<>();

  // So each thread using this class can have its own SignalFeaturePredictorPlugin.
  private static ThreadLocal<SignalFeaturePredictorPlugin> signalFeaturePredictorPluginThreadLocal =
      ThreadLocal.withInitial(() -> {
        SignalFeaturePredictorPlugin plugin = pluginRegistry.lookup(
            PluginInfo.from("signalFeaturePredictor1dPlugin", "1.0.0"),
            SignalFeaturePredictorPlugin.class).get();
        try {
          plugin.initialize(new PluginConfiguration());
        } catch (IOException ioe) {
          logger.error("error initializing SignalFeaturePredictorPlugin", ioe);
        }
        return plugin;
      });

  // So each thread using this class can have its own EllipticityCorrectionPlugin
  private static ThreadLocal<EllipticityCorrectionPlugin> ellipticityCorrectionPluginThreadLocal =
      ThreadLocal.withInitial(() -> {
        EllipticityCorrectionPlugin plugin = pluginRegistry.lookup(
            PluginInfo.from("dziewonskiGilbertEllipticityCorrection", "1.0.0"),
            EllipticityCorrectionPlugin.class).get();

        Properties p = new Properties();
        try {
          p.load(FeaturePredictionCorrection1dApplier.class.getResourceAsStream(
              "application.properties"));
          Set<String> names = Arrays.stream(p.getProperty("earthmodels").split("\\s*,\\s*"))
              .collect(Collectors.toSet());
          plugin.initialize(names);
        } catch (IOException ioe) {
          logger.error("error initializing SignalFeaturePredictorPlugin", ioe);
        }
        return plugin;
      });


  private FeaturePredictionCorrection1dApplier(String earthModelName, EventLocation sourceLocation,
      Location receiverLocation, PhaseType phase) {
    this.earthModelName = earthModelName;
    this.sourceLocation = sourceLocation;
    this.receiverLocation = receiverLocation;
    this.phase = phase;

    pluginRegistry.loadAndRegister();
  }

  /**
   * Constructs a FeaturePredictionCorrection1dApplier
   *
   * @param earthModelName name of earth model
   * @param sourceLocation geographic location of the event source
   * @param receiverLocation geographic location of the phase receiver
   * @param phase type of phase
   * @return a FeaturePredictionCorrection1dApplier
   */
  public static FeaturePredictionCorrection1dApplier from(String earthModelName,
      EventLocation sourceLocation, Location receiverLocation, PhaseType phase) {
    return new FeaturePredictionCorrection1dApplier(earthModelName, sourceLocation,
        receiverLocation, phase);
  }

  @Override
  public FeaturePredictionComponent computeCorrection(ElevationCorrection1dDefinition correction)
      throws IOException {
    // TODO - at some point, the usingGlobalVelocity bool must be taken into account in this calculation
    //correction.isUsingGlobalVelocity();

    ElevationCorrectionUtility elevationCorrectionUtility = getElevationCorrectionUtility(
        earthModelName
    );

    SignalFeaturePredictorPlugin plugin = getSignalFeaturePredictorPlugin();

    // Note that corrections=null in the following call.  So this does not result in another
    // call to this (i.e., computeCorrection) method
    FeaturePrediction<?> horizontalSlowness = plugin
        .predict(earthModelName, FeatureMeasurementTypes.SLOWNESS, sourceLocation, receiverLocation,
            phase, null);

    if (horizontalSlowness.getPredictionType() instanceof NumericMeasurementType) {

      double value;

      if (horizontalSlowness.getPredictedValue().isPresent()) {

        value = elevationCorrectionUtility.correct(
            receiverLocation,
            horizontalSlowness.getPredictedValue()
                .map(pv -> (NumericMeasurementValue) pv)
                .orElseThrow(
                    () -> new IllegalStateException("Expected valid prediction, got invalid"))
                .getMeasurementValue()
                .getValue(),
            phase
        );
      } else {

        value = Double.NaN;
      }

      final double UNCERTAINTY = 0.0;
      DoubleValue correctionValue = DoubleValue.from(value, UNCERTAINTY, Units.SECONDS);

      return FeaturePredictionComponent.from(
          correctionValue,
          false,
          FeaturePredictionCorrectionType.ELEVATION_CORRECTION
      );
    } else {
      //NOTE: Should not be possible to reach this.
      throw new IllegalStateException("Feature prediction type did not match that of value.");
    }
  }

  @Override
  public FeaturePredictionComponent computeCorrection(EllipticityCorrection1dDefinition
      correction)
      throws IOException {

    EllipticityCorrectionPlugin plugin = getEllipticityCorrectionPlugin();

    try {

      return plugin.correct(earthModelName, sourceLocation, receiverLocation, phase);

    } catch (OutOfRangeException outOfRangeException) {

      logger.warn(outOfRangeException.getMessage());

      // Fix for defect 10110. If some parameters are out of range for the correction model,
      // an OutOfRangeException results. Just return NaN as the correction component.
      return FeaturePredictionComponent.from(
          DoubleValue.from(Double.NaN, 0.0, Units.SECONDS),
          false,
          FeaturePredictionCorrectionType.ELLIPTICITY_CORRECTION
      );
    }
  }
  /**
   * Get an {@link ElevationCorrectionUtility} instance for the specified earth model.
   * @param earthModelName
   * @return
   * @throws IOException
   */
  private static final ElevationCorrectionUtility getElevationCorrectionUtility(
      final String earthModelName) throws IOException {
    final AtomicReference<IOException> ioRef = new AtomicReference<>();
    ElevationCorrectionUtility ecu = elevationCorrectionUtilityMap.computeIfAbsent(earthModelName,
        emn -> {
          ElevationCorrectionUtility elevationCorrectionUtility = null;
          MediumVelocities mediumVelocities = new MediumVelocities();
          try {
            mediumVelocities.initialize(emn);
            elevationCorrectionUtility = ElevationCorrectionUtility.from(mediumVelocities);
            elevationCorrectionUtility.initialize();
          } catch (IOException ioe) {
            ioRef.set(ioe);
          }
          return elevationCorrectionUtility;
        });
    if (ioRef.get() != null) {
      throw ioRef.get();
    }
    return ecu;
  }

  /**
   * Get a {@link SignalFeaturePredictorPlugin} specific to the calling thread.
   * @return
   */
  private static SignalFeaturePredictorPlugin getSignalFeaturePredictorPlugin() {
    return signalFeaturePredictorPluginThreadLocal.get();
  }

  /**
   * Get a {@link EllipticityCorrectionPlugin} specifc to the calling thread.
   * @return
   */
  private static EllipticityCorrectionPlugin getEllipticityCorrectionPlugin() {
    return ellipticityCorrectionPluginThreadLocal.get();
  }

  /**
   * Removes plugins associated with the calling thread. This is a cleanup method.
   */
  public static void removeThreadSpecificPlugins() {
    signalFeaturePredictorPluginThreadLocal.remove();
    ellipticityCorrectionPluginThreadLocal.remove();
  }
}