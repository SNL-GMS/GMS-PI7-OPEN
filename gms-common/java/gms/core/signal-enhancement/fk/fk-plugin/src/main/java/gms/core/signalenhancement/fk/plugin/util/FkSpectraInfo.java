package gms.core.signalenhancement.fk.plugin.util;

import java.util.Objects;

public class FkSpectraInfo {
  private final double lowFrequency;
  private final double highFrequency;
  private final double eastSlowStart;
  private final double eastSlowDelta;
  private final double northSlowStart;
  private final double northSlowDelta;

  public static FkSpectraInfo create(double lowFrequency, double highFrequency, double eastSlowStart,
      double eastSlowDelta, double northSlowStart, double northSlowDelta) {
    return new FkSpectraInfo(lowFrequency, highFrequency, eastSlowStart, eastSlowDelta, northSlowStart, northSlowDelta);
  }

  private FkSpectraInfo(double lowFrequency, double highFrequency, double eastSlowStart,
      double eastSlowDelta, double northSlowStart, double northSlowDelta) {
    this.lowFrequency = lowFrequency;
    this.highFrequency = highFrequency;
    this.eastSlowStart = eastSlowStart;
    this.eastSlowDelta = eastSlowDelta;
    this.northSlowStart = northSlowStart;
    this.northSlowDelta = northSlowDelta;
  }

  public double getLowFrequency() {
    return lowFrequency;
  }

  public double getHighFrequency() {
    return highFrequency;
  }

  public double getEastSlowStart() {
    return eastSlowStart;
  }

  public double getEastSlowDelta() {
    return eastSlowDelta;
  }

  public double getNorthSlowStart() {
    return northSlowStart;
  }

  public double getNorthSlowDelta() {
    return northSlowDelta;
  }

  @Override
  public String toString() {
    return "FkSpectraInfo{" +
        "lowFrequency=" + lowFrequency +
        ", highFrequency=" + highFrequency +
        ", eastSlowStart=" + eastSlowStart +
        ", eastSlowDelta=" + eastSlowDelta +
        ", northSlowStart=" + northSlowStart +
        ", northSlowDelta=" + northSlowDelta +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FkSpectraInfo that = (FkSpectraInfo) o;
    return Double.compare(that.getLowFrequency(), getLowFrequency()) == 0 &&
        Double.compare(that.getHighFrequency(), getHighFrequency()) == 0 &&
        Double.compare(that.getEastSlowStart(), getEastSlowStart()) == 0 &&
        Double.compare(that.getEastSlowDelta(), getEastSlowDelta()) == 0 &&
        Double.compare(that.getNorthSlowStart(), getNorthSlowStart()) == 0 &&
        Double.compare(that.getNorthSlowDelta(), getNorthSlowDelta()) == 0;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(getLowFrequency(), getHighFrequency(), getEastSlowStart(), getEastSlowDelta(),
            getNorthSlowStart(), getNorthSlowDelta());
  }
}
