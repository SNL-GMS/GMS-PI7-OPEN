package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestFixtures {

  static final Instant now = Instant.now();
  static final Instant nowMinusThreeSeconds = now.minusSeconds(3);
  static final Instant nowMinusThirtySeconds = now.minusSeconds(30);
  static final Instant nowMinusFiveSeconds = now.minusSeconds(5);
  static final Instant epochTime = Instant.EPOCH;
  static final Instant epochTimePlusOneSecond = Instant.EPOCH.plusSeconds(1);

  private static final Instant receptionTime = now.minusSeconds(2);
  private static final Instant payloadStartTime = receptionTime.minusSeconds(10);
  private static final Instant payloadEndTime = payloadStartTime.plusSeconds(5);
  private static final Instant nowMinusTwentySeconds = now.minusSeconds(20);
  private static final Instant nowMinusOneSecond = now.minusSeconds(1);


  private static final TransferredFileInvoiceMetadata transferredFileInvoiceMetadata = TransferredFileInvoiceMetadata
          .from(12345L);

  private static final TransferredFileInvoiceMetadata transferredFileInvoiceMetadata2 = TransferredFileInvoiceMetadata
          .from(6789L);

  private static final TransferredFileRawStationDataFrameMetadata transferredFileRawStationDataFrameMetadata = TransferredFileRawStationDataFrameMetadata
          .from(payloadStartTime, payloadEndTime, UUID.randomUUID(), Set.of(UUID.randomUUID()));

  private static final TransferredFileRawStationDataFrameMetadata transferredFileRawStationDataFrameMetadata2 = TransferredFileRawStationDataFrameMetadata
          .from(nowMinusThirtySeconds, nowMinusTwentySeconds, UUID.randomUUID(),
                  Set.of(UUID.randomUUID()));

  private static final TransferredFileRawStationDataFrameMetadata transferredFileRawStationDataFrameMetadata3 = TransferredFileRawStationDataFrameMetadata
          .from(nowMinusFiveSeconds, now, UUID.randomUUID(), Set.of(UUID.randomUUID()));

  static final TransferredFile<TransferredFileInvoiceMetadata> transferredInvoice = TransferredFile
          .from("testFile", "testPriority", nowMinusThreeSeconds, receptionTime, TransferredFileStatus.SENT_AND_RECEIVED,
                  TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE, transferredFileInvoiceMetadata);

  static final TransferredFile<TransferredFileInvoiceMetadata> transferredInvoice2 = TransferredFile
          .from("testFile", "testPriority", nowMinusThreeSeconds, receptionTime, TransferredFileStatus.SENT_AND_RECEIVED,
                  TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE, transferredFileInvoiceMetadata2);

  static final TransferredFile<TransferredFileRawStationDataFrameMetadata> transferredRawStationDataFrame
          = TransferredFile.from("testFile", "testPriority", nowMinusThreeSeconds, receptionTime,
          TransferredFileStatus.SENT_AND_RECEIVED, TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
          transferredFileRawStationDataFrameMetadata);

  static final TransferredFile<TransferredFileRawStationDataFrameMetadata> transferredRawStationDataFrame2
          = TransferredFile
          .from("testFile", "testPriority", nowMinusThirtySeconds, nowMinusTwentySeconds,
                  TransferredFileStatus.SENT_AND_RECEIVED,
                  TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
                  transferredFileRawStationDataFrameMetadata2);

  static final TransferredFile<TransferredFileRawStationDataFrameMetadata> transferredRawStationDataFrame3
          = TransferredFile.from("testFile", "testPriority", nowMinusFiveSeconds, nowMinusOneSecond,
          TransferredFileStatus.SENT_AND_RECEIVED, TransferredFileMetadataType.RAW_STATION_DATA_FRAME,
          transferredFileRawStationDataFrameMetadata3);

  static final List<TransferredFile> tfs = List.of(transferredInvoice, transferredInvoice2,
          transferredRawStationDataFrame, transferredRawStationDataFrame2, transferredRawStationDataFrame3);
}
