package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FinalEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;


/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event}
 */
@Entity
@Table(name = "event")
public class EventDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(updatable = false, unique = true)
  private UUID id;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "rejected_signal_detection_associations", updatable = false)
  private Set<UUID> rejectedSignalDetectionAssociations;

  @Column(name = "monitoring_organization")
  private String monitoringOrganization;

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL)
  private Set<EventHypothesisDao> hypotheses;

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL)
  private List<FinalEventHypothesisDao> finalEventHypothesisHistory;

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL)
  private List<PreferredEventHypothesisDao> preferredEventHypothesisHistory;

  /**
   * No-arg constructor for use by JPA.
   */
  public EventDao() {
  }

  /**
   * Create a DAO from the COI object.
   */
  public EventDao(Event event) {
    Objects.requireNonNull(event);
    this.id = event.getId();
    this.rejectedSignalDetectionAssociations = event.getRejectedSignalDetectionAssociations();
    this.monitoringOrganization = event.getMonitoringOrganization();

    List<FinalEventHypothesisDao> finalEventHypothesisDaos = new ArrayList<>();
    List<PreferredEventHypothesisDao> preferredEventHypothesisDaos = new ArrayList<>();

    // Convert EventHypothesis objects to EventHypothesisDaos.  We will create
    // FinalEventHypothesisDaos and PreferredEventHypothesisDaos from references
    // to these EventHypothesisDaos.
    this.hypotheses = convertSet(event.getHypotheses(), EventHypothesisDao::new);

    // Loop through each FinalEventHypothesis in the provided event, and create a
    // FinalEventHypothesisDao with a reference to the corresponding EventHypothesisDao, which
    // has already been created.  Creating new, duplicate EventHypothesisDaos here would result in
    // duplicate rows being stored in the EventHypothesis table, even though the EventHypothesisDaos
    // all have the same contents.  They must all be the same reference to avoid such duplication.
    event.getFinalEventHypothesisHistory().forEach(finalEh -> {

          // Filter for the EventHypothesisDao that corresponds with the current FinalEventHypothesis.
          // This optional will be empty if a corresponding EventHypothesisDao is not found.
          Optional<EventHypothesisDao> optionalEventHypothesisDao = this.hypotheses.stream()
              .filter(ehDao ->

                  ehDao.getId().equals(finalEh.getEventHypothesis().getId())
              ).reduce((a, b) -> {

                    // This reduction lambda only gets called if there is more than one object in
                    // the stream.  If there is more than one object in the stream, it means there
                    // are EventHypothesisDaos with duplicate UUIDs, which should result in an exception.
                    throw new IllegalStateException(
                        "Duplicate event hypothesis UUIDs in set of event hypothesis daos");
                  }
              );

          if (!optionalEventHypothesisDao.isPresent()) {

            // If a matching EventHypothesisDao was not found, we have malformed lists of
            // either EventHypothesisDaos or FinalEventHypotheses.
            throw new IllegalStateException(
                "Did not find matching event hypothesis for given final event hypothesis");
          } else {

            // Create a FinalEventHypothesisDao with a reference to the corresponding EventHypothesisDao,
            // which has already been created.
            finalEventHypothesisDaos.add(new FinalEventHypothesisDao(optionalEventHypothesisDao.get()));
          }
        }
    );

    // Loop through each PreferredEventHypothesis in the provided event, and create a
    // PreferredEventHypothesisDao with a reference to the corresponding EventHypothesisDao, which
    // has already been created.  Creating new, duplicate EventHypothesisDaos here would result in
    // duplicate rows being stored in the EventHypothesis table, even though the EventHypothesisDaos
    // all have the same contents.  They must all be the same reference to avoid such duplication.
    event.getPreferredEventHypothesisHistory().forEach(eh -> {

          // Filter for the EventHypothesisDao that corresponds with the current PreferredEventHypothesis.
          // This optional will be empty if a corresponding EventHypothesisDao is not found.
          Optional<PreferredEventHypothesisDao> optionalPreferredEventHypothesisDao = this.hypotheses
              .stream()
              .filter(ehdao ->

                  ehdao.getId().equals(eh.getEventHypothesis().getId())
              ).map(ehdao ->

                  new PreferredEventHypothesisDao(ehdao, eh.getProcessingStageId())
              ).reduce((a, b) -> {

                    // This reduction lambda only gets called if there is more than one object in
                    // the stream.  If there is more than one object in the stream, it means there
                    // are EventHypothesisDaos with duplicate UUIDs, which should result in an exception.
                    throw new IllegalStateException(
                        "Duplicate event hypothesis UUIDs in set of event hypothesis daos");
                  }
              );

          if (!optionalPreferredEventHypothesisDao.isPresent()) {

            // If a matching EventHypothesisDao was not found, we have malformed lists of
            // either EventHypothesisDaos or PreferredEventHypotheses.
            throw new IllegalStateException(
                "Did not find matching event hypothesis for given preferred event hypothesis");
          } else {

            // Create a PreferredEventHypothesisDao with a reference to the corresponding
            // EventHypothesisDao, which has already been created.
            preferredEventHypothesisDaos
                .add(optionalPreferredEventHypothesisDao.get());
          }
        }
    );

    // The list of FinalEventHypothesisDaos has been created succesfully.  Assign it to this
    // objects finalEventHypothesisHistory.
    this.finalEventHypothesisHistory = finalEventHypothesisDaos;

    // The list of PreferredEventHypothesisDaos has been created succesfully.  Assign it to this
    // objects preferredEventHypothesisHistory.
    this.preferredEventHypothesisHistory = preferredEventHypothesisDaos;
  }

  /**
   * Create a COI from this DTO.
   *
   * @return an Event object.
   */
  public Event toCoi() {
    final Set<EventHypothesis> hypothesisSet =
        convertSet(this.hypotheses, EventHypothesisDao::toCoi);
    final List<FinalEventHypothesis> finalHistory = convertList(
        this.finalEventHypothesisHistory, FinalEventHypothesisDao::toCoi);
    final List<PreferredEventHypothesis> preferredHistory = convertList(
        this.preferredEventHypothesisHistory, PreferredEventHypothesisDao::toCoi);
    return Event.from(this.id, this.rejectedSignalDetectionAssociations,
        this.monitoringOrganization, hypothesisSet, finalHistory, preferredHistory);
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

  public Set<UUID> getRejectedSignalDetectionAssociations() {
    return rejectedSignalDetectionAssociations;
  }

  public void setRejectedSignalDetectionAssociations(
      Set<UUID> rejectedSignalDetectionAssociations) {
    this.rejectedSignalDetectionAssociations = rejectedSignalDetectionAssociations;
  }

  public String getMonitoringOrganization() {
    return monitoringOrganization;
  }

  public void setMonitoringOrganization(String monitoringOrganization) {
    this.monitoringOrganization = monitoringOrganization;
  }

  public Set<EventHypothesisDao> getHypotheses() {
    return hypotheses;
  }

  public void setHypotheses(
      Set<EventHypothesisDao> hypotheses) {
    this.hypotheses = hypotheses;
  }

  public List<FinalEventHypothesisDao> getFinalEventHypothesisHistory() {
    return finalEventHypothesisHistory;
  }

  public void setFinalEventHypothesisHistory(
      List<FinalEventHypothesisDao> finalEventHypothesisHistory) {
    this.finalEventHypothesisHistory = finalEventHypothesisHistory;
  }

  public List<PreferredEventHypothesisDao> getPreferredEventHypothesisHistory() {
    return preferredEventHypothesisHistory;
  }

  public void setPreferredEventHypothesisHistory(
      List<PreferredEventHypothesisDao> preferredEventHypothesisHistory) {
    this.preferredEventHypothesisHistory = preferredEventHypothesisHistory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventDao eventDao = (EventDao) o;
    return getPrimaryKey() == eventDao.getPrimaryKey() &&
        Objects.equals(getId(), eventDao.getId()) &&
        Objects.equals(getRejectedSignalDetectionAssociations(),
            eventDao.getRejectedSignalDetectionAssociations()) &&
        Objects.equals(getMonitoringOrganization(), eventDao.getMonitoringOrganization()) &&
        Objects.equals(getHypotheses(), eventDao.getHypotheses()) &&
        Objects
            .equals(getFinalEventHypothesisHistory(), eventDao.getFinalEventHypothesisHistory()) &&
        Objects.equals(getPreferredEventHypothesisHistory(),
            eventDao.getPreferredEventHypothesisHistory());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPrimaryKey(), getId(), getRejectedSignalDetectionAssociations(),
        getMonitoringOrganization(), getHypotheses(), getFinalEventHypothesisHistory(),
        getPreferredEventHypothesisHistory());
  }

  @Override
  public String toString() {
    return "EventDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", rejectedSignalDetectionAssociations=" + rejectedSignalDetectionAssociations +
        ", monitoringOrganization='" + monitoringOrganization + '\'' +
        ", hypotheses=" + hypotheses +
        ", finalEventHypothesisHistory=" + finalEventHypothesisHistory +
        ", preferredEventHypothesisHistory=" + preferredEventHypothesisHistory +
        '}';
  }

  private static <A, B> List<B> convertList(List<A> as, Function<A, B> f) {
    return as.stream().map(f).collect(Collectors.toList());
  }

  private static <A, B> Set<B> convertSet(Set<A> as, Function<A, B> f) {
    return as.stream().map(f).collect(Collectors.toSet());
  }
}
