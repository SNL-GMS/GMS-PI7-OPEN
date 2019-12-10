package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;


/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis}
 * to allow read and write access to the relational database.
 */
@Entity
@Table(name = "preferred_event_hypothesis")

public class PreferredEventHypothesisDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(name = "processing_stage_id", nullable = false)
  private UUID processingStageId;

  @OneToOne(cascade = CascadeType.ALL)
  private EventHypothesisDao eventHypothesis;

  /**
   * Default constructor for JPA
   */
  public PreferredEventHypothesisDao() {
  }

  /**
   * Create a DAO from the COI object.
   *
   * @param preferredEventHypothesis preferred event hypothesis
   */
  public PreferredEventHypothesisDao(PreferredEventHypothesis preferredEventHypothesis) {
    Validate.notNull(preferredEventHypothesis);
    this.processingStageId = preferredEventHypothesis.getProcessingStageId();
    this.eventHypothesis = new EventHypothesisDao(preferredEventHypothesis.getEventHypothesis());
  }

  public PreferredEventHypothesisDao(EventHypothesisDao eventHypothesisDao,
      UUID processingStageId) {

    Objects.requireNonNull(eventHypothesisDao, "Null eventHypothesisDao");
    Objects.requireNonNull(processingStageId, "Null processingStageId");

    this.eventHypothesis = eventHypothesisDao;
    this.processingStageId = processingStageId;
  }

  /**
   * Convert this DAO into its corresponding COI object.
   *
   * @return A PreferredEventHypothesis COI object.
   */
  public PreferredEventHypothesis toCoi() {
    return PreferredEventHypothesis.from(getProcessingStageId(),
        getEventHypothesis().toCoi());
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getProcessingStageId() {
    return processingStageId;
  }

  public void setProcessingStageId(UUID processingStageId) {
    this.processingStageId = processingStageId;
  }

  public EventHypothesisDao getEventHypothesis() {
    return eventHypothesis;
  }

  public void setEventHypothesis(
      EventHypothesisDao eventHypothesis) {
    this.eventHypothesis = eventHypothesis;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PreferredEventHypothesisDao)) {
      return false;
    }

    PreferredEventHypothesisDao that = (PreferredEventHypothesisDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
    if (processingStageId != null ? !processingStageId.equals(that.processingStageId)
        : that.processingStageId != null) {
      return false;
    }
    return eventHypothesis != null ? eventHypothesis.equals(that.eventHypothesis)
        : that.eventHypothesis == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (processingStageId != null ? processingStageId.hashCode() : 0);
    result = 31 * result + (eventHypothesis != null ? eventHypothesis.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PreferredEventHypothesisDao{" +
        "primaryKey=" + primaryKey +
        ", processingStageId=" + processingStageId +
        ", eventHypothesis=" + eventHypothesis +
        '}';
  }
}
