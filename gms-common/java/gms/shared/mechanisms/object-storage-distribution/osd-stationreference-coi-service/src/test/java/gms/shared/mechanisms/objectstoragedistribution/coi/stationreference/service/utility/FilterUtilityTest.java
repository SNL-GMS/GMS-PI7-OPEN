package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.testUtilities.TestFixtures;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.Test;

public class FilterUtilityTest {

  private final List<ReferenceNetwork> networks = TestFixtures.allNetworks;
  private static Instant
      changeTime1 = TestFixtures.network.getActualChangeTime(),
      changeTime2 = TestFixtures.network2.getActualChangeTime(),
      changeTime3 = TestFixtures.network_v2.getActualChangeTime(),
      changeTime4 = TestFixtures.network2_v2.getActualChangeTime();
  private static final Function<ReferenceNetwork, Instant> timeExtractor
      = ReferenceNetwork::getActualChangeTime;
  private static final Function<ReferenceNetwork, UUID> idExtractor
      = ReferenceNetwork::getEntityId;

  @Test
  public void testFilterByStartTime() {
    // case 1: filter with start time before all networks, should find all.
    List<ReferenceNetwork> filtered = FilterUtility.filterByStartTime(
        networks, changeTime1.minusSeconds(1), timeExtractor, idExtractor);
    assertNoDuplicates(filtered);
    assertEquals(networks, filtered);
    // case 2: filter with start time equal to first network change time, should find all.
    filtered = FilterUtility.filterByStartTime(
        networks, changeTime1, timeExtractor, idExtractor);
    assertNoDuplicates(filtered);
    assertEquals(networks, filtered);
    // case 3: filter by time after first network change time, should find all
    filtered = FilterUtility.filterByStartTime(networks,
        changeTime1.plusSeconds(1), timeExtractor, idExtractor);
    assertNoDuplicates(filtered);
    assertEquals(networks, filtered);
    // case 4: filter by time at 2nd network came online, should find all.
    filtered = FilterUtility.filterByStartTime(networks, changeTime2,
        timeExtractor, idExtractor);
    assertNoDuplicates(filtered);
    assertEquals(networks, filtered);
    // case 5: filter by time of network_v2, should find {network_v2, network2, network2_v2}
    // (exclude first 'network' because it was overtaken by network_v2)
    filtered = FilterUtility.filterByStartTime(networks, changeTime3,
        timeExtractor, idExtractor);
    assertNoDuplicates(filtered);
    assertEquals(List.of(TestFixtures.network2, TestFixtures.network_v2, TestFixtures.network2_v2),
        filtered);
    // case 6: filter by time of network2_v2, should find {network_v2, network2_v2}
    // (exclude 'network2' because it was overtaken by network2_v2)
    filtered = FilterUtility.filterByStartTime(networks, changeTime4,
        timeExtractor, idExtractor);
    assertNoDuplicates(filtered);
    assertEquals(List.of(TestFixtures.network_v2, TestFixtures.network2_v2), filtered);
    // case 7: filter by time after network2_v2, should find {network_v2, network2_v2}
    filtered = FilterUtility.filterByStartTime(networks, changeTime4.plusSeconds(1),
        timeExtractor, idExtractor);
    assertNoDuplicates(filtered);
    assertEquals(List.of(TestFixtures.network_v2, TestFixtures.network2_v2), filtered);
  }

  @Test
  public void testFilterByEndTime() {
  // case 1: filter with end time before all networks, should find none
    List<ReferenceNetwork> filtered = FilterUtility.filterByEndTime(
        networks, changeTime1.minusSeconds(1), timeExtractor);
    assertNotNull(filtered);
    assertTrue(filtered.isEmpty());
    // case 2: filter with end time equal to first network change time, should only find first network.
    filtered = FilterUtility.filterByEndTime(
        networks, changeTime1, timeExtractor);
    assertEquals(List.of(TestFixtures.network), filtered);
    // case 3: filter with end time after first network change time (but before network2 change time),
    // should only find first network
    filtered = FilterUtility.filterByEndTime(
        networks, changeTime1.plusSeconds(1), timeExtractor);
    assertEquals(List.of(TestFixtures.network), filtered);
    // case 4: filter with end time equal to second network change time, should find
    // {network, network2}.
    filtered = FilterUtility.filterByEndTime(
        networks, changeTime2, timeExtractor);
    assertNoDuplicates(filtered);
    assertEquals(List.of(TestFixtures.network, TestFixtures.network2), filtered);
    // case 5: filter with end time equal to network_v2 change time, should find
    // {network, network2, network_v2}
    filtered = FilterUtility.filterByEndTime(
        networks, changeTime3, timeExtractor);
    assertNoDuplicates(filtered);
    assertEquals(List.of(TestFixtures.network, TestFixtures.network2, TestFixtures.network_v2),
        filtered);
    // case 6: filter with end time equal to network2_v2 change time,
    // should find all networks
    filtered = FilterUtility.filterByEndTime(
        networks, changeTime4, timeExtractor);
    assertNoDuplicates(filtered);
    assertEquals(TestFixtures.allNetworks, filtered);
    // case 7: filter with end time after network2_v2 change time,
    // should find all networks
    filtered = FilterUtility.filterByEndTime(
        networks, changeTime4.plusSeconds(1), timeExtractor);
    assertNoDuplicates(filtered);
    assertEquals(TestFixtures.allNetworks, filtered);
  }

  @Test
  public void filterByStartTimeNullArgumentValidationTest() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(FilterUtility.class,
        "filterByStartTime", networks, changeTime1, timeExtractor, idExtractor);
  }

  @Test
  public void filterByEndTimeNullArgumentValidationTest() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(FilterUtility.class,
        "filterByEndTime", networks, changeTime1, timeExtractor);
  }

  private static <T> void assertNoDuplicates(List<T> elems) {
    assertEquals(new HashSet<>(elems).size(), elems.size());
  }

}
