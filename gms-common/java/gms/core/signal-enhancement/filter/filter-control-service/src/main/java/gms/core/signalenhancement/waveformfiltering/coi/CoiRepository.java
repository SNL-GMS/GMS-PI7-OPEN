package gms.core.signalenhancement.waveformfiltering.coi;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CoiRepository {

  List<ReferenceChannel> getChannels(List<UUID> channelIds) throws IOException;

  /**
   * Retrieves Waveform data for the channel and time range of the input {@link
   * ChannelSegmentDescriptor}
   *
   * @param descriptor A claim check descriptor defining a {@link ChannelSegment}
   * @return A {@link ChannelSegment}<{@link Waveform}> matching the provided descriptor
   */
  ChannelSegment<Waveform> getWaveforms(ChannelSegmentDescriptor descriptor) throws IOException;

  ChannelSegmentStorageResponse storeChannelSegments(Collection<ChannelSegment<Waveform>> channelSegments) throws IOException;
}
