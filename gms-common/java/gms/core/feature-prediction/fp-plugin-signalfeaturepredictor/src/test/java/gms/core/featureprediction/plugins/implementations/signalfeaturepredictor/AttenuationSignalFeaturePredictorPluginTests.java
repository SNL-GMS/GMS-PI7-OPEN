package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.io.IOException;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AttenuationSignalFeaturePredictorPluginTests {

  private SignalFeaturePredictorPlugin plugin;

  private FeatureMeasurementType attenuationType;
  private String earthmodel;
  private PhaseType phaseType;

  @BeforeEach
  void init() throws IOException {

    this.plugin = new BicubicSplineSignalFeaturePredictor();
    this.plugin.initialize(new PluginConfiguration());

    this.attenuationType = FeatureMeasurementTypes.MAGNITUDE_CORRECTION;

    this.earthmodel = "VeithClawson72";

    this.phaseType = PhaseType.P;
  }

  @ParameterizedTest
  @MethodSource("testPredictAttenuationProvider")
  void testPredictAttenuation(EventLocation eventLocation, Location receiverLocation,
      double expectedValue, double expectedUncertainty) throws IOException {

    FeaturePrediction<?> prediction = this.plugin
        .predict(this.earthmodel, this.attenuationType, eventLocation, receiverLocation,
            this.phaseType, new FeaturePredictionCorrection[]{});

    Assertions.assertEquals(expectedValue,
        ((NumericMeasurementValue) prediction.getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue()
            .getValue());
    Assertions
        .assertEquals(expectedUncertainty,
            ((NumericMeasurementValue) prediction.getPredictedValue().orElseThrow(AssertionError::new)).getMeasurementValue()
                .getStandardDeviation());
  }

  static Stream<Arguments> testPredictAttenuationProvider() {
    return Stream.of(
        Arguments.arguments(
            EventLocation.from(
                0.0,
                0.0,
                0.0,
                Instant.EPOCH
            ),
            Location.from(
                0.0,
                0.0,
                0.0,
                0.0
            ),
            0.301,
            0.0
        ),
        Arguments.arguments(
            EventLocation.from(
                0.0,
                0.0,
                40.0,
                Instant.EPOCH
            ),
            Location.from(
                0.0,
                0.0,
                0.0,
                0.0
            ),
            -1.139,
            0.100
        ),
        Arguments.arguments(
            EventLocation.from(
                0.0,
                0.0,
                0.0,
                Instant.EPOCH
            ),
            Location.from(
                0.0,
                90.0,
                0.0,
                0.0
            ),
            4.061,
            4.048
        ),
        Arguments.arguments(
            EventLocation.from(
                0.0,
                0.0,
                40.0,
                Instant.EPOCH
            ),
            Location.from(
                0.0,
                90.0,
                0.0,
                0.0
            ),
            3.821,
            3.808
        )
    );
  }
}
