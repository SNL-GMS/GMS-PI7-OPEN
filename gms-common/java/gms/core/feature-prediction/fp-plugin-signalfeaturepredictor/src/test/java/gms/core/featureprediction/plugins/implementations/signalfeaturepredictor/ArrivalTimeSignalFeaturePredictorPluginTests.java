package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ElevationCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.io.IOException;
import java.time.Instant;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class ArrivalTimeSignalFeaturePredictorPluginTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static SignalFeaturePredictorPlugin plugin = new BicubicSplineSignalFeaturePredictor();
  private final String EARTH_MODEL = new String("ak135");
  private final FeatureMeasurementType TYPE = FeatureMeasurementTypes.ARRIVAL_TIME;
  private final EventLocation SOURCE_LOCATION = EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH);
  private final Location RECEIVER_LOCATION = Location.from(0.0, 90.0, 0.0, 0.0);
  private final FeaturePredictionCorrection[] CORRECTIONS = null;
  private final double MAX_FRACTION_DIFF = 1.3E-02;

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

  private void testValue(double expected, double computed, String name) {
    if (Math.abs(expected) < 1.0e-9) { // if effectively equal to zero
      assertEquals(String
          .format("Percent difference in %s values exceeds maximum (computed: %f, expected: %f).",
              name, computed, expected), expected, 0.0, MAX_FRACTION_DIFF);
    } else {
      double fraction = Math.abs(expected - computed) / expected;
      assertTrue(String
          .format("Percent difference in %s values exceeds maximum (computed: %f, expected: %f).",
              name, computed, expected), fraction <= MAX_FRACTION_DIFF);
    }
  }

  private <T, V extends FeatureMeasurementType<T>> void runTest(
      V featureMeasurementType, PhaseType phaseType,
      double sourceLatitudeDegrees, double sourceLongitudeDegrees, double sourceDepthKm,
      double receiverLatitudeDegrees, double receiverLongitudeDegrees,
      double receiverElevationKm, double receiverDepthKm,
      FeaturePredictionCorrection[] corrections,
      double expectedBaseline, double expectedEllipticityCorrection,
      double expectedElevationCorrection) throws IOException {

    PluginRegistry.getRegistry().loadAndRegister();

    EventLocation sourceLocation = EventLocation
        .from(sourceLatitudeDegrees, sourceLongitudeDegrees, sourceDepthKm, Instant.EPOCH);
    Location receiverLocation = Location
        .from(receiverLatitudeDegrees, receiverLongitudeDegrees, receiverDepthKm,
            receiverElevationKm);

    FeaturePrediction<?> featurePrediction = plugin
        .predict(EARTH_MODEL, featureMeasurementType, sourceLocation, receiverLocation,
            phaseType, corrections);

    Instant value = ((InstantValue) featurePrediction.getPredictedValue()
        .orElseThrow(AssertionError::new)).getValue();
    double numericValue = value.getEpochSecond() + (double) value.getNano() / 1_000_000_000;

    testValue(expectedBaseline + expectedElevationCorrection + expectedEllipticityCorrection,
        numericValue, "arrival time");

    for (FeaturePredictionComponent fpc : featurePrediction.getFeaturePredictionComponents()) {
      switch (fpc.getPredictionComponentType()) {
        case BASELINE_PREDICTION:
          testValue(expectedBaseline, fpc.getValue().getValue(), "baseline arrival time");
          break;
        case ELEVATION_CORRECTION:
          testValue(expectedElevationCorrection, fpc.getValue().getValue(), "elevation correction");
          break;
        case ELLIPTICITY_CORRECTION:
          testValue(expectedEllipticityCorrection, fpc.getValue().getValue(),
              "ellipticity correction");
          break;
        default:
          assertTrue("Illegal feature prediction correction type", false);
      }
    }
  }

  @Test
  public void testPredictOnTableNodePoints() throws IOException {
    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.768972, 82.300655, 0.6081,
        0.0, null, 476.9154, 0.0, 0.0);

    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        50.6185, 78.56147, 0.1983,
        0.0, null, 512.3243, 0.0, 0.0);

    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        -19.9428, 134.3511, 0.388,
        0.0, null, 433.3080, 0.0, 0.0);

    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        -25.015124, 25.596598, 1.1483,
        0.0, null, 768.6207, 0.0, 0.0);

    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        -23.665134, 133.905261, 0.6273,
        0.0, null, 454.9622, 0.0, 0.0);

    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.793683, 82.290569, 0.6176,
        0.0, null, 477.1020, 0.0, 0.0);
  }

  @Test
  public void testPredictWithCorrectionsMK01() throws IOException {
    FeaturePredictionCorrection[] corrections;
    final double expectedBaselineArrivalTime = 477.599083;
    final double expectedEllipticityCorrection = 0.023775;
    final double expectedElevationCorrection = 0.095205;

    corrections = new FeaturePredictionCorrection[]{};
    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.768972, 82.300655, 0.6081,
        0.0, corrections,
        expectedBaselineArrivalTime, expectedEllipticityCorrection, expectedElevationCorrection);

    corrections = new FeaturePredictionCorrection[]{EllipticityCorrection1dDefinition.create()};
    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.768972, 82.300655, 0.6081,
        0.0, corrections,
        expectedBaselineArrivalTime, expectedEllipticityCorrection, expectedElevationCorrection);

    corrections = new FeaturePredictionCorrection[]{ElevationCorrection1dDefinition.create(false)};
    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.768972, 82.300655, 0.6081,
        0.0, corrections,
        expectedBaselineArrivalTime, expectedEllipticityCorrection, expectedElevationCorrection);

    corrections = new FeaturePredictionCorrection[]{EllipticityCorrection1dDefinition.create(),
        ElevationCorrection1dDefinition.create(false)};
    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.768972, 82.300655, 0.6081,
        0.0, corrections,
        expectedBaselineArrivalTime, expectedEllipticityCorrection, expectedElevationCorrection);
  }

  @Test
  public void testPredictWithCorrectionsMKAR() throws IOException {
    FeaturePredictionCorrection[] corrections;
    final double expectedBaselineArrivalTime = 477.785491;
    final double expectedEllipticityCorrection = 0.023431;
    final double expectedElevationCorrection = 0.096696;

    corrections = new FeaturePredictionCorrection[]{EllipticityCorrection1dDefinition.create()};
    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.793683, 82.290569, 0.6176,
        0.0, corrections,
        expectedBaselineArrivalTime, expectedEllipticityCorrection, expectedElevationCorrection);

    corrections = new FeaturePredictionCorrection[]{ElevationCorrection1dDefinition.create(false)};
    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.793683, 82.290569, 0.6176,
        0.0, corrections,
        expectedBaselineArrivalTime, expectedEllipticityCorrection, expectedElevationCorrection);

    corrections = new FeaturePredictionCorrection[]{EllipticityCorrection1dDefinition.create(),
        ElevationCorrection1dDefinition.create(false)};
    runTest(FeatureMeasurementTypes.ARRIVAL_TIME, PhaseType.P,
        10.0, 110.0, 70.0,
        46.793683, 82.290569, 0.6176,
        0.0, corrections,
        expectedBaselineArrivalTime, expectedEllipticityCorrection, expectedElevationCorrection);

  }

}