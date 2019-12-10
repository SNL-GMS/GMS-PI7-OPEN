package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SignalDetectionHypothesis} factory creation
 */
public class SignalDetectionHypothesisTests {

  private static final UUID id = UUID.randomUUID();
  private static final boolean rejected = false;
  private static final FeatureMeasurement<InstantValue> arrivalMeasurement = EventTestFixtures.arrivalTimeFeatureMeasurement;
  private static final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement = EventTestFixtures.phaseFeatureMeasurement;
  private static final List<FeatureMeasurement<?>> featureMeasurements = List
      .of(arrivalMeasurement, phaseMeasurement);
  private static final UUID creationInfoId = UUID.randomUUID();

  private static final String monitoringOrganization = "CTBTO";
  private static final UUID stationId = UUID.randomUUID();
  private static final SignalDetection signalDetection = SignalDetection
      .from(id, monitoringOrganization, stationId,
          Collections.emptyList(), creationInfoId);

  private SignalDetectionHypothesis signalDetectionHypothesis;

  @Test
  public void testSerialization() throws Exception {
    final SignalDetectionHypothesis hyp = SignalDetectionHypothesis.from(
        id, UUID.randomUUID(), rejected, featureMeasurements, creationInfoId);
    TestUtilities.testSerialization(hyp, SignalDetectionHypothesis.class);
  }

  @BeforeEach
  public void createSignalDetectionHypothesis() {
    signalDetection.addSignalDetectionHypothesis(featureMeasurements, creationInfoId);
    signalDetectionHypothesis = signalDetection.getSignalDetectionHypotheses().get(0);
  }

  @Test
  public void testFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(SignalDetectionHypothesis.class, "from",
        id, signalDetection.getId(), rejected,
        featureMeasurements, creationInfoId);
  }

  @Test
  public void testFrom() {
    final SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.from(
        id, signalDetection.getId(), rejected, featureMeasurements, creationInfoId);
    assertEquals(signalDetection.getId(), signalDetectionHypothesis.getParentSignalDetectionId());
    assertEquals(id, signalDetectionHypothesis.getId());
    assertEquals(rejected, signalDetectionHypothesis.isRejected());
    assertArrayEquals(featureMeasurements.toArray(),
        signalDetectionHypothesis.getFeatureMeasurements().toArray());
    assertEquals(creationInfoId, signalDetectionHypothesis.getCreationInfoId());
  }

  public void testNoArrivalTimeFeatureMeasurement() {
    assertThrows(IllegalArgumentException.class,
        () -> SignalDetectionHypothesis.from(UUID.randomUUID(), UUID.randomUUID(),
            rejected, List.of(phaseMeasurement), creationInfoId));
  }

  public void testNoPhaseFeatureMeasurement() {
    assertThrows(IllegalArgumentException.class,
        () -> SignalDetectionHypothesis.from(UUID.randomUUID(), UUID.randomUUID(),
            rejected, List.of(arrivalMeasurement), creationInfoId));
  }

  public void testDuplicateFeatureMeasurementTypes() {
    assertThrows(IllegalArgumentException.class,
        () -> SignalDetectionHypothesis.from(UUID.randomUUID(), UUID.randomUUID(), rejected,
            List.of(arrivalMeasurement, phaseMeasurement, phaseMeasurement), creationInfoId));
  }

  @Test
  public void testGetFeatureMeasurementByType() {
    Optional<FeatureMeasurement<InstantValue>> arrivalTime = signalDetectionHypothesis.getFeatureMeasurement(
        FeatureMeasurementTypes.ARRIVAL_TIME);
    assertNotNull(arrivalTime);
    assertTrue(arrivalTime.isPresent());
    assertEquals(arrivalMeasurement, arrivalTime.get());
    // get phase measurement
    Optional<FeatureMeasurement<PhaseTypeMeasurementValue>> phase = signalDetectionHypothesis.getFeatureMeasurement(
        FeatureMeasurementTypes.PHASE);
    assertNotNull(phase);
    assertTrue(phase.isPresent());
    assertEquals(phaseMeasurement, phase.get());
    // get non-existent measurement
    Optional<FeatureMeasurement<NumericMeasurementValue>> emergenceAngle = signalDetectionHypothesis.getFeatureMeasurement(
        FeatureMeasurementTypes.EMERGENCE_ANGLE);
    assertEquals(Optional.empty(), emergenceAngle);
  }

  public void testGetFeatureMeasurementByTypeNull() {
    assertThrows(NullPointerException.class,
        () -> signalDetectionHypothesis.getFeatureMeasurement(null));
  }

  @Test
  public void testWithMeasurementsBuilder() {
    SignalDetectionHypothesis actual = signalDetectionHypothesis.withMeasurements(List.of(
        FeatureMeasurementTypes.ARRIVAL_TIME)).build();
    assertTrue(actual.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).isPresent());
    assertFalse(actual.getFeatureMeasurement(FeatureMeasurementTypes.PHASE).isPresent());
  }

  @Test
  public void testWithoutMeasurementsBuilder() {
    SignalDetectionHypothesis actual = signalDetectionHypothesis.withoutMeasurements(List.of(
        FeatureMeasurementTypes.ARRIVAL_TIME)).build();
    assertFalse(actual.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).isPresent());
    assertTrue(actual.getFeatureMeasurement(FeatureMeasurementTypes.PHASE).isPresent());
  }

  @Test
  public void testBuilderSetMeasurementsThenAddMeasurement() {
    assertDoesNotThrow(() -> {
      SignalDetectionHypothesis.builder(UUID.randomUUID(),
          UUID.randomUUID(), false, UUID.randomUUID())
          .setFeatureMeasurementsByType(ImmutableMap.of(FeatureMeasurementTypes.ARRIVAL_TIME,
              EventTestFixtures.arrivalTimeFeatureMeasurement))
          .addMeasurement(EventTestFixtures.phaseFeatureMeasurement)
          .build();
    });
  }
}
