package gms.dataacquisition.stationreceiver.cd11.common;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AcknackFrame;

import org.junit.Test;


public class Cd11GapListTest {

  // Process an Acknack frame (set the range).
  // 20 chars, left justified
  private final String FRAMESET_ACKED = "140PG:0             ";


  /*
  According to CD-1.1 Protocol default/no frames received Acknacks
  are low=0, high=-1, empty gaps
   */
  @Test
  public void testInitialization(){
    Cd11GapList cd11GapList = new Cd11GapList();
    assertEquals(0, cd11GapList.getLowestSequenceNumber());
    assertEquals(-1, cd11GapList.getHighestSequenceNumber());
    assertArrayEquals(new long[]{}, cd11GapList.getGaps());
  }

  @Test
  public void testMinMaxAndGapManagement(){
    //Empty Gap list, shouldn't have any gaps
    Cd11GapList cd11GapList = new Cd11GapList();
    long[] gaps = cd11GapList.getGaps();
    assertEquals(0, cd11GapList.getGaps().length);

    //Adding only one frame, should be our min/max no gaps
    cd11GapList.addSequenceNumber(2000);
    assertEquals(2000, cd11GapList.getLowestSequenceNumber());
    assertEquals(2000, cd11GapList.getHighestSequenceNumber());
    assertEquals(0, cd11GapList.getGaps().length);

    //Now add another frame to create a gap
    cd11GapList.addSequenceNumber(4000);
    assertEquals(2000, cd11GapList.getLowestSequenceNumber());
    assertEquals(4000, cd11GapList.getHighestSequenceNumber());
    assertArrayEquals(new long[]{2001, 4000}, cd11GapList.getGaps());

    //Check new min
    cd11GapList.addSequenceNumber(1900);
    assertEquals(1900, cd11GapList.getLowestSequenceNumber());
    assertEquals(4000, cd11GapList.getHighestSequenceNumber());
    assertArrayEquals(new long[]{1901, 2000, 2001, 4000}, cd11GapList.getGaps());

    //Check new max
    cd11GapList.addSequenceNumber(4500);
    assertEquals(1900, cd11GapList.getLowestSequenceNumber());
    assertEquals(4500, cd11GapList.getHighestSequenceNumber());
    assertArrayEquals(new long[]{1901, 2000, 2001, 4000, 4001, 4500}, cd11GapList.getGaps());
  }

  /*
    The only thing we use Acknack frames for is checking for a reset.
    Tests that an Acknack that triggered a reset, but has an invalid sequence range is ignored.
    For example, if high is greater than low.
   */
  @Test
  public void testInvalidFrameWindow(){
    long[] seqNums = new long[]{2000, 4000};
    //Initial Values
    Cd11GapList cd11GapList = createGapListWithSeqNums(seqNums);
    assertEquals(2000, cd11GapList.getLowestSequenceNumber());
    assertEquals(4000, cd11GapList.getHighestSequenceNumber());
    long[] gaps = cd11GapList.getGaps();
    assertArrayEquals(new long[]{2001, 4000}, cd11GapList.getGaps());

    //Now checkForReset with an invalid range, see that low/high were not altered
    Cd11AcknackFrame badAck = new Cd11AcknackFrame(FRAMESET_ACKED, 3000, 2500, new long[]{});
    cd11GapList.checkForReset(badAck);
    assertEquals(2000, cd11GapList.getLowestSequenceNumber());
    assertEquals(4000, cd11GapList.getHighestSequenceNumber());
    assertArrayEquals(new long[]{2001, 4000}, cd11GapList.getGaps());
  }

  /*
    Test we ignore GapList in Acknacks from provider even if it
    has a valid frame window
  */
  @Test
  public void testIgnoreIncomingAcknackGaps() {
    Cd11GapList cd11GapList = createGapListFromRange(20,25);

    long[] cd11gaps = cd11GapList.getGaps();
    cd11gaps = cd11GapList.getGaps();
    //All our frames are sequential, no gaps
    assertEquals(0, cd11GapList.getGaps().length);

    // Receive a frame, local gaps from 26-30
    // Prove gap creation is being triggered
    cd11GapList.addSequenceNumber(30);
    long[] gaps = cd11GapList.getGaps();
    assertEquals(2, cd11GapList.getGaps().length);
    long[] localGaps = new long[]{26,30};
    assertArrayEquals(localGaps, cd11GapList.getGaps());

    //Mimic received Acknack with gaps from provider. Provider says it doesn't have 26-28
    //but we should still have those in our gap list regardless to keep requesting for it
    Cd11AcknackFrame ack2 = new Cd11AcknackFrame(
            FRAMESET_ACKED,
            20, 30,
            new long[]{26,28});
    cd11GapList.checkForReset(ack2);
    assertEquals(2, cd11GapList.getGaps().length);
    assertArrayEquals(localGaps, cd11GapList.getGaps());
  }

  /*
    When we receive a Acknack frame with the highest sequence number lower than the
    current low we assume the sequence numbers got reset so drop the gaplist and
    start again
   */
  @Test
  public void testResetGapList() {
    Cd11GapList cd11GapList = createGapListWithSeqNums(new long[]{20,21,25});
    assertEquals(20, cd11GapList.getLowestSequenceNumber());
    assertEquals(25, cd11GapList.getHighestSequenceNumber());
    //See that we have gaps
    assertArrayEquals(new long[]{22,25}, cd11GapList.getGaps());

    //Now reset the gap range
    Cd11AcknackFrame newRange = new Cd11AcknackFrame(FRAMESET_ACKED, 10,15, new long[]{11, 13});
    cd11GapList.checkForReset(newRange);
    //No more gaps because of the new range, also ignore the acknack gaps
    assertEquals(0, cd11GapList.getGaps().length);
    cd11GapList.addSequenceNumber(15);
    assertEquals(15, cd11GapList.getLowestSequenceNumber());
    assertEquals(15, cd11GapList.getHighestSequenceNumber());
    assertEquals(0, cd11GapList.getGaps().length);

    //Add another seq num, now there should be a gap, only reflect what we have
    cd11GapList.addSequenceNumber(10);
    assertArrayEquals(new long[]{11,15}, cd11GapList.getGaps());
  }


  /*
    Test that receiving a -1 as max highest sequence number is valid and handled
   */
  @Test
  public void testNegativeMaxSequenceNumber(){
    long[] emptyGaps = new long[]{};
    //Because the value is unsigned, -1 is  a valid value that can be sent for shorthand for max long value
    Cd11GapList cd11GapList = createGapListWithSeqNums(new long[]{-1});
    assertEquals(-1, cd11GapList.getHighestSequenceNumber());
  }

  /*
    See if we handle large sequence numbers correctly (outside max int)
   */
  @Test
  public void testBigSequenceNumbers() {
    //Setup.
    final long
            bigSeq_1 = Long.parseUnsignedLong("18446744073709550400"),
            bigSeq_2 = Long.parseUnsignedLong("18446744073709550500");
    long[] seqNums = new long[]{500,505,510,515,520,
            bigSeq_1,bigSeq_2};
    // Setup.
    Cd11GapList cd11GapList = createGapListWithSeqNums(seqNums);

    // Check the gaps.
    long[] gaps = cd11GapList.getGaps();
    assertEquals(12, gaps.length);
    assertEquals(501, gaps[0]);
    assertEquals(505, gaps[1]);
    assertEquals(506, gaps[2]);
    assertEquals(510, gaps[3]);
    assertEquals(511, gaps[4]);
    assertEquals(515, gaps[5]);
    assertEquals(516, gaps[6]);
    assertEquals(520, gaps[7]);
    assertEquals(521, gaps[8]);
    assertEquals(bigSeq_1, gaps[9]);
    assertEquals(bigSeq_1 + 1, gaps[10]);
    assertEquals(bigSeq_2, gaps[11]);
    assertEquals(500, cd11GapList.getLowestSequenceNumber());
    assertEquals(bigSeq_2, cd11GapList.getHighestSequenceNumber());
  }

  /*
    Convenience methods
    Normal operation adds one sequence number at a time, but for testing purposes
    these are faster to set up gaps.
   */
  private Cd11GapList createGapListWithSeqNums(long[] sequenceNumbersToAdd){
    Cd11GapList cd11GapList = new Cd11GapList();
    for(long seqNum : sequenceNumbersToAdd){
      cd11GapList.addSequenceNumber(seqNum);
    }
    return cd11GapList;
  }

  private Cd11GapList createGapListFromRange(long lower, long higher){
    Cd11GapList cd11GapList = new Cd11GapList();
    for(long i = lower; i<= higher; i++){
      cd11GapList.addSequenceNumber(i);
    }
    return cd11GapList;
  }
}
