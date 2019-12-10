package gms.shared.utilities.standardtestdataset;

import com.google.common.collect.ListMultimap;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ReferenceStationFileReader {

  private final ListMultimap<String, ReferenceStation> stationsByName;

  public ReferenceStationFileReader(String stationsFilePath) throws Exception {
    this.stationsByName = StationReferenceFileReaderUtility.readBy(
        stationsFilePath, ReferenceStation[].class, ReferenceStation::getName);
  }

  public Optional<UUID> findStationIdByNameAndTime(String stationName, Instant time) {
    return StationReferenceFileReaderUtility.findByNameAndTime(
        this.stationsByName, stationName, time, ReferenceStation::getActualChangeTime)
        .map(ReferenceStation::getVersionId);
  }

}
