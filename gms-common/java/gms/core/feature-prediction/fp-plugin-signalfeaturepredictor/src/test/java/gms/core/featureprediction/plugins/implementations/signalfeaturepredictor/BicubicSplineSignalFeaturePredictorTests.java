package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.io.IOException;
import java.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class BicubicSplineSignalFeaturePredictorTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private SignalFeaturePredictorPlugin uninitializedPlugin = new BicubicSplineSignalFeaturePredictor();
  private SignalFeaturePredictorPlugin plugin = new BicubicSplineSignalFeaturePredictor();
  private final String EARTH_MODEL = new String("ak135");
  private final FeatureMeasurementType TYPE = FeatureMeasurementTypes.ARRIVAL_TIME;
  private final FeatureMeasurementType INVALID_TYPE = FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2;
  private final EventLocation SOURCE_LOCATION = EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH);
  private final Location RECEIVER_LOCATION = Location.from(0.0, 90.0, 0.0, 0.0);
  private final FeaturePredictionCorrection[] CORRECTIONS = null;

  @Test
  public void testUninitializedSignalFeaturePredictor() throws IOException {
    exception.expectMessage(
        "BicubicSplineSignalFeaturePredictor.predict() called before initialize() was called.");
    uninitializedPlugin
        .predict(EARTH_MODEL, TYPE, SOURCE_LOCATION, RECEIVER_LOCATION, PhaseType.P, CORRECTIONS);
  }

  @Test
  public void testInvalidMeasurementTypeThrowsException() throws IOException {
    plugin.initialize(new PluginConfiguration());
    exception.expectMessage(
        "Invalid feature measurement type: " + INVALID_TYPE);
    plugin
        .predict(EARTH_MODEL, INVALID_TYPE, SOURCE_LOCATION, RECEIVER_LOCATION, PhaseType.P, CORRECTIONS);
  }

}
