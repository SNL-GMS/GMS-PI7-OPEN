package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

class CandidateEventGeneratorTests {

  PhaseInfo phaseInfoAbq = PhaseInfo.from(
      PhaseType.P,
      true,
      300.0,
      270.0,
      270.0,
      250.0,
      350.0,
      3.5,
      3.5,
      1.0,
      25.0,
      10.0,
      5.0,
      1.0,
      1.0);
  PhaseInfo phaseInfoSFe = PhaseInfo.from(
      PhaseType.P,
      true,
      300.0,
      285.0,
      285.0,
      250.0,
      350.0,
      3.5,
      3.5,
      1.0,
      25.0,
      10.0,
      5.0,
      1.0,
      1.0);


  ReferenceStation stationTuc = ReferenceStation.create(
      "TUC",  // Tucumcari
      "FAKE",
      StationType.SeismicArray,
      InformationSource.create(
          "FAKE",
          Instant.EPOCH,
          "FAKE"
      ),
      "FAKE",
      35.2,
      -103.8,
      1.5,
      Instant.EPOCH,
      Instant.EPOCH,
      List.of()
  );

  UUID stationIdTuc = stationTuc.getVersionId();

  NodeStation nodeStationAbq = NodeStation.from(
      UUID.randomUUID(),
      stationIdTuc,
      1.0,
      new TreeSet<>() {{
        add(phaseInfoAbq);
      }}
  );
  NodeStation nodeStationSFe = NodeStation.from(
      UUID.randomUUID(),
      stationIdTuc,
      1.0,
      new TreeSet<>() {{
        add(phaseInfoSFe);
      }}
  );

  GridNode gridNodeAbq = GridNode.from(
      UUID.randomUUID(),
      35.1,
      -106.5,
      50.0,
      5.0,
      new TreeSet<>() {{
        add(nodeStationAbq);
      }}
  );
  GridNode gridNodeSFe = GridNode.from(
      UUID.randomUUID(),
      35.7,
      -106.1,
      50.0,
      5.0,
      new TreeSet<>() {{
        add(nodeStationSFe);
      }}
  );

  Set<GridNode> gridNodeSet = new HashSet<>() {{
    add(gridNodeAbq);
    add(gridNodeSFe);
  }};

  static private SignalDetectionHypothesis sdhMock1;
  static private SignalDetectionHypothesis sdhMock2;

  @BeforeAll
  public static void initSdhMocks() {
    sdhMock1 = Mockito.mock(SignalDetectionHypothesis.class);
    Mockito.when(sdhMock1.getFeatureMeasurement(any())).thenAnswer(invocation -> {
      assertTrue(invocation.getArgument(0) == FeatureMeasurementTypes.SLOWNESS
          || invocation.getArgument(0) == FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH
          || invocation.getArgument(0) == FeatureMeasurementTypes.ARRIVAL_TIME);

      if (invocation.getArgument(0) == FeatureMeasurementTypes.SLOWNESS) {
        return Optional.of(FeatureMeasurement.from(
            UUID.randomUUID(),
            UUID.randomUUID(),
            FeatureMeasurementTypes.SLOWNESS,
            NumericMeasurementValue.from(
                Instant.now(),
                DoubleValue.from(27.0, 1.0, Units.SECONDS_PER_DEGREE))));
      } else if (invocation.getArgument(0) == FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH) {
        return Optional.of(FeatureMeasurement.from(
            UUID.randomUUID(),
            UUID.randomUUID(),
            FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
            NumericMeasurementValue.from(
                Instant.now(),
                DoubleValue.from(267.0, 1.0, Units.DEGREES))));
      } else if (invocation.getArgument(0) == FeatureMeasurementTypes.ARRIVAL_TIME) {
        return Optional.of(FeatureMeasurement.from(
            UUID.randomUUID(),
            UUID.randomUUID(),
            FeatureMeasurementTypes.ARRIVAL_TIME,
            InstantValue.from(Instant.EPOCH, Duration.ZERO)));
      }
      throw new Exception();
    });

    sdhMock2 = Mockito.mock(SignalDetectionHypothesis.class);
    Mockito.when(sdhMock2.getFeatureMeasurement(any())).thenAnswer(invocation -> {
      assertTrue(invocation.getArgument(0) == FeatureMeasurementTypes.SLOWNESS
          || invocation.getArgument(0) == FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH
          || invocation.getArgument(0) == FeatureMeasurementTypes.ARRIVAL_TIME);

      if (invocation.getArgument(0) == FeatureMeasurementTypes.SLOWNESS) {
        return Optional.of(FeatureMeasurement.from(
            UUID.randomUUID(),
            UUID.randomUUID(),
            FeatureMeasurementTypes.SLOWNESS,
            NumericMeasurementValue.from(
                Instant.now(),
                DoubleValue.from(30.0, 1.0, Units.SECONDS_PER_DEGREE))));
      } else if (invocation.getArgument(0) == FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH) {
        return Optional.of(FeatureMeasurement.from(
            UUID.randomUUID(),
            UUID.randomUUID(),
            FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
            NumericMeasurementValue.from(
                Instant.now(),
                DoubleValue.from(260.0, 1.0, Units.DEGREES))));
      } else if (invocation.getArgument(0) == FeatureMeasurementTypes.ARRIVAL_TIME) {
        return Optional.of(FeatureMeasurement.from(
            UUID.randomUUID(),
            UUID.randomUUID(),
            FeatureMeasurementTypes.ARRIVAL_TIME,
            InstantValue.from(Instant.EPOCH, Duration.ZERO)));
      }
      throw new Exception();
    });
  }

  @Test
  public void testGenerateCandidateEvents() throws Exception {
    Map<UUID, Set<GridNode>> map = new HashMap<>();
    map.put(stationIdTuc, gridNodeSet);

    SdhStationAssociation sdhsaAbq = SdhStationAssociation.from(sdhMock1, stationTuc);
    SdhStationAssociation sdhsaSFe = SdhStationAssociation.from(sdhMock2, stationTuc);
    List<SdhStationAssociation> sdhsaList = new ArrayList<>() {{
      add(sdhsaAbq);
      add(sdhsaSFe);
    }};

    SignalDetectionAssociatorDefinition definition = Mockito.mock(SignalDetectionAssociatorDefinition.class);
    BDDMockito.given(definition.getSigmaSlowness()).willReturn(80.0);

    CandidateEventGenerator generator = new CandidateEventGenerator()
        .definition(definition)
        .sigmaTime(2.0)
        .gridNodeMap(map)
        .sdhStationAssociations(sdhsaList);

    Optional<Set<CandidateEvent>> opt = generator.generate();

    assertTrue(opt.isPresent());

    Assertions.assertEquals(4, opt.get().size());
  }
}
