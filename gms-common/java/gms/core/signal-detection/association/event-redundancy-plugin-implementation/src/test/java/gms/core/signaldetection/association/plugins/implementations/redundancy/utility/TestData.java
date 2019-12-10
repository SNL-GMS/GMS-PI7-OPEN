package gms.core.signaldetection.association.plugins.implementations.redundancy.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Test data for various functionalies, which are seperated into subclasses.
 */
public class TestData {

  //
  // Test data for testing weighted criterion functionality.
  //
  public static class WeightedCriterionData {

    public static Set<SignalDetectionHypothesis> multipleSdhsWithSingleMeasurment =
        Set.of(
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SLOWNESS,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.SECONDS_PER_DEGREE
                        )
                    )
                )
            ).build(),
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.DEGREES
                        )
                    )
                )
            ).build(),
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.ARRIVAL_TIME,
                    InstantValue.from(
                        Instant.EPOCH,
                        Duration.ZERO
                    )
                )
            ).build()
        );

    public static Set<SignalDetectionHypothesis> twoSdhsThreeMeasurements =
        Set.of(
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SLOWNESS,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.SECONDS_PER_DEGREE
                        )
                    )
                )
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.DEGREES
                        )
                    )
                )
            ).build(),
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.ARRIVAL_TIME,
                    InstantValue.from(
                        Instant.EPOCH,
                        Duration.ZERO
                    )
                )
            ).build()
        );

    public static Set<SignalDetectionHypothesis> twoSdhsFourMeasurements =
        Set.of(
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SLOWNESS,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.SECONDS_PER_DEGREE
                        )
                    )
                )
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.DEGREES
                        )
                    )
                )
            ).build(),
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.ARRIVAL_TIME,
                    InstantValue.from(
                        Instant.EPOCH,
                        Duration.ZERO
                    )
                )
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.DEGREES
                        )
                    )
                )
            ).build()
        );

    public static Set<SignalDetectionHypothesis> twoSdhsFourRelevantMeasurementsOneIrrelevant =
        Set.of(
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SLOWNESS,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.SECONDS_PER_DEGREE
                        )
                    )
                )
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.DEGREES
                        )
                    )
                )
            ).build(),
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.ARRIVAL_TIME,
                    InstantValue.from(
                        Instant.EPOCH,
                        Duration.ZERO
                    )
                )
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.DEGREES
                        )
                    )
                )
            ).addMeasurement(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.MAGNITUDE_CORRECTION,
                    NumericMeasurementValue.from(
                        Instant.EPOCH,
                        DoubleValue.from(
                            0.0,
                            0.0,
                            Units.DEGREES
                        )
                    )
                )
            ).build()
        );
  }

}
