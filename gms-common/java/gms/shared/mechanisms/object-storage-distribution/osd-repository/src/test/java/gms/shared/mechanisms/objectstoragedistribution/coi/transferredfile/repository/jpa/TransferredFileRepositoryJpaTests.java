package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa;

import static gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TestFixtures.transferredInvoice;
import static gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TestFixtures.transferredRawStationDataFrame;
import static gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TestFixtures.transferredRawStationDataFrame2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TransferredFileRepositoryJpaTests {

  private static TransferredFileRepositoryJpa transferredFileRepositoryJpa;
  private static EntityManagerFactory entityManagerFactory;

  private static List<TransferredFile<?>> storedFiles = List
      .of(TestFixtures.transferredInvoice,
          TestFixtures.transferredRawStationDataFrame);

  @Before
  public void setUp() throws Exception {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
    transferredFileRepositoryJpa = new TransferredFileRepositoryJpa(entityManagerFactory);

    // Load some initial TransferredFiles objects before each test is run -
    // 1 transferredInvoice and 1 transferredRawStationDataFrame
    transferredFileRepositoryJpa.store(storedFiles);
  }

  @After
  public void tearDown() {
    entityManagerFactory.close();
    entityManagerFactory = null;
    transferredFileRepositoryJpa = null;
  }

  @Test
  public void testRetrieveAll() throws Exception {

    // Check for the 2 TransferredFiles objects that were loaded during the @Before stage
    List<TransferredFile> retrievedFiles = transferredFileRepositoryJpa.retrieveAll();
    assertResultsEqual(storedFiles, retrievedFiles);
  }

  @Test
  public void testRetrieveByTransferTime() throws Exception {

    // retrieve all that has been stored up until now, which is 2 TransferredFiles objects
    List<TransferredFile> retrievedFiles = transferredFileRepositoryJpa
        .retrieveByTransferTime(Instant.EPOCH, Instant.now());
    // assert stored contents match retrieved contents
    assertResultsEqual(storedFiles, retrievedFiles);

    // nothing had been stored at the EPOCH point in time - returned list should be empty
    List<TransferredFile> epochFiles = transferredFileRepositoryJpa
        .retrieveByTransferTime(Instant.EPOCH, Instant.EPOCH);
    assertTrue(epochFiles.isEmpty());

    // using a more specific time frame, test retrieving only 1 of the 3 entries persisted by
    // storing and retrieving a new single object that has a payload start/end time earlier than
    // any other object persisted so far
    transferredFileRepositoryJpa.store(List.of(TestFixtures.transferredRawStationDataFrame2));
    List<TransferredFile> rsdf2Files = transferredFileRepositoryJpa
        .retrieveByTransferTime(TestFixtures.transferTime2, TestFixtures.receptionTime2);
    assertResultsEqual(List.of(transferredRawStationDataFrame2), rsdf2Files);
  }

  @Test
  public void testStoreNewFile() throws Exception {

    // store a new object
    transferredFileRepositoryJpa.store(List.of(TestFixtures.transferredInvoice2));
    List<TransferredFile> retrievedFiles = transferredFileRepositoryJpa.retrieveAll();
    // assert that new object was stored and there are now a total of 3 objects persisted
    // (2 were stored during @Before stage)
    assertNotNull(retrievedFiles);
    assertEquals(3, retrievedFiles.size());
    assertTrue(retrievedFiles.contains(TestFixtures.transferredInvoice2));
  }

  @Test
  public void testUpdateStoredFile() throws Exception {

    // remove all of the objects (2) that were stored during the @Before stage
    Duration duration = Duration.ofSeconds(0);
    transferredFileRepositoryJpa.removeSentAndReceived(duration);
    List<TransferredFile> retrievedTransferredFiles = transferredFileRepositoryJpa.retrieveAll();
    assertTrue(retrievedTransferredFiles.isEmpty());

    // store a new object to test updating one of it's field
    transferredFileRepositoryJpa.store(List.of(TestFixtures.transferredInvoice2));
    List<TransferredFile> retrievedFile = transferredFileRepositoryJpa.retrieveAll();
    assertResultsEqual(List.of(TestFixtures.transferredInvoice2), retrievedFile);

    // verify stored object's status - it should be SENT
    TransferredFile<?> retrievedStoredFile = retrievedFile.get(0);
    assertEquals(TransferredFileStatus.SENT, retrievedStoredFile.getStatus());

    // use the autovalue builder to set a field to a new value, keeping the other values the same
    TransferredFile<?> someUpdatedFile = retrievedStoredFile.toBuilder()
        .setStatus(TransferredFileStatus.SENT_AND_RECEIVED)
        .build();

    // store updated object and check to make sure there are still the same number of objects persisted
    transferredFileRepositoryJpa.store(List.of(someUpdatedFile));
    List<TransferredFile> updatedFiles = transferredFileRepositoryJpa.retrieveAll();
    assertNotNull(updatedFiles);
    // assert that the modified file was stored as an "update" and not as "new" - there should only be 1 object persisted
    assertEquals(1, updatedFiles.size());

    // individual assertions of each field being the same, except for the updated one (status)
    final TransferredFile<?> onlyFile = updatedFiles.get(0);
    assertEquals(TestFixtures.transferredInvoice2.getFileName(), onlyFile.getFileName());
    assertEquals(TestFixtures.transferredInvoice2.getPriority(), onlyFile.getPriority());
    assertEquals(TestFixtures.transferredInvoice2.getTransferTime(), onlyFile.getTransferTime());
    assertEquals(TestFixtures.transferredInvoice2.getReceptionTime(), onlyFile.getReceptionTime());
    // check the modified field for the updated status value
    assertEquals(TransferredFileStatus.SENT_AND_RECEIVED, onlyFile.getStatus());
    assertEquals(TestFixtures.transferredInvoice2.getMetadataType(), onlyFile.getMetadataType());
    assertEquals(TestFixtures.transferredInvoice2.getMetadata(), onlyFile.getMetadata());
  }

  @Test
  public void testRemoveSentAndReceived() throws Exception {

    // remove all of the objects (2 SENT_AND_RECEIVED) that were stored during the @Before stage
    Duration duration = Duration.ofSeconds(0);   // now minus - 0 seconds
    transferredFileRepositoryJpa.removeSentAndReceived(duration);
    List<TransferredFile> sentAndReceivedFiles = transferredFileRepositoryJpa.retrieveAll();
    assertTrue(sentAndReceivedFiles.isEmpty());

    // store a single object that has a status of SENT, not SENT_AND_RECEIVED (so it won't be removed)
    transferredFileRepositoryJpa.store(List.of(TestFixtures.transferredInvoice2));
    List<TransferredFile> sentFile = transferredFileRepositoryJpa.retrieveAll();
    assertResultsEqual(List.of(TestFixtures.transferredInvoice2), sentFile);

    // check if newly stored file gets removed - it shouldn't since status is not SENT_AND_RECEIVED
    // Note - a call to removeSentAndReceived with a duration of 0 seconds should remove all
    // SENT_AND_RECEIVED objects with a transfer time older than now
    transferredFileRepositoryJpa.removeSentAndReceived(duration);
    List<TransferredFile> retrievedSentFile = transferredFileRepositoryJpa.retrieveAll();
    // assert it wasn't removed and is the only object stored
    assertResultsEqual(List.of(TestFixtures.transferredInvoice2), retrievedSentFile);
  }

  @Test
  public void testRemoveSentAndReceivedByAgeOfTransferTime() throws Exception {

    // 2 objects with status of SENT_AND_RECEIVED were stored during the @Before stage, with
    // a transfer time of now
    // Store a new SENT_AND_RECEIVED object, then remove it due to it's transfer time age,
    // leaving only the original 2 objects that were stored during the @Before stage

    transferredFileRepositoryJpa.store(List.of(transferredRawStationDataFrame2));
    List<TransferredFile> rsdf2Files = transferredFileRepositoryJpa
        .retrieveByTransferTime(TestFixtures.transferTime2, TestFixtures.receptionTime2);
    assertResultsEqual(List.of(transferredRawStationDataFrame2), rsdf2Files);

    List<TransferredFile> allRetrievedFiles = transferredFileRepositoryJpa.retrieveAll();
    assertEquals(3, allRetrievedFiles.size());
     assertTrue(allRetrievedFiles.contains(transferredInvoice));
     assertTrue(allRetrievedFiles.contains(transferredRawStationDataFrame));
     assertTrue(allRetrievedFiles.contains(transferredRawStationDataFrame2));

    // storing and then remove a new object with a transfer time that is much older
    // than the original 2 objects that were stored during the @Before stage
    Duration duration = Duration.ofSeconds(60 * 30);   // 30 mins ago
    transferredFileRepositoryJpa.removeSentAndReceived(duration);
    List<TransferredFile> sentAndReceivedFiles = transferredFileRepositoryJpa.retrieveAll();
    assertEquals(2, sentAndReceivedFiles.size());
    assertResultsEqual(storedFiles, sentAndReceivedFiles);
  }

  // Helper function
  private void assertResultsEqual(List<TransferredFile<?>> expected, List<TransferredFile> actual) {
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
    assertEquals(new HashSet<>(expected), new HashSet<>(actual));
  }

}
