package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileRawStationDataFrameMetadata;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "transferred_file_raw_station_data_frame")
public class TransferredFileRawStationDataFrameDao extends
    TransferredFileDao<TransferredFileRawStationDataFrameMetadata> {

  @Id
  @GeneratedValue
  private long primaryKey;

  private TransferredFileRawStationDataFrameMetadataDao metadata;

  public TransferredFileRawStationDataFrameDao() {

  }

  public TransferredFileRawStationDataFrameDao(
      TransferredFile<TransferredFileRawStationDataFrameMetadata> tf) {
    super(tf);
    this.metadata = new TransferredFileRawStationDataFrameMetadataDao(tf.getMetadata());
  }

  @Override
  public TransferredFileRawStationDataFrameMetadata getMetadataCoi() {
    return this.metadata.toCoi();
  }

  public TransferredFileRawStationDataFrameMetadataDao getMetadata() {
    return this.metadata;
  }

  public void setMetadata(
      TransferredFileRawStationDataFrameMetadataDao metadata) {
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
    TransferredFileRawStationDataFrameDao that = (TransferredFileRawStationDataFrameDao) o;
    return primaryKey == that.primaryKey &&
        Objects.equals(metadata, that.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), primaryKey, metadata);
  }

  @Override
  public String toString() {
    return "TransferredFileRawStationDataFrameDao{" +
        "metadata=" + metadata +
        '}';
  }
}
