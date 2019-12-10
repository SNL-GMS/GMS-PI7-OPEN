package gms.core.waveformqc.waveformsignalqc.algorithm;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Intermediate object representing a spike in waveform data.  Eventually used to create
 * {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask}
 * objects by the {@link WaveformSpike3PtInterpreter}
 */
public class WaveformSpike3PtQcMask {

  private final UUID channelId;
  private final UUID channelSegmentId;
  private final Instant spikeTime;

  private WaveformSpike3PtQcMask(UUID channelId, UUID channelSegmentId,
      Instant spikeTime) {

    this.channelId = channelId;
    this.channelSegmentId = channelSegmentId;
    this.spikeTime = spikeTime;
  }

  /**
   * Obtains an instance of {@link WaveformSpike3PtQcMask}
   *
   * @param channelId UUID to a processing channel id.
   * providing the spikey waveform, not null
   * @param channelSegmentId UUID to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment}
   * containing the spike, not null
   * @param spikeTime time position of the spike, not null
   * @return new {@link WaveformSpike3PtQcMask}, not null
   */
  public static WaveformSpike3PtQcMask create(UUID channelId,
      UUID channelSegmentId, Instant spikeTime) {

    Objects.requireNonNull(channelId,
        "WaveformSpike3PtQcMask cannot have null channelId");
    Objects.requireNonNull(channelSegmentId,
        "WaveformSpike3PtQcMask cannot have null channelSegmentId");
    Objects.requireNonNull(spikeTime,
        "WaveformSpike3PtQcMask cannot have null spikeTime");

    return new WaveformSpike3PtQcMask(channelId, channelSegmentId, spikeTime);
  }

  public QcMaskType getQcMaskType() {
    return QcMaskType.SPIKE;
  }

  public UUID getChannelId() {
    return channelId;
  }

  public UUID getChannelSegmentId() {
    return channelSegmentId;
  }

  public Instant getStartTime() {
    return spikeTime;
  }

  public Instant getEndTime() {
    return spikeTime;
  }

}
