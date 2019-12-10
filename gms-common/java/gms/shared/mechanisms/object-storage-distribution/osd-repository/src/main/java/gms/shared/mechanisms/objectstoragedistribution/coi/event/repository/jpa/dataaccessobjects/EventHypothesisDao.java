package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "event_hypothesis")
public class EventHypothesisDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column(name = "event_id")
  private UUID eventId;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "parent_event_hypotheses", updatable = false)
  private Set<UUID> parentEventHypotheses;

  @Column(name = "is_rejected")
  private boolean isRejected;

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL)
  private Set<LocationSolutionDao> locationSolutions;

  @OneToOne(cascade = CascadeType.ALL)
  private PreferredLocationSolutionDao preferredLocationSolution;

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL)
  private Set<SignalDetectionEventAssociationDao> associations;

  /**
   * Default constructor for JPA.
   */
  public EventHypothesisDao() {
  }

  /**
   * Create a DAO from the COI object.
   */
  public EventHypothesisDao(EventHypothesis eventHypothesis) {

    Objects.requireNonNull(eventHypothesis, "Null eventHypothesis");

    this.id = eventHypothesis.getId();
    this.eventId = eventHypothesis.getEventId();
    this.parentEventHypotheses = eventHypothesis.getParentEventHypotheses();
    this.isRejected = eventHypothesis.isRejected();

    this.locationSolutions = eventHypothesis.getLocationSolutions().stream()
        .map(LocationSolutionDao::new).collect(Collectors.toSet());

    if (eventHypothesis.getPreferredLocationSolution().isPresent()) {

      this.preferredLocationSolution = new PreferredLocationSolutionDao();

      UUID preferredLocationSolutionId = eventHypothesis.getPreferredLocationSolution().get()
          .getLocationSolution().getId();

      Optional<LocationSolutionDao> optionalLocationSolutionDao = this.locationSolutions
          .stream().filter(ls ->
              ls.getEntityId().equals(preferredLocationSolutionId)
          ).reduce((a, b) -> {

                // This reduction lambda only gets called if there is more than one object in
                // the stream.  If there is more than one object in the stream, it means there
                // are LocationSolutionDaos with duplicate UUIDs, which should result in an exception.
                throw new IllegalStateException(
                    "Duplicate location solution UUIDs in set of location solution daos");
              }
          );

      if (optionalLocationSolutionDao.isPresent()) {

        this.preferredLocationSolution.setLocationSolution(optionalLocationSolutionDao.get());
      } else {
        // If a matching LocationSolutionDao was not found, we either have a malformed list of
        // LocationSolutionDaos or a malformed PreferredLocationSolution.
        throw new IllegalStateException(
            "Did not find matching location solution for given preferred location solution");
      }
    }

    this.associations = eventHypothesis.getAssociations().stream()
        .map(SignalDetectionEventAssociationDao::new)
        .collect(Collectors.toSet());
  }

  /**
   * Create a COI object from the DAO.
   */
  public EventHypothesis toCoi() {
    Validate.notNull(preferredLocationSolution);
    return EventHypothesis.from(this.id, this.eventId, this.parentEventHypotheses, this.isRejected,
        this.locationSolutions.stream().map(LocationSolutionDao::toCoi)
            .collect(Collectors.toSet()), this.preferredLocationSolution.toCoi(),
        this.associations.stream().map(SignalDetectionEventAssociationDao::toCoi)
            .collect(Collectors.toSet()));
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getEventId() {
    return eventId;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }

  public Set<UUID> getParentEventHypotheses() {
    return parentEventHypotheses;
  }

  public void setParentEventHypotheses(Set<UUID> parentEventHypotheses) {
    this.parentEventHypotheses = parentEventHypotheses;
  }

  public boolean isRejected() {
    return isRejected;
  }

  public void setRejected(boolean rejected) {
    isRejected = rejected;
  }

  public Set<LocationSolutionDao> getLocationSolutions() {
    return locationSolutions;
  }

  public void setLocationSolutions(
      Set<LocationSolutionDao> locationSolutions) {
    this.locationSolutions = locationSolutions;
  }

  public PreferredLocationSolutionDao getPreferredLocationSolution() {
    return preferredLocationSolution;
  }

  public void setPreferredLocationSolution(
      PreferredLocationSolutionDao preferredLocationSolution) {
    this.preferredLocationSolution = preferredLocationSolution;
  }

  public Set<SignalDetectionEventAssociationDao> getAssociations() {
    return associations;
  }

  public void setAssociations(
      Set<SignalDetectionEventAssociationDao> associations) {
    this.associations = associations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EventHypothesisDao that = (EventHypothesisDao) o;

    if (isRejected != that.isRejected) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (eventId != null ? !eventId.equals(that.eventId) : that.eventId != null) {
      return false;
    }
    if (parentEventHypotheses != null ? !parentEventHypotheses.equals(that.parentEventHypotheses)
        : that.parentEventHypotheses != null) {
      return false;
    }
    if (locationSolutions != null ? !locationSolutions.equals(that.locationSolutions)
        : that.locationSolutions != null) {
      return false;
    }
    if (preferredLocationSolution != null ? !preferredLocationSolution
        .equals(that.preferredLocationSolution) : that.preferredLocationSolution != null) {
      return false;
    }
    return associations != null ? associations.equals(that.associations)
        : that.associations == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (eventId != null ? eventId.hashCode() : 0);
    result = 31 * result + (parentEventHypotheses != null ? parentEventHypotheses.hashCode() : 0);
    result = 31 * result + (isRejected ? 1 : 0);
    result = 31 * result + (locationSolutions != null ? locationSolutions.hashCode() : 0);
    result =
        31 * result + (preferredLocationSolution != null ? preferredLocationSolution.hashCode()
            : 0);
    result = 31 * result + (associations != null ? associations.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EventHypothesisDao{" +
        "id=" + id +
        ", eventId=" + eventId +
        ", parentEventHypotheses=" + parentEventHypotheses +
        ", isRejected=" + isRejected +
        ", locationSolutions=" + locationSolutions +
        ", preferredLocationSolution=" + preferredLocationSolution +
        ", associations=" + associations +
        '}';
  }
}
