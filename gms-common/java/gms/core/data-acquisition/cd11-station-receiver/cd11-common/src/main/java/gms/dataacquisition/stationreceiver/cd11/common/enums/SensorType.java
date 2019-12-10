package gms.dataacquisition.stationreceiver.cd11.common.enums;

public enum SensorType {

  SEISMIC((byte) 0), HYDROACOUSTIC((byte) 1),
  INFRASONIC((byte) 2), WEATHER((byte) 3),
  OTHER((byte) 4);

  public final byte code;

  SensorType(byte code) {
    this.code = code;
  }

  public static SensorType of(byte code) {
    // no translation is defined for negative numbers
    if (code < 0) {
      throw new IllegalArgumentException("code for SensorType cannot be negative");
    }

    switch (code) {
      case 0:
        return SEISMIC;
      case 1:
        return HYDROACOUSTIC;
      case 2:
        return INFRASONIC;
      case 3:
        return WEATHER;
    }
    return OTHER;  // code > 3 means 'other'
  }
}
