package gms.core.signaldetection.association.control.gacache;

import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.cache.Cache;
import javax.cache.Cache.Entry;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;


public class GACacheJCache implements GACache {

  private final Cache<UUID, SdhStationAssociation> sdhStationAssociationCache;
  private final Cache<UUID, EventHypothesis> eventHypothesisCache;
  private final Cache<UUID, ReferenceStation> referenceStationCache;


  public static GACacheJCache create(
      CacheManager cacheManager,
      Configuration<UUID, SdhStationAssociation> sdhStationAssociationCacheConfiguration,
      Configuration<UUID, EventHypothesis> eventHypothesisCacheConfiguration,
      Configuration<UUID, ReferenceStation> referenceStationCacheConfiguration
  ) {

    Objects.requireNonNull(cacheManager, "Null cacheManager");

    Cache<UUID, SdhStationAssociation> sdhStationAssociationCache = cacheManager
        .createCache("sdhStationAssociationCache", sdhStationAssociationCacheConfiguration);
    Cache<UUID, EventHypothesis> eventHypothesisCache = cacheManager
        .createCache("eventHypothesisCache", eventHypothesisCacheConfiguration);
    Cache<UUID, ReferenceStation> referenceStationCache = cacheManager
        .createCache("referenceStationCache", referenceStationCacheConfiguration);

    return new GACacheJCache(
        sdhStationAssociationCache,
        eventHypothesisCache,
        referenceStationCache
    );
  }


  private GACacheJCache(
      Cache<UUID, SdhStationAssociation> sdhStationAssociationCache,
      Cache<UUID, EventHypothesis> eventHypothesisCache,
      Cache<UUID, ReferenceStation> referenceStationCache
  ) {

    Objects.requireNonNull(sdhStationAssociationCache, "Null sdhStationAssociationCache");
    Objects.requireNonNull(eventHypothesisCache, "Null eventHypothesisCache");
    Objects.requireNonNull(referenceStationCache, "Null referenceStationCache");

    this.sdhStationAssociationCache = sdhStationAssociationCache;
    this.eventHypothesisCache = eventHypothesisCache;
    this.referenceStationCache = referenceStationCache;
  }


  @Override
  public boolean cacheSdhStationAssociation(SdhStationAssociation sdhStationAssociation) {

    Objects.requireNonNull(sdhStationAssociation, "Null sdhStationAssociation");

    SignalDetectionHypothesis signalDetectionHypothesis = sdhStationAssociation
        .getSignalDetectionHypothesis();

    UUID sdhId = signalDetectionHypothesis.getId();

    Objects.requireNonNull(sdhId, "Null sdhId");

    return this.sdhStationAssociationCache.putIfAbsent(sdhId, sdhStationAssociation);
  }


  @Override
  public Optional<SdhStationAssociation> getSdhStationAssociationBySdhId(
      UUID sdhId) {

    Objects.requireNonNull(sdhId, "Null sdhId");

    SdhStationAssociation sdhStationAssociation = this.sdhStationAssociationCache.get(sdhId);

    return Optional.ofNullable(sdhStationAssociation);
  }


  @Override
  public Set<SdhStationAssociation> getSdhStationAssociationByTimeRange(Instant beginning,
      Instant end) {

    Objects.requireNonNull(beginning, "Null beginning");
    Objects.requireNonNull(end, "Null end");

    return extractSdhStationAssociationsInTimeRangeFromCache(beginning, end);
  }


  @Override
  public boolean cacheReferenceStation(ReferenceStation referenceStation) {

    Objects.requireNonNull(referenceStation, "Null referenceStation");

    return this.referenceStationCache
        .putIfAbsent(referenceStation.getVersionId(), referenceStation);
  }


  @Override
  public Optional<ReferenceStation> getReferenceStationById(UUID referenceStationVersionId) {

    ReferenceStation referenceStation = this.referenceStationCache.get(referenceStationVersionId);

    return Optional.ofNullable(referenceStation);
  }


  @Override
  public boolean cacheEventHypothesis(EventHypothesis eventHypothesis) {

    Objects.requireNonNull(eventHypothesis, "Null eventHypothesis");

    return this.eventHypothesisCache.putIfAbsent(eventHypothesis.getId(), eventHypothesis);
  }


  @Override
  public Optional<EventHypothesis> getEventHypothesisById(UUID eventHypothesisId) {

    EventHypothesis eventHypothesis = this.eventHypothesisCache.get(eventHypothesisId);

    return Optional.ofNullable(eventHypothesis);
  }


  // Extracts all SdhStationAssociations from the cache that are at or between the provided time boundaries.
  private Set<SdhStationAssociation> extractSdhStationAssociationsInTimeRangeFromCache(
      Instant beginning, Instant end) {

    Objects.requireNonNull(beginning, "Null beginning");
    Objects.requireNonNull(end, "Null end");

    // Instantiate set to contain SdhStationAssociations that are at or between the provided
    // beginning and end times.
    Set<SdhStationAssociation> sdhStationAssociations = new HashSet<>();

    // Get an Iterator to iterate over every SdhStationAssociation entry in the cache.
    Iterator<Entry<UUID, SdhStationAssociation>> sdhStationAssociationCacheIterator = this.sdhStationAssociationCache
        .iterator();

    // Use the iterator to iterate over every SdhStationAssociation entry in the cache.  Adds
    // each SdhStationAssociation that is within the provided beginning and end times to the set.
    iterateAndAddSdhStationAssociationsWithinTimeRange(
        sdhStationAssociationCacheIterator,
        sdhStationAssociations,
        beginning,
        end
    );

    return sdhStationAssociations;
  }


  // Uses the provided iterator to iterate over the entire cache, adding SdhStationAssociations
  // that have arrival times at or between the provided beginning and end times.
  private void iterateAndAddSdhStationAssociationsWithinTimeRange(
      Iterator<Entry<UUID, SdhStationAssociation>> sdhStationAssociationCacheIterator,
      Set<SdhStationAssociation> sdhStationAssociations,
      Instant beginning,
      Instant end
  ) {

    Objects.requireNonNull(sdhStationAssociationCacheIterator,
        "Null sdhStationAssociationCacheIterator");
    Objects.requireNonNull(sdhStationAssociations, "Null sdhStationAssociations");
    Objects.requireNonNull(beginning, "Null beginning");
    Objects.requireNonNull(end, "Null end");

    // Loop over every SdhStationAssociation in the cache.
    while (sdhStationAssociationCacheIterator.hasNext()) {

      // Extract the cache entry the iterator currently points to.
      Entry<UUID, SdhStationAssociation> sdhStationAssociationEntry = sdhStationAssociationCacheIterator
          .next();

      // Add the current SdhStationAssociation to the set if it at or between the provided beginning
      // and end times.
      this.addSdhStationAssociationToSetIfInTimeRange(sdhStationAssociations,
          sdhStationAssociationEntry, beginning, end);
    }
  }


  // Adds the SdhStationAssociation contained in the provided sdhStationAssociationEntry
  // to the provided sdhStationAssociations set if its arrival time is at or between the
  // provided beginning and end times.
  private void addSdhStationAssociationToSetIfInTimeRange(
      Set<SdhStationAssociation> sdhStationAssociations,
      Entry<UUID, SdhStationAssociation> sdhStationAssociationEntry,
      Instant beginning,
      Instant end) {

    Objects.requireNonNull(sdhStationAssociations, "Null sdhStationAssociations");
    Objects.requireNonNull(sdhStationAssociationEntry, "Null sdhStationAssociationEntry");
    Objects.requireNonNull(beginning, "Null beginning");
    Objects.requireNonNull(end, "Null end");

    // Extract the arrival time Instant from the provided SdhStationAssociation.
    Instant sdhArrivalTime = this
        .getArrivalTimeFromSdhStationAssociationEntry(sdhStationAssociationEntry);

    // If the extracted arrival time is at or between the provided beginning and end times, add
    // the SdhStationAssociation to the set.
    if (this.arrivalTimeWithinRange(sdhArrivalTime, beginning, end)) {

      sdhStationAssociations.add(sdhStationAssociationEntry.getValue());
    }
  }


  // Retrieves the ARRIVAL_TIME value from the Signal Detection Hypothesis contained in the
  // SdhStationAssociation contained in the provided sdhStationAssociationEntry.
  private Instant getArrivalTimeFromSdhStationAssociationEntry(
      Entry<UUID, SdhStationAssociation> sdhStationAssociationEntry) {

    Objects.requireNonNull(sdhStationAssociationEntry, "Null sdhStationAssociationEntry");

    // Extract the SdhStationAssociation from the provided sdhStationAssociationEntry.
    SdhStationAssociation sdhStationAssociation = sdhStationAssociationEntry.getValue();

    // Extract the SignalDetectionHypothesis from the SdhStationAssociation.
    SignalDetectionHypothesis sdh = sdhStationAssociation.getSignalDetectionHypothesis();

    // Extract the ARRIVAL_TIME feature measurement from the SignalDetectionHypothesis.  Throws
    // IllegalStateException if the SignalDetectionHypothesis does not contain an ARRIVAL_TIME
    // feature measurement.
    FeatureMeasurement<InstantValue> sdhArrivalTimeInstantValue = sdh
        .getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME)
        .orElseThrow(() -> new IllegalStateException("Mandatory ARRIVAL_TIME not found"));

    // Extract and return the arrival time's Instant
    return sdhArrivalTimeInstantValue.getMeasurementValue().getValue();
  }


  // Checks whether the provided arrivalTime is equal to or between the provided beginning and end times.
  private boolean arrivalTimeWithinRange(Instant arrivalTime, Instant beginning, Instant end) {

    Objects.requireNonNull(arrivalTime, "Null arrivalTime");
    Objects.requireNonNull(beginning, "Null beginning");
    Objects.requireNonNull(end, "Null end");

    // Returns true if the arrival time is between or equal to the provided beginning or end times.
    return (arrivalTime.isAfter(beginning) && arrivalTime.isBefore(end) ||
        (arrivalTime.equals(beginning) || arrivalTime.equals(end)));
  }
}
