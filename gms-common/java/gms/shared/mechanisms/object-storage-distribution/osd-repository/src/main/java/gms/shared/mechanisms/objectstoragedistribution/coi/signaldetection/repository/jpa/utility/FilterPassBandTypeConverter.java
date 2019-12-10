package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import java.util.EnumMap;
import java.util.Map;
import javax.persistence.Converter;

/**
 * JPA converter to translate the {@link FilterPassBandType} enumeration to and from a database
 * column. Generates an integer identity for the database value.  When the FilterPassBandType is
 * updated this converter must also be updated to assign an id for the new literal.
 *
 * Intended to avoid issues with standard JPA enumeration mappings to integer (subject to data
 * inconsistencies if the literals are renumbered or removed) and to string (subject to data
 * inconsistencies if the literals are renamed).  This conversion is still subject to issues if a
 * literal is removed in which case {@link #convertToEntityAttribute(Integer)} would not resolve to
 * any literal.
 */
@Converter(autoApply = true)
public class FilterPassBandTypeConverter extends AttributeToIntegerConverter<FilterPassBandType> {

  private static final Map<FilterPassBandType, Integer> toColumn;

  static {
    toColumn = new EnumMap<>(FilterPassBandType.class);
    toColumn.put(FilterPassBandType.LOW_PASS, 0);
    toColumn.put(FilterPassBandType.HIGH_PASS, 1);
    toColumn.put(FilterPassBandType.BAND_PASS, 2);
    toColumn.put(FilterPassBandType.BAND_STOP, 3);
  }

  public FilterPassBandTypeConverter() {
    super(toColumn);
  }
}
