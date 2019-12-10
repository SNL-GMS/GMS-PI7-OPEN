package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import java.util.EnumMap;
import java.util.Map;
import javax.persistence.Converter;

/**
 * JPA converter to translate the {@link ChannelDataType} enumeration to and from a database column.
 * Generates an integer identity for the database value.  When the {@link ChannelDataType} is updated this
 * converter must also be updated to assign an id for the new literal.
 *
 * Intended to avoid issues with standard JPA enumeration mappings to integer (subject to data
 * inconsistencies if the literals are renumbered or removed) and to string (subject to data
 * inconsistencies if the literals are renamed).  This conversion is still subject to issues if a
 * literal is removed in which case {@link #convertToEntityAttribute(Integer)} would not resolve to
 * any literal.
 */
@Converter(autoApply = true)
public class ChannelDataTypeConverter extends AttributeToIntegerConverter<ChannelDataType> {

  private static final Map<ChannelDataType, Integer> toColumn;

  static {
    toColumn = new EnumMap<>(ChannelDataType.class);
    toColumn.put(ChannelDataType.HYDROACOUSTIC_ARRAY, 0);
    toColumn.put(ChannelDataType.INFRASOUND_ARRAY, 1);
    toColumn.put(ChannelDataType.SEISMIC_3_COMPONENT, 2);
    toColumn.put(ChannelDataType.SEISMIC_ARRAY, 3);
    toColumn.put(ChannelDataType.SHORT_PERIOD_LOW_GAIN_VERTICAL, 4);
    toColumn.put(ChannelDataType.UNKNOWN, 5);
  }

  public ChannelDataTypeConverter() {
    super(toColumn);
  }
}