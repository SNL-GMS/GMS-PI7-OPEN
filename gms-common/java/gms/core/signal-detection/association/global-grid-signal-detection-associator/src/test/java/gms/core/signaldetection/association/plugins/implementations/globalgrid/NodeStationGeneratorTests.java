package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NodeStationGeneratorTests {

  @Test
  void testNodeStationGeneratorConfiguration() throws Exception {
    // Tests that the generator continues to throw IllegalStateExceptions until
    // all required parameters have been set.

    NodeStationGenerator generator = new NodeStationGenerator();

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    SignalFeaturePredictionUtility predictionUtility = new SignalFeaturePredictionUtility();
    generator.predictionUtility(predictionUtility);
    assertTrue(predictionUtility == generator.predictionUtility());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    String ttpEarthModel = "ak135";
    generator.travelTimePredictionEarthModel(ttpEarthModel);
    assertEquals(ttpEarthModel, generator.travelTimePredictionEarthModel());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    String mapEarthModel = "VeithClawson72";
    generator.magnitudeAttenuationPredictionEarthModel(mapEarthModel);
    assertEquals(mapEarthModel, generator.magnitudeAttenuationPredictionEarthModel());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    generator.gridCylinderRadiusDegrees(2.0);
    assertEquals(2.0, generator.gridCylinderRadiusDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    generator.gridCylinderHeightKm(50.0);
    assertEquals(50.0, generator.gridCylinderHeightKm());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    generator.gridPointLatDegrees(20.0);
    assertEquals(20.0, generator.gridPointLatDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    generator.gridPointLonDegrees(75.0);
    assertEquals(75.0, generator.gridPointLonDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    generator.gridPointDepthKm(10.0);
    assertEquals(10.0, generator.gridPointDepthKm());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    ReferenceStation referenceStation = ReferenceStation.create(
        "MKAR",
        "WHAT??",
        StationType.SeismicArray,
        InformationSource.create("WHO??", Instant.EPOCH, "NO!"),
        "NO!!",
        0.0,
        10.0,
        0.0,
        Instant.EPOCH,
        Instant.EPOCH,
        List.of()
    );

    generator.referenceStation(referenceStation);
    assertEquals(referenceStation, generator.referenceStation());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    List<PhaseType> phaseTypes = List.of(PhaseType.P, PhaseType.S);
    generator.phaseTypes(phaseTypes);
    assertEquals(phaseTypes, generator.phaseTypes());

    assertThrows(IllegalStateException.class, () -> {
      Optional<NodeStation> opt = generator.generate();
    });

    generator.minimumMagnitude(0.5);
    assertEquals(0.5, generator.minimumMagnitude());

    Optional<NodeStation> opt = generator.generate();
  }

  @Test
  void testGenerateNodeStation() throws Exception {

    NodeStationGenerator nodeStationGenerator = new NodeStationGenerator()
        .predictionUtility(new SignalFeaturePredictionUtility())
        .travelTimePredictionEarthModel("ak135")
        .magnitudeAttenuationPredictionEarthModel("VeithClawson72")
        .minimumMagnitude(3.5)
        .gridCylinderRadiusDegrees(0.25)
        .gridCylinderHeightKm(10.0)
        .gridPointLatDegrees(0.0)
        .gridPointLonDegrees(0.0)
        .gridPointDepthKm(50.0)
        .referenceStation(
            ReferenceStation.create(
                "MKAR",
                "Never put bread up your nose ...",
                StationType.SeismicArray,
                InformationSource
                    .create("Because even after ...", Instant.EPOCH, "you take it out ..."),
                "it still feels like it's in there.",
                0.0, 10.0, 0.0,
                Instant.EPOCH, Instant.EPOCH,
                List.of()
            )
        )
        .phaseTypes(
            List.of(PhaseType.P)
        );

    NodeStation nodeStation = nodeStationGenerator.generate().orElseThrow(
        () -> new AssertionError("Expected a valid NodeStation. Received empty Optional"));

    // Assert NodeStationGenerator created the correct number/type of phases
    Assertions.assertEquals(1, nodeStation.getPhaseInfos().size());
    Assertions.assertEquals(PhaseType.P, nodeStation.getPhaseInfos().first().getPhaseType());

    Assertions.assertEquals(10.0, nodeStation.getDistanceFromGridPointDegrees(), 1.0e-8);
  }

}
