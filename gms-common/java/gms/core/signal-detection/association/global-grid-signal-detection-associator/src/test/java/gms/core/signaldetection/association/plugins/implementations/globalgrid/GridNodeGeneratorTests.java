package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GridNodeGeneratorTests {

  @Test
  void testGridNodeGeneratorConfiguration() throws Exception {

    GridNodeGenerator gridNodeGenerator = new GridNodeGenerator();

    // Can't succeed until all required parameters have been set.

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    gridNodeGenerator.gridPointLatDegrees(45.0);
    assertEquals(45.0, gridNodeGenerator.gridPointLatDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    gridNodeGenerator.gridPointLonDegrees(-120.0);
    assertEquals(-120.0, gridNodeGenerator.gridPointLonDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    gridNodeGenerator.gridPointDepthKm(25.0);
    assertEquals(25.0, gridNodeGenerator.gridPointDepthKm());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    SignalFeaturePredictionUtility utility = new SignalFeaturePredictionUtility();
    gridNodeGenerator.predictionUtility(utility);

    assertTrue(utility == gridNodeGenerator.predictionUtility());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    gridNodeGenerator.travelTimePredictionEarthModel("ak135");
    assertEquals("ak135", gridNodeGenerator.travelTimePredictionEarthModel());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    gridNodeGenerator.magnitudeAttenuationPredictionEarthModel(
        "VeithClawson72"
    );
    assertEquals("VeithClawson72",
        gridNodeGenerator.magnitudeAttenuationPredictionEarthModel());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    gridNodeGenerator.minimumMagnitude(1.0);
    assertEquals(1.0, gridNodeGenerator.minimumMagnitude());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    gridNodeGenerator.gridCylinderHeightKm(50.0);
    assertEquals(50.0, gridNodeGenerator.gridCylinderHeightKm());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    gridNodeGenerator.gridCylinderRadiusDegrees(5.0);
    assertEquals(5.0, gridNodeGenerator.gridCylinderRadiusDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    List<ReferenceStation> referenceStations = List.of(ReferenceStation.create(
        "MKAR",
        "Never put bread up your nose ...",
        StationType.SeismicArray,
        InformationSource
            .create("Because even after ...", Instant.EPOCH, "you take it out ..."),
        "it still feels like it's in there.",
        0.0, 20.0, 0.0,
        Instant.EPOCH, Instant.EPOCH,
        List.of()
    ));

    gridNodeGenerator.referenceStations(referenceStations);
    assertEquals(referenceStations, gridNodeGenerator.referenceStations());

    assertThrows(IllegalStateException.class, () -> {
      Optional<GridNode> opt = gridNodeGenerator.generate();
    });

    List<PhaseType> phaseTypes = List.of(PhaseType.P);

    gridNodeGenerator.phaseTypes(phaseTypes);
    assertEquals(phaseTypes, gridNodeGenerator.phaseTypes());

    Optional<GridNode> opt = gridNodeGenerator.generate();
  }

  @Test
  void testGenerateGridNode() throws Exception {

    GridNodeGenerator gridNodeGenerator = new GridNodeGenerator();

    gridNodeGenerator.gridPointLatDegrees(0.0)
        .gridPointLonDegrees(10.0)
        .gridPointDepthKm(50.0)
        .predictionUtility(new SignalFeaturePredictionUtility())
        .travelTimePredictionEarthModel("ak135")
        .magnitudeAttenuationPredictionEarthModel("VeithClawson72")
        .minimumMagnitude(0.0)
        .gridCylinderRadiusDegrees(0.25)
        .gridCylinderHeightKm(10.0)
        .referenceStations(List.of(ReferenceStation.create(
            "MKAR",
            "Never put bread up your nose ...",
            StationType.SeismicArray,
            InformationSource
                .create("Because even after ...", Instant.EPOCH, "you take it out ..."),
            "it still feels like it's in there.",
            0.0, 20.0, 0.0,
            Instant.EPOCH, Instant.EPOCH,
            List.of()
            )))
        .phaseTypes(List.of(PhaseType.P));

    GridNode gridNode = gridNodeGenerator.generate().orElseThrow(
        () -> {
          throw new AssertionError("Expected a valid GridNode.  Received empty Optional.");
        });

    assertEquals(0.0, gridNode.getCenterLatitudeDegrees());
    assertEquals(10.0, gridNode.getCenterLongitudeDegrees());
    assertEquals(50.0, gridNode.getCenterDepthKm());
    assertEquals(10.0, gridNode.getGridCellHeightKm());
    assertEquals(1, gridNode.getNodeStations().size());
    assertEquals(PhaseType.P,
        gridNode.getNodeStations().iterator().next().getPhaseInfos().first().getPhaseType());
  }

}
