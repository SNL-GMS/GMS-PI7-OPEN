package gms.core.signaldetection.association.control.gacache;

import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


/**
 * Specifies interface for global-grid-signal-detection-associator to use when accessing the cache
 * of {@link SdhStationAssociation}s, {@link ReferenceStation}s, and {@link EventHypothesis}
 * objects.  The cache contains objects that {@link gms.core.signaldetection.association.plugins.SignalDetectionAssociator}
 * implementations use during the association algorithm.
 */
public interface GACache {


  /**
   * Stores the given {@link SdhStationAssociation} in the cache.  If the {@link
   * SdhStationAssociation} is already stored in the cache, no store operation is performed, and
   * this method returns false. Otherwise, the provided {@link SdhStationAssociation} is stored in
   * the cache, and this method returns true.
   *
   * @param sdhStationAssociation {@link SdhStationAssociation} to store in the cache. Not null.
   * @return Returns true if the {@link SdhStationAssociation} was added to the cache, false if the
   * {@link SdhStationAssociation} was already present in the cache and thus was not added.
   */
  boolean cacheSdhStationAssociation(SdhStationAssociation sdhStationAssociation);


  /**
   * Retrieves an {@link Optional} {@link SdhStationAssociation} with the provided {@link UUID} from
   * the cache.
   *
   * @param sdhId {@link UUID} of the {@link SdhStationAssociation} to retrieve from the cache. Not
   * null.
   * @return {@link Optional} of the {@link SdhStationAssociation} with the provided {@link UUID}.
   * If the {@link SdhStationAssociation} does not exist in the cache, returns an empty {@link
   * Optional}.
   */
  Optional<SdhStationAssociation> getSdhStationAssociationBySdhId(UUID sdhId);


  /**
   * Retrieves a {@link Set} of {@link SdhStationAssociation} objects containing {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis}
   * objects that fall at or between the given beginning and end {@link Instant} objects.
   *
   * @param beginning {@link Instant} specifying the beginning of the time range to query for,
   * inclusive. Not null.
   * @param end {@link Instant} specifying the end of the time range to query for, inclusive. Not
   * null.
   * @return {@link Set} of {@link SdhStationAssociation} objects.  If none are found for the given
   * time range, then the returned {@link Set} will be empty.
   */
  Set<SdhStationAssociation> getSdhStationAssociationByTimeRange(Instant beginning,
      Instant end);


  /**
   * Stores the given {@link ReferenceStation} in the cache.  If the {@link ReferenceStation} is
   * already stored in the cache, no store operation is performed, and this method returns false.
   * Otherwise, the provided {@link ReferenceStation} is stored in the cache, and this method
   * returns true.
   *
   * @param referenceStation {@link ReferenceStation} to store in the cache. Not null.
   * @return Returns true if the {@link ReferenceStation} was added to the cache, false if the
   * {@link ReferenceStation} was already present in the cache and thus was not added.
   */
  boolean cacheReferenceStation(ReferenceStation referenceStation);


  /**
   * Retrieves an {@link Optional} {@link ReferenceStation} with the provided {@link UUID} from the
   * cache.
   *
   * @param referenceStationVersionId {@link UUID} of the {@link ReferenceStation} to retrieve from
   * the cache.  This {@link UUID} corresponds with {@link ReferenceStation}'s version id field. Not
   * null.
   * @return {@link Optional} of the {@link ReferenceStation} with the provided version {@link
   * UUID}. If the {@link ReferenceStation} does not exist in the cache, returns an empty {@link
   * Optional}.
   */
  Optional<ReferenceStation> getReferenceStationById(UUID referenceStationVersionId);


  /**
   * Stores the given {@link EventHypothesis} in the cache.  If the {@link EventHypothesis} is
   * already stored in the cache, no store operation is performed, and this method returns false.
   * Otherwise, the provided {@link EventHypothesis} is stored in the cache, and this method returns
   * true.
   *
   * @param eventHypothesis {@link EventHypothesis} to store in the cache. Not null.
   * @return Returns true if the {@link EventHypothesis} was added to the cache, false if the {@link
   * EventHypothesis} was already present in the cache and thus was not added.
   */
  boolean cacheEventHypothesis(EventHypothesis eventHypothesis);


  /**
   * Retrieves an {@link Optional} {@link EventHypothesis} with the provided {@link UUID} from the
   * cache.
   *
   * @param eventHypothesisId {@link UUID} of the {@link EventHypothesis} to retrieve from the
   * cache.  Not null.
   * @return {@link Optional} of the {@link EventHypothesis} with the provided {@link UUID}. If the
   * {@link EventHypothesis} does not exist in the cache, returns an empty {@link Optional}.
   */
  Optional<EventHypothesis> getEventHypothesisById(UUID eventHypothesisId);
}
