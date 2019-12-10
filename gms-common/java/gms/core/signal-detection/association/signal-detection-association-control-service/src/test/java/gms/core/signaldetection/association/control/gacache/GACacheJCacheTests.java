package gms.core.signaldetection.association.control.gacache;

import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class GACacheJCacheTests {

  // GACache to use for testing storage and retrieval cache operations
  private GACache gaCache;

  // CacheManager used to create the GACache and close the cache after finishing
  private CacheManager cacheManager;

  @BeforeEach
  void init() {

    // Get JCache provider
    CachingProvider cachingProvider = Caching.getCachingProvider();

    // Get CacheManager from cachingProvider
    this.cacheManager = cachingProvider.getCacheManager();

    // Create configuration for SdhStationAssociation cache
    MutableConfiguration<UUID, SdhStationAssociation> sdhStationAssociationCacheConfig = new MutableConfiguration<>();
    sdhStationAssociationCacheConfig.setStoreByValue(false);

    // Create configuration for EventHypothesis cache
    MutableConfiguration<UUID, EventHypothesis> eventHypothesisCacheConfig = new MutableConfiguration<>();
    eventHypothesisCacheConfig.setStoreByValue(false);

    // Create configuration for ReferenceStation cache
    MutableConfiguration<UUID, ReferenceStation> referenceStationCacheConfig = new MutableConfiguration<>();
    referenceStationCacheConfig.setStoreByValue(false);

    // Create the GACache using the previously created CacheManager and default configuration
    this.gaCache = GACacheJCache.create(
        cacheManager,
        sdhStationAssociationCacheConfig,
        eventHypothesisCacheConfig,
        referenceStationCacheConfig
    );
  }

  @AfterEach
  void teardown() {

    // Close the cache after each test
    this.cacheManager.close();
  }

  @Test
  void testStoreAndRetrieveSdhStationAssociationBySdhId() {

    // Create SignalDetectionHypothesis to use for SdhStationAssociation
    SignalDetectionHypothesis sdh = SignalDetectionHypothesis.create(
        UUID.randomUUID(),
        FeatureMeasurement.create(
            UUID.randomUUID(),
            FeatureMeasurementTypes.ARRIVAL_TIME,
            InstantValue.from(
                Instant.EPOCH,
                Duration.ZERO
            )
        ),
        FeatureMeasurement.create(
            UUID.randomUUID(),
            FeatureMeasurementTypes.PHASE,
            PhaseTypeMeasurementValue.from(
                PhaseType.P,
                0.0
            )
        ),
        UUID.randomUUID()
    );

    // Create ReferenceStation to use for SdhStationAssociation
    ReferenceStation referenceStation = ReferenceStation.create(
        "DINGO DAN",
        "RANGO RON",
        StationType.SeismicArray,
        InformationSource.create(
            "DANGO DEB",
            Instant.EPOCH,
            "DONGO DARREL"
        ),
        "JANGO JIM",
        0.1,
        2.3,
        4.5,
        Instant.EPOCH,
        Instant.EPOCH,
        List.of()
    );

    // Create SdhStationAssociation to store in the cache
    SdhStationAssociation sdhStationAssociation = SdhStationAssociation
        .from(sdh, referenceStation);

    // Get the SignalDetectionHypothesis' id to use to check that the correct SdhStationAssociation
    // object was retrieved from the cache.
    UUID sdhId = sdh.getId();

    // Store the mock SdhStationAssociation in the cache
    boolean wasStored = this.gaCache.cacheSdhStationAssociation(sdhStationAssociation);

    // Assert that the mock SdhStationAssociation was successfully stored in the cache
    Assertions.assertTrue(wasStored);

    // Retrieve the SdhStationAssociation from the cache.  Throw IllegalStateException if not found.
    SdhStationAssociation retrievedSdhStationAssociation = gaCache
        .getSdhStationAssociationBySdhId(sdhId)
        .orElseThrow(() -> new IllegalStateException(
            "Expected optional SdhStationAssociation to not be empty"));

    // Assert we got the correct SdhStationAssociation from the cache
    Assertions
        .assertEquals(sdhId, retrievedSdhStationAssociation.getSignalDetectionHypothesis().getId());
  }


  @Test
  void testStoreAndRetrieveSdhStationAssociationByTimeRange() {

    // Create SignalDetectionHypothesis 1 to use for first SdhStationAssociation
    SignalDetectionHypothesis sdh1 = SignalDetectionHypothesis.create(
        UUID.randomUUID(),
        FeatureMeasurement.create(
            UUID.randomUUID(),
            FeatureMeasurementTypes.ARRIVAL_TIME,
            InstantValue.from(
                Instant.EPOCH.plusSeconds(1),
                Duration.ZERO
            )
        ),
        FeatureMeasurement.create(
            UUID.randomUUID(),
            FeatureMeasurementTypes.PHASE,
            PhaseTypeMeasurementValue.from(
                PhaseType.P,
                0.0
            )
        ),
        UUID.randomUUID()
    );

    // Create SignalDetectionHypothesis 2 to use for second SdhStationAssociation
    SignalDetectionHypothesis sdh2 = SignalDetectionHypothesis.create(
        UUID.randomUUID(),
        FeatureMeasurement.create(
            UUID.randomUUID(),
            FeatureMeasurementTypes.ARRIVAL_TIME,
            InstantValue.from(
                Instant.EPOCH.plusSeconds(2),
                Duration.ZERO
            )
        ),
        FeatureMeasurement.create(
            UUID.randomUUID(),
            FeatureMeasurementTypes.PHASE,
            PhaseTypeMeasurementValue.from(
                PhaseType.P,
                0.0
            )
        ),
        UUID.randomUUID()
    );

    // Create ReferenceStation to use for SdhStationAssociation
    ReferenceStation referenceStation = ReferenceStation.create(
        "DINGO DAN",
        "RANGO RON",
        StationType.SeismicArray,
        InformationSource.create(
            "DANGO DEB",
            Instant.EPOCH,
            "DONGO DARREL"
        ),
        "JANGO JIM",
        0.1,
        2.3,
        4.5,
        Instant.EPOCH,
        Instant.EPOCH,
        List.of()
    );

    // Create SdhStationAssociations to store in the cache
    SdhStationAssociation sdhStationAssociation1 = SdhStationAssociation
        .from(sdh1, referenceStation);
    SdhStationAssociation sdhStationAssociation2 = SdhStationAssociation
        .from(sdh2, referenceStation);

    // Get the SignalDetectionHypothesis' id to use to check that the correct SdhStationAssociation
    // objects were retrieved from the cache.
    UUID sdh1Id = sdh1.getId();
    UUID sdh2Id = sdh2.getId();

    // Store the SdhStationAssociations in the cache
    boolean was1Stored = this.gaCache.cacheSdhStationAssociation(sdhStationAssociation1);
    boolean was2Stored = this.gaCache.cacheSdhStationAssociation(sdhStationAssociation2);

    // Assert that the SdhStationAssociations were successfully stored in the cache
    Assertions.assertTrue(was1Stored);
    Assertions.assertTrue(was2Stored);

    // Query for sdhStationAssociations by time range
    // - sdhStationAssociations both lie completely inside the beginning and end times
    Set<SdhStationAssociation> sdhStationAssociations = this.gaCache
        .getSdhStationAssociationByTimeRange(
            Instant.EPOCH,
            Instant.EPOCH.plusSeconds(3)
        );

    // Assert both SdhStationAssociations were returned
    Assertions.assertEquals(Set.of(sdhStationAssociation1, sdhStationAssociation2),
        sdhStationAssociations);

    // Query for sdhStationAssociations by time range
    // - One SdhStationAssociation lies exactly on the beginning time and one lies exactly on the
    //   end time
    sdhStationAssociations = this.gaCache
        .getSdhStationAssociationByTimeRange(
            Instant.EPOCH.plusSeconds(1),
            Instant.EPOCH.plusSeconds(2)
        );

    // Assert both SdhStationAssociations were returned
    Assertions.assertEquals(Set.of(sdhStationAssociation1, sdhStationAssociation2),
        sdhStationAssociations);

    // Query for sdhStationAssociations by time range
    // - The first SdhStationAssociation lies within the time range and the second lies
    //   after the end time
    sdhStationAssociations = this.gaCache
        .getSdhStationAssociationByTimeRange(
            Instant.EPOCH.plusSeconds(0),
            Instant.EPOCH.plusSeconds(1).plusMillis(500)
        );

    // Assert the first SdhStationAssociation was returned
    Assertions.assertEquals(Set.of(sdhStationAssociation1), sdhStationAssociations);

    // Query for sdhStationAssociations by time range
    // - The first SdhStationAssociation lies before the beginning time and the second lies
    //   within the time range
    sdhStationAssociations = this.gaCache
        .getSdhStationAssociationByTimeRange(
            Instant.EPOCH.plusSeconds(1).plusMillis(500),
            Instant.EPOCH.plusSeconds(2)
        );

    // Assert the second SdhStationAssociation was returned
    Assertions.assertEquals(Set.of(sdhStationAssociation2), sdhStationAssociations);

    // Query for sdhStationAssociations by time range
    // - Both SdhStationAssociations lie outside the time range
    sdhStationAssociations = this.gaCache
        .getSdhStationAssociationByTimeRange(
            Instant.EPOCH.plusSeconds(1).plusMillis(500),
            Instant.EPOCH.plusSeconds(1).plusMillis(600)
        );

    // Assert no SdhStationAssociations were returned
    Assertions.assertEquals(Set.of(), sdhStationAssociations);
  }


  @Test
  void testStoreAndRetrieveEventHypothesisById() {

    // Create EventHypothesis to store in cache
    UUID eventHypothesisId = UUID.randomUUID();
    EventHypothesis mockEventHypothesis = Mockito.mock(EventHypothesis.class);
    Mockito.when(mockEventHypothesis.getId()).thenReturn(eventHypothesisId);

    // Store the mock EventHypothesis in the cache
    boolean wasStored = this.gaCache.cacheEventHypothesis(mockEventHypothesis);

    // Assert that the mock EventHypothesis was successfully stored in the cache
    Assertions.assertTrue(wasStored, "Expected true (EventHypothesis was stored successfully)");

    // Retrieve the EventHypothesis from the cache.  Throw IllegalStateException if not found.
    EventHypothesis retrievedEventHypothesis = gaCache
        .getEventHypothesisById(eventHypothesisId)
        .orElseThrow(
            () -> new IllegalStateException("Expected optional EventHypothesis to not be empty"));

    // Assert the correct EventHypothesis was retrieved from the cache
    Assertions.assertEquals(eventHypothesisId, retrievedEventHypothesis.getId());
  }


  @Test
  void testStoreAndRetrieveReferenceStationByVersionId() {

    // Create ReferenceStation to store in the cache
    ReferenceStation referenceStation = ReferenceStation.create(
        "DINGO DAN",
        "RANGO RON",
        StationType.SeismicArray,
        InformationSource.create(
            "DANGO DEB",
            Instant.EPOCH,
            "DONGO DARREL"
        ),
        "JANGO JIM",
        0.1,
        2.3,
        4.5,
        Instant.EPOCH,
        Instant.EPOCH,
        List.of()
    );

    // Get the ReferenceStation's version id to use to check that the correct ReferenceStation
    // object was retrieved from the cache.
    UUID referenceStationVersionId = referenceStation.getVersionId();

    // Store the ReferenceStation in the cache
    boolean wasStored = this.gaCache.cacheReferenceStation(referenceStation);

    // Assert that the ReferenceStation was successfully stored in the cache
    Assertions.assertTrue(wasStored, "Expected true (ReferenceStation was stored successfully)");

    // Retrieve the ReferenceStation from the cache.  Throw IllegalStateException if not found.
    ReferenceStation retrievedReferenceStation = gaCache
        .getReferenceStationById(referenceStationVersionId)
        .orElseThrow(
            () -> new IllegalStateException("Expected optional ReferenceStation to not be empty"));

    // Assert the correct ReferenceStation was retrieved from the cache
    Assertions.assertEquals(referenceStationVersionId, retrievedReferenceStation.getVersionId());
  }
}
