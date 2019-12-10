package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SignalDetectionRepositoryJpaTests {

  private EntityManagerFactory entityManagerFactory;

  private SignalDetectionRepository signalDetectionRepositoryJpa;
  private final UUID stationId = UUID.randomUUID();
  private final UUID creationInfoId = UUID.randomUUID();
  private final UUID channelSegmentId = UUID.randomUUID();
  private final FeatureMeasurement<InstantValue> arrivalTimeMeasurement = FeatureMeasurement
      .create(stationId, FeatureMeasurementTypes.ARRIVAL_TIME,
          InstantValue.from(Instant.now(), Duration.ofMillis(2)));
  private final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement =
      FeatureMeasurement.create(
          stationId, FeatureMeasurementTypes.PHASE,
          PhaseTypeMeasurementValue.from(PhaseType.P, 0.5));

  private List<FeatureMeasurement<?>> featureMeasurements = List.of(arrivalTimeMeasurement,
      phaseMeasurement);

  private String monitoringOrganization = "CTBTO";

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
    signalDetectionRepositoryJpa = SignalDetectionRepositoryJpa.create(entityManagerFactory);
  }

  @After
  public void tearDown() {
    entityManagerFactory.close();
    entityManagerFactory = null;
    signalDetectionRepositoryJpa = null;
  }

  @Test
  public void testCreateNullEntityManagerFactoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot create SignalDetectionRepositoryJpa with a null EntityManagerFactory");
    SignalDetectionRepositoryJpa.create(null);
  }

  @Test
  public void testStoreNullParameter() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store a null BeamCreationInfo");
    BeamCreationInfo bci = null;
    signalDetectionRepositoryJpa.store(bci);

    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store a null SignalDetection");
    SignalDetection sd = null;
    signalDetectionRepositoryJpa.store(sd);
  }

  @Test
  public void testRetrieveAllExpectEmptyCollection() {
    Collection<SignalDetection> signalDetections = signalDetectionRepositoryJpa.retrieveAll();

    assertNotNull(signalDetections);
    assertEquals(0, signalDetections.size());
  }

  @Test
  public void testFindSignalDetectionByIdNullId() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot query using a null SignalDetection id");
    signalDetectionRepositoryJpa.findSignalDetectionById(null);
  }

  @Test
  public void testFindSignalDetectionByIdInvalidId() {
    Optional<SignalDetection> signalDetection =
        signalDetectionRepositoryJpa.findSignalDetectionById(UUID.randomUUID());

    assertEquals(false, signalDetection.isPresent());
  }

  /**
   * Test storing a SignalDetection with one SignalDetectionHypothesis, with the exact same object
   * count and values coming back from a query.
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreSignalDetection() {
    storeAndRetrieveAll(1);
  }

  @Test
  public void testStoreSignalDetectionHypthesesNullCollection() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store null hypotheses collection");
    signalDetectionRepositoryJpa.store((Collection<SignalDetectionHypothesisDescriptor>) null);
  }

  @Test
  public void testStoreSignalDetectionHypotheses() {
    SignalDetection signalDetection = SignalDetection.create(monitoringOrganization,
        stationId,
        featureMeasurements,
        creationInfoId);

    for (int i = 0; i < 3; i++) {
      FeatureMeasurement<InstantValue> measurement1 = FeatureMeasurement.create(channelSegmentId,
          FeatureMeasurementTypes.ARRIVAL_TIME,
          InstantValue.from(Instant.EPOCH, Duration.ofSeconds(1)));
      FeatureMeasurement<PhaseTypeMeasurementValue> measurement2 =
          FeatureMeasurement.create(channelSegmentId,
              FeatureMeasurementTypes.PHASE,
              PhaseTypeMeasurementValue.from(PhaseType.P, .98));
      signalDetection.addSignalDetectionHypothesis(List.of(measurement1, measurement2),
          creationInfoId);
    }

    signalDetectionRepositoryJpa.store(signalDetection);

    assertTrue(
        signalDetectionRepositoryJpa.findSignalDetectionById(signalDetection.getId()).isPresent());

    FeatureMeasurement<InstantValue> measurement1 = FeatureMeasurement.create(channelSegmentId,
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(Instant.EPOCH, Duration.ofSeconds(1)));
    FeatureMeasurement<PhaseTypeMeasurementValue> measurement2 =
        FeatureMeasurement.create(channelSegmentId,
            FeatureMeasurementTypes.PHASE,
            PhaseTypeMeasurementValue.from(PhaseType.P, .98));
    FeatureMeasurement<NumericMeasurementValue> newValue =
        FeatureMeasurement.create(channelSegmentId,
            FeatureMeasurementTypes.PERIOD,
            NumericMeasurementValue.from(Instant.EPOCH,
                DoubleValue.from(1.0, 2.0, Units.UNITLESS)));
    signalDetection.addSignalDetectionHypothesis(List.of(measurement1, measurement2, newValue),
        creationInfoId);

    signalDetectionRepositoryJpa.store(signalDetection.getSignalDetectionHypotheses().stream()
        .map(sdh -> SignalDetectionHypothesisDescriptor.from(sdh, signalDetection.getStationId()))
        .collect(toList()));

    List<UUID> signalDetectionHypothesisIds =
        signalDetection.getSignalDetectionHypotheses().stream()
            .map(SignalDetectionHypothesis::getId)
            .collect(toList());

    assertEquals(5, signalDetectionHypothesisIds.size());

    Collection<SignalDetectionHypothesis> hypotheses2 =
        signalDetectionRepositoryJpa
            .findSignalDetectionHypothesesByIds(signalDetectionHypothesisIds);
    assertEquals(signalDetectionHypothesisIds.size(), hypotheses2.size());

    for (SignalDetectionHypothesis hypothesis : signalDetection.getSignalDetectionHypotheses()) {
      Optional<SignalDetectionHypothesis> possibleStoredHypothesis =
          signalDetectionRepositoryJpa.findSignalDetectionHypothesisById(hypothesis.getId());
      assertTrue(signalDetectionHypothesisIds.contains(hypothesis.getId()));
      assertTrue(possibleStoredHypothesis.isPresent());
      SignalDetectionHypothesis storedHypothesis = possibleStoredHypothesis.get();
      assertEquals(hypothesis.getParentSignalDetectionId(),
          storedHypothesis.getParentSignalDetectionId());
      assertEquals(hypothesis.getCreationInfoId(), storedHypothesis.getCreationInfoId());
      assertEquals(hypothesis.getFeatureMeasurements().size(),
          storedHypothesis.getFeatureMeasurements().size());
      for (FeatureMeasurement featureMeasurement : hypothesis.getFeatureMeasurements()) {
        Optional<FeatureMeasurement> possibleFeatureMeasurement =
            storedHypothesis.getFeatureMeasurement(featureMeasurement.getFeatureMeasurementType());
        assertTrue(possibleFeatureMeasurement.isPresent());
        FeatureMeasurement storedFeatureMeasurement = possibleFeatureMeasurement.get();
        assertEquals(featureMeasurement, storedFeatureMeasurement);
      }
    }
  }

  @Test
  public void testStoreSignalDetectionHypothesesAddFeatureMeasurements() {
    SignalDetection signalDetection = SignalDetection.create(monitoringOrganization,
        stationId,
        featureMeasurements,
        creationInfoId);

    FeatureMeasurement<InstantValue> measurement1 = FeatureMeasurement.create(channelSegmentId,
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(Instant.EPOCH, Duration.ofSeconds(1)));
    FeatureMeasurement<PhaseTypeMeasurementValue> measurement2 =
        FeatureMeasurement.create(channelSegmentId,
            FeatureMeasurementTypes.PHASE,
            PhaseTypeMeasurementValue.from(PhaseType.P, .98));
    signalDetection.addSignalDetectionHypothesis(List.of(measurement1, measurement2),
        creationInfoId);

    signalDetectionRepositoryJpa.store(signalDetection);

    SignalDetectionHypothesis original = signalDetection.getSignalDetectionHypotheses().get(0);

    FeatureMeasurement<NumericMeasurementValue> newMeasurement = FeatureMeasurement.create(
        channelSegmentId,
        FeatureMeasurementTypes.PERIOD,
        NumericMeasurementValue.from(Instant.EPOCH,
            DoubleValue.from(1.0, 2.0, Units.UNITLESS)));

    SignalDetectionHypothesis updated = original.toBuilder()
        .addMeasurement(newMeasurement)
        .build();

    signalDetectionRepositoryJpa.store(
        List.of(SignalDetectionHypothesisDescriptor.from(updated, signalDetection.getStationId())));

    Optional<SignalDetectionHypothesis> possibleSdh =
        signalDetectionRepositoryJpa.findSignalDetectionHypothesisById(updated.getId());

    assertTrue(possibleSdh.isPresent());

    SignalDetectionHypothesis stored = possibleSdh.get();
    assertEquals(updated.getFeatureMeasurements().size(), stored.getFeatureMeasurements().size());
    assertEquals(updated, stored);
  }

  @Test
  public void testRetrieveAllSignalDetections() {
    storeAndRetrieveAll(2);
  }

  /**
   * Internal method to save "count" number of signal detections to the database, and then retrieve
   * all SignalDetections from the database and verify they matched what was saved.
   */
  private void storeAndRetrieveAll(int count) {
    // Loop through and save "count" number of SignalDetections
    int i = count;
    ArrayList<SignalDetection> signalDetections = new ArrayList<>(count);
    while (i > 0) {
      SignalDetection signalDetection = SignalDetection.create(
          monitoringOrganization, stationId, featureMeasurements, creationInfoId);
      signalDetections.add(signalDetection);

      signalDetectionRepositoryJpa.store(signalDetection);

      i--;
    }

    // Query for all signal detections and make sure we get back same amount.
    Collection<SignalDetection> dbSignalDetections = signalDetectionRepositoryJpa.retrieveAll();
    assertEquals(count, dbSignalDetections.size());
    assertTrue(signalDetections.containsAll(dbSignalDetections));
  }

  /**
   * Test storing a SignalDetection with one hypothesis, adding another hypothesis and re-storing
   * the SignalDetection. This tests that previously persisted SignalDetections and hypotheses are
   * not duplicated in the database
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreSignalDetectionNewSignalDetectionHypothesis() {
    SignalDetection signalDetection = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);

    signalDetectionRepositoryJpa.store(signalDetection);

    signalDetection.addSignalDetectionHypothesis(featureMeasurements, creationInfoId);

    signalDetectionRepositoryJpa.store(signalDetection);

    Optional<SignalDetection> signalDetectionOptional = signalDetectionRepositoryJpa
        .findSignalDetectionById(signalDetection.getId());
    assertTrue(signalDetectionOptional.isPresent());
    assertEquals(signalDetection, signalDetectionOptional.get());
  }

  @Test
  public void testFindSignalDetectionsById() {
    storeAndFindById(1);
    storeAndFindById(2);
  }

  @Test
  public void testFindSignalDetectionHypothesesByIds() {
    // make two signal detections with two hypotheses each
    final SignalDetection signalDetection = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);
    signalDetection.addSignalDetectionHypothesis(featureMeasurements, UUID.randomUUID());
    signalDetection.addSignalDetectionHypothesis(featureMeasurements, UUID.randomUUID());
    final SignalDetection signalDetection2 = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);
    signalDetection2.addSignalDetectionHypothesis(featureMeasurements, UUID.randomUUID());
    signalDetection2.addSignalDetectionHypothesis(featureMeasurements, UUID.randomUUID());
    // store both signal detections
    signalDetectionRepositoryJpa.store(signalDetection);
    signalDetectionRepositoryJpa.store(signalDetection2);
    final SignalDetectionHypothesis hyp1 = signalDetection.getSignalDetectionHypotheses().get(0);
    final SignalDetectionHypothesis hyp2 = signalDetection2.getSignalDetectionHypotheses().get(0);
    // query for hypotheses by a hypothesis ID from the first detection, only find the first
    // hypothesis.
    List<SignalDetectionHypothesis> results =
        signalDetectionRepositoryJpa.findSignalDetectionHypothesesByIds(
            Set.of(hyp1.getId()));
    assertEquals(Set.of(hyp1), new HashSet<>(results));
    // query for hypotheses by a hypothesis ID from the 2nd detection, only find the second
    // hypothesis.
    results = signalDetectionRepositoryJpa.findSignalDetectionHypothesesByIds(
        List.of(hyp2.getId()));
    assertEquals(Set.of(hyp2), new HashSet<>(results));
    // query for hypotheses by two hypothesis ID's, one from both detections, find both of those
    // hypotheses.
    results = signalDetectionRepositoryJpa.findSignalDetectionHypothesesByIds(
        List.of(hyp1.getId(), hyp2.getId()));
    assertEquals(Set.of(hyp1, hyp2), new HashSet<>(results));
    // query for hypotheses by a couple of random hypothesis ID's, find nothing
    results = signalDetectionRepositoryJpa.findSignalDetectionHypothesesByIds(
        List.of(UUID.randomUUID(), UUID.randomUUID()));
    assertTrue(results.isEmpty());
    // query for hypotheses by one random ID and one real one, find one hypotheses (and no error
    // occurs)
    results = signalDetectionRepositoryJpa.findSignalDetectionHypothesesByIds(
        List.of(UUID.randomUUID(), hyp2.getId()));
    assertEquals(Set.of(hyp2), new HashSet<>(results));
  }

  /**
   * Internal method to save "count" number of signal detections to the database, and then retrieve
   * those SignalDetections based upon ID from the database and verify they matched what was saved.
   */
  private void storeAndFindById(int count) {
    // Loop through and save "count" number of SignalDetections
    int i = count;
    ArrayList<SignalDetection> signalDetections = new ArrayList<>(count);
    while (i > 0) {
      SignalDetection signalDetection = SignalDetection.create(
          monitoringOrganization, stationId, featureMeasurements, creationInfoId);
      signalDetections.add(signalDetection);

      signalDetectionRepositoryJpa.store(signalDetection);

      i--;
    }

    // Test query by signal ID
    ArrayList<SignalDetection> dbSignalDetections = new ArrayList<>(count);
    for (SignalDetection signalDetection : signalDetections) {
      dbSignalDetections.add(
          signalDetectionRepositoryJpa.findSignalDetectionById(signalDetection.getId()).get());
    }

    // Test query using a list of IDs
    List<UUID> signalDetectionIds =
        signalDetections.stream().map(SignalDetection::getId).collect(toList());
    List<SignalDetection> dbSignalDetections2 =
        signalDetectionRepositoryJpa.findSignalDetectionsByIds(signalDetectionIds);

    assertEquals(count, dbSignalDetections.size());
    assertTrue(signalDetections.containsAll(dbSignalDetections));
    assertTrue(dbSignalDetections.containsAll(dbSignalDetections2));
    assertTrue(dbSignalDetections2.containsAll(dbSignalDetections));
  }

  @Test
  public void testFindSignalDetectionHypothesisById() {
    // create and store a signal detection with two hypotheses.
    final SignalDetection signalDetection = SignalDetection.create(
        monitoringOrganization, stationId, featureMeasurements, creationInfoId);
    signalDetection.addSignalDetectionHypothesis(
        featureMeasurements, creationInfoId);
    signalDetection.addSignalDetectionHypothesis(
        featureMeasurements, creationInfoId);
    signalDetectionRepositoryJpa.store(signalDetection);
    for (SignalDetectionHypothesis sdh : signalDetection.getSignalDetectionHypotheses()) {
      final Optional<SignalDetectionHypothesis> retrievedSdh
          = signalDetectionRepositoryJpa.findSignalDetectionHypothesisById(sdh.getId());
      assertNotNull(retrievedSdh);
      assertTrue(retrievedSdh.isPresent());
      assertEquals(retrievedSdh.get(), sdh);
    }
    // retrieve SDH by non-existent ID, assert Optional.empty is returned.
    final Optional<SignalDetectionHypothesis> emptySdh
        = signalDetectionRepositoryJpa.findSignalDetectionHypothesisById(UUID.randomUUID());
    assertNotNull(emptySdh);
    assertFalse(emptySdh.isPresent());
    // try retrieving SDH by null ID, get an error.
    exception.expect(NullPointerException.class);
    exception.expectMessage("cannot find signal detection hypothesis by null id");
    signalDetectionRepositoryJpa.findSignalDetectionHypothesisById(null);
  }

  @Test
  public void testFindSignalDetectionsByStationId() {
    // create and store two signal detections, each with two hypotheses.
    final Instant time1 = Instant.EPOCH, time2 = time1.plusSeconds(30);
    final FeatureMeasurement<InstantValue> arrivalTime1 = FeatureMeasurement.create(stationId,
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(time1, Duration.ofMillis(1)));
    final SignalDetection det = SignalDetection.create(
        monitoringOrganization, stationId, List.of(arrivalTime1, phaseMeasurement), creationInfoId);
    det.addSignalDetectionHypothesis(
        featureMeasurements, UUID.randomUUID());
    final FeatureMeasurement<InstantValue> arrivalTime2 = FeatureMeasurement.create(stationId,
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(time2, Duration.ofMillis(1)));
    final SignalDetection det2 = SignalDetection.create(
        monitoringOrganization, stationId, List.of(arrivalTime2, phaseMeasurement), creationInfoId);
    det2.addSignalDetectionHypothesis(
        featureMeasurements, creationInfoId);
    signalDetectionRepositoryJpa.store(det);
    signalDetectionRepositoryJpa.store(det2);
    // query by stationId and time range that finds both
    List<SignalDetection> dets = signalDetectionRepositoryJpa.findSignalDetections(
        time1, time2);

    assertThat(dets, hasItems(det, det2));
    // query by time range that only finds first one
    dets = signalDetectionRepositoryJpa.findSignalDetections(
        time1.minusMillis(5), time1.plusMillis(5));
    assertEquals(List.of(det), dets);
    // query by time range that only finds second one
    dets = signalDetectionRepositoryJpa.findSignalDetections(
        time2.minusMillis(5), time2.plusMillis(5));
    assertEquals(List.of(det2), dets);
    // query by time range that finds neither
    dets = signalDetectionRepositoryJpa.findSignalDetections(
        time2.plusSeconds(1), time2.plusSeconds(10));
    assertEquals(List.of(), dets);
  }

  @Test
  public void testFindSignalDetectionsByStationIds() {
    // create and store two signal detections, each with one hypotheses.
    final Instant time1 = Instant.EPOCH, time2 = time1.plusSeconds(30), time3 = time2
        .plusSeconds(30);
    final UUID staId1 = UUID.randomUUID(), staId2 = UUID.randomUUID();
    final FeatureMeasurement<InstantValue> arrivalTime1 = FeatureMeasurement.create(stationId,
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(time1, Duration.ofMillis(1)));
    final SignalDetection det = SignalDetection.create(
        monitoringOrganization, staId1, List.of(arrivalTime1, phaseMeasurement), creationInfoId);
    det.addSignalDetectionHypothesis(
        featureMeasurements, UUID.randomUUID());
    final FeatureMeasurement<InstantValue> arrivalTime2 = FeatureMeasurement.create(stationId,
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(time2, Duration.ofMillis(1)));
    final SignalDetection det2 = SignalDetection.create(
        monitoringOrganization, staId2, List.of(arrivalTime2, phaseMeasurement), creationInfoId);
    det2.addSignalDetectionHypothesis(
        featureMeasurements, UUID.randomUUID());
    det2.addSignalDetectionHypothesis(
        List.of(FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
            InstantValue.from(time3, Duration.ofMillis(1))),
            phaseMeasurement), UUID.randomUUID());
    signalDetectionRepositoryJpa.store(det);
    signalDetectionRepositoryJpa.store(det2);
    // query by staId1 and staId2 and time range that finds both
    Map<UUID, List<SignalDetection>> dets
        = signalDetectionRepositoryJpa.findSignalDetectionsByStationIds(
        List.of(staId1, staId2), time1, time2);
    assertEquals(dets.get(staId1), List.of(det));
    assertEquals(dets.get(staId2), List.of(det2));
    assertEquals(2, dets.keySet().size());  // only two keys present (no extraneous results)
    // query by staId1 and staId2 and time range that only finds first one
    dets = signalDetectionRepositoryJpa.findSignalDetectionsByStationIds(
        List.of(staId1, staId2), time1.minusMillis(5), time1.plusMillis(5));
    assertEquals(dets.get(staId1), List.of(det));
    assertEquals(1, dets.keySet().size());
    // query by staId1 and staId2 and time range that only finds second one
    dets = signalDetectionRepositoryJpa.findSignalDetectionsByStationIds(
        List.of(staId1, staId2), time2.minusMillis(5), time2.plusMillis(5));
    assertEquals(dets.get(staId2), List.of(det2));
    assertEquals(1, dets.keySet().size());
    // query by staId1 and staId2 and time range that finds neither
    dets = signalDetectionRepositoryJpa.findSignalDetectionsByStationIds(
        List.of(staId1, staId2), time1.minusSeconds(60), time1.minusSeconds(30));
    assertTrue(dets.isEmpty());
    // query by non-existent ID's and time range that would have found both, expect neither.
    dets = signalDetectionRepositoryJpa.findSignalDetectionsByStationIds(
        List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()), time1, time2);
    assertTrue(dets.isEmpty());
    // query by staId2 and non-existent ID, time range that would have found both, only expect
    // detection 2.
    dets = signalDetectionRepositoryJpa.findSignalDetectionsByStationIds(
        List.of(staId2, UUID.randomUUID()), time1, time2);
    assertEquals(dets.get(staId2), List.of(det2));
    assertEquals(1, dets.keySet().size());
  }
}
