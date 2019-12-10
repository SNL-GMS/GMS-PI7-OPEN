package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.SortedSet;
import java.util.UUID;

/**
 *  A GridNode is a point at the center of a grid cell.  It contains the data associated
 *  with the grid cell.
 */
@AutoValue
public abstract class GridNode {

  /**
   * @return unique identifier of this node
   */
  public abstract UUID getId();

  /**
   *
   * @return latitude of center of node
   */
  public abstract double getCenterLatitudeDegrees();

  /**
   *
   * @return longitude of center of node
   */
  public abstract double getCenterLongitudeDegrees();

  /**
   *
   * @return depth of center of node
   */
  public abstract double getCenterDepthKm();

  /**
   *
   * @return grid cell height
   */
  public abstract double getGridCellHeightKm();

  /**
   *
   * @return set of stations associated with this node
   */
  public abstract SortedSet<NodeStation> getNodeStations();

  public static Builder builder() {
    return new AutoValue_GridNode.Builder();
  }

  public abstract Builder toBuilder();

  @JsonCreator
  public static GridNode from(
      @JsonProperty("id") UUID id,
      @JsonProperty("centerLatitudeDegrees") double centerLatitudeDegrees,
      @JsonProperty("centerLongitudeDegrees") double centerLongitudeDegrees,
      @JsonProperty("centerDepthKm") double centerDepthKm,
      @JsonProperty("gridCellHeightKm") double gridCellHeightKm,
      @JsonProperty("nodeStations") SortedSet<NodeStation> nodeStations
  ) {

    return builder()
        .setId(id)
        .setCenterLatitudeDegrees(centerLatitudeDegrees)
        .setCenterLongitudeDegrees(centerLongitudeDegrees)
        .setCenterDepthKm(centerDepthKm)
        .setGridCellHeightKm(gridCellHeightKm)
        .setNodeStations(nodeStations)
        .build();
  }

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder setId(UUID id);

    public abstract Builder setCenterLatitudeDegrees(double centerLatitudeDegrees);

    public abstract Builder setCenterLongitudeDegrees(double centerLongitudeDegrees);

    public abstract Builder setCenterDepthKm(double centerDepthKm);

    public abstract Builder setGridCellHeightKm(double gridCellHeightKm);

    public abstract Builder setNodeStations(SortedSet<NodeStation> nodeStations);

    abstract GridNode autoBuild();

    public GridNode build() {
      return autoBuild();
    }

  }

}
