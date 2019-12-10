package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceDigitizerDto {
  @JsonCreator
  static ReferenceDigitizer from(
      @JsonProperty("entityId") UUID entityId,
      @JsonProperty("versionId") UUID versionId,
      @JsonProperty("name") String name,
      @JsonProperty("manufacturer") String manufacturer,
      @JsonProperty("model") String model,
      @JsonProperty("serialNumber") String serialNumber,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime,
      @JsonProperty("informationSource") InformationSource informationSource,
      @JsonProperty("comment") String comment,
      @JsonProperty("description") String description) {
    return ReferenceDigitizer.from(entityId, versionId, name,
        manufacturer, model, serialNumber,
        actualChangeTime, systemChangeTime,
        informationSource, comment, description);
  }
}

