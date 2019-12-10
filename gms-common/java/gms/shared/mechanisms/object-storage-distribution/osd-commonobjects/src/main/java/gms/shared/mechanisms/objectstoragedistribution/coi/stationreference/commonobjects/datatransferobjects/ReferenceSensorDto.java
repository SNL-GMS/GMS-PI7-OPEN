package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceSensorDto {

  @JsonCreator
  static ReferenceSensor from(
      @JsonProperty("id") UUID id,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("instrumentManufacturer") String instrumentManufacturer,
      @JsonProperty("instrumentModel") String instrumentModel,
      @JsonProperty("serialNumber") String serialNumber,
      @JsonProperty("numberOfComponents") int numberOfComponents,
      @JsonProperty("cornerPeriod") double cornerPeriod,
      @JsonProperty("lowPassband") double lowPassband,
      @JsonProperty("highPassband") double highPassband,
      @JsonProperty("actualTime") Instant actualTime,
      @JsonProperty("systemTime") Instant systemTime,
      @JsonProperty("informationSource") InformationSource informationSource,
      @JsonProperty("comment") String comment
      ) {
    return ReferenceSensor.from(id, channelId,
        instrumentManufacturer, instrumentModel,
        serialNumber, numberOfComponents, cornerPeriod, lowPassband, highPassband,
        actualTime, systemTime, informationSource, comment);
  }

}