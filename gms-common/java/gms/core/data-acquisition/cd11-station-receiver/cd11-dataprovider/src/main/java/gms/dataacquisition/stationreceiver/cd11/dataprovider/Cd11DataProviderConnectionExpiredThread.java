package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11DataProviderConnectionExpiredThread extends GracefulThread {

  private static Logger logger =
      LoggerFactory.getLogger(Cd11DataProviderConnectionExpiredThread.class);

  private final BlockingQueue<Message> eventQueue;
  private final Cd11Socket cd11Socket;
  private final long connectionExpiredTimeLimitSec;

  public Cd11DataProviderConnectionExpiredThread(
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
          logger.error("Shutting down thread because seconds " + seconds + "s > " + connectionExpiredTimeLimitSec);
          // Generate an event.
          eventQueue.put(new Message(MessageType.Shutdown));

          // Shut down the thread.
          break;
        }

        Thread.sleep((connectionExpiredTimeLimitSec - seconds) * 1000);
      }
    } catch (InterruptedException e) {
      logger.debug(String.format(
          "InterruptedException thrown in thread %1$s, closing thread.", this.getThreadName()), e);
    } catch (Exception e) {
      logger.error(String.format(
          "Unexpected exception thrown in thread %1$s, and thread must now close.",
          this.getThreadName()), e);
    }
  }
}
