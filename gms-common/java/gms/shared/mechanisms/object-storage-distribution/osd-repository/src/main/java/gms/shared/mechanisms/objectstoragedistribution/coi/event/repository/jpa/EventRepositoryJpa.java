package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FinalEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.EventRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects.EventDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects.EventHypothesisDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects.FinalEventHypothesisDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects.PreferredEventHypothesisDao;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventRepositoryJpa implements EventRepository {

  private static final Logger logger = LoggerFactory.getLogger(EventRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  public EventRepositoryJpa() {
    this(CoiEntityManagerFactory.create());
  }

  public EventRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
  }

  @Override
  public void storeEvents(Collection<Event> events, Collection<? super Event> outErrorEvents) {
    Objects.requireNonNull(events, "Cannot store null events");
    if (events.isEmpty()) {
      return;  // nothing to do
    }
    final Set<UUID> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
    final Set<UUID> existingIds = findEventsByIds(eventIds).stream().map(Event::getId)
        .collect(Collectors.toSet());

    final EntityManager em = entityManagerFactory.createEntityManager();

    for (Event e : events) {
      try {
        if (existingIds.contains(e.getId())) {
          throw new DataExistsException("Events with ids " + existingIds + " already stored");
        }
        em.getTransaction().begin();
        em.persist(new EventDao(e));
        em.getTransaction().commit();
      } catch (Exception ex) {
        logger.error("For event " + e.toString());
        logger.error("Exception trying to store Event", ex);
        outErrorEvents.add(e);
        em.getTransaction().rollback();
      }
    }
    em.close();
  }

  @Override
  public void updateEvents(Collection<Event> events, Collection<? super Event> outErrorEvents) {
    Objects.requireNonNull(events, "Cannot update null events");
    if (events.isEmpty()) {
      return;  // nothing to do
    }

    // Gather a list of event UUIDs.
    final Set<UUID> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

    // Gather the corresponding EventDao objects.
    final Collection<EventDao> existingEvents = findEventDaosByIds(eventIds);

    // Determine which Event objects already exist in the database.
    final Collection<UUID> existingEventUUIDs = existingEvents.stream()
        .map(EventDao::getId)
        .collect(Collectors.toSet());

    final EntityManager em = entityManagerFactory.createEntityManager();

    // Update the existing events.
    try {
      // Check that all of the Event objects passed in exist in the database.
      if (!existingEventUUIDs.containsAll(eventIds) || !eventIds.containsAll(existingEventUUIDs)) {
        throw new IllegalStateException("Trying to update more events than are in the database");
      }

      List<EventDao> mergedEvents = new ArrayList<>();

      // Loop through each EventDao.
      for (EventDao existingEventFromOsd : existingEvents) {

        // Retrieve the user-updated Event that corresponds to this EventDao.
        Optional<Event> updatedEventFromUserOptional = events.stream()
            .filter(x -> x.getId().equals(existingEventFromOsd.getId()))
            .findFirst();
        if (!updatedEventFromUserOptional.isPresent()) {
          throw new IllegalStateException("Corresponding EventDao does not exist.");
        }
        Event updatedEventFromUser = updatedEventFromUserOptional.get();

        // Merge the existing and updated event objects.
        mergeEventDao(existingEventFromOsd, updatedEventFromUser, outErrorEvents);

        // Add merged event.
        mergedEvents.add(existingEventFromOsd);
      }

      // Store in the database.
      // Insert the merged events
      for (EventDao e : mergedEvents) {
        try {
          em.getTransaction().begin();
          em.merge(e);
          em.getTransaction().commit();
        } catch (Exception ex) {
          logger.error(String.format("For event %s", e.toString()));
          logger.error("Exception trying to store Event", ex);
          outErrorEvents.add(events.stream()
              .filter(event -> event.getId().equals(e.getId()))
              .iterator().next());
        }
      }
    } catch (Exception ex) {
      logger.error("Exception trying to store Events", ex);
      outErrorEvents.addAll(events);
    } finally {
      em.close();
    }
  }

  /**
   * Merges differences between a user-updated Event and its corresponding existing EventDao.
   *
   * @param existingEventFromOsd existing EventDao queried from the database
   * @param updatedEventFromUser updated Event provided by the user
   * @return Merged event
   */
  private void mergeEventDao(
      EventDao existingEventFromOsd,
      Event updatedEventFromUser,
      Collection<? super Event> outErrorEvents
  ) {
    // Check that the monitoring organizations match.
    if (!existingEventFromOsd.getMonitoringOrganization()
        .equals(updatedEventFromUser.getMonitoringOrganization())) {
      throw new IllegalStateException(
          "Existing event and updated event do not share the same 'monitoring organization' value.");
    }

    // Create the set of merged Rejected Signal Detection Associations.
    Set<UUID> existingRejectedSignalDetectionAssociations = existingEventFromOsd
        .getRejectedSignalDetectionAssociations();
    existingRejectedSignalDetectionAssociations
        .addAll(updatedEventFromUser.getRejectedSignalDetectionAssociations());

    // Create the set of merged Hypotheses.
    Set<EventHypothesisDao> existingEventHypothesisDaos = existingEventFromOsd.getHypotheses();
    Set<EventHypothesis> updatedEventHypotheses = updatedEventFromUser.getHypotheses();

    // Determine which EventHypotheses are new
    Set<EventHypothesis> newEventHypotheses = updatedEventHypotheses.stream().map(eh -> {

          // Filter for existing EventHypothesisDao that matches the given EventHypothesis.  If the
          // returned optional EventHypothesisDao is empty, it means no existing EventHypothesisDao
          // was found that matches the given EventHypothesis.  The given EventHypothesis must be new.
          Optional<EventHypothesisDao> optionalEventHypothesisDao = existingEventHypothesisDaos.stream()
              .filter(ehDao ->
                  eh.getId().equals(ehDao.getId())
              ).reduce((a, b) -> {
                    throw new IllegalStateException(
                        "Found multiple EventHypothesis objects with the same UUID.");
                  }
              );

          // If we found an EventHypothesisDao that matches the given EventHypothesis, the given
          // EventHypothesis is not new.  Otherwise, it's new, so return it.
          if (optionalEventHypothesisDao.isPresent()) {

            return null;
          } else {

            return eh;
          }
        }
    ).filter(Objects::nonNull).collect(Collectors.toSet());

    // Create new EventHypothesisDaos and add them to the existing daos
    existingEventHypothesisDaos.addAll(
        newEventHypotheses.stream().map(EventHypothesisDao::new).collect(Collectors.toSet())
    );

    // Create the list of merged Final Event Hypothesis History objects.

    List<FinalEventHypothesisDao> existingFinalEventHypothesisDaos = existingEventFromOsd
        .getFinalEventHypothesisHistory();
    List<FinalEventHypothesis> updatedFinalEventHypotheses = updatedEventFromUser
        .getFinalEventHypothesisHistory();

    // Determine which FinalEventHypotheses are new
    List<FinalEventHypothesis> newFinalEventHypotheses = updatedFinalEventHypotheses.stream()
        .map(eh -> {

              // Filter for existing EventHypothesisDao that matches the given EventHypothesis.  If the
              // returned optional EventHypothesisDao is empty, it means no existing EventHypothesisDao
              // was found that matches the given EventHypothesis.  The given EventHypothesis must be new.
              Optional<FinalEventHypothesisDao> optionalFinalEventHypothesisDao = existingFinalEventHypothesisDaos
                  .stream()
                  .filter(ehDao ->
                      eh.getEventHypothesis().getId().equals(ehDao.getEventHypothesis().getId())
                  ).reduce((a, b) -> {
                        throw new IllegalStateException(
                            "Found multiple FinalEventHypothesis objects with the same UUID.");
                      }
                  );

              // If we found an EventHypothesisDao that matches the given EventHypothesis, the given
              // EventHypothesis is not new.  Otherwise, it's new, so return it.
              if (optionalFinalEventHypothesisDao.isPresent()) {

                return null;
              } else {

                return eh;
              }
            }
        ).filter(Objects::nonNull).collect(Collectors.toList());

    // Create new EventHypothesisDaos and add them to the existing daos
    existingFinalEventHypothesisDaos.addAll(
        newFinalEventHypotheses.stream().map(finalEh -> {

              // Filter for the EventHypothesisDao that corresponds with the current FinalEventHypothesis.
              // This optional will be empty if a corresponding EventHypothesisDao is not found.
              Optional<EventHypothesisDao> optionalEventHypothesisDao = existingEventHypothesisDaos
                  .stream()
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
                return new FinalEventHypothesisDao(optionalEventHypothesisDao.get());
              }
            }
        ).collect(Collectors.toList())
    );

    // Create the list of merged Preferred Event Hypothesis History objects.

    List<PreferredEventHypothesisDao> existingPreferredEventHypothesisDaos = existingEventFromOsd
        .getPreferredEventHypothesisHistory();
    List<PreferredEventHypothesis> updatedPreferredEventHypotheses = updatedEventFromUser
        .getPreferredEventHypothesisHistory();

    // Determine which FinalEventHypotheses are new
    List<PreferredEventHypothesis> newPreferredEventHypotheses = updatedPreferredEventHypotheses
        .stream()
        .map(eh -> {

              // Filter for existing EventHypothesisDao that matches the given EventHypothesis.  If the
              // returned optional EventHypothesisDao is empty, it means no existing EventHypothesisDao
              // was found that matches the given EventHypothesis.  The given EventHypothesis must be new.
              Optional<PreferredEventHypothesisDao> optionalPreferredEventHypothesisDao = existingPreferredEventHypothesisDaos
                  .stream()
                  .filter(ehDao ->
                      eh.getEventHypothesis().getId().equals(ehDao.getEventHypothesis().getId())
                  ).reduce((a, b) -> {
                        throw new IllegalStateException(
                            "Found multiple PreferredEventHypothesis objects with the same UUID.");
                      }
                  );

              // If we found an EventHypothesisDao that matches the given EventHypothesis, the given
              // EventHypothesis is not new.  Otherwise, it's new, so return it.
              if (optionalPreferredEventHypothesisDao.isPresent()) {

                return null;
              } else {

                return eh;
              }
            }
        ).filter(Objects::nonNull).collect(Collectors.toList());

    // Create new EventHypothesisDaos and add them to the existing daos
    existingPreferredEventHypothesisDaos.addAll(
        newPreferredEventHypotheses.stream().map(prefEh -> {

              // Filter for the EventHypothesisDao that corresponds with the current FinalEventHypothesis.
              // This optional will be empty if a corresponding EventHypothesisDao is not found.
              Optional<EventHypothesisDao> optionalEventHypothesisDao = existingEventHypothesisDaos
                  .stream()
                  .filter(ehDao ->

                      ehDao.getId().equals(prefEh.getEventHypothesis().getId())
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
                return new PreferredEventHypothesisDao(optionalEventHypothesisDao.get(),
                    prefEh.getProcessingStageId());
              }
            }
        ).collect(Collectors.toList())
    );

    // Create the merged EventDao object.
  }

  @Override
  public Collection<Event> findEventsByIds(Collection<UUID> eventIds) {
    Objects.requireNonNull(eventIds, "Cannot find events by null or empty eventIds");
    final EntityManager em = entityManagerFactory.createEntityManager();
    try {
      final String query = "SELECT DISTINCT e FROM " + EventDao.class.getSimpleName()
          + " e WHERE e.id IN :ids";
      final List<EventDao> daos = em.createQuery(query, EventDao.class)
          .setParameter("ids", eventIds)
          .getResultList();
      return daos.stream()
          .map(EventDao::toCoi)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      logger.error("Exception trying to store find Events by ids", ex);
      throw new RuntimeException(ex);
    } finally {
      em.close();
    }
  }

  @Override
  public Collection<Event> findEventsByTimeAndLocation(Instant startTime, Instant endTime,
      double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {

    Objects.requireNonNull(startTime,
        "Cannot find events by time and location with null start time");
    Objects.requireNonNull(endTime,
        "Cannot find events by time and location with null end time");

    final EntityManager em = entityManagerFactory.createEntityManager();
    try {
      final String query = "SELECT e FROM " + EventDao.class.getSimpleName()
          + " e INNER JOIN e.hypotheses as ehs"
          + " INNER JOIN ehs.preferredLocationSolution.locationSolution as ls"
          + " INNER JOIN ls.location as loc"
          + " WHERE loc.latitudeDegrees >= :minLat AND loc.latitudeDegrees <= :maxLat"
          + " AND loc.longitudeDegrees >= :minLon AND loc.longitudeDegrees <= :maxLon"
          + " AND loc.time >= :startTime AND loc.time <= :endTime";
      final List<EventDao> daos = em.createQuery(query, EventDao.class)
          .setParameter("minLat", minLatitude)
          .setParameter("maxLat", maxLatitude)
          .setParameter("minLon", minLongitude)
          .setParameter("maxLon", maxLongitude)
          .setParameter("startTime", startTime)
          .setParameter("endTime", endTime)
          .getResultList();
      return daos.stream()
          .map(EventDao::toCoi)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      logger.error("Exception trying to find Events by time/lat/lon", ex);
      throw new RuntimeException(ex);
    } finally {
      em.close();
    }
  }

  private Collection<EventDao> findEventDaosByIds(Collection<UUID> eventIds) {
    Objects.requireNonNull(eventIds, "Cannot find events by null or empty eventIds");
    final EntityManager em = entityManagerFactory.createEntityManager();
    try {
      final String query = "SELECT DISTINCT e FROM " + EventDao.class.getSimpleName()
          + " e WHERE e.id IN :ids";
      return em.createQuery(query, EventDao.class)
          .setParameter("ids", eventIds)
          .getResultList();
    } catch (Exception ex) {
      logger.error("Exception trying to store find Events by ids", ex);
      throw new RuntimeException(ex);
    } finally {
      em.close();
    }
  }
}
