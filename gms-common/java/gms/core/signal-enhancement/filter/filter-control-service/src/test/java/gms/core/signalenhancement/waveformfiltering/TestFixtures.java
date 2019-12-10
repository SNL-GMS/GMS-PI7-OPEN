package gms.core.signalenhancement.waveformfiltering;

import static java.util.UUID.randomUUID;

import gms.core.signalenhancement.waveformfiltering.http.StreamingRequest;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;

/**
 * Objects used in filter control HTTP testing
 */
public class TestFixtures {

  public static StreamingRequest getStreamingRequest() {
    return StreamingRequest.builder()
        .addChannelSegment(randomChannelSegment())
        .putChannelIds(randomUUID(), randomUUID())
        .putPluginParam("test", "test")
        .build();
  }

  public static ChannelSegment<Waveform> randomChannelSegment() {
    return ChannelSegment.from(randomUUID(), randomUUID(), "ChannelName",
        ChannelSegment.Type.RAW, new TreeSet<>(List.of(randomWaveform())), CreationInfo.DEFAULT);
  }

  public static Waveform randomWaveform() {
    return Waveform.withValues(Instant.EPOCH, 2, randoms(20));
  }

  public static ChannelSegment<Waveform> channelSegmentFromWaveforms(List<Waveform> waveforms) {
    return ChannelSegment.from(randomUUID(), randomUUID(), "ChannelName",
        ChannelSegment.Type.RAW, new TreeSet<>(waveforms), CreationInfo.DEFAULT);
  }

  private static double[] randoms(int length) {
    double[] random = new double[length];
    IntStream.range(0, length).forEach(i -> random[i] = Math.random());
    return random;
  }

}
