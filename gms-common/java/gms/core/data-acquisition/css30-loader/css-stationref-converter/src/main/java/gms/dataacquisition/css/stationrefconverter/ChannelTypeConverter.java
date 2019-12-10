package gms.dataacquisition.css.stationrefconverter;

import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_EXTERNAL_TEMPERATURE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_HIGH_GAIN_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_HIGH_GAIN_ORTHOGONAL_1;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_HIGH_GAIN_ORTHOGONAL_2;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_HIGH_GAIN_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_PRESSURE_ATMOSPHERE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_PRESSURE_INFRASOUND;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_WIND_DIRECTION;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.BROADBAND_WIND_SPEED;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.EXTREMELY_SHORT_PERIOD_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.EXTREMELY_SHORT_PERIOD_HIGH_GAIN_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.EXTREMELY_SHORT_PERIOD_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.EXTREMELY_SHORT_PERIOD_PRESSURE_HYDROPHONE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.EXTREMELY_SHORT_PERIOD_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.HIGH_BROADBAND_HIGH_GAIN_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.HIGH_BROADBAND_HIGH_GAIN_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.HIGH_BROADBAND_HIGH_GAIN_ORTHOGONAL_1;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.HIGH_BROADBAND_HIGH_GAIN_ORTHOGONAL_2;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.HIGH_BROADBAND_HIGH_GAIN_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.HIGH_BROADBAND_PRESSURE_INFRASOUND;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_CURRENT_SENSOR;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_EXTERNAL_RELATIVE_HUMIDITY;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_EXTERNAL_TEMPERATURE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_HIGH_GAIN_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_HIGH_GAIN_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_HIGH_GAIN_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_PRESSURE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_VOLTAGE_SENSOR;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_WIND_DIRECTION;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.LONG_PERIOD_WIND_SPEED;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_EXTERNAL_TEMPERATURE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_HIGH_GAIN_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_HIGH_GAIN_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_HIGH_GAIN_ORTHOGONAL_1;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_HIGH_GAIN_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_PRESSURE_ABSOLUTE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_WIND_DIRECTION;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.MID_PERIOD_WIND_SPEED;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_HIGH_GAIN_EAST_WEST;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_HIGH_GAIN_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_HIGH_GAIN_ORTHOGONAL_1;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_HIGH_GAIN_ORTHOGONAL_2;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_HIGH_GAIN_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_NORTH_SOUTH;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_PRESSURE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.SHORT_PERIOD_VERTICAL;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.UNKNOWN;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.UNUSED;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.WEATHER_CHANNEL_BAROMETRIC_PRESSURE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.WEATHER_CHANNEL_OUTSIDE_TEMPERATURE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.WEATHER_CHANNEL_WIND_DIRECTION;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.WEATHER_CHANNEL_WIND_SPEED;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.WEATHER_TEMPERATURE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.WEATHER_WIND_DIRECTION;
import static gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType.WEATHER_WIND_SPEED;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.HashMap;
import java.util.Map;

public final class ChannelTypeConverter {

  private static final Map<String, ChannelType> nameToChanType = new HashMap<>();

  static {
    nameToChanType.put("BDA", BROADBAND_PRESSURE_ATMOSPHERE);
    nameToChanType.put("BDF", BROADBAND_PRESSURE_INFRASOUND);
    nameToChanType.put("BH1", BROADBAND_HIGH_GAIN_ORTHOGONAL_1);
    nameToChanType.put("BH2", BROADBAND_HIGH_GAIN_ORTHOGONAL_2);
    nameToChanType.put("BHE", BROADBAND_HIGH_GAIN_EAST_WEST);
    nameToChanType.put("BHN", BROADBAND_HIGH_GAIN_NORTH_SOUTH);
    nameToChanType.put("BHZ", BROADBAND_HIGH_GAIN_VERTICAL);
    nameToChanType.put("BKO", BROADBAND_EXTERNAL_TEMPERATURE);
    nameToChanType.put("BWD", BROADBAND_WIND_DIRECTION);
    nameToChanType.put("BWS", BROADBAND_WIND_SPEED);
    nameToChanType.put("EDH", EXTREMELY_SHORT_PERIOD_PRESSURE_HYDROPHONE);
    nameToChanType.put("EHE", EXTREMELY_SHORT_PERIOD_EAST_WEST);
    nameToChanType.put("EHN", EXTREMELY_SHORT_PERIOD_HIGH_GAIN_NORTH_SOUTH);
    nameToChanType.put("EHZ", EXTREMELY_SHORT_PERIOD_VERTICAL);
    nameToChanType.put("HDF", HIGH_BROADBAND_PRESSURE_INFRASOUND);
    nameToChanType.put("HH1", HIGH_BROADBAND_HIGH_GAIN_ORTHOGONAL_1);
    nameToChanType.put("HH2", HIGH_BROADBAND_HIGH_GAIN_ORTHOGONAL_2);
    nameToChanType.put("HHE", HIGH_BROADBAND_HIGH_GAIN_EAST_WEST);
    nameToChanType.put("HHN", HIGH_BROADBAND_HIGH_GAIN_NORTH_SOUTH);
    nameToChanType.put("HHZ", HIGH_BROADBAND_HIGH_GAIN_VERTICAL);
    nameToChanType.put("LDA", LONG_PERIOD_PRESSURE);
    nameToChanType.put("LEA", LONG_PERIOD_CURRENT_SENSOR);
    nameToChanType.put("LEV", LONG_PERIOD_VOLTAGE_SENSOR);
    nameToChanType.put("LHE", LONG_PERIOD_HIGH_GAIN_EAST_WEST);
    nameToChanType.put("LHN", LONG_PERIOD_HIGH_GAIN_NORTH_SOUTH);
    nameToChanType.put("LHZ", LONG_PERIOD_HIGH_GAIN_VERTICAL);
    nameToChanType.put("LIO", LONG_PERIOD_EXTERNAL_RELATIVE_HUMIDITY);
    nameToChanType.put("LKO", LONG_PERIOD_EXTERNAL_TEMPERATURE);
    nameToChanType.put("LWD", LONG_PERIOD_WIND_DIRECTION);
    nameToChanType.put("LWS", LONG_PERIOD_WIND_SPEED);
    nameToChanType.put("MDA", MID_PERIOD_PRESSURE_ABSOLUTE);
    nameToChanType.put("MH1", MID_PERIOD_HIGH_GAIN_ORTHOGONAL_1);
    nameToChanType.put("MH2", MID_PERIOD_HIGH_GAIN_ORTHOGONAL_1);
    nameToChanType.put("MHE", MID_PERIOD_HIGH_GAIN_EAST_WEST);
    nameToChanType.put("MHN", MID_PERIOD_HIGH_GAIN_NORTH_SOUTH);
    nameToChanType.put("MHZ", MID_PERIOD_HIGH_GAIN_VERTICAL);
    nameToChanType.put("MKO", MID_PERIOD_EXTERNAL_TEMPERATURE);
    nameToChanType.put("MWD", MID_PERIOD_WIND_DIRECTION);
    nameToChanType.put("MWS", MID_PERIOD_WIND_SPEED);
    nameToChanType.put("SH1", SHORT_PERIOD_HIGH_GAIN_ORTHOGONAL_1);
    nameToChanType.put("SH2", SHORT_PERIOD_HIGH_GAIN_ORTHOGONAL_2);
    nameToChanType.put("SHE", SHORT_PERIOD_HIGH_GAIN_EAST_WEST);
    nameToChanType.put("SHN", SHORT_PERIOD_HIGH_GAIN_NORTH_SOUTH);
    nameToChanType.put("SHZ", SHORT_PERIOD_HIGH_GAIN_VERTICAL);
    nameToChanType.put("WDO", WEATHER_CHANNEL_BAROMETRIC_PRESSURE);
    nameToChanType.put("WKO", WEATHER_CHANNEL_OUTSIDE_TEMPERATURE);
    nameToChanType.put("WWD", WEATHER_CHANNEL_WIND_DIRECTION);
    nameToChanType.put("WWS", WEATHER_CHANNEL_WIND_SPEED);
    nameToChanType.put("BE", BROADBAND_EAST_WEST);
    nameToChanType.put("BN", BROADBAND_NORTH_SOUTH);
    nameToChanType.put("BZ", BROADBAND_VERTICAL);
    nameToChanType.put("EE", EXTREMELY_SHORT_PERIOD_EAST_WEST);
    nameToChanType.put("EN", EXTREMELY_SHORT_PERIOD_NORTH_SOUTH);
    nameToChanType.put("EZ", EXTREMELY_SHORT_PERIOD_VERTICAL);
    nameToChanType.put("LE", LONG_PERIOD_EAST_WEST);
    nameToChanType.put("LN", LONG_PERIOD_NORTH_SOUTH);
    nameToChanType.put("LZ", LONG_PERIOD_VERTICAL);
    nameToChanType.put("ME", MID_PERIOD_EAST_WEST);
    nameToChanType.put("MN", MID_PERIOD_NORTH_SOUTH);
    nameToChanType.put("MZ", MID_PERIOD_VERTICAL);
    nameToChanType.put("S3", UNUSED);
    nameToChanType.put("SD", SHORT_PERIOD_PRESSURE);
    nameToChanType.put("SE", SHORT_PERIOD_EAST_WEST);
    nameToChanType.put("SN", SHORT_PERIOD_NORTH_SOUTH);
    nameToChanType.put("SZ", SHORT_PERIOD_VERTICAL);
    nameToChanType.put("WD", WEATHER_WIND_DIRECTION);
    nameToChanType.put("WS", WEATHER_WIND_SPEED);
    nameToChanType.put("WT", WEATHER_TEMPERATURE);
  }

  public static ChannelType getChannelType(String chanName) {
    if (chanName == null) {
      return UNKNOWN;
    }
    return nameToChanType.getOrDefault(chanName.trim().toUpperCase(), UNKNOWN);
  }

}
