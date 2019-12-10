package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoiceMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileRawStationDataFrameMetadata;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * TransferredFileRepository provides permanent, persistent storage of TransferredFiles with status
 * of either SENT or RECEIVED (i.e. those TransferredFiles appearing in a TransferredFileInvoice
 * where the corresponding data file has not been received; those TransferredFiles referencing
 * received data files that have not appeared in a TransferredFileInvoice).  TransferredFiles with
 * status of SENT are used to populate the "Gap List" display.  TransferredFiles with status of
 * SENT_AND_RECEIVED are regularly purged from the TransferredFileRepository.
 * TransferredFileRepository needs to be accessible from multiple applications (i.e. one or more
 * TransferAuditorUtility instances operating in data acquisition sequences and
 * TransferredFileRepositoryService).
 *
 * TransferredFileRepository has both load and loadByTransferTime operations to support the "Gap
 * List" displaying either recently missed transfers or missed transfers from arbitrary time
 * intervals.
 */

/**
 * Define an interface for storing, removing, and retrieving {@link TransferredFile}
 */
public interface TransferredFileRepositoryInterface {

  /**
   * Close database connections.
   *
   * @return True if successful, otherwise false.
   */
  boolean close();

  /**
   * Stores the provided TransferredFiles if they are not already in the TransferredFileRepository
   */
  void store(Collection<TransferredFile<?>> transferredFiles) throws Exception;

  /**
   * Removes TransferredFiles older than the Duration provided.
   */
  void removeSentAndReceived(Duration olderThan) throws Exception;

  /**
   * Loads and returns all TransferredFiles in the repository. Returns an empty collection if no
   * matching TransferredFiles are found in the repository.
   */
  List<TransferredFile> retrieveAll() throws Exception;

  /**
   * Loads and returns all TransferredFiles in the repository which have a transferTime attribute
   * between transferStartTime and transferEndTime.  Both times are inclusive. Returns an empty
   * collection if no matching TransferredFiles are found in the repository.
   */
  List<TransferredFile> retrieveByTransferTime(Instant startTime,
                                               Instant endTime) throws Exception;

  /**
   * Finds a transferred file in the repository if it exists.
   * @param file the file to search for
   * @param <T> the type of the transferred file
   * @return the file if it exists, or empty
   */
  <T extends TransferredFile> Optional<TransferredFile> find(T file) throws Exception;
}
