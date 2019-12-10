package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Raw Waveform Data QC Mask Category. Each category maintains a list of {@link QcMaskType} objects
 * that are considered valid types for the specified category. If a category list is empty any type
 * will cause the isValidType() method to return false (fail). These lists must be updated each time
 * a new type is added/modified/removed to/from the {@link QcMaskType} enumeration.
 */
public enum QcMaskCategory {
  DATA_AUTHENTICATION(Collections.emptyList()),
  WAVEFORM_QUALITY(Arrays.asList(
      QcMaskType.REPAIRABLE_GAP,
      QcMaskType.LONG_GAP,
      QcMaskType.SPIKE,
      QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE)),
  STATION_SOH(Arrays.asList(
      QcMaskType.SENSOR_PROBLEM,
      QcMaskType.STATION_PROBLEM,
      QcMaskType.CALIBRATION,
      QcMaskType.STATION_SECURITY,
      QcMaskType.TIMING)),
  CHANNEL_PROCESSING(Collections.emptyList()),
  ANALYST_DEFINED(Arrays.asList(QcMaskType.values())),
  REJECTED(Collections.emptyList());

  private HashSet<QcMaskType> validQcMaskTypes = new HashSet<>();

  QcMaskCategory(Collection<QcMaskType> types) {
    validQcMaskTypes.addAll(types);
  }

  /**
   * Returns true if the input QcMaskType is contained in the valid set of QcMaskTypes for this
   * category. If the category is REJECTED then true is returned always and the input is ignored.
   *
   * @param type QcMaskType input
   * @return True if category is REJECTED or the input type is contained in validQcMaskTypes for
   * this category.
   */
  public boolean isValidType(QcMaskType type) {
    return (this == REJECTED) || validQcMaskTypes.contains(type);
  }
}
