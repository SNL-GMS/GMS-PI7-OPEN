package gms.shared.utilities.signalprocessing.normalization;

import java.util.function.DoubleUnaryOperator;

public enum Transform {

  ABS(d -> Math.abs(d)),
  SQUARE(d -> d * d);

  private final DoubleUnaryOperator transformFunction;

  Transform(DoubleUnaryOperator transformFunction) {
    this.transformFunction = transformFunction;
  }

  public DoubleUnaryOperator getTransformFunction() {
    return transformFunction;
  }
}
