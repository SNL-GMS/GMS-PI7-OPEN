package gms.dataacquisition.stationreceiver.cd11.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class GapList {

    private boolean firstSeqNum = true;
    private long min;
    private long max;
    private TreeSet<Gap> gapsList = new TreeSet<>();
    private Logger logger = LoggerFactory.getLogger(GapList.class);

    // The following is the largest sequence number available
    // It looks weird for -1 to be the largest number but
    // when it's converted to an unsigned 64 bit value it
    // is as follows 0x FFFF FFFF FFFF FFFF.
    static public final long LAST_SEQUENCE_NUMBER = -1;

    /**
     * Constructs the object, and sets the initial min and max range values.
     *
     * @param min initial minimum range value
     * @param max initial maximum range value
     */
    @JsonCreator
    GapList(
            @JsonProperty("min") long min,
            @JsonProperty("max") long max){
        Validate.isTrue(
                Long.compareUnsigned(min, max) <= 0,
                "Minimum value must be less than or equal to the maximum value; min: "
                        + min + ", max: " + max);

        // Set the min and max values.
        this.min = min;
        this.max = max;

        // Create the first gap.
        this.gapsList.add(new Gap(min, max));
    }

    /**
     * Fills in the gap list with the given value.
     *
     * @param value Value to fill in.
     */
    synchronized void addValue(long value) {
        // Check if there are no gaps.
        if (this.gapsList.size() == 0) {
            return;
        }

        //Because we initialize with min = 0, max = -1 and only update with frames, the ifs in the else block will
        //never catch until this happens
        if(firstSeqNum){
            this.min = value;
            this.max = value;
            firstSeqNum = false;
        }
        else {
            if (Long.compareUnsigned(value, this.min) < 0) {
                this.min = value;
            }
            if (Long.compareUnsigned(value, this.max) > 0) {
                this.max = value;
            }
        }
        // Check if the value falls within a gap.
        Optional<Gap> result = this.gapsList.stream().filter(x -> x.contains(value)).findFirst();
        // LESS EFFICIENT // Optional<Gap> result = binarySearch(this.gapsList, value);

        // SCENARIO 1: Value does not fall within an existing gap.
        if (!result.isPresent()) {
            return;
        }
        Gap gap = result.get();

        // SCENARIO 2: Check if the gap was simply eliminated.
        if ((Long.compareUnsigned(gap.getStart(), value) == 0) && (Long.compareUnsigned(gap.getEnd(), value)
                == 0)) {
            // Remove the gap from the gapsList.
            this.gapsList.remove(gap);
        }

        // SCENARIO 3: Check if the gap's lower limit needs to be incremented.
        else if (Long.compareUnsigned(gap.getStart(), value) == 0) {
            gap.setStart(gap.getStart() + 1);

            // Update the gap's "modified" time.
            gap.setModifiedTime(Instant.now());
        }

        // SCENARIO 4: Check if the gap's upper limit needs to be decremented.
        else if (Long.compareUnsigned(gap.getEnd(), value) == 0) {
            gap.setEnd(gap.getEnd() - 1);

            // Update the gap's "modified" time.
            gap.setModifiedTime(Instant.now());
        }

        // SCENARIO 5: Check if the gap needs to be split into two gaps.
        else {
            Instant now = Instant.now();

            // Store the current end value of the gap.
            long oldEnd = gap.getEnd();

            // Modify the existing gap to span the range of the lower split.
            gap.setEnd(value - 1);

            // Update the existing gap's "modified" time.
            gap.setModifiedTime(now);

            // Add a new gap to span the range of the upper split.
            this.gapsList.add(new Gap(value + 1, oldEnd, now));
        }
    }

    /**
     * Fills in the gap list with the given range of values.
     *
     * @param startValue range start value
     * @param endValue   range end value
     */
    synchronized void addValueRange(long startValue, long endValue) {
        Validate.isTrue(Long.compareUnsigned(startValue, this.min) >= 0,
                "Start value must be greater than or equal to the current minimum.");
        Validate.isTrue(Long.compareUnsigned(startValue, this.max) <= 0,
                "Start value must be less than or equal to the current maximum.");
        Validate.isTrue(Long.compareUnsigned(endValue, this.min) >= 0,
                "End value must be greater than or equal to the current minimum.");
        Validate.isTrue(Long.compareUnsigned(endValue, this.max) <= 0,
                "End value must be less than or equal to the current maximum.");

        // Check if there are no gaps.
        if (this.gapsList.size() == 0) {
            return;
        }

        // Modify or remove existing gaps that fall within the specified range.
        Instant now = Instant.now();
        List<Gap> removeList = new ArrayList<>();
        List<Gap> adjustList = new ArrayList<>();
        for (Gap gap : this.gapsList) {
            if (Long.compareUnsigned(gap.getEnd(), startValue) < 0) {
                // Leave these gaps along, since they occur before the specified range.
                continue;
            } else if (Long.compareUnsigned(gap.getStart(), endValue) > 0) {
                // Skip all gaps past this point, since they will all occur after the specified range.
                break;
            } else if (Long.compareUnsigned(gap.getStart(), startValue) >= 0 &&
                    Long.compareUnsigned(gap.getEnd(), endValue) <= 0) {
                // Remove these gaps entirely, since their full range fits inside the specified range.
                removeList.add(gap);
            } else if (Long.compareUnsigned(gap.getStart(), startValue) < 0 &&
                    Long.compareUnsigned(gap.getEnd(), endValue) > 0) {
                // This gap spans both before the start and after end of the specified range. It must be split.
                removeList.add(gap);
                adjustList.add(new Gap(gap.getStart(), startValue - 1, now));
                adjustList.add(new Gap(endValue + 1, gap.getEnd(), now));
            } else if (Long.compareUnsigned(gap.getStart(), startValue) < 0 &&
                    Long.compareUnsigned(gap.getEnd(), endValue) <= 0) {
                // This gap spans before the start of the specified range, and must be adjusted.
                removeList.add(gap);
                adjustList.add(new Gap(gap.getStart(), startValue - 1, now));
            } else if (Long.compareUnsigned(gap.getStart(), startValue) >= 0 &&
                    Long.compareUnsigned(gap.getEnd(), endValue) > 0) {
                // This gap spans after the end of the specified range, and must be adjusted.
                removeList.add(gap);
                adjustList.add(new Gap(endValue + 1, gap.getEnd(), now));
            }
        }
        removeList.forEach(this.gapsList::remove);
        this.gapsList.addAll(adjustList);
    }

    /**
     * Returns the minimum range value.
     *
     * @return minimum range value
     */
    public synchronized long getMin() {
        return this.min;
    }

    /**
     * Returns the maximum range value.
     *
     * @return maximum range value
     */
    public synchronized long getMax() {
        return this.max;
    }

    public synchronized TreeSet<Gap> getGapsList() {
        return this.gapsList;
    }

    /**
     * Returns the total number of gaps in the gap list.
     *
     * @return total number of gaps
     */
    synchronized int getTotalGaps() {
        return this.gapsList.size();
    }

    /**
     * Removes gaps that were last modified before the specified expiration.
     *
     * @param expiration expiration time
     */
    synchronized void removeGapsModifiedBefore(Instant expiration) {
        this.gapsList.removeIf(x ->
            x.getModifiedTime().isBefore(expiration));
    }

    /**
     * List of gaps. NOTE: start and end positions are inclusive of the gap.
     *
     * @return List of gaps.
     */
    ArrayList<ImmutablePair<Long, Long>> getGaps() {
        return getGaps(false, false);
    }

    /**
     * List of gaps.
     *
     * @param exclusiveStart Gap start position will be exclusive to the gap.
     * @param exclusiveEnd   Gap end position will be exclusive to the gap.
     * @return List of gaps.
     */
    synchronized ArrayList<ImmutablePair<Long, Long>> getGaps(
            boolean exclusiveStart, boolean exclusiveEnd) {
        ArrayList<ImmutablePair<Long, Long>> gapRanges = new ArrayList<>();
        for (Gap gap : this.gapsList) {
            long lower = (exclusiveStart) ? gap.getStart() - 1 : gap.getStart();
            long upper;
            //If upper is Max unsigned, leave it
            if (isMaxUnsignedValue(gap.getEnd())) {
                upper = gap.getEnd();
            }
            //Otherwise do the exclusive end check
            else {
                upper = (exclusiveEnd) ? gap.getEnd() + 1 : gap.getEnd();
            }
            gapRanges.add(new ImmutablePair<>(lower, upper));
        }
        return gapRanges;
    }

    private static Optional<Gap> binarySearch(SortedSet<Gap> gaps, long value) {
        if (gaps.isEmpty()) {
            return Optional.empty();
        }

        // NOTE: This operation may be very inefficient (memory usage and copy time).
        List<Gap> gapsList = new ArrayList<>(gaps);

        int low = 0, high = gaps.size() - 1;
        while (low <= high) {
            // get the middle element
            int index = (low + high) / 2;
            Gap middleGap = gapsList.get(index);

            // check if the middle element matches the search. if so, return it.
            if (middleGap.contains(value)) {
                // Value was found.
                return Optional.of(middleGap);
            }

            // if gap is higher than value, search lower half
            if (Long.compareUnsigned(middleGap.getStart(), value) > 0) {
                high = index - 1;
            } else { // gap is lower than value, search upper half.
                low = index + 1;
            }
        }

        // Value was not found.
        return Optional.empty();
    }

    /*
      Check if max is -1 (unsigned max)
     */
    static boolean isMaxUnsignedValue(long val) {
        return Long.compareUnsigned(-1, val) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GapList gapList = (GapList) o;
        return min == gapList.min &&
                max == gapList.max &&
                Objects.equals(gapsList, gapList.gapsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, gapsList);
    }

    @Override
    public String toString() {
        return "GapList{" +
                "min=" + min +
                ", max=" + max +
                ", gapsList=" + gapsList +
                '}';
    }
}
