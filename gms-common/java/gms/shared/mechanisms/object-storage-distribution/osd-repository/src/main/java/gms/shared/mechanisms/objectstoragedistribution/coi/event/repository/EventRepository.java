package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface EventRepository {

  /**
   * Stores a collection of events.  The Event's should all be new (never existed before).
   * @param events events to store
   */
  void storeEvents(Collection<Event> events, Collection<? super Event> outErrorEvents);

  /**
   * Updates a collection of events that already exist in the database
   *
   * @param events events to update
   */
  void updateEvents(Collection<Event> events, Collection<? super Event> outErrorEvents);

  /**
   * Finds events by their primary id's.
   * @param eventIds the id's to search for
   * @return a collection of event's with the given id's, may be empty
   */
  Collection<Event> findEventsByIds(Collection<UUID> eventIds);

  /**
   * Finds events by time range and location.  To be included in the results,
   * the event has to have a preferred hypothesis that matches the query params
   * (is in the requested location and time range)
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @param minLatitude the minimum latitude
   * @param maxLatitude the maximum latitude
   * @param minLongitude the minimum longitude
   * @param maxLongitude the maximum longitude
   * @return events that are in the given time range and location, may be empty
   */
  //TODO: should this have another signature excluding the optional params?
  Collection<Event> findEventsByTimeAndLocation(Instant startTime, Instant endTime,
      double minLatitude, double maxLatitude, double minLongitude, double maxLongitude);
}
