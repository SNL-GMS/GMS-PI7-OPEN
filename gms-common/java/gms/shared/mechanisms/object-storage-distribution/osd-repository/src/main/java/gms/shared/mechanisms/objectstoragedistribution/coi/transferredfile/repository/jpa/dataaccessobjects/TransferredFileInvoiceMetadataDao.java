package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoiceMetadata;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TransferredFileInvoiceMetadataDao {

  @Column(nullable = false)
  private long sequenceNumber;

  public TransferredFileInvoiceMetadataDao() {
  }

  public TransferredFileInvoiceMetadataDao(TransferredFileInvoiceMetadata metadata) {
    this.sequenceNumber = metadata.getSequenceNumber();
  }

  public TransferredFileInvoiceMetadata toCoi() {
    return TransferredFileInvoiceMetadata.from(this.sequenceNumber);
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferredFileInvoiceMetadataDao that = (TransferredFileInvoiceMetadataDao) o;
    return sequenceNumber == that.sequenceNumber;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sequenceNumber);
  }

  @Override
  public String toString() {
    return "TransferredFileInvoiceMetadataDao{" +
        "sequenceNumber=" + sequenceNumber +
        '}';
  }
}

