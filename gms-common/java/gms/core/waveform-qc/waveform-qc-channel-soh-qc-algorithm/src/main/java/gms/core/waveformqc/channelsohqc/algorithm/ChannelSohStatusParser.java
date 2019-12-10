package gms.core.waveformqc.channelsohqc.algorithm;

import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.core.waveformqc.plugin.objects.SohStatusBit;
import gms.core.waveformqc.plugin.objects.SohStatusSegment;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processes {@link ChannelSohStatusSegment} and creates a {@link ChannelSohQcMask} for each
 * SohStatusBit.SET status value.
 */
public class ChannelSohStatusParser {

  private ChannelSohStatusParser() {
  }

  /**
   * Generates {@link ChannelSohQcMask}s based on Channel SOH status in {@link
   * ChannelSohStatusSegment}
   *
   * @param channelSohStatusSegment acquired Channel SOH status information, not null
   * @return list of {@link ChannelSohQcMask}
   */
  public static List<ChannelSohQcMask> parseStatuses(
      ChannelSohStatusSegment channelSohStatusSegment) {
    Objects.requireNonNull(channelSohStatusSegment,
        "ChannelSohStatusParser createChannelSohQcMasks cannot accept null ChannelSohStatusSegment");

    final Function<SohStatusSegment, ChannelSohQcMask> channelSohQcMaskFromStatus =
        status -> ChannelSohQcMask.from(
            channelSohStatusSegment.getType(),
            status.getStartTime(),
            status.getEndTime());

    return channelSohStatusSegment.getStatusSegments().stream()
        .filter(segment -> SohStatusBit.SET.equals(segment.getStatusBit()))
        .map(channelSohQcMaskFromStatus)
        .collect(Collectors.toList());
  }
}
