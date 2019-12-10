package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import java.util.UUID;

/**
 * Create class to allow transformation to and from JSON.
 */
public interface CalibrationDto {

  @JsonCreator
  static Calibration from(
      @JsonProperty("id") UUID id,
      @JsonProperty("factor") double factor,
      @JsonProperty("period") double period,
      @JsonProperty("factorError") double factorError,
      @JsonProperty("timeShift") double timeShift) {
    return Calibration.from(id, factor, period, factorError, timeShift);
  }
}
