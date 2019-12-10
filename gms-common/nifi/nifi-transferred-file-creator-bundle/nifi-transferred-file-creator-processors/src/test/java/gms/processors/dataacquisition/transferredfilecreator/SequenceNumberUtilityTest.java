package gms.processors.dataacquisition.transferredfilecreator;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class SequenceNumberUtilityTest {

    private static String testSeqNumFile = "src/test/resources/seqNumFile.txt";
    private static long startingSeqNum = 300; //should match whats in the file above


    @Before
    public void setup() throws Exception {
        reset();
    }

    //needed to set the file back to default original starting value
    @AfterClass
    public static void reset() throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(testSeqNumFile));
        writer.write(String.valueOf(startingSeqNum));
        writer.close();
    }

    //Each call should return 1 + the previous sequence number, starts at 300
    @Test
    public void testGetAndUpdateSequenceNumber() throws IOException {
        assertEquals(startingSeqNum + 1, SequenceNumberUtility.getAndUpdateSequenceNumber(testSeqNumFile));
        assertEquals(startingSeqNum + 2, SequenceNumberUtility.getAndUpdateSequenceNumber(testSeqNumFile));
        assertEquals(startingSeqNum + 3, SequenceNumberUtility.getAndUpdateSequenceNumber(testSeqNumFile));

    }


    //Shouldn't be any duplicate sequence numbers since it is synchronized
    @Test
    public void testMultipleThreads() {
        final int updateTimes = 1000;
        final TestThread thread1 = new TestThread(updateTimes);
        final TestThread thread2 = new TestThread(updateTimes);

        thread1.start();
        thread2.start();

        thread1.waitUntilThreadStops();
        thread2.waitUntilThreadStops();

        final List<Long> allSeqs = new ArrayList<>(thread1.getSequenceNumbers());
        allSeqs.addAll(thread2.getSequenceNumbers());
        Collections.sort(allSeqs);

        //shouldn't be any duplicates, everything should be sequential
        assertEquals(startingSeqNum + 1, allSeqs.get(0).longValue());
        for(int i=1; i<allSeqs.size(); i++){
            assertEquals(allSeqs.get(i-1) + 1, allSeqs.get(i).longValue());
        }
    }

    private static void populateSeqNums(int updateTimes, List<Long> threadSeqNums) {
        for(int i=0; i<updateTimes; i++) {
            try {
                threadSeqNums.add(SequenceNumberUtility.getAndUpdateSequenceNumber(testSeqNumFile));
            } catch (IOException e) {
                e.printStackTrace();
                fail("boo");
            }
        }
    }

    private static final class TestThread extends GracefulThread {

        private final int updateTimes;
        private final List<Long> seqNums = new ArrayList<>();

        public TestThread(int updateTimes) {
            super("goodThread" + System.currentTimeMillis(),
                true, true);
            this.updateTimes = updateTimes;
        }

        @Override
        protected void onStart() {
            populateSeqNums(updateTimes, seqNums);
        }

        public List<Long> getSequenceNumbers() {
            return Collections.unmodifiableList(this.seqNums);
        }
    }
}
