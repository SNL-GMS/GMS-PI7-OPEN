package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignalDetectionHypothesisDaoTests {

  private SignalDetectionHypothesis updatedHypothesis;
  private SignalDetectionHypothesisDao hypothesisDao;

  @Before
  public void testUpdateDifferentIds() {
    UUID creationInfoId = UUID.randomUUID();
    SignalDetectionDao sdDao = new SignalDetectionDao(UUID.randomUUID(),
        "test",
        UUID.randomUUID(),
        creationInfoId);

    FeatureMeasurement<InstantValue> arrivalMeasurement = FeatureMeasurement.from(UUID.randomUUID(),
        UUID.randomUUID(),
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(Instant.EPOCH, Duration.ofSeconds(1)));

    FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement = FeatureMeasurement.from(
        UUID.randomUUID(),
        UUID.randomUUID(),
        FeatureMeasurementTypes.PHASE,
        PhaseTypeMeasurementValue.from(PhaseType.P, 0.95));

    hypothesisDao = new SignalDetectionHypothesisDao(UUID.randomUUID(),
        sdDao,
        false,
        new InstantFeatureMeasurementDao(arrivalMeasurement),
        new PhaseFeatureMeasurementDao(phaseMeasurement),
        Collections.emptyList(),
        creationInfoId);

    DoubleValue otherValue = DoubleValue.from(1.0, 0.9, Units.UNITLESS);

    FeatureMeasurement<NumericMeasurementValue> otherMeasurement = FeatureMeasurement.from(
        UUID.randomUUID(),
        UUID.randomUUID(),
        FeatureMeasurementTypes.SLOWNESS,
        NumericMeasurementValue.from(Instant.EPOCH, otherValue));

    updatedHypothesis = SignalDetectionHypothesis
        .builder(hypothesisDao.getSignalDetectionHypothesisId(),
            sdDao.getSignalDetectionId(),
            hypothesisDao.isRejected(),
            creationInfoId)
        .addMeasurement(arrivalMeasurement)
        .addMeasurement(phaseMeasurement)
        .addMeasurement(otherMeasurement)
        .build();
  }

  @Test
  public void testUpdateDifferentId() {
    SignalDetectionHypothesis otherHypothesis = SignalDetectionHypothesis.builder(UUID.randomUUID(),
        hypothesisDao.getParentSignalDetection().getSignalDetectionId(),
        true,
        hypothesisDao.getCreationInfoId())
        .build();

    assertThrows(IllegalStateException.class, () -> hypothesisDao.update(otherHypothesis));
  }

  @Test
  public void testUpdate() {
    hypothesisDao.update(updatedHypothesis);

    assertEquals(updatedHypothesis.getId(), hypothesisDao.getSignalDetectionHypothesisId());
    assertEquals(updatedHypothesis.getParentSignalDetectionId(), hypothesisDao.getParentSignalDetection().getSignalDetectionId());
    assertEquals(updatedHypothesis.isRejected(), hypothesisDao.isRejected());
    assertEquals(updatedHypothesis.getCreationInfoId(), hypothesisDao.getCreationInfoId());
    assertEquals(updatedHypothesis.getFeatureMeasurements().size(), hypothesisDao.getFeatureMeasurements().size() + 2);

    Optional<FeatureMeasurement<InstantValue>> expectedArrival = updatedHypothesis.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME);
    assertTrue(expectedArrival.isPresent());
    assertEquals(expectedArrival.get(), hypothesisDao.getArrivalTimeMeasurement().toCoi());

    Optional<FeatureMeasurement<PhaseTypeMeasurementValue>> expectedPhase = updatedHypothesis.getFeatureMeasurement(FeatureMeasurementTypes.PHASE);
    assertTrue(expectedPhase.isPresent());
    assertEquals(expectedPhase.get(), hypothesisDao.getPhaseMeasurement().toCoi());

    Map<String, FeatureMeasurement<?>> featureMeasurementByTypeName = updatedHypothesis.getFeatureMeasurements()
        .stream()
        .collect(Collectors.toMap(FeatureMeasurement::getFeatureMeasurementTypeName, Function.identity()));

    for (FeatureMeasurementDao<?> measurementDao : hypothesisDao.getFeatureMeasurements()) {
      FeatureMeasurement<?> updatedMeasurement = featureMeasurementByTypeName.get(measurementDao.getFeatureMeasurementType());
      assertNotNull(updatedMeasurement);
      assertEquals(updatedMeasurement, measurementDao.toCoi());
    }
  }

}
