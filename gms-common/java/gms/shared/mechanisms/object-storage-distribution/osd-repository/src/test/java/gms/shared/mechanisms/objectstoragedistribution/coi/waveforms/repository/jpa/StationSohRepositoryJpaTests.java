package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.AcquiredChannelSohAnalogDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.AcquiredChannelSohBooleanDao;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StationSohRepositoryJpaTests {

  private StationSohRepositoryJpa stationSohPersistenceJpa;
  private EntityManagerFactory entityManagerFactory;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
    stationSohPersistenceJpa = new StationSohRepositoryJpa(entityManagerFactory);
  }

  @After
  public void tearDown() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      entityManager.createQuery("DELETE FROM " + AcquiredChannelSohAnalogDao.class.getSimpleName())
          .executeUpdate();
      entityManager.createQuery("DELETE FROM " + AcquiredChannelSohBooleanDao.class.getSimpleName())
          .executeUpdate();
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
      entityManagerFactory.close();
    }
  }

  // Note: tests the operation that stores multiple AcquiredChannelSoh's at once
  @Test
  public void storeSohAndRetrieveTest() throws Exception {
    // store 2 SOH's, one analog and one bool.
    stationSohPersistenceJpa.storeSoh(List.of(
        TestFixtures.channelSohAnalog, TestFixtures.channelSohBool));
    // Retrieve all analog SOH's, there should be one.
    List<AcquiredChannelSohAnalog> analogs = new ArrayList<>(
        stationSohPersistenceJpa.retrieveAllAnalogSoh());
    assertNotNull(analogs);
    assertEquals(1, analogs.size());
    // Check that the last item added is identical to the test fixture.
    assertEquals(analogs.get(0), TestFixtures.channelSohAnalog);
    // Retrieve all boolean SOH's, there should be one.
    List<AcquiredChannelSohBoolean> bools = new ArrayList<>(
        stationSohPersistenceJpa.retrieveAllBooleanSoh());
    assertEquals(1, bools.size());
    // Check that the last item added is identical to the test fixture.
    assertEquals(bools.get(0), TestFixtures.channelSohBool);
  }

  @Test
  public void storeAndRetrieveAnalogSohTest() throws Exception {
    // Test normal case, which should add a record.
    stationSohPersistenceJpa.storeAnalogSoh(TestFixtures.channelSohAnalog);
    // Retrieve all records in the SOH table, there should be some.
    List<AcquiredChannelSohAnalog> list = new ArrayList<>(
        stationSohPersistenceJpa.retrieveAllAnalogSoh());
    assertNotNull(list);
    assertEquals(1, list.size());
    // Check that the item added is identical to the test fixture.
    assertEquals(list.get(0), TestFixtures.channelSohAnalog);

  }

  @Test
  public void storeAndRetrieveBooleanSohTest() throws Exception {
    // Test normal case, which should add a record.
    stationSohPersistenceJpa.storeBooleanSoh(TestFixtures.channelSohBool);
    // Retrieve all records in the SOH table, there should be some.
    List<AcquiredChannelSohBoolean> list = new ArrayList<>(
        stationSohPersistenceJpa.retrieveAllBooleanSoh());
    assertEquals(1, list.size());
    // Check that the item added is identical to the test fixture.
    assertEquals(list.get(0), TestFixtures.channelSohBool);
  }

  @Ignore
  @Test(expected = DataExistsException.class)
  public void storeAnalogSohTwiceTest() throws Exception {
    stationSohPersistenceJpa.storeAnalogSoh(TestFixtures.channelSohAnalog);
    stationSohPersistenceJpa.storeAnalogSoh(TestFixtures.channelSohAnalog);
  }

  @Ignore
  @Test(expected = DataExistsException.class)
  public void storeBooleanSohTwiceTest() throws Exception {
    stationSohPersistenceJpa.storeBooleanSoh(TestFixtures.channelSohBool);
    stationSohPersistenceJpa.storeBooleanSoh(TestFixtures.channelSohBool);
  }

  @Test(expected = Exception.class)
  public void storeNullAnalogSohTest() throws Exception {
    // Should throw exception.
    stationSohPersistenceJpa.storeAnalogSoh(null);
  }

  @Test(expected = Exception.class)
  public void storeNullBooleanSohTest() throws Exception {
    // Should throw exception.
    stationSohPersistenceJpa.storeBooleanSoh(null);
  }

  @Test
  public void testRetrieveAcquiredChannelSohBooleanByIdNullExpectException() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(stationSohPersistenceJpa,
        "retrieveAcquiredChannelSohBooleanById", UUID.randomUUID());
  }

  @Test
  public void testRetrieveAcquiredChannelSohAnalogByIdNullExpectException() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(stationSohPersistenceJpa,
        "retrieveAcquiredChannelSohAnalogById", UUID.randomUUID());
  }

  @Test
  public void testRetrieveAcquiredChannelSohBooleanByIdNotFound() throws Exception {
    Optional<AcquiredChannelSohBoolean> result = stationSohPersistenceJpa
        .retrieveAcquiredChannelSohBooleanById(new UUID(0L, 0L));

    assertFalse(result.isPresent());
  }

  @Test
  public void testRetrieveAcquiredChannelSohAnalogByIdNotFound() throws Exception {
    Optional<AcquiredChannelSohAnalog> result = stationSohPersistenceJpa
        .retrieveAcquiredChannelSohAnalogById(new UUID(0L, 0L));

    assertFalse(result.isPresent());
  }

  @Test
  public void testRetrieveBooleanSohById() throws Exception {
    AcquiredChannelSohBoolean expectedResult = TestFixtures.channelSohBool;
    stationSohPersistenceJpa.storeBooleanSoh(expectedResult);

    Optional<AcquiredChannelSohBoolean> result = stationSohPersistenceJpa
        .retrieveAcquiredChannelSohBooleanById(expectedResult.getId());

    assertTrue(result.isPresent());
    assertEquals(expectedResult, result.get());
  }

  @Test
  public void testRetrieveAnalogSohById() throws Exception {
    AcquiredChannelSohAnalog expectedResult = TestFixtures.channelSohAnalog;
    stationSohPersistenceJpa.storeAnalogSoh(expectedResult);

    Optional<AcquiredChannelSohAnalog> result = stationSohPersistenceJpa
        .retrieveAcquiredChannelSohAnalogById(expectedResult.getId());

    assertTrue(result.isPresent());
    assertEquals(expectedResult, result.get());
  }

  @Test
  public void testRetrieveBooleanSohByProcessingChannelAndTimeRangeNullParameters()
      throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(stationSohPersistenceJpa,
        "retrieveBooleanSohByProcessingChannelAndTimeRange",
        new UUID(0L, 0L), Instant.ofEpochSecond(100000), Instant.ofEpochSecond(100001));
  }

  @Test
  public void testRetrieveBooleanSohByProcessingChannelAndTimeRangeStartTimeGreaterThanEndTime()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot run query with start time greater than end time");

    stationSohPersistenceJpa.retrieveBooleanSohByProcessingChannelAndTimeRange(new UUID(0L, 0L),
        Instant.ofEpochSecond(100001), Instant.ofEpochSecond(100000));
  }

  @Test
  public void testRetrieveBooleanSohByProcessingChannelAndTimeRange() throws Exception {
    AcquiredChannelSohBoolean expectedResult = TestFixtures.channelSohBool;

    stationSohPersistenceJpa.storeBooleanSoh(expectedResult);

    testSohQueryByProcessingChannelAndTimeRange(expectedResult,
        (id, start, end) -> stationSohPersistenceJpa
            .retrieveBooleanSohByProcessingChannelAndTimeRange(id, start, end));
  }

  @Test
  public void testRetrieveAnalogSohByProcessingChannelAndTimeRangeNullParameters()
      throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(stationSohPersistenceJpa,
        "retrieveAnalogSohByProcessingChannelAndTimeRange",
        new UUID(0L, 0L), Instant.ofEpochSecond(100000), Instant.ofEpochSecond(100001));
  }

  @Test
  public void testRetrieveAnalogSohByProcessingChannelAndTimeRangeStartTimeGreaterThanEndTime()
      throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot run query with start time greater than end time");

    stationSohPersistenceJpa.retrieveAnalogSohByProcessingChannelAndTimeRange(new UUID(0L, 0L),
        Instant.ofEpochSecond(100001), Instant.ofEpochSecond(100000));
  }

  @Test
  public void testRetrieveAnalogSohByProcessingChannelAndTimeRange() throws Exception {
    AcquiredChannelSohAnalog expectedResult = TestFixtures.channelSohAnalog;

    stationSohPersistenceJpa.storeAnalogSoh(expectedResult);

    testSohQueryByProcessingChannelAndTimeRange(expectedResult,
        (id, start, end) -> stationSohPersistenceJpa
            .retrieveAnalogSohByProcessingChannelAndTimeRange(id, start, end));
  }

  /**
   * Utility interface used to help test the id and time based channel soh queries
   *
   * @param <T> type of Acquired Channel Soh returned by the query ({@link
   * AcquiredChannelSohBoolean} or {@link AcquiredChannelSohAnalog})
   */
  @FunctionalInterface
  private interface QueryExecutor<T extends AcquiredChannelSoh> {

    List<T> execute(UUID id, Instant start, Instant end) throws Exception;
  }

  /**
   * Runs a variety of time and id based tests using the provided {@link QueryExecutor} to provide
   * query results and comparing them with the provided expected result
   *
   * @param expectedResult expected channel soh value obtained from some of the queries
   * @param query executes an id and time based {@link AcquiredChannelSoh} query
   * @param <T> type of AcquiredChannelSoh returned by the query ({@link AcquiredChannelSohBoolean}
   * or {@link AcquiredChannelSohAnalog})
   */
  private <T extends AcquiredChannelSoh> void testSohQueryByProcessingChannelAndTimeRange(
      T expectedResult, QueryExecutor<T> query) throws Exception {

    //test wrong processing channel id
    List<T> results = query.execute(
        new UUID(0L, 0L),
        TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_START.plus(1, ChronoUnit.MINUTES));
    assertTrue(results.isEmpty());

    //test range smaller than start
    results = query.execute(
        expectedResult.getChannelId(),
        TestFixtures.SEGMENT_START.minus(10, ChronoUnit.MINUTES),
        TestFixtures.SEGMENT_START.minus(1, ChronoUnit.MINUTES));
    assertTrue(results.isEmpty());

    //test range greater than end
    results = query.execute(
        expectedResult.getChannelId(),
        TestFixtures.SEGMENT_END.plus(1, ChronoUnit.MINUTES),
        TestFixtures.SEGMENT_START.plus(10, ChronoUnit.MINUTES));
    assertTrue(results.isEmpty());

    //test start inclusive
    results = query.execute(
        expectedResult.getChannelId(),
        TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_START.plus(1, ChronoUnit.MINUTES));

    assertEquals(1, results.size());
    assertEquals(expectedResult, results.get(0));

    //test end inclusive
    results = query.execute(
        expectedResult.getChannelId(),
        TestFixtures.SEGMENT_END.minus(1, ChronoUnit.MINUTES),
        TestFixtures.SEGMENT_END);

    assertEquals(1, results.size());
    assertEquals(expectedResult, results.get(0));

    //test inside time range
    results = query.execute(
        expectedResult.getChannelId(),
        TestFixtures.SEGMENT_START.plusMillis(10),
        TestFixtures.SEGMENT_END.minusMillis(10));

    assertEquals(1, results.size());
    assertEquals(expectedResult, results.get(0));

    //test outside time range
    results = query.execute(
        expectedResult.getChannelId(),
        TestFixtures.SEGMENT_START.minus(1, ChronoUnit.MINUTES),
        TestFixtures.SEGMENT_END.plus(1, ChronoUnit.MINUTES));

    assertEquals(1, results.size());
    assertEquals(expectedResult, results.get(0));
  }
}
