package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.util.Set;
import java.util.UUID;

public interface StationDto {

  @JsonCreator
  static Station from(
          @JsonProperty("id") UUID id,
          @JsonProperty("name") String name,
          @JsonProperty("description") String description,
          @JsonProperty("stationType") StationType stationType,
          @JsonProperty("latitude") double latitude,
          @JsonProperty("longitude") double longitude,
          @JsonProperty("elevation") double elevation,
          @JsonProperty("sites") Set<Site> sites) {
    return Station.from(id, name, description, stationType, latitude, longitude, elevation, sites);
  }

}


