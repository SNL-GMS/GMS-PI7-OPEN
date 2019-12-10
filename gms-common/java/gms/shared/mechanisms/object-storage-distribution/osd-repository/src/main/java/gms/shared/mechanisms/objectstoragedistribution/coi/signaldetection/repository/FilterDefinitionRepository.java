package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import java.util.Collection;

/**
 * Repository for {@link FilterDefinition}
 */
public interface FilterDefinitionRepository {

  /**
   * Stores the {@link FilterDefinition}
   *
   * @param filterDefinition FilterDefinition to store, not null
   * @throws NullPointerException if filterDefinition is null
   */
  void store(FilterDefinition filterDefinition);

  /**
   * Retrieves all of the {@link FilterDefinition}s stored in this {@link
   * FilterDefinitionRepository}
   *
   * @return collection of FilterDefinitions, not null
   */
  Collection<FilterDefinition> retrieveAll();
}
