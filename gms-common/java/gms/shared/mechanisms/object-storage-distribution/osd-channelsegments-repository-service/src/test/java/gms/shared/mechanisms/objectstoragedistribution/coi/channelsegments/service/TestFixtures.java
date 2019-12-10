package gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.DoubleStream;

public class TestFixtures {

  static {
    TestFixtures.channelSegments = List.of(TestFixtures.randomWaveform(
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        Instant.EPOCH.plusSeconds(1),
        Instant.EPOCH.plusSeconds(2)
        )
    );
  }

  public static List<ChannelSegment<? extends Timeseries>> channelSegments;

  private static int sampleCount(Instant start, Instant end) {
    return (int) Math
        .ceil(40 * (end.toEpochMilli() - start.toEpochMilli()) / 1E3);
  }

  private static DoubleStream randomValues(int count) {
    return new Random().doubles(count);
  }

  private static ChannelSegment<Waveform> randomWaveform(UUID channelId, Instant start,
      Instant end) {

    int sampleCount = sampleCount(start, end);
    return ChannelSegment.create(channelId, "Test Waveform", Type.RAW,
        List.of(Waveform.withValues(start, 40.0, randomValues(sampleCount).toArray())),
        CreationInfo.DEFAULT);
  }
}
