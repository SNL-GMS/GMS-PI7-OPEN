package gms.dataacquisition.stationreceiver.cd11.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

public class Gap implements Comparable<Gap> {

  private long start;
  private long end;
  private Instant modifiedTime;

  Gap(long start, long end) {
    this(start, end, Instant.now());
  }

  @JsonCreator
  Gap(
      @JsonProperty("min") long start,
      @JsonProperty("max") long end,
      @JsonProperty("modifiedTime") Instant modifiedTime) {
    this.start = start;
    this.end = end;
    this.modifiedTime = modifiedTime;
  }

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public long getEnd() {
    return end;
  }

  public void setEnd(long end) {
    this.end = end;
  }

  public Instant getModifiedTime() {
    return modifiedTime;
  }

  public void setModifiedTime(Instant modifiedTime) {
    this.modifiedTime = modifiedTime;
  }

  boolean contains(long value) {
    return ((Long.compareUnsigned(value, this.start) >= 0) && (
        Long.compareUnsigned(value, this.end) <= 0));
  }

  @Override
  public int compareTo(Gap gap) {
    // Check for overlapping gaps.
    if ((Long.compareUnsigned(this.start, gap.end) <= 0) &&
        (Long.compareUnsigned(this.end, gap.start) >= 0)) {

      // Check for equality (the only valid overlap condition).
      if ((Long.compareUnsigned(this.start, gap.start) == 0) &&
          (Long.compareUnsigned(this.end, gap.end) == 0)) {
        return 0;
      } else {
        // Invalid overlap condition.
        throw new IllegalArgumentException("Range pairs overlap.");
      }
    } else { // No overlap.
      return (Long.compareUnsigned(this.start, gap.end) > 0) ? 1 : -1;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.start, this.end);
  }

  @Override
  public String toString() {
    return "Gap{" +
        "start=" + start +
        ", end=" + end +
        ", modifiedTime=" + modifiedTime +
        '}';
  }
}
