package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NodeStationTests {

  private static final UUID id = UUID.randomUUID();

  private static final UUID stationId = UUID.randomUUID();

  private static double distanceFromGridPointDegrees = 60.0;

  private static PhaseInfo phaseInfo1 = PhaseInfo.from(
      PhaseType.P,
      true,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1
  );

  private static PhaseInfo phaseInfo2 = PhaseInfo.from(
      PhaseType.P,
      true,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2
  );

  private static PhaseInfo phaseInfo3 = PhaseInfo.from(
      PhaseType.P,
      true,
      0.11,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1,
      0.1
  );

  private static PhaseInfo phaseInfo4 = PhaseInfo.from(
      PhaseType.P,
      true,
      0.22,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2,
      0.2
  );


  private static SortedSet<PhaseInfo> phaseInfoSet1 = new TreeSet<>(Set.of(phaseInfo1, phaseInfo2));
  private static SortedSet<PhaseInfo> phaseInfoSet2 = new TreeSet<>(Set.of(phaseInfo3, phaseInfo4));

  NodeStation nodeStation1 = NodeStation.from(
      id,
      stationId,
      distanceFromGridPointDegrees,
      phaseInfoSet1
  );

  NodeStation nodeStation2 = NodeStation.from(
      id,
      stationId,
      distanceFromGridPointDegrees,
      phaseInfoSet2
  );

  @Test
  void testSerailizetion() throws Exception {
    TestUtilities.testSerialization(nodeStation1, NodeStation.class);
  }

  @Test
  void testFrom() {
    NodeStation testNodeStation = NodeStation.from(
        id,
        stationId,
        distanceFromGridPointDegrees,
        new TreeSet<>() {{
          add(phaseInfo1);
          add(phaseInfo2);
        }}
    );
    Assertions.assertEquals(id, testNodeStation.getId());
    Assertions.assertEquals(stationId, testNodeStation.getStationId());
    Assertions.assertEquals(distanceFromGridPointDegrees,
        testNodeStation.getDistanceFromGridPointDegrees());
    Assertions.assertEquals(phaseInfoSet1, testNodeStation.getPhaseInfos());
  }

  @Test
  void testCompare() {
    Assertions.assertEquals(phaseInfo1.compareTo(phaseInfo3), nodeStation1.compareTo(nodeStation2));
  }
}
