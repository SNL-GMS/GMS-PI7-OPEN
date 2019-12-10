package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoiceMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileMetadataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileRawStationDataFrameMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class TestFixtures {

  public static final Instant now = Instant.now();
  private static final Instant transferTime = now;
  private static final Instant receptionTime = now.plusSeconds(5);  // now + 5 secs
  // now plus 5, minus 10 = now minus 5 secs
  private static final Instant payloadStartTime = receptionTime.minusSeconds(10); // now - 5 secs
  // now minus 5, plus 5 = now
  private static final Instant payloadEndTime = payloadStartTime.plusSeconds(5);  // now

  // 1 hour ago (3600 secs)
  public static final Instant transferTime2 = now.minusSeconds(60 * 60);
  // 50 mins ago (3000 secs)
  public static final Instant receptionTime2 = now.minusSeconds(60 * 50);
  // 70 mins ago (4200 secs)
  private static final Instant payloadStartTime2 = now.minusSeconds(60 * 70);
  // 1 hour ago (3600 secs)
  private static final Instant payloadEndTime2 = now.minusSeconds(60 * 60);

  //---------------------------------
  // TransferredFileInvoiceMetadata
  public static final TransferredFileInvoiceMetadata transferredFileInvoiceMetadata =
      TransferredFileInvoiceMetadata
          .from(12345L);

  public static final TransferredFileInvoiceMetadata transferredFileInvoiceMetadata2 =
      TransferredFileInvoiceMetadata
          .from(6789L);

  public static final TransferredFileInvoiceMetadata transferredFileInvoiceMetadata3 =
      TransferredFileInvoiceMetadata
          .from(11111L);

  //---------------------------------
  // TransferredFileRawStationDataFrameMetadata
  public static final TransferredFileRawStationDataFrameMetadata transferredFileRawStationDataFrameMetadata =
      TransferredFileRawStationDataFrameMetadata
          .from(payloadStartTime, payloadEndTime, UUID.randomUUID(), Set.of(UUID.randomUUID()));

  public static final TransferredFileRawStationDataFrameMetadata transferredFileRawStationDataFrameMetadata2 =
      TransferredFileRawStationDataFrameMetadata
          .from(payloadStartTime2, payloadEndTime2, UUID.randomUUID(), Set.of(UUID.randomUUID()));

  //---------------------------------
  // TransferredFile <InvoiceMetadata>
  public static final TransferredFile<TransferredFileInvoiceMetadata> transferredInvoice = TransferredFile
      .from("Invoice", "PriorityInvoice", transferTime, receptionTime,
          TransferredFileStatus.SENT_AND_RECEIVED,
          TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE, transferredFileInvoiceMetadata);

  public static final TransferredFile<TransferredFileInvoiceMetadata> transferredInvoice2 = TransferredFile
      .from("Invoice2", "PriorityInvoice2", transferTime2, receptionTime2,
          TransferredFileStatus.SENT,
          TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE, transferredFileInvoiceMetadata2);

  //---------------------------------
  // TransferredFile <RawStationDataFrameMetadata>
  public static final TransferredFile<TransferredFileRawStationDataFrameMetadata> transferredRawStationDataFrame
      = TransferredFile.from("RSDF", "PriorityRSDF", transferTime, receptionTime,
      TransferredFileStatus.SENT_AND_RECEIVED,
      TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
      transferredFileRawStationDataFrameMetadata);

  public static final TransferredFile<TransferredFileRawStationDataFrameMetadata> transferredRawStationDataFrame2
      = TransferredFile
      .from("RSDF2", "PriorityRSDF2", transferTime2, receptionTime2,
          TransferredFileStatus.SENT_AND_RECEIVED,
          TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
          transferredFileRawStationDataFrameMetadata2);

}
