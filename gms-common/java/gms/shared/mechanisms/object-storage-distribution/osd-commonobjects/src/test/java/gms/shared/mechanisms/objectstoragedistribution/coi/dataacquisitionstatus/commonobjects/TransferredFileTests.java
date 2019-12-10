package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TransferredFileTests {

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Test
  public void testSerializationInvoice() throws Exception {
    final JavaType type = objectMapper.getTypeFactory()
            .constructParametricType(TransferredFile.class, TransferredFileInvoiceMetadata.class);
    TestUtilities.testSerialization(DataAcquisitionStatusTestFixtures.transferredInvoice,
            type, objectMapper);
  }

  @Test
  public void testSerializationRawStationDataFrameMetadata() throws Exception {
    final JavaType type = objectMapper.getTypeFactory()
            .constructParametricType(TransferredFile.class,
                    TransferredFileRawStationDataFrameMetadata.class);
    TestUtilities
            .testSerialization(DataAcquisitionStatusTestFixtures.transferredRawStationDataFrame,
                    type, objectMapper);
  }

  /**
   * Test "from" with fields that must be present, and not based on transferred file status
   */
  @Test
  public void testFromTransferredFileInvoiceMetadataSent() {
    TransferredFile<TransferredFileInvoiceMetadata> tf = TransferredFile
            .from("testFile", "testPriority", DataAcquisitionStatusTestFixtures.now,
                    DataAcquisitionStatusTestFixtures.receptionTime,
                    TransferredFileStatus.SENT,
                    TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE,
                    DataAcquisitionStatusTestFixtures.transferredFileInvoiceMetadata);

    assertEquals("testFile", tf.getFileName());
    assertEquals(Optional.of("testPriority"), tf.getPriority());
    assertEquals(Optional.of(DataAcquisitionStatusTestFixtures.now),
            tf.getTransferTime());
    assertEquals(Optional.of(DataAcquisitionStatusTestFixtures.receptionTime),
            tf.getReceptionTime());
    assertEquals(TransferredFileStatus.SENT, tf.getStatus());
    assertEquals(TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE,
            tf.getMetadataType());
    assertEquals(DataAcquisitionStatusTestFixtures.transferredFileInvoiceMetadata,
            tf.getMetadata());
  }

  /**
   * Test "from" with fields that must be present, and not based on transferred file status
   */
  @Test
  public void testFromTransferredRawStationDataFrameMetadataReceived() {
    TransferredFile<TransferredFileRawStationDataFrameMetadata> tf = TransferredFile
            .from("testFile", "testPriority", DataAcquisitionStatusTestFixtures.now,
                    DataAcquisitionStatusTestFixtures.receptionTime,
                    TransferredFileStatus.RECEIVED,
                    TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
                    DataAcquisitionStatusTestFixtures.transferredFileRawStationDataFrameMetadata);

    assertEquals("testFile", tf.getFileName());
    assertEquals(Optional.of("testPriority"), tf.getPriority());
    assertEquals(Optional.of(DataAcquisitionStatusTestFixtures.now),
            tf.getTransferTime());
    assertEquals(Optional.of(DataAcquisitionStatusTestFixtures.receptionTime),
            tf.getReceptionTime());
    assertEquals(TransferredFileStatus.RECEIVED, tf.getStatus());
    assertEquals(TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
            tf.getMetadataType());
    assertEquals(DataAcquisitionStatusTestFixtures.transferredFileRawStationDataFrameMetadata,
            tf.getMetadata());
  }

  @Test
  public void testCreateSent() {
    TransferredFile<TransferredFileInvoiceMetadata> tf = TransferredFile
            .createSent("testFile", "testPriority",
                    DataAcquisitionStatusTestFixtures.now,
                    DataAcquisitionStatusTestFixtures.transferredFileInvoiceMetadata);

    assertEquals("testFile", tf.getFileName());
    assertEquals(Optional.of("testPriority"), tf.getPriority());
    assertEquals(Optional.of(DataAcquisitionStatusTestFixtures.now),
            tf.getTransferTime());
    assertEquals(TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE,
            tf.getMetadataType());
    assertEquals(DataAcquisitionStatusTestFixtures.transferredFileInvoiceMetadata,
            tf.getMetadata());
  }

  @Test
  public void testCreateReceived() {
    TransferredFile<TransferredFileRawStationDataFrameMetadata> tf = TransferredFile
            .createReceived("testFile",
                    DataAcquisitionStatusTestFixtures.receptionTime,
                    DataAcquisitionStatusTestFixtures.transferredFileRawStationDataFrameMetadata);

    assertEquals("testFile", tf.getFileName());
    assertEquals(Optional.of(DataAcquisitionStatusTestFixtures.receptionTime),
            tf.getReceptionTime());
    assertEquals(TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
            tf.getMetadataType());
    assertEquals(DataAcquisitionStatusTestFixtures.transferredFileRawStationDataFrameMetadata,
            tf.getMetadata());
  }

  @Test
  public void testCreateSentAndReceived() {
    TransferredFile<TransferredFileInvoiceMetadata> tf = TransferredFile
            .createSentAndReceived("testFile", "testPriority",
                    DataAcquisitionStatusTestFixtures.now,
                    DataAcquisitionStatusTestFixtures.receptionTime,
                    DataAcquisitionStatusTestFixtures.transferredFileInvoiceMetadata);

    assertEquals("testFile", tf.getFileName());
    assertEquals(Optional.of("testPriority"), tf.getPriority());
    assertEquals(Optional.of(DataAcquisitionStatusTestFixtures.now),
            tf.getTransferTime());
    assertEquals(Optional.of(DataAcquisitionStatusTestFixtures.receptionTime),
            tf.getReceptionTime());
    assertEquals(TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE,
            tf.getMetadataType());
    assertEquals(DataAcquisitionStatusTestFixtures.transferredFileInvoiceMetadata,
            tf.getMetadata());
  }

  @Test
  public void testBuilder() {
    final TransferredFile<TransferredFileInvoiceMetadata> invoice
        = DataAcquisitionStatusTestFixtures.transferredInvoice;
    assertEquals(TransferredFileStatus.SENT_AND_RECEIVED, invoice.getStatus());
    final TransferredFileStatus newStatus = TransferredFileStatus.SENT;
    final TransferredFile<TransferredFileInvoiceMetadata> modifiedInvoice
        = invoice.toBuilder().setStatus(newStatus).build();
    assertNotEquals(modifiedInvoice.getStatus(), invoice.getStatus());
    assertEquals(newStatus, modifiedInvoice.getStatus());
  }

  /**
   * Test validation of MetadataType (transferred file invoice) matching actual metadata class
   * instance (raw station data frame). Expect an exception to be thrown
   */
  @Test(expected = IllegalArgumentException.class)
  public void testMetadataTypeMatchesMetadataClassException() {
    TransferredFile.from("testFile", "testPriority",
            DataAcquisitionStatusTestFixtures.now, DataAcquisitionStatusTestFixtures.now,
            TransferredFileStatus.SENT,
            TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE,
            DataAcquisitionStatusTestFixtures.transferredFileRawStationDataFrameMetadata);
  }

}
