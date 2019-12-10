package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.TestFixtures;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class RawStationDataFrameRepositoryJpaTests {

  private static RawStationDataFrameRepositoryInterface dataFramePersistence;

  @BeforeClass
  public static void setUp() throws Exception {
    EntityManagerFactory entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
    dataFramePersistence = new RawStationDataFrameRepositoryJpa(entityManagerFactory);
    // store 2 frames for further testing
    dataFramePersistence.storeRawStationDataFrame(TestFixtures.frame1);
    dataFramePersistence.storeRawStationDataFrame(TestFixtures.frame2);
  }

  @Ignore
  @Test(expected = DataExistsException.class)
  public void testStoreFrame1Again() throws Exception {
    dataFramePersistence.storeRawStationDataFrame(TestFixtures.frame1);
  }

  @Test
  public void retrieveByStationIdTest() throws Exception {
    // Query for frame1's id with time range [frame1.start, frame2.end], should only find frame1
    // since the id of frame2 is different.
    List<RawStationDataFrame> results = dataFramePersistence.retrieveByStationId(
            TestFixtures.frame1.getStationId(), TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END2);
    assertEquals(List.of(TestFixtures.frame1), results);
    // Query for frame2's id with time range [frame1.start, frame2.end], should only find frame2
    // since the id of frame1 is different.
    results = dataFramePersistence.retrieveByStationId(
            TestFixtures.frame2.getStationId(), TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END2);
    assertEquals(List.of(TestFixtures.frame2), results);
    // query for frame1's id with time range [frame1.start - 1, frame2.end],
    // finds frame 1 since it has some data for the first second of the range
    // (and frame2's id differs)
    results = dataFramePersistence.retrieveByStationId(
            TestFixtures.frame1.getStationId(), TestFixtures.SEGMENT_END.minusSeconds(1),
            TestFixtures.SEGMENT_END2);
    assertEquals(List.of(TestFixtures.frame1), results);
    // query for a fake name, with time range [frame1.start, frame2.end],
    // finds nothing because the id is no good.
    results = dataFramePersistence.retrieveByStationId(UUID.randomUUID(),
            TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END2);
    assertEquals(List.of(), results);
    // query for frame1's id in [EPOCH, frame1.start], only find frame 1.
    results = dataFramePersistence.retrieveByStationId(
            TestFixtures.frame1.getStationId(), Instant.EPOCH, TestFixtures.SEGMENT_START);
    assertEquals(List.of(TestFixtures.frame1), results);
    // query for frame1's name in [EPOCH, frame1.start - 1], find nothing because time range
    // is before frame1.start.
    results = dataFramePersistence.retrieveByStationId(
            TestFixtures.frame1.getStationId(), Instant.EPOCH, TestFixtures.SEGMENT_START.minusSeconds(1));
    assertEquals(List.of(), results);
    // query for frame2's id in [frame2.start, frame2.end], only finds frame2.
    results = dataFramePersistence.retrieveByStationId(
            TestFixtures.frame2.getStationId(), TestFixtures.SEGMENT_START2, TestFixtures.SEGMENT_END2);
    assertEquals(List.of(TestFixtures.frame2), results);
    // query for frame2's id in [frame1.start, frame2.start - 1], finds nothing.
    results = dataFramePersistence.retrieveByStationId(
            TestFixtures.frame2.getStationId(), TestFixtures.SEGMENT_START,
            TestFixtures.SEGMENT_START2.minusSeconds(1));
    assertEquals(List.of(), results);
  }

  @Test
  public void retrieveAllTest() throws Exception {
    // Retrieve frames, giving exact start/end times of the two known frames.
    // Ensure they were retrieved.
    List<RawStationDataFrame> results = dataFramePersistence.retrieveAll(
            TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END2);
    assertEquals(TestFixtures.allFrames, results);
    // query in [frame1.start - 1, frame2.start + 1],
    // since the time range (barely) touches both frames they should both be returned.
    results = dataFramePersistence.retrieveAll(TestFixtures.SEGMENT_END.minusSeconds(1),
            TestFixtures.SEGMENT_START2.plusSeconds(1));
    assertEquals(TestFixtures.allFrames, results);
    // query in [frame1.start - 1, frame2.end + 1], should find both frames.
    results = dataFramePersistence.retrieveAll(TestFixtures.SEGMENT_START.minusSeconds(1),
            TestFixtures.SEGMENT_END2.plusSeconds(1));
    assertEquals(TestFixtures.allFrames, results);
    // query in [EPOCH, frame1.start], finds the first frame only.
    results = dataFramePersistence.retrieveAll(Instant.EPOCH, TestFixtures.SEGMENT_START);
    assertEquals(List.of(TestFixtures.frame1), results);
    // query in [EPOCH, frame1.start - 1], finds nothing.
    results = dataFramePersistence.retrieveAll(Instant.EPOCH, TestFixtures.SEGMENT_START.minusSeconds(1));
    assertEquals(List.of(), results);
    // query in [frame2.start, frame2.end], only finds the 2nd frame.
    results = dataFramePersistence.retrieveAll(TestFixtures.SEGMENT_START2, TestFixtures.SEGMENT_END2);
    assertEquals(List.of(TestFixtures.frame2), results);
    // query in [frame2.end + 1, frame2.end + 61], finds nothing.
    results = dataFramePersistence.retrieveAll(TestFixtures.SEGMENT_END2.plusSeconds(1),
            TestFixtures.SEGMENT_END2.plusSeconds(61));
    assertEquals(List.of(), results);
  }

  @Test(expected = Exception.class)
  public void storeNullFrameTest() throws Exception {
    dataFramePersistence.storeRawStationDataFrame(null);
  }

}
