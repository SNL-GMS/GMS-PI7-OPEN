package gms.dataacquisition.css.stationrefconverter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.AffiliationRecord;
import gms.dataacquisition.cssreader.data.AffiliationRecordCss30;
import gms.dataacquisition.cssreader.data.AffiliationRecordNnsaKbCore;
import gms.dataacquisition.cssreader.data.InstrumentRecord;
import gms.dataacquisition.cssreader.data.NetworkRecord;
import gms.dataacquisition.cssreader.data.NetworkRecordCss30;
import gms.dataacquisition.cssreader.data.NetworkRecordNnsaKbCore;
import gms.dataacquisition.cssreader.data.NetworkRecordP3;
import gms.dataacquisition.cssreader.data.SensorRecord;
import gms.dataacquisition.cssreader.data.SensorRecordCss30;
import gms.dataacquisition.cssreader.data.SensorRecordNnsaKbCore;
import gms.dataacquisition.cssreader.data.SiteChannelRecord;
import gms.dataacquisition.cssreader.data.SiteRecord;
import gms.dataacquisition.cssreader.data.SiteRecordCss30;
import gms.dataacquisition.cssreader.data.SiteRecordNnsaKbCore;
import gms.dataacquisition.cssreader.flatfilereaders.GenericFlatFileReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Various utilty functions for reading station reference files into convenient data structures.
 */
public class ReaderUtility {

  /**
   * Reads a CSS affiliations file into affiliation records.
   * Supports CSS 3.0 and NnsaKbCore formats.
   * @param path location of the affiliation file
   * @return a list of affiliation records read from the file
   * @throws Exception can't read the file, etc.
   */
  public static List<AffiliationRecord> readAffiliations(String path) throws Exception {
    return GenericFlatFileReader.read(
        path,
        Map.of(AffiliationRecordCss30.RECORD_LENGTH, AffiliationRecordCss30.class,
            AffiliationRecordNnsaKbCore.RECORD_LENGTH, AffiliationRecordNnsaKbCore.class));
  }

  /**
   * Reads a CSS instrument file into a data structure of instrument records
   * keyed by their inid.
   * @param path path to the file
   * @return the data
   * @throws Exception can't read the file, etc.
   */
  public static Map<Integer, InstrumentRecord> readInstrumentRecordsIntoMapByInid(String path)
      throws Exception {
    final List<InstrumentRecord> records = GenericFlatFileReader.read(path, InstrumentRecord.class);
    return records.stream().collect(Collectors.toMap(InstrumentRecord::getInid, Function.identity()));
  }

  /**
   * Reads CSS sensor file into a data structure of sensor records
   * keyed by channel id.
   * Supports CSS 3.0 and NnsaKbCore formats.
   * @param path the location of the sensor file
   * @return the data
   * @throws Exception can't read the file, etc.
   */
  public static ListMultimap<Integer, SensorRecord> readSensorRecordsIntoMultimapByChannelId(String path) throws Exception {
    return readRecordsIntoMultiMapByKey(
        path,
        Map.of(SensorRecordCss30.getRecordLength(), SensorRecordCss30.class,
            SensorRecordNnsaKbCore.getRecordLength(), SensorRecordNnsaKbCore.class),
        SensorRecord::getChanid);
  }

  /**
   * Reads CSS sitechan file into a data structure of site channel records
   * keyed by channel id.
   * @param path the location of the sitechan file
   * @return the data
   * @throws Exception can't read the file, etc.
   */
  public static List<SiteChannelRecord> readSitechanRecords(String path)
      throws Exception {
    return GenericFlatFileReader.read(path, SiteChannelRecord.class);
  }

  /**
   * Reads CSS network file into network records.
   * @param path the location of the network file
   * @return the data
   * @throws Exception can't read the file, etc.
   */
  public static Collection<NetworkRecord> readNetworkRecords(String path) throws Exception {
    return GenericFlatFileReader.read(path, Map.of(
        NetworkRecordCss30.getRecordLength(), NetworkRecordCss30.class,
        NetworkRecordNnsaKbCore.getRecordLength(), NetworkRecordNnsaKbCore.class,
        NetworkRecordP3.getRecordLength(), NetworkRecordP3.class));
  }

  /**
   * Reads CSS network file into site records.
   * @param path the location of the site file
   * @return the data
   * @throws Exception can't read the file, etc.
   */
  public static Collection<SiteRecord> readSiteRecords(String path) throws Exception {
    return GenericFlatFileReader.read(path,
        Map.of(SiteRecordCss30.getRecordLength(), SiteRecordCss30.class,
            SiteRecordNnsaKbCore.getRecordLength(), SiteRecordNnsaKbCore.class));
  }

  /**
   * Generic function to read records from the specified file path.
   * @param path location of the file
   * @param type the class of the record to be read
   * @param keyExtractor a function from the record to the desired key of the returned data structure
   * @param <Key> the type of the key
   * @param <Record> the type of the record
   * @return the data
   * @throws Exception can't read the file, etc.
   */
  public static <Key, Record> ListMultimap<Key, Record> readRecordsIntoMultiMapByKey(String path,
      Class<? extends Record> type, Function<Record, Key> keyExtractor) throws Exception {

    final List<Record> records = GenericFlatFileReader.read(path, type);
    final ListMultimap<Key, Record> result = ArrayListMultimap.create();
    for (Record rec : records) {
      result.put(keyExtractor.apply(rec), rec);
    }
    return result;
  }

  private static <Key, Record> ListMultimap<Key, Record> readRecordsIntoMultiMapByKey(String path,
      Map<Integer, Class<? extends Record>> lineLengthToType, Function<Record, Key> keyExtractor) throws Exception {

    final List<Record> records = GenericFlatFileReader.read(path, lineLengthToType);
    final ListMultimap<Key, Record> result = ArrayListMultimap.create();
    for (Record rec : records) {
      result.put(keyExtractor.apply(rec), rec);
    }
    return result;
  }

}
