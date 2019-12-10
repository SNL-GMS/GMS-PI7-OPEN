package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FinalEventHypothesis;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;

/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FinalEventHypothesis}
 * to allow read and write access to the relational database.
 */
@Entity
@Table(name = "final_event_hypothesis")

public class FinalEventHypothesisDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @OneToOne(cascade = CascadeType.ALL)
  private EventHypothesisDao eventHypothesis;

  /**
   * Default constructor for JPA
   */
  public FinalEventHypothesisDao() {
  }

  ;

  /**
   * Create a DAO from the COI object.
   *
   * @param finalEventHypothesis final Event Hypothesis object.
   */
  public FinalEventHypothesisDao(FinalEventHypothesis finalEventHypothesis) {
    Validate.notNull(finalEventHypothesis);
    this.eventHypothesis = new EventHypothesisDao(
        finalEventHypothesis.getEventHypothesis());
  }

  public FinalEventHypothesisDao(EventHypothesisDao eventHypothesisDao) {

    Objects.requireNonNull(eventHypothesisDao, "Null eventHypothesisDao");

    this.eventHypothesis = eventHypothesisDao;
  }

  /**
   * Convert this DAO into its corresponding COI object.
   *
   * @return A FinalEventHypothesis COI object.
   */
  public FinalEventHypothesis toCoi() {
    return FinalEventHypothesis.from(getEventHypothesis().toCoi());
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
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
    if (!(o instanceof FinalEventHypothesisDao)) {
      return false;
    }

    FinalEventHypothesisDao that = (FinalEventHypothesisDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
    return eventHypothesis != null ? eventHypothesis.equals(that.eventHypothesis)
        : that.eventHypothesis == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (eventHypothesis != null ? eventHypothesis.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "FinalEventHypothesisDao{" +
        "primaryKey=" + primaryKey +
        ", eventHypothesis=" + eventHypothesis +
        '}';
  }
}
