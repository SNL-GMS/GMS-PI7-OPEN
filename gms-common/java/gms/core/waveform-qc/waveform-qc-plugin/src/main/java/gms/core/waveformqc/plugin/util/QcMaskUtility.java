package gms.core.waveformqc.plugin.util;

import static java.util.Objects.requireNonNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;

public class QcMaskUtility {

  /**
   * Obtain the {@link QcMaskType} corresponding to a {@link AcquiredChannelSohType}
   *
   * @param sohType get the QcMaskType for this AcquiredChannelSohType
   * @return an QcMaskType, not null
   * @throws IllegalArgumentException if the AcquiredChannelSohType does not correspond to any
   * QcMaskType
   */
  public static QcMaskType getQcMaskType(AcquiredChannelSohType sohType) {

    requireNonNull(sohType,
        "ChannelSohStatusSegment.correspondingQcMaskType cannot convert a null AcquiredChannelSohType");

    switch (sohType) {
      case DEAD_SENSOR_CHANNEL:
      case ZEROED_DATA:
      case CLIPPED:
        return QcMaskType.SENSOR_PROBLEM;

      case MAIN_POWER_FAILURE:
      case BACKUP_POWER_UNSTABLE:
        return QcMaskType.STATION_PROBLEM;

      case CALIBRATION_UNDERWAY:
      case DIGITIZER_ANALOG_INPUT_SHORTED:
      case DIGITIZER_CALIBRATION_LOOP_BACK:
        return QcMaskType.CALIBRATION;

      case EQUIPMENT_HOUSING_OPEN:
      case DIGITIZING_EQUIPMENT_OPEN:
      case VAULT_DOOR_OPENED:
      case AUTHENTICATION_SEAL_BROKEN:
      case EQUIPMENT_MOVED:
        return QcMaskType.STATION_SECURITY;

      case CLOCK_DIFFERENTIAL_TOO_LARGE:
      case GPS_RECEIVER_OFF:
      case GPS_RECEIVER_UNLOCKED:
      case CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD:
      case DATA_TIME_MINUS_TIME_LAST_GPS_SYNCHRONIZATION_OVER_THRESHOLD:
        return QcMaskType.TIMING;

      default:
        throw new IllegalArgumentException(
            sohType + " is an unknown literal from AcquiredChannelSohType");
    }
  }

  public static String getSystemRationale(AcquiredChannelSohType sohType) {
    switch (sohType) {
      case AUTHENTICATION_SEAL_BROKEN: {
        return "System created: authentication seal broken";
      }
      case BACKUP_POWER_UNSTABLE: {
        return "System created: backup power unstable";
      }
      case CALIBRATION_UNDERWAY: {
        return "System created: calibration underway";
      }
      case CLIPPED: {
        return "System created: clipped data";
      }
      case CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD: {
        return "System created: clock differential in microseconds > threshold";
      }
      case CLOCK_DIFFERENTIAL_TOO_LARGE: {
        return "System created: clock differential too large";
      }
      case DATA_TIME_MINUS_TIME_LAST_GPS_SYNCHRONIZATION_OVER_THRESHOLD: {
        return "System created: data time - time of last GPS synchronization > threshold";
      }
      case DEAD_SENSOR_CHANNEL: {
        return "System created: dead sensor channel";
      }
      case DIGITIZER_ANALOG_INPUT_SHORTED: {
        return "System created: digitizer analog input shorted";
      }
      case DIGITIZER_CALIBRATION_LOOP_BACK: {
        return "System created: digitizer calibration loop back";
      }
      case DIGITIZING_EQUIPMENT_OPEN: {
        return "System created: digitizing equipment open";
      }
      case EQUIPMENT_HOUSING_OPEN: {
        return "System created: equipment housing open";
      }
      case EQUIPMENT_MOVED: {
        return "System created: equipment moved";
      }
      case GPS_RECEIVER_OFF: {
        return "System created: GPS receiver off";
      }
      case GPS_RECEIVER_UNLOCKED: {
        return "System created: GPS receiver unlocked";
      }
      case MAIN_POWER_FAILURE: {
        return "System created: main power failure";
      }
      case VAULT_DOOR_OPENED: {
        return "System created: vault door opened";
      }
      case ZEROED_DATA: {
        return "System created: zeroed data";
      }

      default:
        throw new IllegalArgumentException(
            sohType + " is an unknown literal from AcquiredChannelSohType");
    }
  }
  
}
