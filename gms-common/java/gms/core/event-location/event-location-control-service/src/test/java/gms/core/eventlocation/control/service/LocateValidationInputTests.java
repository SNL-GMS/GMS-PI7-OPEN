package gms.core.eventlocation.control.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationUncertainty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocateValidationInputTests {

  @Test
  void testLocateValidationInput() {

    Set<ReferenceStation> referenceStations = LocateValidationInputTests
        .generateReferenceStations();
    List<SignalDetection> signalDetections = LocateValidationInputTests
        .generateSignalDetections(referenceStations);

    List<SignalDetectionHypothesis> associatedSignalDetectionHypotheses = List.of(
        signalDetections.get(0).getSignalDetectionHypotheses().get(0),
        signalDetections.get(1).getSignalDetectionHypotheses().get(1),
        signalDetections.get(2).getSignalDetectionHypotheses().get(0)
    );

    EventHypothesis eventHypothesis = LocateValidationInputTests
        .generateEventHypothesis(associatedSignalDetectionHypotheses);

    LocateValidationInput interactiveInput = LocateValidationInput.create(
        eventHypothesis,
        signalDetections,
        referenceStations
    );

    EventHypothesis inputEventHypothesis = interactiveInput.getEventHypothesis();
    List<SignalDetectionHypothesis> inputSignalDetectionHypotheses = interactiveInput
        .getSignalDetectionHypotheses();
    List<ReferenceStation> inputReferenceStations = interactiveInput.getReferenceStations();

    Assertions.assertEquals(eventHypothesis, inputEventHypothesis);
    Assertions.assertEquals(associatedSignalDetectionHypotheses, inputSignalDetectionHypotheses);
    Assertions.assertEquals(
        signalDetections.stream().map(SignalDetection::getStationId).collect(Collectors.toList()),
        inputReferenceStations.stream().map(ReferenceStation::getVersionId)
            .collect(Collectors.toList()));
  }

  @Test
  void testLocateValidationInputMissingHypothesis() {

    Set<ReferenceStation> referenceStations = LocateValidationInputTests
        .generateReferenceStations();
    List<SignalDetection> signalDetections = LocateValidationInputTests
        .generateSignalDetections(referenceStations);

    List<SignalDetectionHypothesis> associatedSignalDetectionHypotheses = List.of(
        signalDetections.get(0).getSignalDetectionHypotheses().get(0),
        SignalDetectionHypothesis.from(
            UUID.randomUUID(),
            UUID.randomUUID(),
            false,
            List.of(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.PHASE,
                    PhaseTypeMeasurementValue.from(PhaseType.P, 1.0)
                ),
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.ARRIVAL_TIME,
                    InstantValue.from(Instant.EPOCH, Duration.ZERO)
                )
            ),
            UUID.randomUUID()
        ),
        signalDetections.get(2).getSignalDetectionHypotheses().get(0)
    );

    EventHypothesis eventHypothesis = LocateValidationInputTests
        .generateEventHypothesis(associatedSignalDetectionHypotheses);

    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
        LocateValidationInput.create(
            eventHypothesis,
            signalDetections,
            referenceStations
        )
    );

    Assertions.assertEquals("One or more associated Signal Detection Hypotheses was not provided",
        exception.getMessage());
  }

  @Test
  void testLocateValidationInputMissingStation() {

    Set<ReferenceStation> referenceStations = LocateValidationInputTests
        .generateReferenceStations();
    List<SignalDetection> signalDetections = LocateValidationInputTests
        .generateSignalDetections(referenceStations);

    signalDetections.add(
        SignalDetection.create(
            "FAKE",
            UUID.randomUUID(),
            List.of(
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.PHASE,
                    PhaseTypeMeasurementValue.from(PhaseType.P, 1.0)
                ),
                FeatureMeasurement.create(
                    UUID.randomUUID(),
                    FeatureMeasurementTypes.ARRIVAL_TIME,
                    InstantValue.from(Instant.EPOCH, Duration.ZERO)
                )
            ),
            UUID.randomUUID()
        )
    );

    EventHypothesis eventHypothesis = LocateValidationInputTests.generateEventHypothesis(
        signalDetections.stream().flatMap(sd -> sd.getSignalDetectionHypotheses().stream())
            .collect(Collectors.toSet()));

    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
        LocateValidationInput.create(
            eventHypothesis,
            signalDetections,
            referenceStations
        )
    );

    Assertions.assertEquals("Missing one or more Reference Stations associated with the provided Signal Detections",
        exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("testNullParametersProvider")
  void testNullParameters(EventHypothesis eventHypothesis, List<SignalDetection> signalDetections,
      Set<ReferenceStation> referenceStations, String expectedExceptionMsg) {

    Throwable exception = Assertions.assertThrows(NullPointerException.class, () ->
        LocateValidationInput.create(eventHypothesis, signalDetections, referenceStations)
    );

    Assertions.assertEquals(expectedExceptionMsg, exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("testEmptyParametersProvider")
  void testEmptyParameters(EventHypothesis eventHypothesis, List<SignalDetection> signalDetections,
      Set<ReferenceStation> referenceStations, String expectedExceptionMsg) {

    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
        LocateValidationInput.create(eventHypothesis, signalDetections, referenceStations)
    );

    Assertions.assertEquals(expectedExceptionMsg, exception.getMessage());
  }

  static Stream<Arguments> testNullParametersProvider() {

    Set<ReferenceStation> referenceStations = LocateValidationInputTests
        .generateReferenceStations();
    List<SignalDetection> signalDetections = LocateValidationInputTests
        .generateSignalDetections(referenceStations);

    EventHypothesis eventHypothesis = LocateValidationInputTests.generateEventHypothesis(
        signalDetections.stream().flatMap(sd -> sd.getSignalDetectionHypotheses().stream())
            .collect(Collectors.toSet()));

    return Stream.of(
        Arguments.arguments(
            null,
            signalDetections,
            referenceStations,
            "Null eventHypothesis"
        ),
        Arguments.arguments(
            eventHypothesis,
            null,
            referenceStations,
            "Null signalDetections"
        ),
        Arguments.arguments(
            eventHypothesis,
            signalDetections,
            null,
            "Null referenceStations"
        )
    );
  }

  static Stream<Arguments> testEmptyParametersProvider() {

    Set<ReferenceStation> referenceStations = LocateValidationInputTests
        .generateReferenceStations();
    List<SignalDetection> signalDetections = LocateValidationInputTests
        .generateSignalDetections(referenceStations);

    EventHypothesis eventHypothesis = LocateValidationInputTests.generateEventHypothesis(
        signalDetections.stream().flatMap(sd -> sd.getSignalDetectionHypotheses().stream())
            .collect(Collectors.toSet()));

    return Stream.of(
        Arguments.arguments(
            eventHypothesis,
            List.of(),
            referenceStations,
            "Empty signalDetections"
        ),
        Arguments.arguments(
            eventHypothesis,
            signalDetections,
            Set.of(),
            "Empty referenceStations"
        )
    );
  }

  private static List<SignalDetection> generateSignalDetections(Set<ReferenceStation> referenceStations) {

    List<UUID> signalDetectionIds = new ArrayList<>();

    ReferenceStation[] referenceStationsArray = referenceStations.toArray(new ReferenceStation[]{});

    for (int i = 0; i < 3; i++) {

      signalDetectionIds.add(UUID.randomUUID());
    }

    List<SignalDetection> signalDetections = new ArrayList<>();

    UUID stationId = referenceStationsArray[0].getVersionId();

    for (int i = 0; i < 3; i++) {

      List<SignalDetectionHypothesis> signalDetectionHypotheses = new ArrayList<>();

      for (int j = 0; j < 2; j++) {

        signalDetectionHypotheses.add(
            SignalDetectionHypothesis.from(
                UUID.randomUUID(),
                signalDetectionIds.get(i),
                false,
                List.of(
                    FeatureMeasurement.create(
                        UUID.randomUUID(),
                        FeatureMeasurementTypes.PHASE,
                        PhaseTypeMeasurementValue.from(PhaseType.P, 1.0)
                    ),
                    FeatureMeasurement.create(
                        UUID.randomUUID(),
                        FeatureMeasurementTypes.ARRIVAL_TIME,
                        InstantValue.from(Instant.EPOCH, Duration.ZERO)
                    )
                ),
                UUID.randomUUID()
            )
        );
      }

      UUID finalStationId;

      if (i % 2 == 0) {
        finalStationId = stationId;
      } else {
        finalStationId = referenceStationsArray[1].getVersionId();
      }

      signalDetections.add(
          SignalDetection.from(
              signalDetectionIds.get(i),
              "FAKE",
              finalStationId,
              signalDetectionHypotheses,
              UUID.randomUUID()
          )
      );
    }

    return signalDetections;
  }

  private static Set<ReferenceStation> generateReferenceStations() {
    ArrayList<ReferenceStation> referenceStations = new ArrayList<>();

    for (int i=0; i < 2; i++) {
      referenceStations.add(ReferenceStation.create(
          "FAKE"+i,
          i+"EKAF",
          StationType.SeismicArray,
          InformationSource.create(
              "FAKE",
              Instant.EPOCH,
              "EKAF"
          ),
          "FAKE",
          0.0,
          0.0,
          0.0,
          Instant.EPOCH,
          Instant.EPOCH,
          List.of()
      ));
    }

    return referenceStations.stream().collect(Collectors.toSet());
  }

  // Utility method for generating EventHypothesis objects associated with the provided Signal Detection Hypotheses
  private static EventHypothesis generateEventHypothesis(
      Collection<SignalDetectionHypothesis> signalDetectionHypotheses) {

    PreferredLocationSolution preferredLocationSolution = PreferredLocationSolution.from(
        LocationSolution.from(
            UUID.randomUUID(),
            EventLocation.from(
                0.0,
                0.0,
                0.0,
                Instant.EPOCH
            ),
            LocationRestraint.from(
                RestraintType.UNRESTRAINED,
                null,
                RestraintType.UNRESTRAINED,
                null,
                DepthRestraintType.UNRESTRAINED,
                null,
                RestraintType.UNRESTRAINED,
                Instant.EPOCH),
            LocationUncertainty.from(
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                Set.of(),
                Set.of()
            ),
            Set.of(),
            Set.of()
        )
    );

    UUID eventHypothesisId = UUID.randomUUID();

    return EventHypothesis.from(
        UUID.randomUUID(),
        eventHypothesisId,
        Set.of(),
        false,
        Set.of(
            preferredLocationSolution.getLocationSolution()
        ),
        preferredLocationSolution,
        signalDetectionHypotheses.stream().map(sdh ->
            SignalDetectionEventAssociation.create(
                eventHypothesisId,
                sdh.getId()
            )
        ).collect(Collectors.toSet())
    );
  }
}
