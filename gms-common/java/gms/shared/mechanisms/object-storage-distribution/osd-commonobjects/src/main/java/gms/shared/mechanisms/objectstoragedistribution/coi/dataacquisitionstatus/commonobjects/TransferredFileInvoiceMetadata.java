package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * TransferredFileInvoiceMetadata is used to construct a TransferredFile with status of SENT for a
 * TransferredFileInvoice that is known to be missing on the transfer receiver data acquisition
 * partition.
 */

@AutoValue
public abstract class TransferredFileInvoiceMetadata {

  public abstract long getSequenceNumber();

  /**
   * Creates an instance of TransferredFileInvoiceMetadata
   *
   * @return a TransferredFileInvoiceMetadata
   */
  @JsonCreator
  public static TransferredFileInvoiceMetadata from(
      @JsonProperty("sequenceNumber") long sequenceNumber) {

    return new AutoValue_TransferredFileInvoiceMetadata(sequenceNumber);
  }

}
