package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import java.util.Collection;
import java.util.UUID;

/**
 * A Data Transfer Object (DTO) for the ChannelSegment class. Annotates some properties so that
 * Jackson knows how to deserialize them - part of the reason this is necessary is that the original
 * ChannelSegment class is immutable. This class is a Jackson 'Mix-in annotations' class.
 */
public interface ChannelSegmentDto {

  @JsonCreator
  static <T extends Timeseries> ChannelSegment<T> from(
      @JsonProperty("id") UUID id,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("name") String name,
      @JsonProperty("type") ChannelSegment.Type type,
      @JsonProperty("timeseriesType") Timeseries.Type timeseriesType,
      @JsonProperty("timeseries") Collection<T> timeseries,
      @JsonProperty("creationInfo") CreationInfo creationInfo) {

    return ChannelSegment.from(id, channelId, name, type, timeseriesType,
        timeseries, creationInfo);
  }
}