package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FeaturePredictionTests {

  private static UUID id = UUID.randomUUID();

  private static EventLocation eventLocation = EventLocation.from(80.0, 90.0, 0.0, Instant.MIN);

  private static Location receiverLocation = Location.from(90.0, 90.0, 0.0, 0.0);

  private static UUID channelId = UUID.randomUUID();

  private static FeaturePrediction<InstantValue> featurePredictionInstant = FeaturePrediction.from(
      id,
      PhaseType.P,
      Optional.of(
          InstantValue.from(
              Instant.EPOCH,
              Duration.ZERO
          )
      ),
      Set.of(),
      false,
      FeatureMeasurementTypes.ARRIVAL_TIME,
      eventLocation,
      receiverLocation,
      Optional.of(channelId)
  );

  private static FeaturePrediction<NumericMeasurementValue> featurePredictionNumeric = FeaturePrediction
      .from(
          id,
          PhaseType.P,
          Optional.of(
              NumericMeasurementValue.from(
                  Instant.EPOCH,
                  DoubleValue.from(1.0, 0.0, Units.UNITLESS)
              )
          ),
          Set.of(),
          false,
          FeatureMeasurementTypes.SLOWNESS,
          eventLocation,
          receiverLocation,
          Optional.of(channelId)
      );

  private static FeaturePrediction<PhaseTypeMeasurementValue> featurePredictionPhase = FeaturePrediction
      .from(
          id,
          PhaseType.P,
          Optional.of(
              PhaseTypeMeasurementValue.from(
                  PhaseType.S,
                  0.0
              )
          ),
          Set.of(),
          false,
          FeatureMeasurementTypes.PHASE,
          eventLocation,
          receiverLocation,
          Optional.of(channelId)
      );

  @ParameterizedTest
  @MethodSource("testFeaturePredictionProvider")
  void testSerialization(FeaturePrediction<?> referenceFeaturePrediction) throws IOException {

    TestUtilities.testSerialization(referenceFeaturePrediction, FeaturePrediction.class);

  }

  static Stream<Arguments> testFeaturePredictionProvider() {
    return Stream.of(

        //NOTE: Right now only Instant and Numeric feature predictions exist.
        Arguments.arguments(featurePredictionInstant),
        Arguments.arguments(featurePredictionNumeric),

        Arguments.arguments(featurePredictionPhase)
    );
  }

  @Test
  void testEqualHashCodes() {
    NumericMeasurementValue predictedValue = NumericMeasurementValue
        .from(Instant.EPOCH, DoubleValue.from(1.0, 1.0, Units.SECONDS));
    NumericMeasurementValue predictedValue2 = NumericMeasurementValue
        .from(Instant.EPOCH, DoubleValue.from(2.0, 2.0, Units.SECONDS));
    Set<FeaturePredictionComponent> featurePredictionComponents = new HashSet<>();
    boolean extrapolated = false;
    NumericMeasurementType predictionType = FeatureMeasurementTypes.SLOWNESS;
    EventLocation sourceLocation = EventLocation.from(
        10.0, 10.0, 10.0, Instant.EPOCH);
    Location receiverLocation = Location.from(10.0, 10.0, 10.0, 10.0);
    Optional<UUID> channelId = Optional.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    UUID fpUuid = UUID.fromString("22111111-1111-1111-1111-111111111111");
    FeaturePrediction<NumericMeasurementValue> fp1 = FeaturePrediction.from(
        fpUuid,
        PhaseType.P, Optional.of(predictedValue), featurePredictionComponents, extrapolated,
        predictionType,
        sourceLocation, receiverLocation, channelId, Map.of());

    FeaturePrediction<NumericMeasurementValue> fp2 = FeaturePrediction.from(
        fpUuid,
        PhaseType.P, Optional.of(predictedValue), featurePredictionComponents, extrapolated,
        predictionType,
        sourceLocation, receiverLocation, channelId, Map.of());

    assertEquals(fp1.hashCode(), fp2.hashCode());
    assertEquals(fp1, fp2);

    FeaturePrediction<NumericMeasurementValue> fp3 = FeaturePrediction.from(
        UUID.fromString("44111111-1111-1111-1111-111111111111"),
        PhaseType.P, Optional.of(predictedValue2), featurePredictionComponents, extrapolated,
        predictionType,
        sourceLocation, receiverLocation, channelId, Map.of());

    assertNotEquals(fp1.hashCode(), fp3.hashCode());
    assertNotEquals(fp1, fp3);
  }
}
