package gms.core.waveformqc.channelsohqc.algorithm;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Instant;

/**
 * Data object specific to {@link ChannelSohStatusParser}. It outputs the results
 * of the algorithm, to the plugin where it is converted into QcMasks
 */
@AutoValue
public abstract class ChannelSohQcMask {

  /**
   * Obtain the {@link QcMaskType} of this ChannelSohQcMask
   *
   * @return QcMaskType for this ChannelSohQcMask
   */
  public abstract AcquiredChannelSohType getType();

  /**
   * Obtain the StartTime of this ChannelSohQcMask
   *
   * @return Instant StartTime for this ChannelSohQcMask
   */
  public abstract Instant getStartTime();

  /**
   * Obtain the EndTime of this ChannelSohQcMask
   *
   * @return Instant EndTime for this ChannelSohQcMask
   */
  public abstract Instant getEndTime();

  /**
   * Creates a ChannelSohQcMask insuring all the parameters are not null
   * and that the StartTime is before the EndTime
   *
   * @return ChannelSohQcMask
   */
  public static ChannelSohQcMask from(AcquiredChannelSohType type, Instant startTime,
      Instant endTime) {
    Preconditions.checkArgument(startTime.isBefore(endTime),
          "Error creating ChannelSohQcMask: startTime must be before endTime");

    return new AutoValue_ChannelSohQcMask(type, startTime, endTime);
  }
}
