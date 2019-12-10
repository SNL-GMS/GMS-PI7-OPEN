package gms.core.eventlocation.plugins.pluginutils.seedgeneration;

import static com.google.common.collect.Streams.zip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SimpleSeedGeneratorTests {

  private static int LOWEST_TIME = 1;
  private static double LOWEST_LON = 45.0;

  private static UUID[] signalDetectionUUIDs = {
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID()
  };

  private static double[] longitudes = {
      LOWEST_LON * 2.1,
      LOWEST_LON,
      LOWEST_LON + 20.0
  };

  private static EventLocation defaultLocation = EventLocation.from(
      0.0,
      LOWEST_LON,
      0.0,
      Instant.EPOCH);

  private static InstantValue[] arrivalTimes = {
      InstantValue.from(Instant.ofEpochSecond(LOWEST_TIME * 1000), Duration.ZERO),
      InstantValue.from(Instant.ofEpochSecond(LOWEST_TIME), Duration.ZERO),
      InstantValue.from(Instant.ofEpochSecond(LOWEST_TIME * 100), Duration.ZERO)
  };

  private static FeatureMeasurement<?> featureMeasurements[] = {
      FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
          arrivalTimes[0]),
      FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
          arrivalTimes[1]),
      FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
          arrivalTimes[2])
  };

  private static List<ReferenceStation> stations = List.of(
      ReferenceStation.create(
          "station1",
          "station1",
          StationType.SeismicArray,
          InformationSource.create(
              "station1",
              Instant.now(),
              "station1"
          ),
          "station1",
          0.0,
          longitudes[0],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station2",
          "station2",
          StationType.SeismicArray,
          InformationSource.create(
              "station2",
              Instant.now(),
              "station2"
          ),
          "station2",
          0.0,
          longitudes[1],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station3",
          "station3",
          StationType.SeismicArray,
          InformationSource.create(
              "station3",
              Instant.now(),
              "station3"
          ),
          "station3",
          0.0,
          longitudes[2],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      )
  );


  @Test
  public void testSomeSignalDetectionsOnlyArrivalTimeMeasurements() {

    SignalDetectionHypothesis[] hypotheses = {
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[0], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[0]).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[1], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[1]).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[2], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[2]).build()
    };

    Map<SignalDetectionHypothesis, ReferenceStation> observationStationMap = new HashMap<>();

    zip(Arrays.stream(hypotheses),
        stations.stream(),
        (sdh, s) -> { observationStationMap.put(sdh, s); return true; });

    SeedGenerator seedGenerator = new SimpleSeedGenerator();

    EventLocation seedLocation = seedGenerator
        .generate(defaultLocation, observationStationMap);

    assertEquals(LOWEST_LON, seedLocation.getLongitudeDegrees());
  }

  @Test
  public void testSomeSignalDetectionsIncludingOtherMeasurementTypes() {

    FeatureMeasurement<?> azimuthMeasurement1 = FeatureMeasurement.create(UUID.randomUUID(),
        FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
        NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(33.0, 0.0, Units.DEGREES)));
    FeatureMeasurement<?> azimuthMeasurement2 = FeatureMeasurement.create(UUID.randomUUID(),
        FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
        NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(33.0, 0.0, Units.DEGREES)));

    SignalDetectionHypothesis[] hypotheses = {
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[0], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[0]).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[1], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[1])
            .addMeasurement(azimuthMeasurement1).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[2], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[2])
            .addMeasurement(azimuthMeasurement2).build()
    };

    Map<SignalDetectionHypothesis, ReferenceStation> observationStationMap = new HashMap<>();

    zip(Arrays.stream(hypotheses),
        stations.stream(),
        (sdh, s) -> { observationStationMap.put(sdh, s); return true; });

    SeedGenerator seedGenerator = new SimpleSeedGenerator();

    EventLocation seedLocation = seedGenerator
        .generate(defaultLocation, observationStationMap);

    assertEquals(LOWEST_LON, seedLocation.getLongitudeDegrees());
  }

  @Test
  public void testReturnsDefaultLocation() {
    FeatureMeasurement<?> azimuthMeasurement1 = FeatureMeasurement.create(UUID.randomUUID(),
        FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
        NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(33.0, 0.0, Units.DEGREES)));
    FeatureMeasurement<?> azimuthMeasurement2 = FeatureMeasurement.create(UUID.randomUUID(),
        FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
        NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(33.0, 0.0, Units.DEGREES)));
    FeatureMeasurement<?> azimuthMeasurement3 = FeatureMeasurement.create(UUID.randomUUID(),
        FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
        NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(33.0, 0.0, Units.DEGREES)));

    SignalDetectionHypothesis[] hypotheses = {
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[0], false, UUID.randomUUID())
            .addMeasurement(azimuthMeasurement1).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[1], false, UUID.randomUUID())
            .addMeasurement(azimuthMeasurement2).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[2], false, UUID.randomUUID())
            .addMeasurement(azimuthMeasurement3).build()
    };

    Map<SignalDetectionHypothesis, ReferenceStation> observationStationMap = new HashMap<>();

    zip(Arrays.stream(hypotheses),
        stations.stream(),
        (sdh, s) -> { observationStationMap.put(sdh, s); return true; });

    SeedGenerator seedGenerator = new SimpleSeedGenerator();

    EventLocation seedLocation = seedGenerator
        .generate(defaultLocation, observationStationMap);

    assertEquals(defaultLocation, seedLocation);
  }

}
