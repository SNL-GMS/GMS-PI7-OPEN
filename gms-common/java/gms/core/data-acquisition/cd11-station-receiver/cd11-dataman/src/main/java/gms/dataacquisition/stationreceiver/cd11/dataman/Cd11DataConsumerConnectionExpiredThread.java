package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11DataConsumerConnectionExpiredThread extends GracefulThread {

  private static Logger logger =
      LoggerFactory.getLogger(Cd11DataConsumerConnectionExpiredThread.class);

  private final BlockingQueue<Message> eventQueue;
  private final Cd11Socket cd11Socket;
  private final long connectionExpiredTimeLimitSec;

  public Cd11DataConsumerConnectionExpiredThread(
      String threadName, BlockingQueue<Message> eventQueue,
      Cd11Socket cd11Socket, long connectionExpiredTimeLimitSec) {
    super(threadName, true, false);

    this.eventQueue = eventQueue;
    this.cd11Socket = cd11Socket;
    this.connectionExpiredTimeLimitSec = connectionExpiredTimeLimitSec;
  }

  @Override
  protected void onStart() {
    try {
      while (this.keepThreadRunning()) {
        // Check whether the connection has expired.
        long seconds = cd11Socket.secondsSinceLastContact();
        if (seconds > connectionExpiredTimeLimitSec) {
          logger.warn(String.format("Sending shutdown event due to timeout: %s > %s configured seconds",
              seconds, connectionExpiredTimeLimitSec));
          // Generate an event.
          eventQueue.put(new Message(MessageType.Shutdown));

          // Shut down the thread.
          break;
        }
        final long sleepTime = (connectionExpiredTimeLimitSec - seconds) * 1000;
        logger.info("connection expired thread; connection did not expire, sleeping " + sleepTime + " ms");
        Thread.sleep(sleepTime);
      }
    } catch (InterruptedException e) {
      logger.debug(String.format(
          "InterruptedException thrown in thread %1$s, closing thread.", this.getThreadName()), e);
    }
  }
}
