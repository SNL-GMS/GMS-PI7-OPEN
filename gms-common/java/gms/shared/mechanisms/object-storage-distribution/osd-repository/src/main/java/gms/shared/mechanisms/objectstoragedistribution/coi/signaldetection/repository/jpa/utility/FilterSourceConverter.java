package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import java.util.EnumMap;
import java.util.Map;
import javax.persistence.Converter;

/**
 * JPA converter to translate the {@link FilterSource} enumeration to and from a database column.
 * Generates an integer identity for the database value.  When the FilterSource is updated this
 * converter must also be updated to assign an id for the new literal.
 *
 * Intended to avoid issues with standard JPA enumeration mappings to integer (subject to data
 * inconsistencies if the literals are renumbered or removed) and to string (subject to data
 * inconsistencies if the literals are renamed).  This conversion is still subject to issues if a
 * literal is removed in which case {@link #convertToEntityAttribute(Integer)} would not resolve to
 * any literal.
 */
@Converter(autoApply = true)
public class FilterSourceConverter extends AttributeToIntegerConverter<FilterSource> {

  private static final Map<FilterSource, Integer> toColumn;

  static {
    toColumn = new EnumMap<>(FilterSource.class);
    toColumn.put(FilterSource.SYSTEM, 0);
    toColumn.put(FilterSource.USER, 1);
  }

  public FilterSourceConverter() {
    super(toColumn);
  }
}