package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import java.util.EnumMap;
import java.util.Map;
import javax.persistence.Converter;

/**
 * JPA converter to translate the {@link ChannelProcessingGroupType} enumeration to and from a database column.
 * Generates an integer identity for the database value.  When the {@link ChannelProcessingGroupType} is updated this
 * converter must also be updated to assign an id for the new literal.
 *
 * Intended to avoid issues with standard JPA enumeration mappings to integer (subject to data
 * inconsistencies if the literals are renumbered or removed) and to string (subject to data
 * inconsistencies if the literals are renamed).  This conversion is still subject to issues if a
 * literal is removed in which case {@link #convertToEntityAttribute(Integer)} would not resolve to
 * any literal.
 */
@Converter(autoApply = true)
public class ChannelProcessingGroupTypeConverter
    extends AttributeToIntegerConverter<ChannelProcessingGroupType> {

  private static final Map<ChannelProcessingGroupType, Integer> toColumn;

  static {
    toColumn = new EnumMap<>(ChannelProcessingGroupType.class);
    toColumn.put(ChannelProcessingGroupType.SINGLE_CHANNEL, 0);
    toColumn.put(ChannelProcessingGroupType.THREE_COMPONENT, 1);
    toColumn.put(ChannelProcessingGroupType.BEAM, 2);
  }

  public ChannelProcessingGroupTypeConverter() {
    super(toColumn);
  }
}