package gms.core.signaldetection.association;

import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class CandidateEventTests {

  private final UUID stationId = UUID.randomUUID();
  private final static PhaseInfo phaseInfo = Mockito.mock(PhaseInfo.class);
  private final NodeStation nodeStation = NodeStation.from(
      UUID.randomUUID(),
      stationId,
      0.0,
      new TreeSet<>() {{
        add(phaseInfo);
      }});
  private final SortedSet<NodeStation> nodeStations = new TreeSet<>() {{
    add(nodeStation);
  }};
  private final GridNode gridNode = GridNode.from(
      UUID.randomUUID(),
      0.0,
      0.0,
      0.0,
      0.0,
      nodeStations);

  private FeatureMeasurement<InstantValue> atfm = FeatureMeasurement.create(
      UUID.randomUUID(),
      FeatureMeasurementTypes.ARRIVAL_TIME,
      InstantValue.from(Instant.EPOCH, Duration.ZERO));  // here's where the observed AT is set

  private FeatureMeasurement<PhaseTypeMeasurementValue> pfm = FeatureMeasurement.create(
      UUID.randomUUID(),
      FeatureMeasurementTypes.PHASE,
      PhaseTypeMeasurementValue.from(PhaseType.P, 0.5));

  private final SignalDetectionHypothesis sdh = SignalDetectionHypothesis.from(
      UUID.randomUUID(),
      UUID.randomUUID(),
      true,
      List.of(atfm, pfm),
      UUID.randomUUID());

  private final double sigmaTime = 2.0;

  private final Set<SignalDetectionHypothesis> sdhSet = new HashSet<>();

  @BeforeAll
  public static void init() {
    Mockito.when(phaseInfo.getPhaseType()).thenReturn(PhaseType.P);
  }

  @Test
  public void testFromWithNullStationId() {
    Assertions.assertEquals("Cannot create a CandidateEvent from a null stationId",
        Assertions.assertThrows(NullPointerException.class,
            () -> CandidateEvent.from(null, gridNode, sdh, sigmaTime, sdhSet)).getMessage());
  }

  @Test
  public void testFromWithNullGridNode() {
    Assertions.assertEquals("Cannot create a CandidateEvent from a null gridNode",
        Assertions.assertThrows(NullPointerException.class,
            () -> CandidateEvent.from(stationId, null, sdh, sigmaTime, sdhSet)).getMessage());
  }

  @Test
  public void testFromWithNullDriverSdh() {
    Assertions.assertEquals("Cannot create a CandidateEvent from a null driverSdh",
        Assertions.assertThrows(NullPointerException.class,
            () -> CandidateEvent.from(stationId, gridNode, null, sigmaTime, sdhSet)).getMessage());
  }

  @Test
  public void testFromWithNegativeSigmaTime() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> CandidateEvent.from(stationId, gridNode, sdh, -1.0, sdhSet));
  }

  @Test
  public void testFromWithNullCorroboratingSet() {
    Assertions.assertEquals("Cannot create a CandidateEvent from a null corroboratingSet",
        Assertions.assertThrows(NullPointerException.class,
            () -> CandidateEvent.from(stationId, gridNode, sdh, sigmaTime, null)).getMessage());
  }

  @Test
  public void testThreeParameterFrom() throws Exception {
    CandidateEvent ce = CandidateEvent.from(stationId, gridNode, sdh, sigmaTime);
    Assertions.assertNotNull(ce.getCorroboratingSet());
  }

  @Test
  public void testGetOriginTime() throws Exception {
    CandidateEvent ce = CandidateEvent.from(stationId, gridNode, sdh, sigmaTime);
    Assertions.assertEquals(Instant.EPOCH, ce.getOriginTime());
  }

  @Test
  public void testPassesConstraintTravelTimeConstraintCheck() throws Exception {
    Mockito.when(phaseInfo.getTravelTimeSeconds()).thenReturn(300.0);
    Mockito.when(phaseInfo.getTravelTimeMaximum()).thenReturn(200.0);
    Mockito.when(phaseInfo.getTravelTimeMinimum()).thenReturn(150.0);

    final ReferenceStation station = ReferenceStation.create(
        "FAKE",
        "East Kaboodleville",
        StationType.SeismicArray,
        InformationSource.from(
            "FAKE",
            Instant.EPOCH,
            "FAKE"
        ),
        "FAKE",
        0.0,
        0.0,
        0.0,
        Instant.EPOCH,
        Instant.EPOCH,
        List.of()
    );

    SortedSet<NodeStation> newNodeStations = new TreeSet<>();

    NodeStation newNodeStation = NodeStation.from(
        station.getEntityId(),
        station.getVersionId(),
        0.0,
        new TreeSet<>() {{
          add(phaseInfo);
        }});

    newNodeStations.add(
        newNodeStation
    );

    final GridNode newGridNode = GridNode.from(
        station.getVersionId(),
        0.0,
        0.0,
        0.0,
        0.0,
        newNodeStations
    );

    CandidateEvent ce = CandidateEvent
        .from(newNodeStation.getStationId(), newGridNode, sdh, sigmaTime, sdhSet);

    // testing the driver SDH of this CandidateEvent which should always pass
    Assertions.assertTrue(
        ce.passesTravelTimeConstraintCheck(SdhStationAssociation.from(sdh, station))
    );
  }

}
