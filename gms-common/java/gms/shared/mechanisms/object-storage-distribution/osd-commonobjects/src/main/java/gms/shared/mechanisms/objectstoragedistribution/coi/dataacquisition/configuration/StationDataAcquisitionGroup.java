package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

@AutoValue
public abstract class StationDataAcquisitionGroup {

  public abstract UUID getId();

  public abstract List<String> getRequestStrings();

  public abstract AcquisitionProtocol getProtocol();

  public abstract String getProviderIpAddress();

  public abstract int getProviderPort();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract Map<String, StationAndChannelId> getIdsByReceivedName();

  public abstract boolean isActive();

  public abstract String getComment();

  @JsonCreator
  public static StationDataAcquisitionGroup from(
      @JsonProperty("id") UUID id,
      @JsonProperty("requestStrings") List<String> requestStrings,
      @JsonProperty("protocol") AcquisitionProtocol protocol,
      @JsonProperty("providerIpAddress") String providerIpAddress,
      @JsonProperty("providerPort") int providerPort,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime,
      @JsonProperty("idsByReceivedName") Map<String, StationAndChannelId> idsByReceivedName,
      @JsonProperty("active") boolean active,
      @JsonProperty("comment") String comment) {

    Validate.notEmpty(requestStrings, "requestStrings cannot be empty or null");
    return new AutoValue_StationDataAcquisitionGroup(id, requestStrings,
        protocol, providerIpAddress, providerPort, actualChangeTime, systemChangeTime,
        Collections.unmodifiableMap(idsByReceivedName), active, comment);
  }

  public static StationDataAcquisitionGroup create(
      List<String> requestStrings, AcquisitionProtocol protocol,
      String providerIpAddress, int providerPort, Instant actualChangeTime,
      Instant systemChangeTime, Map<String, StationAndChannelId> idsByReceivedName,
      boolean active, String comment) {

    return from(UUID.randomUUID(), requestStrings,
        protocol, providerIpAddress, providerPort, actualChangeTime, systemChangeTime,
        idsByReceivedName, active, comment);
  }
}
