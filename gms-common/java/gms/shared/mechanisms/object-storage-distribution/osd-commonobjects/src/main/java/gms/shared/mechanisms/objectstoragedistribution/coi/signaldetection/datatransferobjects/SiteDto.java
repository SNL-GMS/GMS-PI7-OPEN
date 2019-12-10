package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import java.util.Set;
import java.util.UUID;

public interface SiteDto {

  @JsonCreator
  static Site from(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("latitude") double latitude,
      @JsonProperty("longitude") double longitude,
      @JsonProperty("elevation") double elevation,
      @JsonProperty("channels") Set<Channel> channels) {
    return Site.from(id, name, latitude, longitude, elevation, channels);
  }

}

