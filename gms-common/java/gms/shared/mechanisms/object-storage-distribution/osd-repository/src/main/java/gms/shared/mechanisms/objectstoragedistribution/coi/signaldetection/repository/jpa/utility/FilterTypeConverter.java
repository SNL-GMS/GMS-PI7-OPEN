package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import java.util.EnumMap;
import java.util.Map;
import javax.persistence.Converter;

/**
 * JPA converter to translate the {@link FilterType} enumeration to and from a database column.
 * Generates an integer identity for the database value.  When the FilterType is updated this
 * converter must also be updated to assign an id for the new literal.
 *
 * Intended to avoid issues with standard JPA enumeration mappings to integer (subject to data
 * inconsistencies if the literals are renumbered or removed) and to string (subject to data
 * inconsistencies if the literals are renamed).  This conversion is still subject to issues if a
 * literal is removed in which case {@link #convertToEntityAttribute(Integer)} would not resolve to
 * any literal.
 */
@Converter(autoApply = true)
public class FilterTypeConverter extends AttributeToIntegerConverter<FilterType> {

  private static final Map<FilterType, Integer> toColumn;

  static {
    toColumn = new EnumMap<>(FilterType.class);
    toColumn.put(FilterType.FIR_HAMMING, 0);
    toColumn.put(FilterType.IIR_BUTTERWORTH, 1);
  }

  public FilterTypeConverter() {
    super(toColumn);
  }
}