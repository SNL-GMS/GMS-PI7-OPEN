package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.plugins.Attenuation1dPlugin;
import gms.core.featureprediction.plugins.DepthDistance1dModelSet;
import gms.core.featureprediction.plugins.Distance1dModelSet;
import gms.core.featureprediction.plugins.SlownessUncertaintyPlugin;
import gms.core.featureprediction.plugins.TravelTime1dPlugin;
import gms.core.featureprediction.plugins.implementations.signalfeaturepredictor.PredictionType.PredictionReturn;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionVisitor;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionDerivativeType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Contains top-level logic for producing feature predictions. Has its own model sets for earth model,
 * attenuation and slowness uncertainty.
 *
 */
public class BcsFeaturePredictorDelegate {

  private DepthDistance1dModelSet<double[], double[][]> earthModelSet;
  private DepthDistance1dModelSet<double[], double[][]> attenuation1dSet;
  private Distance1dModelSet slownessUncertaintySet;
  private boolean isInitialized = false;
  private boolean wasTravelTimeExtrapolated = false;
  private boolean extrapolateTravelTimes;
  static final Logger logger = LoggerFactory.getLogger(BcsFeaturePredictorDelegate.class);

  /**
   * Iniitialize delegate by loading model sets (for now they are plugins in the plugin registry,
   * maybe this class should not care about plugins)
   *
   * @throws IOException if there is a problem with loading model files.
   */
  //TODO: Pass and use needed config
  public void initialize() throws IOException {
    if (isInitialized) {
      return;
    }

    final String ATTENUATION_PLUGIN_NAME = "standardAsciiAttenuation1dPlugin";
    final String ATTENUATION_PLUGIN_VERSION = "1.0.0";
    final String TRAVEL_TIME_PLUGIN_NAME = "standardAsciiTravelTime1dPlugin";
    final String TRAVEL_TIME_PLUGIN_VERSION = "1.0.0";
    final String SLOWNESS_UNCERTAINTY_PLUGIN_NAME = "standardAsciiSlownessUncertaintyPlugin";
    final String SLOWNESS_UNCERTAINTY_PLUGIN_VERSION = "1.0.0";

    PluginRegistry pluginRegistry = PluginRegistry.getRegistry();
    pluginRegistry.loadAndRegister();

    /*
     *   TODO:     Not sure this class should be responsible for loading plugins. If it is,
     *   TODO: plugin info should be passed in?
     */

    // load 1D Earth Model names
    earthModelSet = pluginRegistry
        .lookup(PluginInfo.from(TRAVEL_TIME_PLUGIN_NAME, TRAVEL_TIME_PLUGIN_VERSION),
            TravelTime1dPlugin.class).orElseThrow(() -> new IllegalStateException(
            "Plugin " + TRAVEL_TIME_PLUGIN_NAME
                + "(version " + TRAVEL_TIME_PLUGIN_VERSION + ") not found"));

    // load 1D Earth Model names for Attenuation plugin
    attenuation1dSet = pluginRegistry
        .lookup(PluginInfo.from(ATTENUATION_PLUGIN_NAME, ATTENUATION_PLUGIN_VERSION),
            Attenuation1dPlugin.class).orElseThrow(() -> new IllegalStateException(
            "Plugin " + ATTENUATION_PLUGIN_NAME
                + "(version " + ATTENUATION_PLUGIN_VERSION + ") not found"));

    Properties p = new Properties();
    p.load(this.getClass().getResourceAsStream("application.properties"));
    Set<String> names = Arrays.stream(p.getProperty("earthmodels").split("\\s*,\\s*"))
        .collect(Collectors.toSet());
    Set<String> attenuationNames = Arrays
        .stream(p.getProperty("attenuationEarthModels").split("\\s*,\\s*"))
        .collect(Collectors.toSet());

    earthModelSet.initialize(names);

    attenuation1dSet.initialize(attenuationNames);

    slownessUncertaintySet = pluginRegistry
        .lookup(
            PluginInfo.from(SLOWNESS_UNCERTAINTY_PLUGIN_NAME, SLOWNESS_UNCERTAINTY_PLUGIN_VERSION),
            SlownessUncertaintyPlugin.class).orElseThrow(() -> new IllegalStateException(
            "Plugin " + SLOWNESS_UNCERTAINTY_PLUGIN_NAME
                + "(version " + SLOWNESS_UNCERTAINTY_PLUGIN_VERSION + ") not found"));

    slownessUncertaintySet.initialize(names);

    extrapolateTravelTimes = Boolean.parseBoolean(p.getProperty("extrapolatetraveltimes"));

    isInitialized = true;
  }

  /**
   *
   * Predict an instant measurement type feature prediction; Only arrival time for now.
   *
   * @param earthModel earth model to reference in the distance/depth model set
   * @param type specific type of measurement to predict
   * @param sourceLocation location of event
   * @param receiverLocation location of receiver
   * @param phase phase to make prediction for
   * @param corrections corrections to apply to prediction
   * @return a feature prediction with an {@InstantValue} value type.
   * @throws IOException
   */
  public FeaturePrediction<InstantValue> predict(String earthModel,
      InstantMeasurementType type,
      EventLocation sourceLocation, Location receiverLocation, PhaseType phase,
      FeaturePredictionCorrection[] corrections) throws IOException {

    Instant start = Instant.now();

    if (!isInitialized) {
      throw new IllegalStateException(
          "DefaultSignalFeatureMeasurementType.predict() called before initialize() was called.");
    }
    // compute base prediction value

    PredictionReturn predictionReturn = getPredictionReturnedValue(earthModel, type, sourceLocation,
        receiverLocation, phase, corrections);

    Pair<Double, Set<FeaturePredictionComponent>> doubleSetPair = getFeaturePredictionComponents(
        predictionReturn, earthModel, sourceLocation, receiverLocation, phase, corrections
    );

    // construct/return FeaturePrediction

    double correctedValue = predictionReturn.value + doubleSetPair.getLeft();

    // Create optional to hold Instant representation of predicted value.  If predicted value
    // is Double.NaN, the optional will be empty.
    Optional<InstantValue> instantPredictedValue;

    if (Double.isNaN(correctedValue)) {

      // If primivite predicted value is Double.NaN, we got an invalid prediction value, so set
      // optional to empty
      instantPredictedValue = Optional.empty();
    } else {

      // Valid predicted value received, populate the optional
      instantPredictedValue = Optional.of(
          InstantValue.from(
              Instant.EPOCH.plus(durationFromDouble(correctedValue)),
              durationFromDouble(predictionReturn.uncertainty)
          )
      );
    }

    Instant finish = Instant.now();
    String message = String
        .format("predict() execution time: %d ms", Duration.between(start, finish).toMillis());
    logger.info(message);

    return constructGenericFeaturePrediction(
        type, sourceLocation, receiverLocation, phase, doubleSetPair.getRight(),
        instantPredictedValue, predictionReturn);
  }

  /**
   *
   * Predict an numeeric measurement type feature prediction
   *
   * @param earthModel earth model to reference in the distance/depth model set
   * @param type specific type of measurement to predict
   * @param sourceLocation location of event
   * @param receiverLocation location of receiver
   * @param phase phase to make prediction for
   * @param corrections corrections to apply to prediction
   * @return a feature prediction with an {@NumericMeasurmentType} value type.
   * @throws IOException
   */
  public FeaturePrediction<NumericMeasurementValue> predict(String earthModel,
      NumericMeasurementType type,
      EventLocation sourceLocation, Location receiverLocation, PhaseType phase,
      FeaturePredictionCorrection[] corrections) throws IOException {
    Instant start = Instant.now();

    if (!isInitialized) {
      throw new IllegalStateException(
          "DefaultSignalFeatureMeasurementType.predict() called before initialize() was called.");
    }
    // compute base prediction value

    PredictionReturn predictionReturn = getPredictionReturnedValue(earthModel, type, sourceLocation,
        receiverLocation, phase, corrections);

    Pair<Double, Set<FeaturePredictionComponent>> doubleSetPair = getFeaturePredictionComponents(
        predictionReturn, earthModel, sourceLocation, receiverLocation, phase, corrections
    );

    // construct/return FeaturePrediction

    // Create optional to hold NumericMeasurementValue representation of primitive predicted value.
    // If predicted value is Double.NaN, the optional will be empty.
    Optional<NumericMeasurementValue> numericPredictedValue;

    double correctedValue = predictionReturn.value + doubleSetPair.getLeft();

    if (Double.isNaN(correctedValue)) {

      // If primivite predicted value is Double.NaN, we got an invalid prediction value, so set
      // optional to empty
      numericPredictedValue = Optional.empty();
    } else {

      // Valid predicted value received, populate the optional
      numericPredictedValue = Optional.of(
          NumericMeasurementValue.from(
              Instant.now(),
              DoubleValue
                  .from(correctedValue,
                      predictionReturn.uncertainty,
                      predictionReturn.units)
          )
      );
    }

    Instant finish = Instant.now();
    String message = String
        .format("predict() execution time: %d ms", Duration.between(start, finish).toMillis());
    logger.info(message);
    //TODO: Is extrapolated ever true?  If not, should this parameter be removed from FeaturePrediction?
    //TODO: In the following return stmt, replace UUID.randomUUID() with actual channelId when it becomes available

    return constructGenericFeaturePrediction(
        type, sourceLocation, receiverLocation, phase, doubleSetPair.getRight(),
        numericPredictedValue, predictionReturn);
  }

  /**
   * Construct a FeaturePredciction that can be staticly type-checked
   *
   * @param type type of feature measurment
   * @param sourceLocation location of event
   * @param receiverLocation location of reciever
   * @param phase phase
   * @param featurePredictionComponents correction components
   * @param measurementValue value, optional of type T
   * @param predictionReturn PredictionReturn object to construct from
   * @param <T> Type of measurement value
   * @return Parameterized feature prediction
   */
  private <T> FeaturePrediction<T> constructGenericFeaturePrediction(
      FeatureMeasurementType<T> type,
      EventLocation sourceLocation, Location receiverLocation, PhaseType phase,
      Set<FeaturePredictionComponent> featurePredictionComponents,
      Optional<T> measurementValue,
      PredictionReturn predictionReturn
  ) {
    return FeaturePrediction
        .create(phase, measurementValue, featurePredictionComponents,
            wasTravelTimeExtrapolated, type,
            sourceLocation, receiverLocation, Optional.empty(),
            //TODO: How to set units for derivatives
            Map.of(FeaturePredictionDerivativeType.D_DX,
                DoubleValue.from(predictionReturn.derivatives[0], 0.0, Units.UNITLESS),
                FeaturePredictionDerivativeType.D_DY,
                DoubleValue.from(predictionReturn.derivatives[1], 0.0, Units.UNITLESS),
                FeaturePredictionDerivativeType.D_DZ,
                DoubleValue.from(predictionReturn.derivatives[2], 0.0, Units.UNITLESS),
                FeaturePredictionDerivativeType.D_DT,
                DoubleValue.from(predictionReturn.derivatives[3], 0.0, Units.UNITLESS)));
  }

  /**
   * Construct a Duration object from a floating point value that is the number of seconds
   *
   * @param fractionalSeconds number of seconds
   * @return Duration value of length fractionalSeconds, down to the nanosecond.
   */
  private Duration durationFromDouble(double fractionalSeconds) {
    return Duration.ofNanos((long) (fractionalSeconds * 1_000_000_000));
  }

  /**
   * Calculate corrections and return them as prediction components
   *
   * @param predictionReturn PredictionReturn value containing raw prediction data
   * @param earthModel earth model to use for corrections
   * @param sourceLocation location of event
   * @param receiverLocation location of reciever
   * @param phase phase
   * @param corrections corrections to add to components
   * @return A pair containing the raw some of correction values and the construct list of
   * feature prediction components
   *
   * @throws IOException
   */
  private Pair<Double, Set<FeaturePredictionComponent>> getFeaturePredictionComponents(
      PredictionReturn predictionReturn, String earthModel,
      EventLocation sourceLocation, Location receiverLocation, PhaseType phase,
      FeaturePredictionCorrection[] corrections) throws IOException {
    // compute corrections

    double sumOfCorrections = 0.0;
    Set<FeaturePredictionComponent> featurePredictionComponents = new HashSet<>();

    if (corrections != null) {
      FeaturePredictionCorrectionVisitor visitor = FeaturePredictionCorrection1dApplier
          .from(earthModel, sourceLocation, receiverLocation, phase);

      for (FeaturePredictionCorrection item : corrections) {
        FeaturePredictionComponent value = item.computeCorrection(visitor);
        featurePredictionComponents.add(value);
        sumOfCorrections += value.getValue().getValue();
      }
    }

    // add baseline prediction to feature prediction components - summing all components should get you the predicted value

    //TODO: Is extrapolated ever true?  If not, should this parameter be removed from FeaturePredictionComponent?
    FeaturePredictionComponent baselineComponent = FeaturePredictionComponent
        .from(DoubleValue.from(predictionReturn.value,
            predictionReturn.uncertainty, predictionReturn.units),
            predictionReturn.wasExtrapolated,
            FeaturePredictionCorrectionType.BASELINE_PREDICTION);
    featurePredictionComponents.add(baselineComponent);

    return Pair.of(sumOfCorrections, featurePredictionComponents);
  }

  /**
   * Call prediction logic that returns raw values, calulated by PredictionType enum
   *
   * @param earthModel earth model used to make prediction
   * @param type tyoe of prediction
   * @param sourceLocation location of event
   * @param receiverLocation location of reciever
   * @param phase phase to make predition for
   * @param corrections set of corrections to calculate
   * @return PredictionReturn object containing raw prediction values
   * @throws IOException
   */
  private PredictionReturn getPredictionReturnedValue(String earthModel,
      FeatureMeasurementType<?> type,
      EventLocation sourceLocation, Location receiverLocation, PhaseType phase,
      FeaturePredictionCorrection[] corrections) throws IOException {
    PredictionType predictionType = PredictionType.valueOf(type.getFeatureMeasurementTypeName());

    PredictionType.PredictionReturn predictionReturn;
    if (predictionType.equals(PredictionType.MAGNITUDE_CORRECTION)) {
      predictionReturn = predictionType.predict(
          new PredictionType.PredictionDefinition(attenuation1dSet, slownessUncertaintySet,
              type, earthModel, sourceLocation, receiverLocation, phase, false));
    } else {
      predictionReturn = predictionType.predict(
          new PredictionType.PredictionDefinition(earthModelSet, slownessUncertaintySet,
              type, earthModel, sourceLocation, receiverLocation, phase, extrapolateTravelTimes));
    }

    Validate.isTrue(corrections == null || corrections.length == 0 || predictionType
        .correctionsValid(corrections), String
        .format("Invalid corrections requested for FeatureMeasurementType \"%s\"",
            type.getFeatureMeasurementTypeName()));

    return predictionReturn;

  }
}
