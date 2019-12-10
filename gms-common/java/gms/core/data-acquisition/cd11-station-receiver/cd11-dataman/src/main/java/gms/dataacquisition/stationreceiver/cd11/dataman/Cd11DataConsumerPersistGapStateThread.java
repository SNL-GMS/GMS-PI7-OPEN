package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11DataConsumerPersistGapStateThread extends GracefulThread {

  private static Logger logger = LoggerFactory.getLogger(Cd11DataConsumerPersistGapStateThread.class);

  private final BlockingQueue<Message> eventQueue;
  private final long storeGapStateTimeoutMs;

  public Cd11DataConsumerPersistGapStateThread(
      String threadName, BlockingQueue<Message> eventQueue,
      long storeGapStateTimeoutInMinutes) {
    super(threadName, true, false);

    Validate.isTrue(storeGapStateTimeoutInMinutes > 0);

    this.eventQueue = eventQueue;
    this.storeGapStateTimeoutMs = storeGapStateTimeoutInMinutes * 60 * 1000;
  }

  @Override
  protected void onStart() {
    try {
      while (this.keepThreadRunning()) {
        Thread.sleep(storeGapStateTimeoutMs);
        logger.info("Putting persist gap state event onto queue");
        Message msg = new Message(MessageType.PersistGapState);
        if (!eventQueue.contains(msg)) {
          eventQueue.put(msg);
        }
      }
    } catch (InterruptedException e) {
      logger.debug(String.format(
          "InterruptedException thrown in thread %1$s, closing thread: %2$s",
          this.getThreadName(), e.getMessage()));
    }
  }
}
