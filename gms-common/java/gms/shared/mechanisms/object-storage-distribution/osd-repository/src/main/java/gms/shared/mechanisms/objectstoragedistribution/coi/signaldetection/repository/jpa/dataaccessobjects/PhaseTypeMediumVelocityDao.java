package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.PhaseTypeConverter;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "phasetypemediumvelocity")
public class PhaseTypeMediumVelocityDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Convert(converter = PhaseTypeConverter.class)
  private PhaseType phaseType;
  private Double velocity;

  public PhaseTypeMediumVelocityDao() { }

  public PhaseTypeMediumVelocityDao(
      PhaseType phaseType, Double velocity) {
    this.phaseType = phaseType;
    this.velocity = velocity;
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public PhaseType getPhaseType() {
    return phaseType;
  }

  public void setPhaseType(PhaseType phaseType) {
    this.phaseType = phaseType;
  }

  public Double getVelocity() {
    return velocity;
  }

  public void setVelocity(Double velocity) {
    this.velocity = velocity;
  }

  @Override
  public String toString() {
    return "PhaseTypeMediumVelocityDao{" +
        "daoId=" + daoId +
        ", phaseType=" + phaseType +
        ", velocity=" + velocity +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PhaseTypeMediumVelocityDao that = (PhaseTypeMediumVelocityDao) o;

    if (daoId != that.daoId) {
      return false;
    }
    if (phaseType != that.phaseType) {
      return false;
    }
    return velocity.equals(that.velocity);
  }

  @Override
  public int hashCode() {
    int result = (int) (daoId ^ (daoId >>> 32));
    result = 31 * result + phaseType.hashCode();
    result = 31 * result + velocity.hashCode();
    return result;
  }

}
