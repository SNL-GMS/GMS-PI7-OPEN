package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.util.EnumMap;
import java.util.Map;
import javax.persistence.Converter;

/**
 * JPA converter to translate the {@link QcMaskType} enumeration to and from a database column.
 * Generates an integer identity for the database value.  When the QcMaskType is updated this
 * converter must also be updated to assign an id for the new literal.
 *
 * Intended to avoid issues with standard JPA enumeration mappings to integer (subject to data
 * inconsistencies if the literals are renumbered or removed) and to string (subject to data
 * inconsistencies if the literals are renamed).  This conversion is still subject to issues if a
 * literal is removed in which case {@link #convertToEntityAttribute(Integer)} would not resolve to
 * any literal.
 */
@Converter(autoApply = true)
public class QcMaskTypeConverter extends AttributeToIntegerConverter<QcMaskType> {

  private static final Map<QcMaskType, Integer> toColumn;

  static {
    toColumn = new EnumMap<>(QcMaskType.class);
    toColumn.put(QcMaskType.SENSOR_PROBLEM, 0);
    toColumn.put(QcMaskType.STATION_PROBLEM, 1);
    toColumn.put(QcMaskType.CALIBRATION, 2);
    toColumn.put(QcMaskType.STATION_SECURITY, 3);
    toColumn.put(QcMaskType.TIMING, 4);
    toColumn.put(QcMaskType.LONG_GAP, 5);
    toColumn.put(QcMaskType.REPAIRABLE_GAP, 6);
    toColumn.put(QcMaskType.SPIKE, 7);
    toColumn.put(QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE, 8);
  }

  public QcMaskTypeConverter() {
    super(toColumn);
  }
}
