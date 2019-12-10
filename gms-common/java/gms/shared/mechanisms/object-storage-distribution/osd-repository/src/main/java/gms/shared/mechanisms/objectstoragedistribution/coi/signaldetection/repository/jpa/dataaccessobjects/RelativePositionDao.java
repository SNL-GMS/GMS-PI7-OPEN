package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * JPA data access object for {@link RelativePosition}
 */
@Embeddable
public class RelativePositionDao {

  @Column(name = "north_displacement_km", nullable = false)
  private double northDisplacementKm;

  @Column(name = "east_displacement_km", nullable = false)
  private double eastDisplacementKm;

  @Column(name = "vertical_displacement_km", nullable = false)
  private double verticalDisplacementKm;

  protected RelativePositionDao() {
  }

  private RelativePositionDao(double northDisplacementKm, double eastDisplacementKm,
      double verticalDisplacementKm) {
    this.northDisplacementKm = northDisplacementKm;
    this.eastDisplacementKm = eastDisplacementKm;
    this.verticalDisplacementKm = verticalDisplacementKm;
  }

  public static RelativePositionDao from(RelativePosition relativePosition) {
    Preconditions.checkNotNull(relativePosition, "Cannot create dao from null RelativePosition");
    return new RelativePositionDao(relativePosition.getNorthDisplacementKm(),
        relativePosition.getEastDisplacementKm(),
        relativePosition.getVerticalDisplacementKm());
  }

  public RelativePosition toCoi() {
    return RelativePosition.from(
        northDisplacementKm,
        eastDisplacementKm,
        verticalDisplacementKm);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RelativePositionDao that = (RelativePositionDao) o;
    return Double.compare(that.northDisplacementKm, northDisplacementKm) == 0 &&
        Double.compare(that.eastDisplacementKm, eastDisplacementKm) == 0 &&
        Double.compare(that.verticalDisplacementKm, verticalDisplacementKm) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(northDisplacementKm, eastDisplacementKm, verticalDisplacementKm);
  }

  @Override
  public String toString() {
    return "RelativePositionDao{" +
        "northDisplacementKm=" + northDisplacementKm +
        ", eastDisplacementKm=" + eastDisplacementKm +
        ", verticalDisplacementKm=" + verticalDisplacementKm +
        '}';
  }
}
