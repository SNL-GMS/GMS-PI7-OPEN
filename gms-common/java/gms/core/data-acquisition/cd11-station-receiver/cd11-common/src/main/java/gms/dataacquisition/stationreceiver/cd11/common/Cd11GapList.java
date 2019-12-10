package gms.dataacquisition.stationreceiver.cd11.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AcknackFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11DataFrame;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;


/**
 * Simplified gap-list for use by CD 1.1 components.
 */
public class Cd11GapList {
    private static Logger logger = LoggerFactory.getLogger(Cd11GapList.class);
    private GapList gapList;

    public Cd11GapList() {
        this(new GapList(0, -1));
    }

    @JsonCreator
    public Cd11GapList(
            @JsonProperty GapList gapList) {
        this.gapList = gapList;
    }

    public GapList getGapList() {
        return gapList;
    }

    /**
     * Checks if the frame range in the Acknack frame completely shifted below our current low/high
     * which would indicate a reset. Low/High of the frame become the new range and any old gaps
     * are dropped.
     *
     * @param acknackFrame CD 1.1 Acknack frame
     */
    public void checkForReset(Cd11AcknackFrame acknackFrame) {
        // Ignore invalid input.
        if (Long.compareUnsigned(acknackFrame.lowestSeqNum, acknackFrame.highestSeqNum) > 0) {
            logger.error(String.format("Acknack frame contains a lowestSeqNum (%s) that is larger than the highestSeqNum (%s)",
                    acknackFrame.lowestSeqNum, acknackFrame.highestSeqNum));
            return;
        }

        // Check for a reset.
        if (Long.compareUnsigned(acknackFrame.highestSeqNum, this.gapList.getMin()) < 0) {
            logger.info("Gap list reset");
            this.gapList = new GapList(0, -1);
        }
    }

    /**
     * Adds a sequence number to the gap list.
     *
     * @param cd11DataFrame CD 1.1 Data frame
     */
    public void addSequenceNumber(Cd11DataFrame cd11DataFrame) {
        this.addSequenceNumber(cd11DataFrame.getFrameHeader().sequenceNumber);
    }

    /**
     * Adds a sequence number to the gap list.
     *
     * @param cd11CommandResponseFrame CD 1.1 Command Response frame
     */
    public void addSequenceNumber(Cd11CommandResponseFrame cd11CommandResponseFrame) {
        this.addSequenceNumber(cd11CommandResponseFrame.getFrameHeader().sequenceNumber);
    }

    /**
     * Adds a sequence number to the gap list.
     *
     * @param value sequence number
     */
    public void addSequenceNumber(long value) {
        this.processSequenceNumber(value, true);
    }

    private void processSequenceNumber(long value, boolean isDataOrCommandFrameSequenceNumber) {
        // Do not allow non-data frames to dramatically change the gap list.
        if (!isDataOrCommandFrameSequenceNumber &&
                (Long.compareUnsigned(value, 1) < 0 ||
                        Long.compareUnsigned(value, getLowestSequenceNumber()) < 0 ||
                        Long.compareUnsigned(value, (this.gapList.getMax() + 20)) > 0)) {
            return;
        }

        // Add the new value.
        try {
            logger.info("Adding sequence number " + value + " to gap list");
            this.gapList.addValue(value);
        } catch (Exception e) {
            logger.warn(String.format("Ignoring invalid sequence number: %d", value),
                    ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Returns the highest sequence number.
     *
     * @return highest sequence number
     */
    public long getHighestSequenceNumber() {
        return this.gapList.getMax();
    }

    /**
     * Returns the lowest sequence number.
     *
     * @return lowest sequence number
     */
    public long getLowestSequenceNumber() {
        return this.gapList.getMin();
    }

    /**
     * Returns an array of gap ranges, as required to produce a CD 1.1 Acknack frame.
     * Note: this doesn't necessary truly reflect what's in the gap list, just what to report
     * back to the provider. For example, this method filters out gaps in the GapList which
     * filters out gaps touching or exceeding the max sequence number to match CD-1.1 protocol
     *
     * @return array of gap ranges
     */
    public long[] getGaps() {
        ArrayList<ImmutablePair<Long, Long>> gaps = this.gapList.getGaps(false, true);

        // Filter out gaps that go beyond the the max of the range. This scenario happens when we set our max
        // from an Acknack but didn't receive that max sequence number. Because protocol specifies the gap end
        // is the highest received frame number we can't create a valid gap
        for (int i = gaps.size() - 1; i >= 0; i--) {
            long gapStart = gaps.get(i).getLeft();
            long gapEnd = gaps.get(i).getRight();

            //First check: if gapEnd goes beyond max (touches the upper boundary)
            //Second check: makes sure initial value for gaps is empty, not from [0,-1]
            //Third Check: remove any gaps below or touch our min (ie the one from 0 - min)
            //Fourth check: remove the gap from 0, -1 (everything)
            //Either case remove that gap because it touches the upper boundary
            if ((Long.compareUnsigned(gapEnd, this.gapList.getMax()) > 0)
                    || GapList.isMaxUnsignedValue(gapEnd)
                    || Long.compareUnsigned(gapEnd, this.gapList.getMin()) <= 0
                    || (gapStart == 0 && gapEnd == -1)) {
                gaps.remove(i);
            }
        }

        // Convert to long array.
        long[] cd11Gaps = new long[gaps.size() * 2];
        int i = 0;
        for (ImmutablePair<Long, Long> gap : gaps) {
            cd11Gaps[i] = gap.getLeft();
            cd11Gaps[i + 1] = gap.getRight();
            i += 2;
        }

        return cd11Gaps;
    }

    /**
     * Removes gaps that have not changed in the specified number of days.
     *
     * @param days days
     */
    public void removeExpiredGaps(int days) {
        Validate.isTrue(days > 0);
        this.gapList.removeGapsModifiedBefore(Instant.now().minusSeconds(60 * 60 * 24 * days));
    }
}
