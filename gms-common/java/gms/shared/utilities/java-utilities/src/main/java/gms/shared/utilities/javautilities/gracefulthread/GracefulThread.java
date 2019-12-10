package gms.shared.utilities.javautilities.gracefulthread;

import java.lang.Thread.State;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

abstract public class GracefulThread implements Runnable {

  // These properties are initialized during object construction.
  private final AtomicReference<String> threadName;
  private final boolean interruptThreadOnStop;
  private final boolean manuallySetThreadAsInitialized;

  // These properties are set/reset by the start() method.
  private Thread thread;
  private AtomicReference<Throwable> uncaughtException = new AtomicReference<>(null);
  private AtomicBoolean shutdownFlag = new AtomicBoolean(false);
  private CountDownLatch threadIsInitialized = new CountDownLatch(1);
  private AtomicReference<String> lastErrorMessage = new AtomicReference<>(null);

  //-------------------- Constructor and Shutdown Handler --------------------

  /**
   * Constructor.
   *
   * @param threadName The name of the thread.
   * @param interruptThreadOnStop If true, calls the stop() method when a shutdown event is
   * triggered (i.e. Ctrl+C, system shutdown, etc).
   * @param manuallySetThreadAsInitialized If true, the setThreadAsInitialized() method must be
   * called in order for the thread to be set as initialized. If false, then the thread will
   * automatically be set as initialized once the run() method begins executing.
   */
  public GracefulThread(
      String threadName, boolean interruptThreadOnStop, boolean manuallySetThreadAsInitialized) {
    Validate.notBlank(threadName);
    this.threadName = new AtomicReference<>(threadName.trim());
    this.interruptThreadOnStop = interruptThreadOnStop;
    this.manuallySetThreadAsInitialized = manuallySetThreadAsInitialized;

    // Configure the thread.
    this.configureNewThread();

    // Register the shutdown handler.
    Runnable shutdownHook = () -> {
      if (this.isRunning()) {
        // Stop the thread.
        this.stop();
      }
    };
    Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
  }

  private void configureNewThread() {
    thread = new Thread(this, this.threadName.get());
    thread.setUncaughtExceptionHandler(this::uncaughtExceptionHandler);
  }

  //-------------------- Thread Start, Stop, and Interrupt Methods --------------------

  /**
   * Runs the onBeforeStart() override method, and starts the thread.
   *
   * NOTE: If onBeforeStart() returns False, an exception will be thrown, and the thread will not be
   * started.
   */
  public final void start() {
    // Check if the thread is already running.
    if (this.isRunning()) {
      throw new IllegalThreadStateException("Thread is already running.");
    }

    // Reset the state of the GracefulThread.
    uncaughtException.set(null);
    shutdownFlag.set(false);
    threadIsInitialized = new CountDownLatch(1);
    lastErrorMessage.set(null);

    // Reconfigure the thread (if it has already been started once before).
    if (!thread.getState().equals(State.NEW)) {
      configureNewThread();
    }

    // Run the onBeforeStart() actions.
    if (!this.onBeforeStart()) {
      throw new IllegalThreadStateException(
          "The onBeforeStart() method prevented this thread from starting.");
    }

    // Start the thread.
    thread.start();
  }

  /**
   * Executes the thread.
   */
  @Override
  public final void run() {
    try {
      // Indicate that the thread is initialized.
      if (!manuallySetThreadAsInitialized) {
        this.threadIsInitialized.countDown();
      }

      // Run the main thread logic.
      this.onStart();

    } catch (Exception e) {
      this.uncaughtExceptionHandler(thread, e);
    }
  }

  /**
   * Signals the thread to shut down gracefully, and runs the onStop() override method.
   *
   * NOTE: If the thread has not yet been started, is not running, or if the stop() method has
   * already been called once on a running thread; then calling stop() again will do nothing (i.e.
   * the onStop() override method will not be run twice).
   */
  public final void stop() {
    // Interrupt the thread, if configured to do so.
    if (interruptThreadOnStop) {
      this.interruptRunningThread();
    }

    // Only allow the onStop() method to be called once, after a thread has been started.
    if (!this.isRunning() || shutdownFlag.get()) {
      // Ignore this call, since the stop() method has already been run once.
      return;
    }

    // Set the shutdownFlag flag.
    shutdownFlag.set(true);

    // Run additional actions, specified by the derived class.
    this.onStop();
  }

  /**
   * Interrupts the running thread by throwing an InterruptedException, which causes most blocking
   * calls to throw the exception.
   *
   * Meant for use by derived classes who override the onStop() method.
   */
  protected final void interruptRunningThread() {
    if (this.isRunning()) {
      // Cause all blocking operations to throw an InterruptedException.
      thread.interrupt();
    }
  }

  //-------------------- Thread Running Status Methods --------------------

  /**
   * Indicates whether the object's thread is running.
   *
   * @return True if the thread is currently running, false if it is not.
   */
  public final boolean isRunning() {
    return thread.isAlive();
  }

  //-------------------- Thread Initialization Methods --------------------

  /**
   * Indicates whether the thread has been initialized: - For a GracefulThread that is not
   * configured for manual initialization, this means that the thread's run() method is executing. -
   * For a GracefulThread that is configured for manual initialization, this means that the thread's
   * setThreadAsInitialized() method has been called.
   *
   * @return True if the thread is running and has been initialized, otherwise false.
   */
  public final boolean isInitialized() {
    return this.isRunning() && threadIsInitialized.getCount() <= 0;
  }

  protected final void setThreadAsInitialized() {
    // Check whether manual initialization has been configured (see GracefulThread constructor).
    if (!manuallySetThreadAsInitialized) {
      throw new IllegalStateException(
          "This GracefulThread is not configured for manual initialization.");
    }

    // Check whether the thread is already initialized.
    if (threadIsInitialized.getCount() > 0) {
      threadIsInitialized.countDown();
    }
  }

  //-------------------- Thread Shutdown Status Methods --------------------

  /**
   * Indicates that the thread should shutdownFlag immediately (meant to be polled by derived
   * classes). (NOTE: Returns the opposite of the keepThreadRunning() method.)
   *
   * @return True if the thread is expected to shutdownFlag, false if it is expected to keep
   * running.
   */
  protected final boolean shutThreadDown() {
    return shutdownFlag.get();
  }

  /**
   * Indicates that the thread is should keep running (meant to be polled by derived classes).
   * (NOTE: Returns the opposite of the shutThreadDown() method.)
   *
   * @return True if the thread is expected to keep running, false if it is expected to shut down.
   */
  protected final boolean keepThreadRunning() {
    return !shutdownFlag.get();
  }

  //-------------------- Error and Exception Handling Methods --------------------

  /**
   * Returns true if an error message was recorded.
   *
   * @return True if an error message exists, false otherwise.
   */
  public final boolean hasErrorMessage() {
    return (lastErrorMessage.get() != null);
  }

  /**
   * Returns the last error message set by the thread (useful for debugging thread crashes).
   *
   * @return The last error message recorded by the thread.
   */
  public final String getErrorMessage() {
    return lastErrorMessage.get();
  }

  /**
   * Sets the last error message (meant for use by the derived thread).
   *
   * @param errorMessage Error message.
   */
  protected final void setErrorMessage(String errorMessage) {
    lastErrorMessage.set(errorMessage);
  }

  /**
   * Returns true if an uncaught exception was thrown.
   *
   * @return True if uncaught exception was thrown, otherwise false.
   */
  public final boolean hasThrownAnUncaughtException() {
    return uncaughtException.get() != null;
  }

  /**
   * Returns the uncaught exception, or null if no uncaught exception was thrown.
   */
  public final Throwable getUncaughtException() {
    return uncaughtException.get();
  }

  /**
   * Catches uncaught exceptions thrown by the thread, logs them, and sets the lastErrorMessage.
   *
   * @param thread Thread that threw the uncaught exception.
   * @param throwable Exception object.
   */
  private void uncaughtExceptionHandler(Thread thread, Throwable throwable) {
    // Store the uncaught exception.
    uncaughtException.set(throwable);

    // Set the last error message.
    String errMsg = String.format(
        "Uncaught Exception Thrown by thread %s: %s",
        thread.getName(), ExceptionUtils.getStackTrace(throwable));
    if (this.hasErrorMessage()) {
      errMsg = String.format(
          "%s\n----------\n%s", errMsg, this.getErrorMessage());
    }
    this.setErrorMessage(errMsg);

    // Runs additional actions, if the derived class has overridden the method.
    this.onUncaughtException(thread, throwable);
  }

  //-------------------- Helper Methods --------------------

  /**
   * Aggregates all of the "last error" messages produced by GracefulThread objects into a single
   * string.
   *
   * @param gracefulThreads Any number of GracefulThread objects.
   * @return String containing all "last error" messages.
   */
  public static String aggregateErrorMessages(GracefulThread... gracefulThreads) {
    if (gracefulThreads.length < 1) {
      return "";
    } else {
      String msg = "";

      for (GracefulThread gt : gracefulThreads) {
        msg += gt.hasErrorMessage() ?
            String.format("\n%1$s:\n%2$s\n", gt.getThreadName(), gt.getErrorMessage()) : "";
      }

      return (msg.trim().isEmpty()) ? "" : "\n--- THREAD ERROR MESSAGES ---\n" + msg;
    }
  }

  //-------------------- Overridable Methods --------------------

  /**
   * Derived classes may override this method to execute code immediately before the thread is
   * started.
   *
   * If this method returns False, an exception will be thrown and the thread will not be started.
   *
   * NOTE: This is useful for: 1) resetting the state of an object, before the thread is run for a
   * second time. 2) checking that the object is in a good state before it is started. 3) preventing
   * a thread from being started or restarted in a bad state.
   *
   * @return True if the thread should be started, False if it should not.
   */
  protected boolean onBeforeStart() {
    return true;
  }

  /**
   * Derived classes may optionally override this method to implement additional actions that will
   * be performed when the stop() method is called.
   */
  protected void onStop() {
  }

  /**
   * Derived classes may optionally override this method to implement additional actions that will
   * be performed when an uncaught exception is encountered.
   *
   * @param thread Thread that threw the uncaught exception.
   * @param throwable Exception object.
   */
  protected void onUncaughtException(Thread thread, Throwable throwable) {
  }

  /**
   * Derived classes must override this method to implement their thread logic. Once this method is
   * called, the thread is considered to be "initialized": 1) the isInitialized() method will return
   * true, and 2) the waitUntilThreadInitializes() method will not block (i.e returns immediately).
   *
   * @throws Exception Uncaught exception thrown by the thread.
   */
  abstract protected void onStart() throws Exception;

  //-------------------- Misc --------------------

  /**
   * Returns the name of the thread.
   *
   * @return Thread name.
   */
  public final String getThreadName() {
    return threadName.get();
  }

  /**
   * Wait for the thread to fully initialize: - For a GracefulThread that is not configured for
   * manual initialization, this means that the thread's run() method is executing. - For a
   * GracefulThread that is configured for manual initialization, this means that the thread's
   * setThreadAsInitialized() method has been called.
   */
  public final void waitUntilThreadInitializes() {
    if (this.isRunning() && !this.isInitialized()) {
      try {
        threadIsInitialized.await();
      } catch (InterruptedException e) {
        // Break out of the blocking call.
      }
    }
  }

  /**
   * Wait for this thread to die (i.e. calls thread.join() method).
   */
  public final void waitUntilThreadStops() {
    if (this.isRunning()) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        // Do nothing.
      }
    }
  }
}
