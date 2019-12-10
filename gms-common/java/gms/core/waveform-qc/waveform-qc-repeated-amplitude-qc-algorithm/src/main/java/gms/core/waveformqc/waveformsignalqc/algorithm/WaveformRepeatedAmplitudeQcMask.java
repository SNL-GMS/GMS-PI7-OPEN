package gms.core.waveformqc.waveformsignalqc.algorithm;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Intermediate object representing repeated amplitudes in waveform data.  Eventually used to create
 * {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask}
 * objects.
 */
public class WaveformRepeatedAmplitudeQcMask {

  private final Instant startTime;
  private final Instant endTime;
  private final UUID channelId;
  private final UUID channelSegmentId;

  /**
   * Obtains a {@link WaveformRepeatedAmplitudeQcMask}
   *
   * @param startTime mask startTime, not null
   * @param endTime mask endTime, not null
   * @param channelId id to the {@link Channel} containing the repeated amplitude value
   * @param channelSegmentId id to the {@link ChannelSegment} containing the repeated amplitude
   * value
   * @return a WaveformRepeatedAmplitudeQcMask, not null
   * @throws NullPointerException if startTime, endTime, channelId, or channelSegmentId are null
   * @throws IllegalArgumentException if startTime is not before endTime
   */
  public static WaveformRepeatedAmplitudeQcMask create(Instant startTime, Instant endTime,
      UUID channelId, UUID channelSegmentId) {

    Objects.requireNonNull(startTime, "WaveformRepeatedAmplitudeQcMask cannot have null startTime");
    Objects.requireNonNull(endTime, "WaveformRepeatedAmplitudeQcMask cannot have null endTime");
    Objects.requireNonNull(channelId, "WaveformRepeatedAmplitudeQcMask cannot have null channelId");
    Objects.requireNonNull(channelSegmentId,
        "WaveformRepeatedAmplitudeQcMask cannot have null channelSegmentId");

    if (!startTime.isBefore(endTime)) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcMask startTime must be before endTime");
    }

    return new WaveformRepeatedAmplitudeQcMask(startTime, endTime, channelId, channelSegmentId);
  }

  private WaveformRepeatedAmplitudeQcMask(Instant startTime, Instant endTime, UUID channelId,
      UUID channelSegmentId) {

    this.startTime = startTime;
    this.endTime = endTime;
    this.channelId = channelId;
    this.channelSegmentId = channelSegmentId;
  }

  /**
   * Obtains the startTime of this {@link WaveformRepeatedAmplitudeQcMask}
   *
   * @return {@link Instant}, not null
   */
  public Instant getStartTime() {
    return startTime;
  }

  /**
   * Obtains the endTime of this {@link WaveformRepeatedAmplitudeQcMask}
   *
   * @return {@link Instant}, not null
   */
  public Instant getEndTime() {
    return endTime;
  }

  /**
   * Obtains the id of the {@link ChannelSegment} masked by this {@link
   * WaveformRepeatedAmplitudeQcMask}
   *
   * @return {@link UUID}, not null
   */
  public UUID getChannelSegmentId() {
    return channelSegmentId;
  }

  /**
   * Obtains the id of the {@link Channel} masked by this {@link WaveformRepeatedAmplitudeQcMask}
   *
   * @return {@link UUID}, not null
   */
  public UUID getChannelId() {
    return channelId;
  }
}
