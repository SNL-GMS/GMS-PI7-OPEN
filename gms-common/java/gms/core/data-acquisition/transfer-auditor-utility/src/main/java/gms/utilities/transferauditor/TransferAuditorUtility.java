package gms.utilities.transferauditor;

import static gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE;
import static gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus.RECEIVED;
import static gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus.SENT;
import static gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus.SENT_AND_RECEIVED;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoice;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoiceMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileRawStationDataFrameMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.TransferredFileRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for processing transferred files that arrive on the data processing partition.
 */
public class TransferAuditorUtility {

  private final Logger logger = LoggerFactory.getLogger(TransferAuditorUtility.class);

  private final TransferredFileRepositoryInterface transferredFileRepo;

  /**
   * Constructor.
   *
   * @param transferredFileRepo the repository interface to use, non-null
   */
  public TransferAuditorUtility(final TransferredFileRepositoryInterface transferredFileRepo) {
    this.transferredFileRepo = Objects.requireNonNull(transferredFileRepo,
        "Cannot create TransferAuditorUtility with null TransferredFileRepositoryInterface");
  }

  /**
   * Processes a frame.
   *
   * @param frame the frame, non-null
   * @param fileName the fileName that the frame was read from, non-null
   * @param receptionTime the time the frame was received, non-null
   * @throws Exception if params are null or interacting with the repository throws an exception
   */
  public void receive(RawStationDataFrame frame, String fileName, Instant receptionTime)
      throws Exception {
    Objects.requireNonNull(frame, "frame cannot be null");
    Objects.requireNonNull(fileName, "fileName cannot be null");
    Objects.requireNonNull(receptionTime, "receptionTime cannot be null");

    final TransferredFile<TransferredFileRawStationDataFrameMetadata> receivedFile
        = TransferredFile.createReceived(fileName, receptionTime,
        createMetadata(frame));

    final Optional<TransferredFile> foundFile
        = this.transferredFileRepo.find(receivedFile);
    // Case 1: file not found.  Create new TransferredFile with status set to RECEIVED
    // and store.
    if (!foundFile.isPresent()) {
      this.transferredFileRepo.store(List.of(receivedFile));
    } else {
      final TransferredFile<?> file = foundFile.get();
      // Case 2: file found and it's status is SENT.  Set the status to SENT_AND_RECEIVED
      // and update the receptionTime.  Store the updated version of the object.
      if (file.getStatus().equals(SENT)) {
        this.transferredFileRepo.store(
            List.of(receiveFilePreviouslySent(file, receptionTime)));
      }
    }
  }

  /**
   * Processes an invoice.
   *
   * @param invoice the invoice, non-null
   * @param fileName the fileName the invoice showed up in, non-null
   * @param receptionTime the time the invoice was received, non-null
   * @throws Exception if params are null or interacting with the repository throws an exception
   */
  public void receive(TransferredFileInvoice invoice, String fileName, Instant receptionTime)
      throws Exception {
    Objects.requireNonNull(invoice, "invoice cannot be null");
    Objects.requireNonNull(fileName, "fileName cannot be null");
    Objects.requireNonNull(receptionTime, "receptionTime cannot be null");

    // collect the files to be stored and pass them into the repository once
    final Collection<TransferredFile<?>> filesToStore = new HashSet<>();

    for (TransferredFile<?> listedFile : invoice.getTransferredFiles()) {
      final Optional<TransferredFile> existing
          = this.transferredFileRepo.find(listedFile);
      if (!existing.isPresent()) {
        if (!listedFile.getStatus().equals(SENT)) {
          logger.error("Listed file in invoice does not have status SENT (always should)");
        } else {
          filesToStore.add(listedFile);
        }
      } else {
        final TransferredFile<?> receivedFile = existing.get();
        if (receivedFile.getStatus().equals(RECEIVED)) {
          filesToStore.add(
              combineSentAndReceived(listedFile, receivedFile));
        }
      }
    }

    // Find the TransferredFile corresponding to this TransferredFileInvoice,
    // update its status to SENT_AND_RECEIVED, and add the
    // TransferredFile to the files to be stored.
    // suppress warning from casting in the stream, made safe by filtering by metadata type
    @SuppressWarnings("unchecked") final Optional<TransferredFile<TransferredFileInvoiceMetadata>> foundInvoice
        = invoice.getTransferredFiles().stream()
        .filter(f -> f.getMetadataType().equals(TRANSFERRED_FILE_INVOICE))
        .map(f -> (TransferredFile<TransferredFileInvoiceMetadata>) f)
        .filter(i -> i.getMetadata().getSequenceNumber()
            == invoice.getSequenceNumber())
        .findAny();
    if (foundInvoice.isPresent()) {
      final TransferredFile<TransferredFileInvoiceMetadata> i
          = foundInvoice.get();
      // need to remove existing to replace with one marked SENT_AND_RECEIVED
      filesToStore.remove(i);
      filesToStore.add(receiveFilePreviouslySent(i, receptionTime));
    } else {
      logger.error("Could not find invoice that should be stored "
          + "(because invoices list themselves)");
    }
    // finally, store the necessary transferred files
    this.transferredFileRepo.store(filesToStore);
  }

  /**
   * Creates a frame metadata from a frame.
   * @param frame the frame
   * @return frame metadata from the frame's attributes
   */
  private static TransferredFileRawStationDataFrameMetadata createMetadata(
      RawStationDataFrame frame) {
    return TransferredFileRawStationDataFrameMetadata.from(
        frame.getPayloadDataStartTime(),
        frame.getPayloadDataEndTime(), frame.getStationId(), frame.getChannelIds());
  }

  /**
   * Creates a file that is 'sent and received' given a sent file and the time the file has now been
   * received
   *
   * @param sentFile the sent file
   * @param receptionTime the time the file has been received
   * @return a file that has the attributes of the sent file but is now 'sent and received' and has
   * the given reception time
   */
  private static TransferredFile<?> receiveFilePreviouslySent(TransferredFile<?> sentFile,
      Instant receptionTime) {
    return sentFile.toBuilder()
        .setStatus(SENT_AND_RECEIVED)
        .setReceptionTime(receptionTime)
        .build();
  }

  /**
   * Takes a 'sent' file and a 'received' file and combines the information in them into a 'sent and
   * received' file.
   *
   * @param sentFile the sent file
   * @param receivedFile the received file
   * @return a 'sent and received' file that combine the info appropriately from the 'sent' file and
   * the 'received' file.  Attributes the two share are chosen from arbitrarily.
   */
  private static TransferredFile<?> combineSentAndReceived(
      TransferredFile<?> sentFile, TransferredFile<?> receivedFile) {
    return TransferredFile.createSentAndReceived(
        sentFile.getFileName(), sentFile.getPriority().get(),
        sentFile.getTransferTime().get(),
        receivedFile.getReceptionTime().get(), sentFile.getMetadata());
  }
}
