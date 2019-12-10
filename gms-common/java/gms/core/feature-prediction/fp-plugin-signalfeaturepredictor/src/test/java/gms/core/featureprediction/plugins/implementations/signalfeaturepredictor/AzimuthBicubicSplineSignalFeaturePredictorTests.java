package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.io.IOException;
import java.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class AzimuthBicubicSplineSignalFeaturePredictorTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private SignalFeaturePredictorPlugin plugin = new BicubicSplineSignalFeaturePredictor();
  private final Instant SOURCE_INSTANT = Instant.EPOCH;
  private final String EARTH_MODEL = new String("ak135");
  private final double MAX_FRACTION_DIFF = 1.0e-3;

  @Before
  public void initialize() throws IOException {
    plugin.initialize(new PluginConfiguration());
  }

  private void runTest(
      NumericMeasurementType featureMeasurementType, PhaseType phaseType,
      double sourceLatitudeDegrees, double sourceLongitudeDegrees, double sourceDepthKm,
      double receiverLatitudeDegrees, double receiverLongitudeDegrees,
      double receiverElevationKm, double receiverDepthKm,
      FeaturePredictionCorrection[] corrections,
      double expectedAzimuthDegrees, double expectedUncertaintyDegrees) throws IOException {

    EventLocation sourceLocation = EventLocation
        .from(sourceLatitudeDegrees, sourceLongitudeDegrees, sourceDepthKm, SOURCE_INSTANT);
    Location receiverLocation = Location
        .from(receiverLatitudeDegrees, receiverLongitudeDegrees, receiverDepthKm,
            receiverElevationKm);

    FeaturePrediction<?> featurePrediction = plugin
        .predict(EARTH_MODEL, featureMeasurementType, sourceLocation, receiverLocation,
            phaseType, corrections);

    if (Math.abs(expectedAzimuthDegrees) < 1.0e-9) { // if effectively equal to zero
      assertEquals(expectedAzimuthDegrees, 0.0, MAX_FRACTION_DIFF);
    } else {
      double fraction =
          Math.abs(expectedAzimuthDegrees - ((NumericMeasurementValue) featurePrediction
              .getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue().getValue())
              / expectedAzimuthDegrees;

      assertTrue(String.format(
          "Percent difference in azimuth values exceeds maximum (computed: %f, expected: %f).",
          ((NumericMeasurementValue) featurePrediction.getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue()
              .getValue(), expectedAzimuthDegrees),
          fraction <= MAX_FRACTION_DIFF);
    }

    if (Math.abs(expectedUncertaintyDegrees) < 1.0e-9) { // if effectively equal to zero
      assertEquals(expectedUncertaintyDegrees, 0.0, MAX_FRACTION_DIFF);
    } else {
      double fraction = Math.abs(
          expectedUncertaintyDegrees - ((NumericMeasurementValue) featurePrediction
              .getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue().getStandardDeviation())
          / expectedUncertaintyDegrees;

      assertTrue(String.format(
          "Percent difference in azimuth uncertainty values exceeds maximum (computed: %f, expected: %f).",
          ((NumericMeasurementValue) featurePrediction.getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue()
              .getStandardDeviation(), expectedUncertaintyDegrees),
          fraction <= MAX_FRACTION_DIFF);
    }

    //Check that derviatives were calcualted
    //TODO: May want to check values. Or not, as derivative calculations were already validated.
    assertTrue(featurePrediction.getFeaturePredictionDerivativeMap().keySet().size() == 4);
  }

  @Test
  public void testAzimuthReturnsZeroSameCoords() throws IOException {
    runTest(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, PhaseType.P,
        20.0, 23.1, 0.0,
        20.0, 23.1, 0.0,
        0.0, null, 0.0, 20.0);
  }

  @Test
  public void testAzimuthSomeInterestingCases() throws IOException {
    runTest(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, PhaseType.P,
        10.0, 110.0, 70.0,
        46.768972, 82.300655, 0.6081,
        0.0, null, 138.3520, 10.000);

    runTest(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, PhaseType.P,
        10.0, 110.0, 70.0,
        50.6185, 78.56147, 0.1983,
        0.0, null, 136.3160, 10.000);

    runTest(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, PhaseType.P,
        10.0, 110.0, 70.0,
        -19.9428, 134.3511, 0.388,
        0.0, null, 318.9606, 10.000);

    runTest(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, PhaseType.P,
        10.0, 110.0, 70.0,
        -25.015124, 25.596598, 1.1483,
        0.0, null, 78.6418, 10.000);

    runTest(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, PhaseType.P,
        10.0, 110.0, 70.0,
        -23.665134, 133.905261, 0.6273,
        0.0, null, 322.3625, 10.000);

    runTest(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, PhaseType.P,
        10.0, 110.0, 70.0,
        46.793683, 82.290569, 0.6176,
        0.0, null, 138.3565, 10.000);
  }
}