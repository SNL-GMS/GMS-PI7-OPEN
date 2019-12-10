package gms.core.signaldetection.association.plugins;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class SdhStationAssociationTests {

  @Test
  public void testFromWithNullSignalDetectionHypothesis() {

    ReferenceStation station = ReferenceStation.create(
        "FAKE",
        "foo",
        StationType.SeismicArray,
        InformationSource.create(
            "DINGO DANIEL",
            Instant.EPOCH,
            "RANGO RONALD"
        ),
        "GET GOOD",
        25.0,
        50.0,
        100.0,
        Instant.EPOCH,
        Instant.EPOCH,
        new ArrayList<>()
    );

    Assertions.assertEquals(
        "Cannot create an SdhStationAssociation from a null signal detection hypothesis",
        Assertions.assertThrows(NullPointerException.class,
            () -> SdhStationAssociation.from(null, station)).getMessage());
  }

  @Test
  public void testFromWithNullStation() {

    FeatureMeasurement<InstantValue> arrivalTimeFeatureMeasurement = FeatureMeasurement.create(
        UUID.randomUUID(),
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(Instant.EPOCH, Duration.ZERO));

    FeatureMeasurement<PhaseTypeMeasurementValue> phaseFeatureMeasurement = FeatureMeasurement
        .create(
            UUID.randomUUID(),
            FeatureMeasurementTypes.PHASE,
            PhaseTypeMeasurementValue.from(PhaseType.P, 0.5));

    SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.from(
        UUID.randomUUID(),
        UUID.randomUUID(),
        true,
        List.of(arrivalTimeFeatureMeasurement, phaseFeatureMeasurement),
        UUID.randomUUID());

    Assertions.assertEquals("Cannot create an SdhStationAssociation from a null reference station",
        Assertions.assertThrows(NullPointerException.class,
            () -> SdhStationAssociation.from(signalDetectionHypothesis, null)).getMessage());
  }
}
