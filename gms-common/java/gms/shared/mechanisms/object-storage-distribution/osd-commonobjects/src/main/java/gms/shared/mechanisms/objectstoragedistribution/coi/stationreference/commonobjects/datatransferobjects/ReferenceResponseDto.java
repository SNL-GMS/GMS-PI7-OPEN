package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceResponseDto {

  @JsonCreator
  static ReferenceResponse from(
      @JsonProperty("id") UUID id,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("responseType") String responseType,
      @JsonProperty("responseData") byte[] responseData,
      @JsonProperty("units") String units,
      @JsonProperty("actualTime") Instant actualTime,
      @JsonProperty("systemTime") Instant systemTime,
      @JsonProperty("informationSource") InformationSource informationSource,
      @JsonProperty("comment") String comment
      ) {
    return ReferenceResponse.from(id, channelId, responseType, responseData, units,
        actualTime, systemTime, informationSource, comment);
  }
}