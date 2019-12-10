package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.shared.utilities.javautilities.gracefulthread.GracefulThread;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11DataConsumerSendAcknackThread extends GracefulThread {

  private static Logger logger = LoggerFactory.getLogger(Cd11DataConsumerSendAcknackThread.class);

  private final BlockingQueue<Message> eventQueue;
  private final Cd11Socket cd11Socket;

  public Cd11DataConsumerSendAcknackThread(
      String threadName,
      BlockingQueue<Message> eventQueue, Cd11Socket cd11Socket) {
    super(threadName, true, false);

    this.eventQueue = eventQueue;
    this.cd11Socket = cd11Socket;
  }

  @Override
  protected void onStart() {
    try {
      while (this.keepThreadRunning()) {
        long seconds = cd11Socket.secondsSinceLastAcknackSent();
        if (seconds > 55) {
          // Generate an event, if one does not already exist in the queue.
          logger.info("Generating send acknack event");
          Message msg = new Message(MessageType.SendAcknack);
          if (!eventQueue.contains(msg)) {
            eventQueue.put(msg);
          }

          Thread.sleep(56000);
        } else {
          Thread.sleep((56 - seconds) * 1000);
        }
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
