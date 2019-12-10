package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.datatransfer.TransferredFileIdResolver;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

/**
 * Each TransferredFile object only contains information about a data file that was transferred
 * between data acquisition partitions but does not include the contents of the transferred file.
 * TransferredFile has metadata that is a generic object since the information needed to populate
 * the "Gap List" display and/or determine if a file has been received varies by the type of data
 * being transferred. Classes providing TransferredFile metadata must only contain fields that can
 * be obtained from the transferred data file.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "metadataType", visible = true)
@JsonTypeIdResolver(TransferredFileIdResolver.class)

@AutoValue
public abstract class TransferredFile<T> {

  public abstract String getFileName();

  public abstract Optional<String> getPriority();

  public abstract Optional<Instant> getTransferTime();

  public abstract Optional<Instant> getReceptionTime();

  public abstract TransferredFileStatus getStatus();

  public abstract TransferredFileMetadataType getMetadataType();

  public abstract T getMetadata();


  private static <T> TransferredFileMetadataType getTypeByMetadata(T metadata) {
    Objects.requireNonNull(metadata);
    final Class<?> metadataClass = metadata.getClass();
    if (TransferredFileRawStationDataFrameMetadata.class.isAssignableFrom(metadataClass)) {
      return TransferredFileMetadataType.RAW_STATION_DATA_FRAME;
    } else if (TransferredFileInvoiceMetadata.class.isAssignableFrom(metadataClass)) {
      return TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE;
    } else {
      throw new IllegalArgumentException("Unknown/unsupported metadata class: " + metadataClass);
    }
  }

  /**
   * Creates an instance of TransferredFile
   *
   * @param filename String Filename
   * @param priority Optional<String> Priority - required here
   * @param transferTime Optional<Instant> TransferTime - required here
   * @param receptionTime Optional<Instant> ReceptionTime - required here
   * @param status enum TransferredFileStatus
   * @param metadataType enum TransferredFileMetadataType
   * @param metadata <T> Metadata
   * @return a <T> TransferredFile<T>
   */
  @SuppressWarnings("unchecked")
  @JsonCreator
  public static <T> TransferredFile<T> from(
      @JsonProperty("fileName") String filename,
      @JsonProperty("priority") String priority,
      @JsonProperty("transferTime") Instant transferTime,
      @JsonProperty("receptionTime") Instant receptionTime,
      @JsonProperty("status") TransferredFileStatus status,
      @JsonProperty("metadataType") TransferredFileMetadataType metadataType,
      @JsonProperty("metadata") T metadata) {

    return builder()
        .setFileName(filename)
        .setPriority(Optional.ofNullable(priority))
        .setTransferTime(Optional.ofNullable(transferTime))
        .setReceptionTime(Optional.ofNullable(receptionTime))
        .setStatus(status)
        .setMetadataType(metadataType)
        .setMetadata(metadata)
        .build();
  }

  /**
   * If status is SENT: fileName, priority, transferTime, and metaData must be populated
   *
   * @param filename String Filename
   * @param priority Optional<String> Priority - required here
   * @param transferTime Optional<Instant> TransferTime - required here
   * @param metadata <T> Metadata
   * @return a <T> TransferredFile<T>
   */
  public static <T> TransferredFile<T> createSent(
      String filename,
      String priority,
      Instant transferTime,
      T metadata) {

    return from(filename, priority,
        transferTime, null, TransferredFileStatus.SENT,
        getTypeByMetadata(metadata), metadata);
  }

  /**
   * If status is RECEIVED: fileName, receptionTime, and metaData must be populated
   *
   * @param filename String Filename
   * @param receptionTime Optional<Instant> ReceptionTime - required here
   * @param metadata <T> Metadata
   * @return a <T> TransferredFile<T>
   */
  public static <T> TransferredFile<T> createReceived(
      String filename,
      Instant receptionTime,
      T metadata) {

    return from(filename, null, null,
        receptionTime, TransferredFileStatus.RECEIVED,
        getTypeByMetadata(metadata), metadata);
  }

  /**
   * If status is SENT_AND_RECEIVED: all attributes must be populated
   *
   * @param filename String Filename
   * @param priority Optional<String> Priority - required here
   * @param transferTime Optional<Instant> TransferTime - required here
   * @param receptionTime Optional<Instant> ReceptionTime - required here
   * @param metadata <T> Metadata
   * @return a <T> TransferredFile<T>
   */
  public static <T> TransferredFile<T> createSentAndReceived(
      String filename,
      String priority,
      Instant transferTime,
      Instant receptionTime,
      T metadata) {

    return from(filename, priority,
        transferTime, receptionTime, TransferredFileStatus.SENT_AND_RECEIVED,
        getTypeByMetadata(metadata), metadata);
  }

  public static Builder builder() {
    return new AutoValue_TransferredFile.Builder();
  }

  public abstract Builder<T> toBuilder();

  /**
   * Builder for this class.
   */
  @AutoValue.Builder
  public abstract static class Builder<T> {

    public abstract Builder<T> setFileName(String fileName);

    public abstract Builder<T> setPriority(String priority);

    public abstract Builder<T> setPriority(Optional<String> priority);

    public abstract Builder<T> setTransferTime(Instant time);

    public abstract Builder<T> setTransferTime(Optional<Instant> time);

    public abstract Builder<T> setReceptionTime(Instant time);

    public abstract Builder<T> setReceptionTime(Optional<Instant> time);

    public abstract Builder<T> setStatus(TransferredFileStatus status);

    public abstract Builder<T> setMetadataType(TransferredFileMetadataType type);

    public abstract Builder<T> setMetadata(T metadata);

    abstract TransferredFile<T> autoBuild();

    public TransferredFile<T> build() {
      final TransferredFile<T> tf = autoBuild();

      if (tf.getStatus().equals(TransferredFileStatus.SENT)) {
        Validate.isTrue(tf.getPriority().isPresent(),
            "SENT TransferredFile must have a priority");
        Validate.isTrue(tf.getTransferTime().isPresent(),
            "SENT TransferredFile must have a transferTime");
      } else if (tf.getStatus().equals(TransferredFileStatus.RECEIVED)) {
        Validate.isTrue(tf.getReceptionTime().isPresent(),
            "RECEIVED TransferredFile must have a receptionTime");
      } else if (tf.getStatus().equals(TransferredFileStatus.SENT_AND_RECEIVED)) {
        Validate.isTrue(tf.getPriority().isPresent(),
            "SENT_AND_RECEIVED TransferredFile must have a priority");
        Validate.isTrue(tf.getTransferTime().isPresent(),
            "SENT_AND_RECEIVED TransferredFile must have a transferTime");
        Validate.isTrue(tf.getReceptionTime().isPresent(),
            "SENT_AND_RECEIVED TransferredFile must have a receptionTime");
      }

      final T metadata = tf.getMetadata();
      final TransferredFileMetadataType metadataType = tf.getMetadataType();
      final TransferredFileMetadataType expectedMetadataType = getTypeByMetadata(metadata);
      Validate.isTrue(expectedMetadataType.equals(metadataType),
          "Inconsistent TransferredFileMetadataType; metadataType is " + metadataType
              + ", but metadata is of type " + metadata.getClass());

      return tf;
    }
  }

}
