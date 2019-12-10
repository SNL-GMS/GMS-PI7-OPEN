package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link SignalDetection}
 */
public interface SignalDetectionDto {

  @JsonCreator
  static SignalDetection from(
      @JsonProperty("id") UUID id,
      @JsonProperty("monitoringOrganization") String monitoringOrganization,
      @JsonProperty("stationId") UUID stationId,
      @JsonProperty("signalDetectionHypotheses") List<SignalDetectionHypothesis> signalDetectionHypotheses,
      @JsonProperty("creationInfoId") UUID creationInfoId){
    return SignalDetection.from(id, monitoringOrganization, stationId, signalDetectionHypotheses, creationInfoId);
  }

}
