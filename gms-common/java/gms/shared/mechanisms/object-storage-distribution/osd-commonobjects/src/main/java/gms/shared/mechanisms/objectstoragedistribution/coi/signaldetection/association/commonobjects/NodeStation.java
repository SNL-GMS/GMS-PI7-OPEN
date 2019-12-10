package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Objects;
import java.util.SortedSet;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

@AutoValue
public abstract class NodeStation implements Comparable<NodeStation> {

  /**
   * @return unique identifier of this object
   */
  public abstract UUID getId();

  /**
   * @return identifier of the station associated with a node
   */
  public abstract UUID getStationId();

  /**
   * @return distance from grid point in degrees
   */
  public abstract double getDistanceFromGridPointDegrees();

  /**
   * @return sorted set of phase specific information for this station
   */
  public abstract SortedSet<PhaseInfo> getPhaseInfos();

  public static Builder builder() {
    return new AutoValue_NodeStation.Builder();
  }

  public abstract Builder toBuilder();

  @JsonCreator
  public static NodeStation from(
      @JsonProperty("id") UUID id,
      @JsonProperty("stationId") UUID stationId,
      @JsonProperty("distanceFromGridPointDegrees") double distanceFromGridPointDegrees,
      @JsonProperty("phaseInfos") SortedSet<PhaseInfo> phaseInfos
  ) {

    Objects.requireNonNull(id, "Null id");
    Objects.requireNonNull(stationId, "Null stationId");
    Objects.requireNonNull(phaseInfos, "Null phaseInfos");

    Validate.notEmpty(phaseInfos, "Cannot instantiate NodeStation with empty phaseInfos");

    return builder()
        .setId(id)
        .setStationId(stationId)
        .setDistanceFromGridPointDegrees(distanceFromGridPointDegrees)
        .setPhaseInfos(phaseInfos)
        .build();
  }

  /**
   * Compares the first {@PhaseInfo} of this NodeStation to the first PhaseInfo of the provided
   * NodeStation
   *
   * @param otherStation the other station to compare to
   * @return -1 if this node station is "less than" otherStation; 0 if this node station is "equal"
   * to otherStation, 1 of this node stations is "greater than" otherStation. Ordering is imposed by
   * {@PhaseInfo#compartTo}
   */
  @Override
  public int compareTo(NodeStation otherStation) {
    return getPhaseInfos().first().compareTo(otherStation.getPhaseInfos().first());
  }

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder setId(UUID id);

    public abstract Builder setStationId(UUID id);

    public abstract Builder setDistanceFromGridPointDegrees(double distanceFromGridPointDegrees);

    public abstract Builder setPhaseInfos(SortedSet<PhaseInfo> phaseInfos);

    abstract NodeStation autoBuild();

    public NodeStation build() {
      return autoBuild();
    }

  }

}
