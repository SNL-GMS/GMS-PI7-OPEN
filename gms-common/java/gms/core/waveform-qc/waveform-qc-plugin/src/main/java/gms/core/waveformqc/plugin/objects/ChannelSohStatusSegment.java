package gms.core.waveformqc.plugin.objects;

import static gms.core.waveformqc.plugin.util.SohStatusSegmentUtility.noAdjacentEqualStatuses;
import static gms.core.waveformqc.plugin.util.SohStatusSegmentUtility.noneOverlap;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Waveform Quality Control Processing specific data object holding equivalent information to a
 * collection of {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean}
 * in a more condensed form.  Each ChannelSohStatusSegment represents status bits is for a single
 * {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel},
 * and {@link AcquiredChannelSohType}, and includes a list of changes in status represented by {@link SohStatusSegment}. There is
 * only a new status when the status value changes but there is always at least one status.
 */
@AutoValue
public abstract class ChannelSohStatusSegment {

  /**
   * Obtain an {@link UUID} to this status object's {@link Channel}
   *
   * @return UUID to a ProcessingChannel, not null
   */
  public abstract UUID getChannelId();

  public abstract AcquiredChannelSohType getType();

  /**
   * An ordered collection of all status changes ({@link Instant} time and boolean value).  Always
   * contains at least one {@link SohStatusSegment}. Always sorted by start time.
   *
   * @return Collection of of SohStatusSegment, not null
   */
  public abstract ImmutableList<SohStatusSegment> getStatusSegments();

  public static Builder builder() {
    return new AutoValue_ChannelSohStatusSegment.Builder();
  }

  public abstract Builder toBuilder();

  public ChannelSohStatusSegment sorted() {
    return toBuilder().setStatusSegments(ImmutableList
        .sortedCopyOf(Comparator.comparing(SohStatusSegment::getStartTime), getStatusSegments()))
        .build();
  }

  /**
   * Obtain a {@link ChannelSohStatusSegment} from the {@link UUID} Channel Id, {@link
   * AcquiredChannelSohType} type, and list of {@link SohStatusSegment}es.
   *
   * This is a recreation factory which assumes the provided sohStatusSegments satisfy the contract
   * provided by ChannelSohStatusSegment, namely: 1. At least one status entry 2. No adjacent status
   * entries with the same {@link SohStatusBit} 3. Statuses ordered by time and non-overlapping
   *
   * @param channelId UUID to a ProcessingChannel
   * @param type an AcquiredChannelSohType
   * @param sohStatusSegments status sohStatusSegments
   * @return ChannelSohStatusSegment
   * @throws NullPointerException if any parameters are null
   * @throws IllegalStateException if sohStatusSegments break the ChannelSohStatusSegment contract
   */
  public static ChannelSohStatusSegment from(UUID channelId,
      AcquiredChannelSohType type, List<SohStatusSegment> sohStatusSegments) {

    return builder()
        .setChannelId(channelId).setType(type).setStatusSegments(sohStatusSegments).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setChannelId(UUID channelId);

    public abstract Builder setType(AcquiredChannelSohType type);

    abstract ImmutableList<SohStatusSegment> getStatusSegments();

    abstract Builder setStatusSegments(ImmutableList<SohStatusSegment> statusSegments);

    public Builder setStatusSegments(Collection<SohStatusSegment> statusSegments) {
      return setStatusSegments(ImmutableList.copyOf(statusSegments));
    }

    abstract ImmutableList.Builder<SohStatusSegment> statusSegmentsBuilder();

    public Builder addStatusSegment(SohStatusSegment statusSegment) {
      statusSegmentsBuilder().add(statusSegment);
      return this;
    }

    public Builder addStatusSegment(Instant startTime, Instant endTime, boolean status) {
      statusSegmentsBuilder()
          .add(SohStatusSegment.from(startTime, endTime, status));
      return this;
    }

    public Builder addStatusSegment(Instant startTime, Instant endTime, SohStatusBit status) {
      statusSegmentsBuilder()
          .add(SohStatusSegment.from(startTime, endTime, status));
      return this;
    }

    abstract ChannelSohStatusSegment autoBuild();

    public ChannelSohStatusSegment build() {
      ChannelSohStatusSegment segment = autoBuild();
      Preconditions.checkState(!segment.getStatusSegments().isEmpty(),
          "Error creating ChannelSohStatusSegment: statusSegments cannot be empty");

      Preconditions.checkState(noneOverlap(segment.getStatusSegments()),
          "Error creating ChannelSohStatusSegment: statusSegments cannot overlap");

      Preconditions.checkState(noAdjacentEqualStatuses(segment.getStatusSegments()),
          "Error creating ChannelSohStatusSegment: statusSegments cannot have adjacent StatusSegments with equal StatusBits");

      return segment;
    }

  }

}
