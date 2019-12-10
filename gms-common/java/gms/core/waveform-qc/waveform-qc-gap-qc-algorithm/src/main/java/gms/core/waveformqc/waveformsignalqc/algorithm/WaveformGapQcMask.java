package gms.core.waveformqc.waveformsignalqc.algorithm;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Intermediate object representing a gap in waveform data.  Eventually used to create
 * {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask}
 * objects by the {@link WaveformGapUpdater}
 */
public class WaveformGapQcMask {

  private final QcMaskType qcMaskType;
  private final UUID channelId;
  private final UUID channelSegmentId;
  private final Instant startTime;
  private final Instant endTime;

  private WaveformGapQcMask(QcMaskType qcMaskType, UUID channelId, UUID channelSegmentId,
      Instant startTime, Instant endTime) {

    this.qcMaskType = qcMaskType;
    this.channelId = channelId;
    this.channelSegmentId = channelSegmentId;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  /**
   * Obtains an instance of {@link WaveformGapQcMask}
   *
   * @param qcMaskType {@link QcMaskType}, not null, must be either {@link
   * QcMaskType#REPAIRABLE_GAP} or {@link QcMaskType#LONG_GAP}
   * @param channelId UUID to a {@link Channel}
   * providing the gappy waveform, not null
   * @param channelSegmentId UUID to the {@link ChannelSegment}
   * containing the gap, not null
   * @param startTime gap start time, not null
   * @param endTime gap end time, not null
   * @return new {@link WaveformGapQcMask}, not null
   */
  public static WaveformGapQcMask create(QcMaskType qcMaskType, UUID channelId,
      UUID channelSegmentId, Instant startTime, Instant endTime) {

    Objects.requireNonNull(qcMaskType,
        "Error creating WaveformGapQcMask: qcMaskType cannot be null");
    Objects.requireNonNull(channelId,
        "Error creating WaveformGapQcMask: channelId cannot be null");
    Objects.requireNonNull(channelSegmentId,
        "Error creating WaveformGapQcMask: channelSegmentId cannot be null");
    Objects.requireNonNull(startTime,
        "Error creating WaveformGapQcMask: startTime cannot be null");
    Objects.requireNonNull(endTime,
        "Error creating WaveformGapQcMask: endTime cannot be null");

    if (!startTime.isBefore(endTime)) {
      throw new IllegalArgumentException(
          "Error creating WaveformGapQcMask: startTime must be before endTime");
    }

    if (QcMaskType.LONG_GAP != qcMaskType && QcMaskType.REPAIRABLE_GAP != qcMaskType) {
      throw new IllegalArgumentException(
          "Error creating WaveformGapQcMask: qcMaskType must be either LONG_GAP or REPAIRABLE_GAP");
    }

    return new WaveformGapQcMask(qcMaskType, channelId, channelSegmentId, startTime,
        endTime);
  }

  public QcMaskType getQcMaskType() {
    return qcMaskType;
  }

  public UUID getChannelId() {
    return channelId;
  }

  public UUID getChannelSegmentId() {
    return channelSegmentId;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }
}
