package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;

/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation}
 * to allow read and write access to the relational database.
 */
@Entity
@Table(name = "signal_detection_event_association")
public class SignalDetectionEventAssociationDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "event_hypothesis_id", nullable = false)
  private UUID eventHypothesisId;

  @Column(name = "signal_detection_hypothesis_id", nullable = false)
  private UUID signalDetectionHypothesisId;

  @Column(name = "is_rejected")
  private boolean isRejected;

  /**
   * Default constructor for JPA.
   */
  public SignalDetectionEventAssociationDao() {
  }

  /**
   * Create a DAO from the COI object.
   *
   * @param signalDetectionEventAssociation The SignalDetectionEventAssociation object.
   */
  public SignalDetectionEventAssociationDao(
      SignalDetectionEventAssociation signalDetectionEventAssociation) throws NullPointerException {
    Validate.notNull(signalDetectionEventAssociation);
    this.id = signalDetectionEventAssociation.getId();
    this.eventHypothesisId = signalDetectionEventAssociation.getEventHypothesisId();
    this.signalDetectionHypothesisId = signalDetectionEventAssociation
        .getSignalDetectionHypothesisId();
    this.isRejected = signalDetectionEventAssociation.isRejected();
  }

  /**
   * Convert this DAO into its corresponding COI object.
   *
   * @return A SignalDetectionEventAssociation COI object.
   */
  public SignalDetectionEventAssociation toCoi() {
    return SignalDetectionEventAssociation
        .from(getId(), getEventHypothesisId(), getSignalDetectionHypothesisId(), isRejected());
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getEventHypothesisId() {
    return eventHypothesisId;
  }

  public void setEventHypothesisId(UUID eventHypothesisId) {
    this.eventHypothesisId = eventHypothesisId;
  }

  public UUID getSignalDetectionHypothesisId() {
    return signalDetectionHypothesisId;
  }

  public void setSignalDetectionHypothesisId(UUID signalDetectionHypothesisId) {
    this.signalDetectionHypothesisId = signalDetectionHypothesisId;
  }

  public boolean isRejected() {
    return isRejected;
  }

  public void setRejected(boolean rejected) {
    isRejected = rejected;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SignalDetectionEventAssociationDao)) {
      return false;
    }

    SignalDetectionEventAssociationDao that = (SignalDetectionEventAssociationDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
    if (isRejected != that.isRejected) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (eventHypothesisId != null ? !eventHypothesisId.equals(that.eventHypothesisId)
        : that.eventHypothesisId != null) {
      return false;
    }
    return signalDetectionHypothesisId != null ? signalDetectionHypothesisId
        .equals(that.signalDetectionHypothesisId) : that.signalDetectionHypothesisId == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (eventHypothesisId != null ? eventHypothesisId.hashCode() : 0);
    result =
        31 * result + (signalDetectionHypothesisId != null ? signalDetectionHypothesisId.hashCode()
            : 0);
    result = 31 * result + (isRejected ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SignalDetectionEventAssociationDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", eventHypothesisId=" + eventHypothesisId +
        ", signalDetectionHypothesisId=" + signalDetectionHypothesisId +
        ", isRejected=" + isRejected +
        '}';
  }
}
