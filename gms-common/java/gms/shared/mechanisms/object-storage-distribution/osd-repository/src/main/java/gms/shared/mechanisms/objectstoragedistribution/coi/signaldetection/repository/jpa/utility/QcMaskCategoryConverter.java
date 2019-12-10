package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import java.util.EnumMap;
import java.util.Map;
import javax.persistence.Converter;

/**
 * JPA converter to translate the {@link QcMaskCategory} enumeration to and from a database column.
 * Generates an integer identity for the database value.  When the QcMaskCategory is updated this
 * converter must also be updated to assign an id for the new literal.
 *
 * Intended to avoid issues with standard JPA enumeration mappings to integer (subject to data
 * inconsistencies if the literals are renumbered or removed) and to string (subject to data
 * inconsistencies if the literals are renamed).  This conversion is still subject to issues if a
 * literal is removed in which case {@link #convertToEntityAttribute(Integer)} would not resolve to
 * any literal.
 */
@Converter(autoApply = true)
public class QcMaskCategoryConverter extends AttributeToIntegerConverter<QcMaskCategory> {

  private static final Map<QcMaskCategory, Integer> toColumn;

  static {
    toColumn = new EnumMap<>(QcMaskCategory.class);
    toColumn.put(QcMaskCategory.DATA_AUTHENTICATION, 0);
    toColumn.put(QcMaskCategory.WAVEFORM_QUALITY, 1);
    toColumn.put(QcMaskCategory.STATION_SOH, 2);
    toColumn.put(QcMaskCategory.CHANNEL_PROCESSING, 3);
    toColumn.put(QcMaskCategory.ANALYST_DEFINED, 4);
    toColumn.put(QcMaskCategory.REJECTED, 5);
  }

  public QcMaskCategoryConverter() {
    super(toColumn);
  }
}
