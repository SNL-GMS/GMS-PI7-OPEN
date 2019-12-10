package gms.dataacquisition.stationreceiver.cd11.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.BeforeClass;
import org.junit.Test;


public class GapListTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  //set up the object mapper for serialization
  @BeforeClass
  public static void setup(){
    objectMapper.findAndRegisterModules();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Test
  public void testBasicFunctionality() {
    GapList gp = new GapList(0, 100);

    // Initially there should only be 1 gap, covering the full range.
    List<ImmutablePair<Long, Long>> pairs = gp.getGaps();
    assertEquals(1, gp.getTotalGaps(), 1);
    assertEquals(1, pairs.size());
    assertEquals(0L, (long) pairs.get(0).getLeft());
    assertEquals(100L, (long) pairs.get(0).getRight());

    // Add a value (which will split the original gap).
    gp.addValue(50);
    pairs = gp.getGaps();
    assertEquals(2, gp.getTotalGaps());
    assertEquals(2, pairs.size());
    assertEquals(0, (long) pairs.get(0).getLeft());
    assertEquals(49, (long) pairs.get(0).getRight());
    assertEquals(51, (long) pairs.get(1).getLeft());
    assertEquals(100, (long) pairs.get(1).getRight());

    // Add another value (which will split the first gap in the gap-list).
    gp.addValue(48);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 3 && pairs.size() == 3 &&
        pairs.get(0).getLeft() == 0 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 49 && pairs.get(1).getRight() == 49 &&
        pairs.get(2).getLeft() == 51 && pairs.get(2).getRight() == 100);

    // Add another value that will effectively eliminate the middle gap.
    gp.addValue(49);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 0 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 100);

    // Add a gap that was added previously (which will have no effect on the gap-list).
    gp.addValue(50);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 0 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 100);

    // Add gaps that increases the lower limit of an existing gap.
    gp.addValue(0);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 1 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 100);

    gp.addValue(1);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 2 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 100);

    // Add gaps that decrease the upper limit of an existing gap.
    gp.addValue(100);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 2 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 99);

    gp.addValue(99);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 2 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 98);

    // Check that inclusive gaps work (no change from previous state).
    pairs = gp.getGaps(false, false);
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 2 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 98);

    // Check that exclusiveEnd works.
    pairs = gp.getGaps(false, true);
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 2 && pairs.get(0).getRight() == 48 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 99);

    // Check that exclusiveStart works.
    pairs = gp.getGaps(true, false);
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 1 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 50 && pairs.get(1).getRight() == 98);

    // Check that exclusiveStart and exclusiveEnd work together.
    pairs = gp.getGaps(true, true);
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 1 && pairs.get(0).getRight() == 48 &&
        pairs.get(1).getLeft() == 50 && pairs.get(1).getRight() == 99);

    // Add a range of values that span from the beginning of an existing gap to the middle of the gap.
    gp.addValueRange(1, 5);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 2 && pairs.size() == 2 &&
        pairs.get(0).getLeft() == 6 && pairs.get(0).getRight() == 47 &&
        pairs.get(1).getLeft() == 51 && pairs.get(1).getRight() == 98);

    // Add a range of values that fall inside a single gap.
    gp.addValueRange(10, 15);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 3 && pairs.size() == 3 &&
        pairs.get(0).getLeft() == 6 && pairs.get(0).getRight() == 9 &&
        pairs.get(1).getLeft() == 16 && pairs.get(1).getRight() == 47 &&
        pairs.get(2).getLeft() == 51 && pairs.get(2).getRight() == 98);

    // Add a range of values that span from the middle of an exist gap to the end of the gap.
    gp.addValueRange(41, 47);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 3 && pairs.size() == 3 &&
        pairs.get(0).getLeft() == 6 && pairs.get(0).getRight() == 9 &&
        pairs.get(1).getLeft() == 16 && pairs.get(1).getRight() == 40 &&
        pairs.get(2).getLeft() == 51 && pairs.get(2).getRight() == 98);

    // Add a range of values that span from before the beginning of an existing gap, to the middle.
    gp.addValueRange(3, 7);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 3 && pairs.size() == 3 &&
        pairs.get(0).getLeft() == 8 && pairs.get(0).getRight() == 9 &&
        pairs.get(1).getLeft() == 16 && pairs.get(1).getRight() == 40 &&
        pairs.get(2).getLeft() == 51 && pairs.get(2).getRight() == 98);

    // Add a range of values that span from the middle of an existing gap, to past the end.
    gp.addValueRange(36, 45);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 3 && pairs.size() == 3 &&
        pairs.get(0).getLeft() == 8 && pairs.get(0).getRight() == 9 &&
        pairs.get(1).getLeft() == 16 && pairs.get(1).getRight() == 35 &&
        pairs.get(2).getLeft() == 51 && pairs.get(2).getRight() == 98);

    // Add a range of values that span across multiple gaps.
    gp.addValueRange(5, 55);
    pairs = gp.getGaps();
    assertTrue(gp.getTotalGaps() == 1 && pairs.size() == 1 &&
        pairs.get(0).getLeft() == 56 && pairs.get(0).getRight() == 98);
  }

  /*
  TODO: is this the desired behavior?
  Currently, gaplist is initialized with hard bounds for its range
  and any sequential sequence number outside that range is ignored
  and will not change min/max. Currently CD11GapList initializes its
  GapList with [0, -1] so every unsigned long is valid
   */
  @Test
  public void testModifyMinimum() {
    GapList gp = new GapList(100, 300);
    // Initial Check
    List<ImmutablePair<Long, Long>> gaps = gp.getGaps();
    assertEquals(1, gaps.size());
    assertEquals(100, gaps.get(0).getLeft().intValue());

    // Shouldn't change minimum
    gp.addValue(200);
    gaps = gp.getGaps();
    assertEquals(2, gaps.size());
    assertEquals(100, gaps.get(0).getLeft().intValue());

    // Shouldn't change minimum, because it is outside the initialization range
    gp.addValue(50);
    gaps = gp.getGaps();
    assertEquals(2, gaps.size());
    assertEquals(100, gaps.get(0).getLeft().intValue());
  }

  /*
    TODO: is this the desired behavior?
    Currently, gaplist is initialized with hard bounds for its range
    and any sequential sequence number outside that range is ignored
    and will not change min/max. Currently CD11GapList initializes its
    GapList with [0, -1] so every unsigned long is valid
     */
  @Test
  public void testModifyMaximum() {
    GapList gp = new GapList(100, 300);
    // Initial Check
    List<ImmutablePair<Long, Long>> gaps = gp.getGaps();
    assertEquals(1, gaps.size());
    assertEquals(300, gaps.get(0).getRight().intValue());

    // Shouldn't change maximum
    gp.addValue(200);
    gaps = gp.getGaps();
    assertEquals(2, gaps.size());
    assertEquals(300, gaps.get(1).getRight().intValue());

    // Shouldn't change maximum, because it is outside the initialization range
    gp.addValue(350);
    gaps = gp.getGaps();
    assertEquals(2, gaps.size());
    assertEquals(300, gaps.get(1).getRight().intValue());
  }

  @Test
  public void testRangesChangesWithLargeNumbers() {
    GapList gp = new GapList(-20, -2);

    // Test with large numbers.
    gp.addValue(-18);
    gp.addValue(-17);
    gp.addValue(-15);

    List<ImmutablePair<Long, Long>> gaps = gp.getGaps();
    assertTrue(gaps.size() == 3 &&
        gaps.get(0).getLeft() == -20 && gaps.get(0).getRight() == -19 &&
        gaps.get(1).getLeft() == -16 && gaps.get(1).getRight() == -16 &&
        gaps.get(2).getLeft() == -14 && gaps.get(2).getRight() == -2 &&

        // Same tests, using unsigned comparison.
        Long.compareUnsigned(gaps.get(0).getLeft(), -20) == 0 &&
        Long.compareUnsigned(gaps.get(0).getRight(), -19) == 0 &&
        Long.compareUnsigned(gaps.get(1).getLeft(), -16) == 0 &&
        Long.compareUnsigned(gaps.get(1).getRight(), -16) == 0 &&
        Long.compareUnsigned(gaps.get(2).getLeft(), -14) == 0 &&
        Long.compareUnsigned(gaps.get(2).getRight(), -2) == 0 &&

        // Same tests, using large numbers.
        Long.compareUnsigned(gaps.get(0).getLeft(), Long.parseUnsignedLong("18446744073709551596")) == 0 &&
        Long.compareUnsigned(gaps.get(0).getRight(), Long.parseUnsignedLong("18446744073709551597")) == 0 &&
        Long.compareUnsigned(gaps.get(1).getLeft(), Long.parseUnsignedLong("18446744073709551600")) == 0 &&
        Long.compareUnsigned(gaps.get(1).getRight(), Long.parseUnsignedLong("18446744073709551600")) == 0 &&
        Long.compareUnsigned(gaps.get(2).getLeft(), Long.parseUnsignedLong("18446744073709551602")) == 0 &&
        Long.compareUnsigned(gaps.get(2).getRight(), Long.parseUnsignedLong("18446744073709551614")) == 0);

  }


  @Test
  public void testRemoveOldGaps() throws IOException {
    //Create old gaps
    final GapList gp = objectMapper.readValue(new File("src/test/resources/oldGaps.json"), GapList.class);

    //Make sure we have 7 gaps to start, try to remove old ones before a certain date, should have 2 left
    assertEquals(7, gp.getTotalGaps());
    gp.removeGapsModifiedBefore(Instant.parse("2018-10-08T17:05:39.660256Z"));
    assertEquals(2, gp.getTotalGaps());
  }

  @Test
  public void testSerialization() throws IOException {
    GapList gp = new GapList(0,100);
    gp.addValue(25);
    gp.addValue(30);
    gp.addValue(35);
    gp.addValue(50);
    gp.addValue(60);
    gp.addValue(70);

    String serialized = objectMapper.writeValueAsString(gp);
    GapList deserialized = objectMapper.readValue(serialized, GapList.class);
    assertEquals(gp, deserialized);
  }

  @Test(expected = IllegalArgumentException.class)
  // this test passes -1 which as an unsigned long is larger than 100
  // -1 is the maximum unsigned long value
  public void testConstructorNegativeMinimum() {
    new GapList(-1, 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorInvalidRange() {
    new GapList(10, 9);
  }
}
