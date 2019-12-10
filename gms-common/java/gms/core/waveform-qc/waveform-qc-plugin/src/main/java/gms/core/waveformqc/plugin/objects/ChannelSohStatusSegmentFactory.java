package gms.core.waveformqc.plugin.objects;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Preconditions;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ChannelSohStatusSegmentFactory {

  public static ChannelSohStatusSegment create(List<AcquiredChannelSohBoolean> sohBooleans,
      Duration adjacentThreshold) {
    Preconditions.checkNotNull(sohBooleans);
    Preconditions.checkNotNull(adjacentThreshold);
    Preconditions.checkArgument(!sohBooleans.isEmpty(),
        "Error creating ChannelSohStatusSegment: sohBooleans cannot be empty");

    Preconditions.checkArgument(
        sohBooleans.stream().map(AcquiredChannelSoh::getChannelId).distinct().count() == 1,
        "Error creating ChannelSohStatusSegment: sohBooleans must have the same channel id");

    Preconditions.checkArgument(
        sohBooleans.stream().map(AcquiredChannelSoh::getType).distinct().count() == 1,
        "Error creating ChannelSohStatusSegment: sohBooleans must have the same type");

    Builder segmentBuilder = ChannelSohStatusSegment.builder()
        .setChannelId(
            sohBooleans.stream().map(AcquiredChannelSoh::getChannelId).findAny().orElseThrow(
                NoSuchElementException::new))
        .setType(sohBooleans.stream().map(AcquiredChannelSoh::getType).findAny().orElseThrow(
            NoSuchElementException::new));

    List<AcquiredChannelSohBoolean> sortedSohBooleans = sohBooleans.stream()
        .sorted(Comparator.comparing(AcquiredChannelSoh::getStartTime)).collect(toList());

    Iterator<AcquiredChannelSohBoolean> sohBooleanIterator = sortedSohBooleans.iterator();

    //initialize
    AcquiredChannelSohBoolean nextSohBoolean = sohBooleanIterator.next();

    Instant currentStart = nextSohBoolean.getStartTime();
    Instant currentEnd = nextSohBoolean.getEndTime();
    boolean currentStatus = nextSohBoolean.getStatus();
    // Add additional SohStatusSegment whenever the value changes or if a status is missing
    while (sohBooleanIterator.hasNext()) {
      nextSohBoolean = sohBooleanIterator.next();

      // Missing an expected acquired status entry.  First add an entry to the filtered list
      // ending the previous entry, add a missing status entry, and then start the next status.
      if (nextSohBoolean.getStartTime()
          .isAfter(currentEnd.plus(adjacentThreshold))) {
        segmentBuilder.addStatusSegment(currentStart, currentEnd, currentStatus);
        segmentBuilder
            .addStatusSegment(currentEnd, nextSohBoolean.getStartTime(), SohStatusBit.MISSING);
        currentStart = nextSohBoolean.getStartTime();
        currentEnd = nextSohBoolean.getEndTime();
        currentStatus = nextSohBoolean.getStatus();
      }
      // Status has changed.  Add an entry ending the previous status and
      // then start the next status.
      else if (nextSohBoolean.getStatus() != currentStatus) {
        segmentBuilder.addStatusSegment(currentStart, currentEnd, currentStatus);
        currentStart = nextSohBoolean.getStartTime();
        currentEnd = nextSohBoolean.getEndTime();
        currentStatus = nextSohBoolean.getStatus();
      }
      // No change to status state, extend the current status
      else {
        currentEnd = nextSohBoolean.getEndTime();
      }
    }

    // Add the final status entry
    segmentBuilder.addStatusSegment(currentStart, currentEnd, currentStatus);

    return segmentBuilder.build();
  }

}
