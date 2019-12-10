package gms.utilities.transferauditor;

import static gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus.RECEIVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoice;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoiceMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileRawStationDataFrameMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.TransferredFileRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class TransferAuditorUtilityTests {

  private static final RawStationDataFrame FRAME = RawStationDataFrame.create(
      UUID.randomUUID(), Set.of(UUID.randomUUID()), AcquisitionProtocol.SEEDLINK,
      Instant.EPOCH, Instant.EPOCH.plusSeconds(1), Instant.now(), new byte[]{(byte) 0},
      AuthenticationStatus.NOT_YET_AUTHENITCATED, CreationInfo.DEFAULT);

  private static final TransferredFileRawStationDataFrameMetadata FRAME_METADATA
      = TransferredFileRawStationDataFrameMetadata.from(
      FRAME.getPayloadDataStartTime(), FRAME.getPayloadDataEndTime(),
      FRAME.getStationId(), FRAME.getChannelIds());

  private TransferredFileRepositoryInterface repo;

  private TransferAuditorUtility auditor;

  @BeforeEach
  public void beforeTest() {
    this.repo = Mockito.mock(TransferredFileRepositoryInterface.class);
    this.auditor = new TransferAuditorUtility(repo);
  }

  /**
   * Tests the 'receive frame' method's behavior when
   * the frame is not present in the repository.
   * The auditor should query for a file created with the given
   * frames' metadata, then store the frame transferred file as 'RECEIVED'.
   */
  @Test
  public void testReceiveFrameNotPresent() throws Exception {
    final Instant receptionTime = Instant.now();
    final TransferredFile<TransferredFileRawStationDataFrameMetadata> searchedFile
        = createReceivedFrame(receptionTime);
    // querying for the file as RECEIVED and it is not found
    given(repo.find(searchedFile)).willReturn(Optional.empty());
    // give the auditor the file
    auditor.receive(FRAME, searchedFile.getFileName(), receptionTime);
    // the auditor searched for the file correctly
    verify(repo).find(searchedFile);
    // auditor stores the file as RECEIVED and
    // with provided reception time in this situation
    verify(repo).store(List.of(
        searchedFile.toBuilder().setStatus(TransferredFileStatus.RECEIVED)
        .setReceptionTime(receptionTime)
        .build()));
  }

  /**
   * Tests the 'receive frame' method's behavior when
   * the frame is present in the repository and marked as 'SENT'
   * (i.e. it has appeared in an invoice previously).
   * The auditor should query for a file created with the given
   * frames' metadata, then store the frame transferred file as 'sent and received'.
   */
  @Test
  public void testReceiveFramePresentAndStatusSent() throws Exception {
    final Instant receptionTime = Instant.now();
    final TransferredFile<TransferredFileRawStationDataFrameMetadata> receivedFile
        = createReceivedFrame(receptionTime);
    final TransferredFile<TransferredFileRawStationDataFrameMetadata> sentFile =
        createSentFrame(receivedFile.getFileName(), Instant.EPOCH);
    // querying for the file as RECEIVED and it is found with status of SENT
    given(repo.find(receivedFile)).willReturn(Optional.of(sentFile));
    // give the auditor the file
    auditor.receive(FRAME, sentFile.getFileName(), receptionTime);
    // the auditor searches for the file correctly
    verify(repo).find(receivedFile);
    // auditor stores the file as SENT_AND_RECEIVED
    verify(repo).store(List.of(combineSentAndReceived(sentFile, receivedFile)));
  }

  /**
   * Tests the 'receive frame' method's behavior when
   * the frame is present in the repository but is *not* marked as SENT;
   * this means it is either RECEIVED or SENT_AND_RECEIVED, either way it need not be processed
   * further.
   * The auditor should query for a file created with the given
   * frames' metadata, then not store the file again because it has already been received.
   */
  @Test
  public void testReceiveFramePresentAndStatusNotSent() throws Exception {
    final Instant receptionTime = Instant.now();
    TransferredFile<TransferredFileRawStationDataFrameMetadata> searchedFile
        = createReceivedFrame(receptionTime);
    // querying for the file as RECEIVED and it is found with status != SENT
    given(repo.find(searchedFile)).willReturn(Optional.of(searchedFile));
    // give the auditor the file
    auditor.receive(FRAME, searchedFile.getFileName(), receptionTime);
    // the auditor searches for the file correctly
    verify(repo).find(searchedFile);
    // auditor did not store anything because the file
    // was already SENT_AND_RECEIVED
    verify(repo, never()).store(any());
    // now mock repository to return RECEIVED which is still != SENT,
    // same behavior (store nothing)
    reset(repo);
    given(repo.find(searchedFile)).willReturn(
        Optional.of(searchedFile.toBuilder().setStatus(RECEIVED).build()));
    // give the auditor the file
    auditor.receive(FRAME, searchedFile.getFileName(), receptionTime);
    // the auditor searches for the file correctly
    verify(repo).find(searchedFile);
    // auditor didn't store anything because file was already SENT_AND_RECEIVED
    verify(repo, never()).store(any());
  }

  /**
   * Tests the auditor processing an invoice.  The test setup/mocking
   * is fairly involved to make a realistic invoice.  The test invoice contains:
   * - the invoice itself (invoices are supposed to list themselves as a transferred file)
   * - a frame file that won't be found in the repository (frameFileNotPresent)
   * - a frame file that will be found in the repository as RECEIVED (frameFilePresentAsReceived)
   * - a frame file that will be found in the repository but not as RECEIVED (frameFilePresentNotAsReceived)
   * - another invoice that won't be found in the repository
   * - another invoice that will be found in the repository as RECEIVED
   *
   * The test asserts that the proper transferred files are provided to a call to the repository
   * to store them.  See the architecture guidance or the implementation of 'receive invoice'
   * for more details on what should happen in each situation.
   */
  @Test
  public void testReceiveInvoice() throws Exception {
    final Instant t1 = Instant.EPOCH, t2 = t1.plusSeconds(1),
        t3 = t2.plusSeconds(1), t4 = t3.plusSeconds(1);
    final Instant receptionTime = Instant.now();
    final TransferredFile<TransferredFileRawStationDataFrameMetadata>
        frameFileNotPresent = createSentFrame(
        "frameFileNotPresent.json", t1),
        frameFilePresentAsReceived = createSentFrame(
            "frameFilePresentAsReceived.json", t2),
        frameFilePresentNotAsReceived = createSentFrame(
            "frameFilePresentNotAsReceived.json", t3);
    final long sequenceNumber = 10L;
    final TransferredFile<TransferredFileInvoiceMetadata> invoiceListedInItself
        = TransferredFile.createSent("invoiceListedInItself.inv",
        "priority", t3,
        TransferredFileInvoiceMetadata.from(sequenceNumber));
    final TransferredFile<TransferredFileInvoiceMetadata> anotherInvoiceNotPresent
        = TransferredFile.createSent("anotherInvoiceNotPresent.inv",
        "priority", t4,
        TransferredFileInvoiceMetadata.from(sequenceNumber + 5));
    final TransferredFile<TransferredFileInvoiceMetadata> anotherInvoicePresentAsReceived
        = TransferredFile.createSent("anotherInvoicePresentAsReceived.inv",
        "priority", t4,
        TransferredFileInvoiceMetadata.from(sequenceNumber + 10));
    // repo doesn't find frameFile1
    given(repo.find(frameFileNotPresent)).willReturn(Optional.empty());
    // repo finds frameFile2 as RECEIVED
    given(repo.find(frameFilePresentAsReceived)).willReturn(Optional.of(
        TransferredFile.createReceived(
            frameFilePresentAsReceived.getFileName(),
            receptionTime, frameFilePresentAsReceived.getMetadata())));
    // repo finds frameFile3 with status != RECEIVED
    // (instead, returns it with status = SENT_AND_RECEIVED)
    given(repo.find(frameFilePresentNotAsReceived)).willReturn(Optional.of(
        TransferredFile.createSentAndReceived(frameFilePresentNotAsReceived.getFileName(),
            frameFilePresentNotAsReceived.getPriority().get(),
            frameFilePresentNotAsReceived.getTransferTime().get(),
            receptionTime,
            frameFilePresentNotAsReceived.getMetadata())));
    // repo does not find invoiceListedAsItself
    given(repo.find(invoiceListedInItself)).willReturn(Optional.empty());
    // repo doesn't anotherInvoiceNotPresent
    given(repo.find(anotherInvoiceNotPresent)).willReturn(Optional.empty());
    // repo finds anotherInvoicePresentAsReceived as RECEIVED
    given(repo.find(anotherInvoicePresentAsReceived)).willReturn(Optional.of(
        TransferredFile.createReceived(
            anotherInvoicePresentAsReceived.getFileName(),
            receptionTime, anotherInvoicePresentAsReceived.getMetadata())));
    @SuppressWarnings("unchecked") final ArgumentCaptor<Collection<TransferredFile<?>>> fileStoreCaptor
        = ArgumentCaptor.forClass(Collection.class);
    doNothing().when(repo).store(fileStoreCaptor.capture());  // setup argument captor
    final TransferredFileInvoice invoice = TransferredFileInvoice.from(
        invoiceListedInItself.getMetadata().getSequenceNumber(), Set.of(
            frameFileNotPresent, frameFilePresentAsReceived, frameFilePresentNotAsReceived,
            invoiceListedInItself, anotherInvoiceNotPresent,
            anotherInvoicePresentAsReceived));
    // give the files to the auditor
    auditor.receive(invoice, "some-invoice.inv", receptionTime);
    // only frameFileNotPresent, frameFilePresentAsReceived, frameFilePresentNotAsReceived,
    // invoiceListedInItself, anotherInvoiceNotPresent, anotherInvoicePresentAsReceived
    // are stored; frameFilePresentNotAsReceived not stored.
    final Collection<TransferredFile<?>> expectedFilesStored = Set.of(frameFileNotPresent,
        createSentAndReceivedFromSent(frameFilePresentAsReceived, receptionTime),
        createSentAndReceivedFromSent(invoiceListedInItself, receptionTime),
        anotherInvoiceNotPresent,
        createSentAndReceivedFromSent(anotherInvoicePresentAsReceived, receptionTime));
    final Collection<TransferredFile<?>> storedFiles = fileStoreCaptor.getValue();
    // check size to ensure duplicates aren't present
    assertEquals(expectedFilesStored.size(), storedFiles.size());
    // the main comparison: check that the expected and actual sent to the repo match
    assertEquals(new HashSet<>(expectedFilesStored), new HashSet<>(storedFiles));
  }

  // creates a 'SENT' frame with the given reception time but defaulted other params for conciseness
  private static TransferredFile<TransferredFileRawStationDataFrameMetadata> createReceivedFrame(
      Instant receptionTime) {
    return TransferredFile.createReceived("foo.json", receptionTime, FRAME_METADATA);
  }

  // creates a 'SENT' frame with the given fileName and transfer time but other fields set
  // in this method for conciseness throughout the tests
  private static TransferredFile<TransferredFileRawStationDataFrameMetadata> createSentFrame(
      String fileName, Instant transferTime) {
    return TransferredFile.createSent(fileName, "priority",
        transferTime, randomFrameMetadata());
  }

  private static TransferredFile<?> createSentAndReceivedFromSent(TransferredFile<?> sentFile,
      Instant receptionTime) {
    return TransferredFile.createSentAndReceived(sentFile.getFileName(),
        sentFile.getPriority().get(), sentFile.getTransferTime().get(),
        receptionTime, sentFile.getMetadata());
  }

  // Creates a frame metadata object with random UUID's
  private static TransferredFileRawStationDataFrameMetadata randomFrameMetadata() {
    return TransferredFileRawStationDataFrameMetadata
        .from(Instant.EPOCH, Instant.EPOCH.plusSeconds(1),
            UUID.randomUUID(), Set.of(UUID.randomUUID(), UUID.randomUUID()));
  }

  private static TransferredFile<?> combineSentAndReceived(
      TransferredFile<?> sentFile, TransferredFile<?> receivedFile) {
    return TransferredFile.createSentAndReceived(
        sentFile.getFileName(), sentFile.getPriority().get(),
        sentFile.getTransferTime().get(),
        receivedFile.getReceptionTime().get(), sentFile.getMetadata());
  }
}
