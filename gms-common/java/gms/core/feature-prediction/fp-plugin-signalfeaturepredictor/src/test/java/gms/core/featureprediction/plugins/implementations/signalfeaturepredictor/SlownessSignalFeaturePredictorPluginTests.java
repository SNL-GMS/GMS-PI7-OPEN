package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import static org.junit.Assert.assertTrue;

import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ElevationCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.io.IOException;
import java.time.Instant;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class SlownessSignalFeaturePredictorPluginTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private SignalFeaturePredictorPlugin plugin = new BicubicSplineSignalFeaturePredictor();
  private final String EARTH_MODEL = new String("ak135");
  private final FeatureMeasurementType TYPE = FeatureMeasurementTypes.SLOWNESS;
  private final EventLocation SOURCE_LOCATION = EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH);
  private final Location RECEIVER_LOCATION = Location.from(0.0, 90.0, 0.0, 0.0);
  private final FeaturePredictionCorrection[] CORRECTIONS = null;
  private final double GLOBAL_SLOWNESS_UNCERTAINTY = 4.5;
  private final double MAX_FRACTION_DIFF = 1.0E-04;

  @Before
  public void initialize() throws IOException {
    plugin.initialize(new PluginConfiguration());
  }

  @Test
  @Ignore
  //TODO: plugin does not throw this error itself. This error is thrown by the interpolator, so should be tested there.
  public void testPredictBadEarthModel() throws IOException {
    final String BAD_EARTH_MODEL = new String("ThirdRockModel");
    exception
        .expectMessage("Earth model, " + BAD_EARTH_MODEL + ", not in earth model 1D plugin set.");
    plugin.predict(BAD_EARTH_MODEL, TYPE, SOURCE_LOCATION, RECEIVER_LOCATION, PhaseType.P,
        CORRECTIONS);
  }

  @Test
  @Ignore
  //TODO: plugin does not throw this error itself. This error is thrown by the interpolator, so should be tested there.
  public void testPredictNullPhase() throws IOException {
    exception.expectMessage("PhaseType is null");
    plugin.predict(EARTH_MODEL, TYPE, SOURCE_LOCATION, RECEIVER_LOCATION, null, CORRECTIONS);
  }

  @Test
  @Ignore
  //TODO: plugin does not throw this error itself. This error is thrown by the interpolator, so should be tested there.
  public void testPredictBadPhase() throws IOException {
    final PhaseType BAD_PHASE = PhaseType.UNKNOWN;
    exception.expectMessage(
        "Phase type, " + BAD_PHASE + ", does not exist in earth model, " + EARTH_MODEL + ".");
    plugin.predict(EARTH_MODEL, TYPE, SOURCE_LOCATION, RECEIVER_LOCATION, BAD_PHASE, CORRECTIONS);
  }

  private void runTest(double depthKm, double longitudeDeg, double elevation, PhaseType phase,
      FeaturePredictionCorrection[] corrections, double expectedSlowness,
      double expectedUncertainty) throws IOException {

    EventLocation sourceLocation = EventLocation.from(0.0, 0.0, depthKm, Instant.EPOCH);
    Location receiverLocation = Location.from(0.0, longitudeDeg, 0.0, elevation);

    FeaturePrediction<?> featurePrediction = plugin
        .predict(EARTH_MODEL, TYPE, sourceLocation, receiverLocation, phase, corrections);

    double fraction = Math.abs(expectedSlowness - ((NumericMeasurementValue) featurePrediction.getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue().getValue())
        / expectedSlowness;
    assertTrue("Percent difference in interpolated slowness values exceeds maximum. Expected: "
            + expectedSlowness + " actual: " + ((NumericMeasurementValue) featurePrediction.getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue().getValue(),
        fraction <= MAX_FRACTION_DIFF);

    fraction =
        Math.abs(expectedUncertainty - ((NumericMeasurementValue) featurePrediction.getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue().getStandardDeviation())
            / expectedUncertainty;
    assertTrue("Percent difference in interpolated uncertainty values exceeds maximum.",
        fraction <= MAX_FRACTION_DIFF);
  }

  @Ignore
  @Test
  public void testPredictOnTableNodePoints() throws IOException {
    runTest(50.0, 2.5, 0.0, PhaseType.P, null, 15.26576, 2.51758449);
    runTest(50.0, 180.0, 0.0, PhaseType.P, null, 6.693921667, GLOBAL_SLOWNESS_UNCERTAINTY);
    runTest(0.0, 95.0, 0.0, PhaseType.P, null, 8.468157895, GLOBAL_SLOWNESS_UNCERTAINTY);
    runTest(0.0, 95.0, 0.0, PhaseType.PKiKP, null, 11.43305263, GLOBAL_SLOWNESS_UNCERTAINTY);
  }

  @Ignore
  @Test
  public void testPredictOffTableNodePoints() throws IOException {
    runTest(0.0, 11.0, 0.0, PhaseType.P, null, 14.41727273, 3.17920375);
    runTest(60.0, 2.75, 0.0, PhaseType.P, null, 15.14836364, 2.5113487);
    runTest(0.0, 11.5, 0.0, PhaseType.PKiKP, null, 86.6313913, GLOBAL_SLOWNESS_UNCERTAINTY);
    runTest(46.7080, 19.64, 0.0, PhaseType.PKiKP, null, 50.50259674, GLOBAL_SLOWNESS_UNCERTAINTY);
    //TODO: find out why the test below is failing
    runTest(0.0, 133.905261, 0.6273, PhaseType.P, null, 8.2258, 2.5);
  }

  @Ignore
  @Test
  public void testPredictWithCorrections() throws IOException {
    //TODO The "truth" data for this test was never validated, so this is really just a regression test.  Need to get real truth data.
    FeaturePredictionCorrection[] corrections;

    corrections = new FeaturePredictionCorrection[]{};
    runTest(50.0, 7.5, 0.6, PhaseType.P, corrections, 14.23088, 2.8688524);
    corrections = new FeaturePredictionCorrection[]{EllipticityCorrection1dDefinition.create()};
    runTest(50.0, 7.5, 0.3, PhaseType.P, corrections, 14.35344, 2.8688524);
    corrections = new FeaturePredictionCorrection[]{ElevationCorrection1dDefinition.create(false)};
    runTest(50.0, 7.5, 1.7, PhaseType.P, corrections, 14.42728, 2.8688524);
    corrections = new FeaturePredictionCorrection[]{EllipticityCorrection1dDefinition.create(),
        ElevationCorrection1dDefinition.create(false)};
    runTest(50.0, 7.5, 4.5, PhaseType.P, corrections, 14.87332, 2.8688524);
  }
}