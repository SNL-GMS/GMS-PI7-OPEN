package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationDataAcquisitionGroup;
import java.time.Instant;

@AutoValue
public abstract class ReceivedStationDataPacket {

  public abstract byte[] getPacket();

  public abstract Instant getReceptionTime();

  public abstract long getSequenceNumber();

  public abstract String getStationIdentifier();

  @JsonCreator
  public static ReceivedStationDataPacket from(
      @JsonProperty("packet") byte[] packet,
      @JsonProperty("receptionTime") Instant receptionTime,
      @JsonProperty("sequenceNumber") long sequenceNumber,
      @JsonProperty("stationIdentifier") String stationIdentifier) {

    return new AutoValue_ReceivedStationDataPacket(packet,
        receptionTime, sequenceNumber, stationIdentifier);
  }
}
