package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoiceMetadata;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "transferred_file_invoice")
public class TransferredFileInvoiceDao extends TransferredFileDao<TransferredFileInvoiceMetadata> {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(nullable = false)
  private TransferredFileInvoiceMetadataDao metadata;

  public TransferredFileInvoiceDao() {
  }

  public TransferredFileInvoiceDao(TransferredFile<TransferredFileInvoiceMetadata> tf) {
    super(tf);
    this.metadata = new TransferredFileInvoiceMetadataDao(tf.getMetadata());
  }

  @Override
  public TransferredFileInvoiceMetadata getMetadataCoi() {
    return this.metadata.toCoi();
  }

  public TransferredFileInvoiceMetadataDao getMetadata() {
    return this.metadata;
  }

  public void setMetadata(
      TransferredFileInvoiceMetadataDao metadata) {
    this.metadata = metadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TransferredFileInvoiceDao that = (TransferredFileInvoiceDao) o;
    return Objects.equals(metadata, that.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), metadata);
  }

  @Override
  public String toString() {
    return "TransferredFileInvoiceDao{" +
        "metadata=" + metadata +
        '}';
  }
}


