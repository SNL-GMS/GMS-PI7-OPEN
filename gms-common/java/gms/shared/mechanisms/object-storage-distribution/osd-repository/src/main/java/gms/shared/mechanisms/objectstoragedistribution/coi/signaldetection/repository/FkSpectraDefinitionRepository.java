package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import java.util.Collection;

public interface FkSpectraDefinitionRepository {
  /**
   * Stores the {@link FkSpectraDefinition}
   *
   * @param fkSpectraDefinition FkSpectraDefinition to store, not null
   * @throws NullPointerException if fkSpectraDefinition is null
   */
  void store(FkSpectraDefinition fkSpectraDefinition);

  /**
   * Retrieves all of the {@link FkSpectraDefinition}s stored in this {@link
   * FkSpectraDefinitionRepository}
   *
   * @return collection of FkSpectraDefinition, not null
   */
  Collection<FkSpectraDefinition> retrieveAll();
}
