package gms.dataacquisition.stationreceiver.cd11.common.enums;

public enum DataType {

  s4((byte) 4), s3((byte) 3), s2((byte) 2),
  i4((byte) 4), i2((byte) 2);

  public final byte size;

  DataType(byte size) {
    this.size = size;
  }
}
