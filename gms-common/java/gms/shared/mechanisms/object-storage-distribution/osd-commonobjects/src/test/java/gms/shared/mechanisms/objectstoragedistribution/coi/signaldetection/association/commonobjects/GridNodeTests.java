package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class GridNodeTests {

  private static final UUID id = UUID.randomUUID();

  private static final UUID nodeStationId1 = UUID.randomUUID();
  private static final UUID nodeStationId2 = UUID.randomUUID();

  private static final UUID nodeStationStationId1 = UUID.randomUUID();
  private static final UUID nodeStationStationId2 = UUID.randomUUID();

  private static final double centerLatitudeDegrees = 46;
  private static final double centerLongitudeDegrees = 45;
  private static final double centerDepthKm = 100;
  private static final double gridCellHeightKm = 1;

  private static PhaseInfo phaseInfo = PhaseInfo.from(
      PhaseType.P,
      true,
      0.0,
      00,
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
      0.0
  );

  private static final SortedSet<NodeStation> nodeStationsRef = new TreeSet<>() {{
    add(NodeStation.from(nodeStationId1, nodeStationStationId1, 0.0, new TreeSet<>() {{
      add(GridNodeTests.phaseInfo);
    }}));
    add(NodeStation.from(nodeStationId2, nodeStationStationId2, 0.1, new TreeSet<>() {{
      add(GridNodeTests.phaseInfo);
    }}));
  }};

  private static final SortedSet<NodeStation> nodeStationsTest = new TreeSet<>() {{
    add(NodeStation.from(nodeStationId1, nodeStationStationId1, 0.0, new TreeSet<>() {{
      add(GridNodeTests.phaseInfo);
    }}));
    add(NodeStation.from(nodeStationId2, nodeStationStationId2, 0.1, new TreeSet<>() {{
      add(GridNodeTests.phaseInfo);
    }}));
  }};


  private static GridNode gridNode = GridNode.from(
      id,
      centerLongitudeDegrees,
      centerLatitudeDegrees,
      centerDepthKm,
      gridCellHeightKm,
      nodeStationsRef
  );

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(gridNode, GridNode.class);
  }

  @Test
  void testFrom() {
    GridNode gridNode = GridNode.from(
        id,
        centerLatitudeDegrees,
        centerLongitudeDegrees,
        centerDepthKm,
        gridCellHeightKm,
        nodeStationsTest
    );

    assertEquals(id, gridNode.getId());
    assertEquals(centerLatitudeDegrees, gridNode.getCenterLatitudeDegrees());
    assertEquals(centerLongitudeDegrees, gridNode.getCenterLongitudeDegrees());
    assertEquals(centerDepthKm, gridNode.getCenterDepthKm());
    assertEquals(gridCellHeightKm, gridNode.getGridCellHeightKm());
    assertEquals(nodeStationsRef, gridNode.getNodeStations());
  }
}
