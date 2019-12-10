package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceCalibrationDto {

  @JsonCreator
  static ReferenceCalibration from(
      @JsonProperty("id") UUID id,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("calibrationInterval") double calibrationInterval,
      @JsonProperty("calibrationFactor") double calibrationFactor,
      @JsonProperty("calibrationFactorError") double calibrationFactorError,
      @JsonProperty("calibrationPeriod") double calibrationPeriod,
      @JsonProperty("timeShift") double timeShift,
      @JsonProperty("actualTime") Instant actualChangeTime,
      @JsonProperty("systemTime") Instant systemChangeTime,
      @JsonProperty("informationSource") InformationSource informationSource,
      @JsonProperty("comment") String comment
      ) {
    return ReferenceCalibration.from(id, channelId, calibrationInterval, calibrationFactor,
        calibrationFactorError, calibrationPeriod, timeShift,
        actualChangeTime, systemChangeTime, informationSource, comment);
  }
}
