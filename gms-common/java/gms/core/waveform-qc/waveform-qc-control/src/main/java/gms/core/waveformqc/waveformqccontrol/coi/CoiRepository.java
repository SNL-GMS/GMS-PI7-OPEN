package gms.core.waveformqc.waveformqccontrol.coi;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Preconditions;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegmentFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

public interface CoiRepository {

  /**
   * Retrieves Waveform data for the channel and time range of the input {@link
   * ChannelSegmentDescriptor}
   *
   * @param descriptor A claim check descriptor defining a {@link ChannelSegment}
   * @return A {@link ChannelSegment}<{@link Waveform}> matching the provided descriptor
   */
  ChannelSegment<Waveform> getWaveforms(ChannelSegmentDescriptor descriptor) throws IOException;

  /**
   * Retrieves {@link AcquiredChannelSohBoolean} data for the channel and time range of the input
   * {@link ChannelSegmentDescriptor}
   *
   * @param descriptor A claim check descriptor defining a {@link ChannelSegment}
   * @return {@link AcquiredChannelSohBoolean}s on the descriptor's channel and time range
   */
  List<AcquiredChannelSohBoolean> getChannelSoh(ChannelSegmentDescriptor descriptor)
      throws IOException;

  /**
   * Retrieves {@link ChannelSohStatusSegment} data for the channel and time range of the input {@link
   * ChannelSegmentDescriptor}
   */
  default List<ChannelSohStatusSegment> getChannelSohStatuses(ChannelSegmentDescriptor descriptor,
      Duration threshold) throws IOException {
    Preconditions.checkNotNull(descriptor);
    Preconditions.checkNotNull(threshold);
    return convertChannelSoh(getChannelSoh(descriptor), threshold);
  }

  /**
   * Converts a collection of {@link AcquiredChannelSohBoolean}s into a collection of {@link
   * ChannelSohStatusSegment}s. There will be one segment per {@link AcquiredChannelSoh.AcquiredChannelSohType}.
   * The channelSohs are guaranteed to be from the same channel due to the way this method is used.
   * @param channelSoh The channelSoh to convert into ChannelSohStatusSegments
   * @param threshold The merge threshold used to determine if multiple AcquiredChannelSohBoolean can
   * be merged into a single ChannelSohStatusSegment
   * @return A collection of the converted ChannelSohStatusSegments
   */
  static List<ChannelSohStatusSegment> convertChannelSoh(
      List<AcquiredChannelSohBoolean> channelSoh, Duration threshold) {

    return channelSoh
        .stream()
        .collect(groupingBy(AcquiredChannelSoh::getType))
        .values()
        .stream()
        .map(sohs -> ChannelSohStatusSegmentFactory.create(sohs, threshold))
        .collect(toList());
  }

  /**
   * Retrieves {@link QcMask} data for the channel and time range of the input {@link
   * ChannelSegmentDescriptor}
   *
   * @param descriptor A claim check descriptor defining a {@link ChannelSegment}
   * @return {@link QcMask}s on the descriptor's channel and time range
   */
  List<QcMask> getQcMasks(ChannelSegmentDescriptor descriptor) throws IOException;

  /**
   * Persists {@link QcMask}s into the implementation's storage backend
   *
   * @param qcMasks List of QcMasks to store
   * @throws IOException There was an I/O error in the storage attempt
   */
  void storeQcMasks(List<QcMask> qcMasks) throws IOException;
}
