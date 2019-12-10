package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class DataAcquisitionStatusTestFixtures {

  public static final Instant now = Instant.now();  // used as transferTime
  public static final Instant receptionTime = now.plusSeconds(5);
  public static final Instant payloadStartTime = receptionTime.minusSeconds(10); // now - 5
  public static final Instant payloadEndTime = payloadStartTime.plusSeconds(5);  // now - 5 + 5 = now

  public static final TransferredFileInvoiceMetadata transferredFileInvoiceMetadata = TransferredFileInvoiceMetadata
      .from(12345L);

  public static final TransferredFileRawStationDataFrameMetadata transferredFileRawStationDataFrameMetadata = TransferredFileRawStationDataFrameMetadata
      .from(payloadStartTime, payloadEndTime, UUID.randomUUID(), Set.of(UUID.randomUUID()));

  public static final TransferredFile<TransferredFileInvoiceMetadata> transferredInvoice = TransferredFile
          .from("testFile", "testPriority", now, receptionTime, TransferredFileStatus.SENT_AND_RECEIVED,
                  TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE, transferredFileInvoiceMetadata);

  public static final TransferredFile<TransferredFileRawStationDataFrameMetadata> transferredRawStationDataFrame
      = TransferredFile.from("testFile", "testPriority", now, receptionTime,
      TransferredFileStatus.SENT_AND_RECEIVED, TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
          transferredFileRawStationDataFrameMetadata);
}
