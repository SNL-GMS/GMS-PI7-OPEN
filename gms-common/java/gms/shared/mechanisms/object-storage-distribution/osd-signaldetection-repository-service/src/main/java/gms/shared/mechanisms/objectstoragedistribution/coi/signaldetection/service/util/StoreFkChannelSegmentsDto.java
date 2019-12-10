package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import java.util.List;
import java.util.Objects;

public class StoreFkChannelSegmentsDto {

  private List<ChannelSegment<FkSpectra>> channelSegments;

  public StoreFkChannelSegmentsDto() {
  }

  public StoreFkChannelSegmentsDto(
      List<ChannelSegment<FkSpectra>> channelSegments) {
    this.channelSegments = channelSegments;
  }

  public List<ChannelSegment<FkSpectra>> getChannelSegments() {
    return channelSegments;
  }

  public void setChannelSegments(
      List<ChannelSegment<FkSpectra>> channelSegments) {
    this.channelSegments = channelSegments;
  }

  @Override
  public String toString() {
    return "StoreFkChannelSegmentsDto{" +
        "channelSegments=" + channelSegments +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StoreFkChannelSegmentsDto that = (StoreFkChannelSegmentsDto) o;
    return channelSegments.equals(that.channelSegments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelSegments);
  }
}
