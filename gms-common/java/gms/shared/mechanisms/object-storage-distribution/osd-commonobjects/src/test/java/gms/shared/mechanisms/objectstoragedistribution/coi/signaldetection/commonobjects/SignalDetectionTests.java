package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link SignalDetection} factory creation
 */
public class SignalDetectionTests {

  private final UUID id = UUID.randomUUID();
  private String monitoringOrganization = "CTBTO";
  private UUID stationId = UUID.randomUUID();
  private final UUID creationInfoId = UUID.randomUUID();

  private List<FeatureMeasurement<?>> featureMeasurements =
      List.of(EventTestFixtures.arrivalTimeFeatureMeasurement, EventTestFixtures.phaseFeatureMeasurement);


  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testSerialization() throws Exception {
    final SignalDetection signalDetection = SignalDetection.from(
        id, "CTBTO", stationId, Collections.emptyList(), creationInfoId);
    signalDetection.addSignalDetectionHypothesis(featureMeasurements, creationInfoId);
    TestUtilities.testSerialization(signalDetection, SignalDetection.class);
  }

  @Test
  public void testFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(SignalDetection.class, "from",
        id, monitoringOrganization, stationId, Collections.emptyList(), creationInfoId);
  }

  @Test
  public void testCreateNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(SignalDetection.class, "create",
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);
  }

  @Test
  public void testFrom() {
    SignalDetection signalDetection = SignalDetection.from(
        id, monitoringOrganization, stationId, Collections.emptyList(), creationInfoId);

    signalDetection.addSignalDetectionHypothesis(featureMeasurements, creationInfoId);

    assertEquals(id, signalDetection.getId());
    assertEquals(monitoringOrganization, signalDetection.getMonitoringOrganization());
    assertEquals(stationId, signalDetection.getStationId());
    assertEquals(1, signalDetection.getSignalDetectionHypotheses().size());
    assertEquals(creationInfoId, signalDetection.getCreationInfoId());
  }

  @Test
  public void testCreate() {
    SignalDetection signalDetection = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);

    assertEquals(monitoringOrganization, signalDetection.getMonitoringOrganization());
    assertEquals(stationId, signalDetection.getStationId());
    assertEquals(1, signalDetection.getSignalDetectionHypotheses().size());
    assertArrayEquals(featureMeasurements.toArray(),
        signalDetection.getSignalDetectionHypotheses().get(0).getFeatureMeasurements().toArray());
    assertEquals(creationInfoId, signalDetection.getCreationInfoId());
  }

  @Test
  public void testRejectNullId() {
    exception.expect(NullPointerException.class);

    SignalDetection signalDetection = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);
    signalDetection.reject(null, UUID.randomUUID());
  }

  @Test
  public void testRejectInvalidId() {
    exception.expect(IllegalArgumentException.class);

    SignalDetection signalDetection = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);
    signalDetection.reject(UUID.randomUUID(), UUID.randomUUID());
  }

  @Test
  public void testReject() {
    SignalDetection signalDetection = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);
    SignalDetectionHypothesis signalDetectionHypothesis = signalDetection
        .getSignalDetectionHypotheses().get(0);

    signalDetection.reject(signalDetectionHypothesis.getId(), UUID.randomUUID());

    assertEquals(2, signalDetection.getSignalDetectionHypotheses().size());
    assertEquals(true, signalDetection.getSignalDetectionHypotheses().get(1).isRejected());
  }

  @Test
  public void testOnlyContainsRejected() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot create a SignalDetection containing only rejected SignalDetectionHypotheses");

    SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.from(
        id, id, true, featureMeasurements, creationInfoId);

    SignalDetection.from(id, monitoringOrganization, stationId,
        Arrays.asList(signalDetectionHypothesis), creationInfoId);
  }

  @Test
  public void testEqualsHashCode() {

    final SignalDetection sd1 = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);

    final SignalDetection sd2 = SignalDetection.from(
        sd1.getId(), sd1.getMonitoringOrganization(), sd1.getStationId(),
        sd1.getSignalDetectionHypotheses(), sd1.getCreationInfoId());

    assertEquals(sd1, sd2);
    assertEquals(sd2, sd1);
    assertEquals(sd1.hashCode(), sd2.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() {

    final SignalDetection sd1 = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);
    SignalDetection sd2 = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);

    // Different id
    assertNotEquals(sd1, sd2);

    // Different monitoring org
    sd2 = SignalDetection.from(sd1.getId(), "diffMonitoringOrg",
        sd1.getStationId(), sd1.getSignalDetectionHypotheses(), sd1.getCreationInfoId());
    assertNotEquals(sd1, sd2);

    // Different station id
    sd2 = SignalDetection.from(sd1.getId(), sd1.getMonitoringOrganization(),
        UUID.randomUUID(), sd1.getSignalDetectionHypotheses(), sd1.getCreationInfoId());
    assertNotEquals(sd1, sd2);

    // Different signal hypotheses
    sd2 = SignalDetection.from(sd1.getId(), sd1.getMonitoringOrganization(),
        sd1.getStationId(), Collections.emptyList(), UUID.randomUUID());
    assertNotEquals(sd1, sd2);

    // Different creation info id
    sd2 = SignalDetection.from(sd1.getId(), sd1.getMonitoringOrganization(),
        sd1.getStationId(), sd1.getSignalDetectionHypotheses(), UUID.randomUUID());
    assertNotEquals(sd1, sd2);
  }
}
