package gms.dataacquisition.css.waveformloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.css.converters.data.WfdiscSampleReference;
import gms.dataacquisition.cssreader.waveformreaders.FlatFileWaveformReader;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

public class ChannelSegmentReader {

  private static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private static final FlatFileWaveformReader waveformReader = new FlatFileWaveformReader();

  public static List<ChannelSegment<Waveform>> readFromFile(File f)
      throws IOException {

    final List<ChannelSegment<Waveform>> segments = objMapper.readValue(f,
        new TypeReference<List<ChannelSegment<Waveform>>>() {});
    Objects.requireNonNull(segments, "Read null segments from file: " + f.getAbsolutePath());
    segments.forEach(seg -> Validate.isTrue(seg.getTimeseries().size() == 1,
        "Timeseries size in ChannelSegment !=1, size: " +
            seg.getTimeseries().size()));
    return segments;
  }

  public static List<ChannelSegment<Waveform>> readFromFile(File f,
      String waveformsDir, Map<UUID, WfdiscSampleReference> segmentIdToWfdiscReference) throws Exception {

    final List<ChannelSegment<Waveform>> segments = readFromFile(f);
    final List<ChannelSegment<Waveform>> populatedSegs = new ArrayList<>();
    
    for (ChannelSegment<Waveform> seg : segments){
      final WfdiscSampleReference sampRef = segmentIdToWfdiscReference.get(seg.getId());
      Objects.requireNonNull(sampRef, "Could not find waveform reference for channel segments with id "
          + seg.getId());
      final double[] samples = waveformReader.readWaveform(
          waveformsDir + File.separator + sampRef.getWaveformFile(),
          sampRef.getfOff(), sampRef.getSampleCount(), sampRef.getDataType());
      populatedSegs.add(withSamples(seg, samples));
    }
    return populatedSegs;
  }

  private static ChannelSegment<Waveform> withSamples(ChannelSegment<Waveform> segment, double[] values) {
    final Waveform wf = segment.getTimeseries().get(0);
    return ChannelSegment.from(segment.getId(), segment.getChannelId(),
        segment.getName(),
        segment.getType(),
        List.of(Waveform.withValues(wf.getStartTime(), wf.getSampleRate(), values)),
        segment.getCreationInfo());
  }
}
