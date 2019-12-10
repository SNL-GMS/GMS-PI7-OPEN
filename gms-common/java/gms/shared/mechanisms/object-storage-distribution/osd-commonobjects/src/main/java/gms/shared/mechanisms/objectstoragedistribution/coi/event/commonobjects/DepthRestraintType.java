package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

/**
 * Define an enumeration of depth restraint types for location solutions.
 */
public enum DepthRestraintType {
  UNRESTRAINED,
  FIXED_AT_DEPTH {
    @Override
    public double getValue(double originalValue, double fixedValue) {
      return fixedValue;
    }
  },
  FIXED_AT_SURFACE {
    @Override
    public double getValue(double originalValue, double fixedValue) {
      return 0.0;
    }
  };

  public double getValue(double originalValue, double fixedValue) {
    return originalValue;
  }
}
