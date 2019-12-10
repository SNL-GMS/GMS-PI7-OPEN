package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.EnumMap;
import java.util.Map;
import javax.persistence.Converter;

/**
 * JPA converter to translate the {@link ChannelType} enumeration to and from a database column.
 * Generates an integer identity for the database value.  When the {@link ChannelType} is updated this
 * converter must also be updated to assign an id for the new literal.
 *
 * Intended to avoid issues with standard JPA enumeration mappings to integer (subject to data
 * inconsistencies if the literals are renumbered or removed) and to string (subject to data
 * inconsistencies if the literals are renamed).  This conversion is still subject to issues if a
 * literal is removed in which case {@link #convertToEntityAttribute(Integer)} would not resolve to
 * any literal.
 */
@Converter(autoApply = true)
public class ChannelTypeConverter extends AttributeToIntegerConverter<ChannelType> {

  private static final Map<ChannelType, Integer> toColumn;

  static {
    toColumn = new EnumMap<>(ChannelType.class);
    toColumn.put(ChannelType.BROADBAND_PRESSURE_ATMOSPHERE, 0);
    toColumn.put(ChannelType.BROADBAND_PRESSURE_INFRASOUND, 1);
    toColumn.put(ChannelType.BROADBAND_HIGH_GAIN_ORTHOGONAL_1, 2);
    toColumn.put(ChannelType.BROADBAND_HIGH_GAIN_ORTHOGONAL_2, 3);
    toColumn.put(ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST, 4);
    toColumn.put(ChannelType.BROADBAND_HIGH_GAIN_NORTH_SOUTH, 5);
    toColumn.put(ChannelType.BROADBAND_HIGH_GAIN_VERTICAL, 6);
    toColumn.put(ChannelType.BROADBAND_EXTERNAL_TEMPERATURE, 7);
    toColumn.put(ChannelType.BROADBAND_WIND_DIRECTION, 8);
    toColumn.put(ChannelType.BROADBAND_WIND_SPEED, 9);
    toColumn.put(ChannelType.EXTREMELY_SHORT_PERIOD_PRESSURE_HYDROPHONE, 10);
    toColumn.put(ChannelType.EXTREMELY_SHORT_PERIOD_HIGH_GAIN_EAST_WEST, 11);
    toColumn.put(ChannelType.EXTREMELY_SHORT_PERIOD_HIGH_GAIN_NORTH_SOUTH, 12);
    toColumn.put(ChannelType.EXTREMELY_SHORT_PERIOD_HIGH_GAIN_VERTICAL, 13);
    toColumn.put(ChannelType.HIGH_BROADBAND_PRESSURE_INFRASOUND, 14);
    toColumn.put(ChannelType.HIGH_BROADBAND_HIGH_GAIN_ORTHOGONAL_1, 15);
    toColumn.put(ChannelType.HIGH_BROADBAND_HIGH_GAIN_ORTHOGONAL_2, 16);
    toColumn.put(ChannelType.HIGH_BROADBAND_HIGH_GAIN_EAST_WEST, 17);
    toColumn.put(ChannelType.HIGH_BROADBAND_HIGH_GAIN_NORTH_SOUTH, 18);
    toColumn.put(ChannelType.HIGH_BROADBAND_HIGH_GAIN_VERTICAL, 19);
    toColumn.put(ChannelType.LONG_PERIOD_PRESSURE, 20);
    toColumn.put(ChannelType.LONG_PERIOD_CURRENT_SENSOR, 21);
    toColumn.put(ChannelType.LONG_PERIOD_VOLTAGE_SENSOR, 22);
    toColumn.put(ChannelType.LONG_PERIOD_HIGH_GAIN_EAST_WEST, 23);
    toColumn.put(ChannelType.LONG_PERIOD_HIGH_GAIN_NORTH_SOUTH, 24);
    toColumn.put(ChannelType.LONG_PERIOD_HIGH_GAIN_VERTICAL, 25);
    toColumn.put(ChannelType.LONG_PERIOD_EXTERNAL_RELATIVE_HUMIDITY, 26);
    toColumn.put(ChannelType.LONG_PERIOD_EXTERNAL_TEMPERATURE, 27);
    toColumn.put(ChannelType.LONG_PERIOD_WIND_DIRECTION, 28);
    toColumn.put(ChannelType.LONG_PERIOD_WIND_SPEED, 29);
    toColumn.put(ChannelType.MID_PERIOD_PRESSURE_ABSOLUTE, 30);
    toColumn.put(ChannelType.MID_PERIOD_HIGH_GAIN_ORTHOGONAL_1, 31);
    toColumn.put(ChannelType.MID_PERIOD_HIGH_GAIN_ORTHOGONAL_2, 32);
    toColumn.put(ChannelType.MID_PERIOD_HIGH_GAIN_EAST_WEST, 33);
    toColumn.put(ChannelType.MID_PERIOD_HIGH_GAIN_NORTH_SOUTH, 34);
    toColumn.put(ChannelType.MID_PERIOD_HIGH_GAIN_VERTICAL, 35);
    toColumn.put(ChannelType.MID_PERIOD_EXTERNAL_TEMPERATURE, 36);
    toColumn.put(ChannelType.MID_PERIOD_WIND_DIRECTION, 37);
    toColumn.put(ChannelType.MID_PERIOD_WIND_SPEED, 38);
    toColumn.put(ChannelType.SHORT_PERIOD_HIGH_GAIN_ORTHOGONAL_1, 39);
    toColumn.put(ChannelType.SHORT_PERIOD_HIGH_GAIN_ORTHOGONAL_2, 40);
    toColumn.put(ChannelType.SHORT_PERIOD_HIGH_GAIN_EAST_WEST, 41);
    toColumn.put(ChannelType.SHORT_PERIOD_HIGH_GAIN_NORTH_SOUTH, 42);
    toColumn.put(ChannelType.SHORT_PERIOD_HIGH_GAIN_VERTICAL, 43);
    toColumn.put(ChannelType.WEATHER_CHANNEL_BAROMETRIC_PRESSURE, 44);
    toColumn.put(ChannelType.WEATHER_CHANNEL_OUTSIDE_TEMPERATURE, 45);
    toColumn.put(ChannelType.WEATHER_CHANNEL_WIND_DIRECTION, 46);
    toColumn.put(ChannelType.WEATHER_CHANNEL_WIND_SPEED, 47);
    toColumn.put(ChannelType.BROADBAND_EAST_WEST, 48);
    toColumn.put(ChannelType.BROADBAND_NORTH_SOUTH, 49);
    toColumn.put(ChannelType.BROADBAND_VERTICAL, 50);
    toColumn.put(ChannelType.EXTREMELY_SHORT_PERIOD_EAST_WEST, 51);
    toColumn.put(ChannelType.EXTREMELY_SHORT_PERIOD_NORTH_SOUTH, 52);
    toColumn.put(ChannelType.EXTREMELY_SHORT_PERIOD_VERTICAL, 53);
    toColumn.put(ChannelType.LONG_PERIOD_EAST_WEST, 54);
    toColumn.put(ChannelType.LONG_PERIOD_NORTH_SOUTH, 55);
    toColumn.put(ChannelType.LONG_PERIOD_VERTICAL, 56);
    toColumn.put(ChannelType.MID_PERIOD_EAST_WEST, 57);
    toColumn.put(ChannelType.MID_PERIOD_NORTH_SOUTH, 58);
    toColumn.put(ChannelType.MID_PERIOD_VERTICAL, 59);
    toColumn.put(ChannelType.UNUSED, 60);
    toColumn.put(ChannelType.SHORT_PERIOD_PRESSURE, 61);
    toColumn.put(ChannelType.SHORT_PERIOD_EAST_WEST, 62);
    toColumn.put(ChannelType.SHORT_PERIOD_NORTH_SOUTH, 63);
    toColumn.put(ChannelType.SHORT_PERIOD_VERTICAL, 64);
    toColumn.put(ChannelType.WEATHER_WIND_DIRECTION, 65);
    toColumn.put(ChannelType.WEATHER_WIND_SPEED, 66);
    toColumn.put(ChannelType.WEATHER_TEMPERATURE, 67);
    toColumn.put(ChannelType.UNKNOWN, 68);
  }

  public ChannelTypeConverter() {
    super(toColumn);
  }
}