package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.Set;

/**
 * TransferredFileInvoice is a list of TransferredFiles listing information about files that have
 * been transferred from one data acquisition partition to another data acquisition partition.
 * TransferredFileInvoice has a sequence number to help recognize when an invoice has failed to
 * transfer.  Each TransferredFile in a TransferredFileInvoice has status set to SENT and contains
 * enough information to determine whether the corresponding file has been received on the receiving
 * partition as well as information used to populate the "Gap List" display.
 */

@AutoValue
public abstract class TransferredFileInvoice {

  public abstract long getSequenceNumber();

  public abstract Set<TransferredFile> getTransferredFiles();

  /**
   * Creates an instance of TransferredFileInvoice
   *
   * @return a set of TransferredFiles
   */
  @JsonCreator
  public static TransferredFileInvoice from(
      @JsonProperty("sequenceNumber") long sequenceNumber,
      @JsonProperty("transferredFiles") Set<TransferredFile> transferredFiles) {

    return new AutoValue_TransferredFileInvoice(sequenceNumber,
        Collections.unmodifiableSet(transferredFiles));
  }

}
