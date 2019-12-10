package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import java.util.Objects;

/**
 * Converts ReferenceCalibration to Calibration.
 */
public class CalibrationConverter {

  /**
   * Converts a ReferenceCalibration to a Calibration.
   * @param c the cal
   * @return a Calibration
   */
  public static Calibration from(ReferenceCalibration c) {
    Objects.requireNonNull(c);

    return Calibration.from(c.getId(), c.getCalibrationFactor(),
        c.getCalibrationPeriod(), c.getCalibrationFactorError(),
        c.getTimeShift());
  }

}
