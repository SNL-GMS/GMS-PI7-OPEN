package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import com.google.auto.service.AutoService;
import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Plugin;
import gms.shared.mechanisms.pluginregistry.Version;
import java.io.IOException;
import java.util.Optional;

/**
 * Signal Feature Prediction plugin using 1D earth models
 */
@AutoService(Plugin.class)
@Name("signalFeaturePredictor1dPlugin")
@Version("1.0.0")
public class BicubicSplineSignalFeaturePredictor implements SignalFeaturePredictorPlugin, Plugin {

  private BcsFeaturePredictorDelegate bcsFeaturePredictorDelegate = null;

  @Override
  public BicubicSplineSignalFeaturePredictor initialize(PluginConfiguration configuration)
      throws IOException {
    if (bcsFeaturePredictorDelegate == null) {
      bcsFeaturePredictorDelegate = new BcsFeaturePredictorDelegate();
      //TODO: Pass and use needed config
      bcsFeaturePredictorDelegate.initialize();
    }

    return this;
  }

  @Override
  public FeaturePrediction<?> predict(String earthModel, FeatureMeasurementType<?> type,
      EventLocation sourceLocation, Location receiverLocation, PhaseType phase,
      FeaturePredictionCorrection[] corrections) throws IOException {
    if (bcsFeaturePredictorDelegate == null) {
      throw new IllegalStateException(
          "BicubicSplineSignalFeaturePredictor.predict() called before initialize() was called.");
    }

    if (type instanceof NumericMeasurementType) {
      return bcsFeaturePredictorDelegate
              .predict(earthModel, (NumericMeasurementType) type, sourceLocation, receiverLocation,
                  phase, corrections);
    } else if (type instanceof InstantMeasurementType) {
      return bcsFeaturePredictorDelegate
              .predict(earthModel, (InstantMeasurementType) type, sourceLocation, receiverLocation,
                  phase, corrections);
    } else {
      throw new IllegalArgumentException(
          "Invalid feature measurement type: " + type);
    }

  }

}