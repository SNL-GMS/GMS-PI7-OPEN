package gms.dataacquisition.stationreceiver.cd11.common.enums;

public enum CompressionFormat {

  NONE((byte) 0), CANADIAN_BEFORE_SIGNATURE((byte) 1),
  CANADIAN_AFTER_SIGNATURE((byte) 2),
  STEIM_BEFORE_SIGNATURE((byte) 3),
  STEIM_AFTER_SIGNATURE((byte) 4);

  public final byte code;

  CompressionFormat(byte code) {
    this.code = code;
  }

  public static CompressionFormat of(byte code) {
    if (code < 0) {
      return null;  // nothing defined for < 0
    }
    switch (code) {
      case 0:
        return NONE;
      case 1:
        return CANADIAN_BEFORE_SIGNATURE;
      case 2:
        return CANADIAN_AFTER_SIGNATURE;
      case 3:
        return STEIM_BEFORE_SIGNATURE;
      case 4:
        return STEIM_AFTER_SIGNATURE;
    }
    // no match found
    throw new IllegalArgumentException("Unknown code " + code);
  }
}
