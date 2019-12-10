package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import java.util.List;
import java.util.UUID;

public interface ProvenanceRepository {

  /**
   * Store for the first time the provided {@link CreationInformation}.
   *
   * @param creationInformation store this CreationInformation and its versions, not null
   */
  void store(CreationInformation creationInformation);

  /**
   * Finds the stored {@link CreationInformation} by its id. Null if not present
   *
   * @param creationInformationId the id of the creation information to be returned.
   * @return The stored creation information.
   */
  List<CreationInformation> findCreationInformationById(UUID creationInformationId);
}
