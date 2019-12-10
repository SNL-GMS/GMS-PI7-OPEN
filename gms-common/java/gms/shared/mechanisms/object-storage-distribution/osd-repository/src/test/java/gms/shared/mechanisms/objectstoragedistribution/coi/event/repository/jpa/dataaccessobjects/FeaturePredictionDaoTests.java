package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FeaturePredictionDaoTests {

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
  void testToAndFromDao(FeaturePrediction<?> referenceFeaturePrediction) {

    FeaturePredictionDao<?> dao = FeaturePredictionDao.from(referenceFeaturePrediction);

    FeaturePrediction<?> testFeaturePrediction = dao.toCoi();

    Assertions.assertEquals(referenceFeaturePrediction, testFeaturePrediction);
  }

  static Stream<Arguments> testFeaturePredictionProvider() {
    return Stream.of(

        //NOTE: Right now only Instant and Numeric feature predictions exist.
        Arguments.arguments(featurePredictionInstant),
        Arguments.arguments(featurePredictionNumeric),

        Arguments.arguments(featurePredictionPhase)
    );
  }

}
