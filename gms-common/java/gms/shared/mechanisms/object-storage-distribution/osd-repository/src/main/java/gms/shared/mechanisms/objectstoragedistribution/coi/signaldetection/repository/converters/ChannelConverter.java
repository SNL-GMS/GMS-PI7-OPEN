package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import java.util.Objects;

/**
 * Converts ReferenceChannel to Channel.
 */
public class ChannelConverter {

  /**
   * Converts a ReferenceChannel to a Channel.
   * @param refChan the reference channel
   * @return a Channel
   */
  public static Channel from(ReferenceChannel refChan) {
    Objects.requireNonNull(refChan);

    return Channel.from(refChan.getVersionId(), refChan.getName(), refChan.getType(),
        refChan.getDataType(), refChan.getLatitude(), refChan.getLongitude(),
        refChan.getElevation(), refChan.getDepth(), refChan.getVerticalAngle(),
        refChan.getHorizontalAngle(), refChan.getNominalSampleRate());
  }
}
