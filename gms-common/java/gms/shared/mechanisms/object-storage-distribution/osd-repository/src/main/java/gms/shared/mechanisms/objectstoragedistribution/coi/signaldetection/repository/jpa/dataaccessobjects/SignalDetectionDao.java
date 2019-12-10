package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "signal_detection")
public class SignalDetectionDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(updatable = false)
  private UUID signalDetectionId;

  @Column
  private String monitoringOrganization;

  @Column(updatable = false)
  private UUID stationId;

  @Column(updatable = false)
  private UUID creationInfoId;

  public SignalDetectionDao(){}

  public SignalDetectionDao(UUID signalDetectionId, String monitoringOrganization, UUID stationId,
      UUID creationInfoId) {
    this.signalDetectionId = signalDetectionId;
    this.monitoringOrganization = monitoringOrganization;
    this.stationId = stationId;
    this.creationInfoId = creationInfoId;
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public UUID getSignalDetectionId() {
    return signalDetectionId;
  }

  public void setSignalDetectionId(UUID signalDetectionId) {
    this.signalDetectionId = signalDetectionId;
  }

  public String getMonitoringOrganization() {
    return monitoringOrganization;
  }

  public void setMonitoringOrganization(String monitoringOrganization) {
    this.monitoringOrganization = monitoringOrganization;
  }

  public UUID getStationId() {
    return stationId;
  }

  public void setStationId(UUID stationId) {
    this.stationId = stationId;
  }

  public UUID getCreationInfoId() {
    return creationInfoId;
  }

  public void setCreationInfoId(UUID creationInfoId) {
    this.creationInfoId = creationInfoId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SignalDetectionDao that = (SignalDetectionDao) o;

    if (daoId != that.daoId) {
      return false;
    }
    if (!signalDetectionId.equals(that.signalDetectionId)) {
      return false;
    }
    if (!monitoringOrganization.equals(that.monitoringOrganization)) {
      return false;
    }
    if (!stationId.equals(that.stationId)) {
      return false;
    }
    return creationInfoId.equals(that.creationInfoId);
  }

  @Override
  public int hashCode() {
    int result = (int) (daoId ^ (daoId >>> 32));
    result = 31 * result + signalDetectionId.hashCode();
    result = 31 * result + monitoringOrganization.hashCode();
    result = 31 * result + stationId.hashCode();
    result = 31 * result + creationInfoId.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SignalDetectionDao{" +
        "daoId=" + daoId +
        ", signalDetectionId=" + signalDetectionId +
        ", monitoringOrganization='" + monitoringOrganization + '\'' +
        ", stationId=" + stationId +
        ", creationInfoId=" + creationInfoId +
        '}';
  }
}
