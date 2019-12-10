package gms.shared.utilities.javautilities.gracefulthread;

import static gms.shared.utilities.javautilities.assertwait.AssertWait.assertTrueWait;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;


public class GracefulThreadTest {

  /**
   * Tests that a GracefulThread can be started and stopped correctly.
   */
  private class BasicFunctioningThread extends GracefulThread {

    private AtomicInteger runCounter = new AtomicInteger(0);
    private AtomicInteger onStopMethodCalled = new AtomicInteger(0);
    private AtomicBoolean onBeforeStartMethodCalled = new AtomicBoolean(false);

    public BasicFunctioningThread() {
      super(
          BasicFunctioningThread.class.getName(),
          true,
          true);
    }

    @Override
    protected boolean onBeforeStart() {
      onBeforeStartMethodCalled.set(true);
      runCounter.set(0);
      onStopMethodCalled.set(0);
      return true;
    }

    @Override
    protected void onStart() {
      while (this.keepThreadRunning()) {
        runCounter.incrementAndGet();
        this.setThreadAsInitialized();
        try {
          Thread.sleep(1000); // This will be interrupted.
        } catch (InterruptedException e) {
          // Do nothing.
        }
      }
    }

    @Override
    protected void onStop() {
      onStopMethodCalled.incrementAndGet();
      onBeforeStartMethodCalled.set(false);
    }

    public boolean mainLoopRunning() {
      return runCounter.get() > 0;
    }

    public boolean onStopMethodWasCalled() {
      return onStopMethodCalled.get() > 0;
    }

    public int onStopMethodCalledCount() {
      return onStopMethodCalled.get();
    }

    public boolean onBeforeStartMethodWasCalled() {
      return onBeforeStartMethodCalled.get();
    }
  }

  /**
   * Tests that a GracefulThread handles uncaught exceptions properly.
   */
  private class UncaughtExceptionThread extends GracefulThread {

    public final String EXCEPTION_MESSAGE = "Test exception 142536475890!";
    public AtomicBoolean uncaughtExceptionCaught = new AtomicBoolean(false);

    public UncaughtExceptionThread() {
      super(UncaughtExceptionThread.class.getName(),
          false,
          false);
    }

    @Override
    protected void onStart() {
      int var = 5 / 0; // Throw an exception.
    }

    @Override
    protected void onUncaughtException(Thread thread, Throwable throwable) {
      uncaughtExceptionCaught.set(true);
    }

    public boolean exceptionWasCaught() {
      return uncaughtExceptionCaught.get();
    }
  }

  /**
   * Tests that a blocking call can be interrupted, and the thread shutdown.
   */
  private class InterruptBlockingCall extends GracefulThread {

    private AtomicBoolean threadIsBlocked = new AtomicBoolean(false);
    private AtomicBoolean threadWasInterrupted = new AtomicBoolean(false);

    public InterruptBlockingCall() {
      super(
          InterruptBlockingCall.class.getName(),
          true,
          true);
    }

    @Override
    protected void onStart() {
      while (true) {
        threadIsBlocked.set(true);
        this.setThreadAsInitialized();
        try {
          Thread.sleep(1000); // This will be interrupted.
        } catch (InterruptedException e) {
          threadWasInterrupted.set(true);
          break; // Break out of the loop!
        }
      }
      threadIsBlocked.set(false);
    }

    public boolean isThreadBlocked() {
      return threadIsBlocked.get();
    }

    public boolean wasThreadInterrupted() {
      return threadWasInterrupted.get();
    }
  }

  /**
   * Tests that a GracefulThread can be waited on, until it shuts down.
   */
  private class WaitForThreadToStop extends GracefulThread {

    private long sleepTimeMs;

    public WaitForThreadToStop(long sleepTimeMs) {
      super(WaitForThreadToStop.class.getName(),
          false,
          false);
      this.sleepTimeMs = sleepTimeMs;
    }

    @Override
    protected void onStart() {
      try {
        Thread.sleep(sleepTimeMs);
      } catch (InterruptedException e) {
        // Do nothing.
      }
    }
  }

  /**
   * Tests that automatic initialization and the isInitialized() method works as expected.
   */
  private class InitializationThread extends GracefulThread {

    CountDownLatch startLatch = new CountDownLatch(1);

    public InitializationThread() {
      super(InitializationThread.class.getName(),
          false,
          false);
    }

    @Override
    protected void onStart() throws Exception {
      startLatch.await();
    }

    public void setStartLatch() {
      startLatch.countDown();
    }
  }

  /**
   * Tests that manual initialization works as expected.
   */
  private class InitializableThread extends GracefulThread {

    CountDownLatch startPreInitLatch = new CountDownLatch(1);
    CountDownLatch startPostInitLatch = new CountDownLatch(1);

    public InitializableThread() {
      super(InitializableThread.class.getName(),
          false,
          true);
    }

    @Override
    protected void onStart() throws Exception {
      startPreInitLatch.await();
      this.setThreadAsInitialized();
      startPostInitLatch.await();
    }

    public void setStartPreInitLatch() {
      startPreInitLatch.countDown();
      ;
    }

    public void setStartPostInitLatch() {
      startPostInitLatch.countDown();
      ;
    }
  }

  /**
   * Tests that a GracefulThread that is not configured for manual initialization will throw an
   * exception when the setThreadAsInitialized() is called.
   */
  private class NonInitializableThread extends GracefulThread {

    public NonInitializableThread() {
      super(NonInitializableThread.class.getName(),
          false,
          false);
    }

    @Override
    protected void onStart() throws Exception {
      // Manually attempt to initialize this thread.
      this.setThreadAsInitialized();
    }
  }

  @Test
  public void testBasicFunctionality() {
    BasicFunctioningThread gt = new BasicFunctioningThread();

    // Check that the thread name is correct.
    assertTrue(gt.getThreadName().equals(BasicFunctioningThread.class.getName()));

    // Run the thread multiple times, to ensure that threads can be safely restarted.
    for (int i = 0; i < 3; i++) {
      // Check that the thread is stopped.
      assertFalse(gt.isRunning());

      // Start the thread.
      System.out.println(String.format("Starting thread #%d.", i + 1));
      gt.start();

      // Check that the thread is running.
      assertTrue(gt.isRunning());

      // Check that the main loop is running.
      gt.waitUntilThreadInitializes();
      assertTrue(gt.mainLoopRunning());

      // Stop the thread.
      gt.stop();
      gt.waitUntilThreadStops();

      // Check that the thread is stopped.
      assertFalse(gt.isRunning());

      // Check that the onStop() method was run.
      assertTrue(gt.onStopMethodWasCalled());

      // Check the last error message.
      assertFalse(gt.hasErrorMessage());
      assertNull(gt.getErrorMessage());
    }
  }

  @Test(expected = IllegalThreadStateException.class)
  public void testStartingARunningThread() {
    BasicFunctioningThread gt = new BasicFunctioningThread();
    gt.start();
    gt.start(); // Throws exception, since thread is already running.
  }

  @Test
  public void testMultipleCallsToStopMethod() {
    BasicFunctioningThread gt = new BasicFunctioningThread();
    assertFalse(gt.onStopMethodWasCalled());

    // Calling stop() before the thread is started should not cause onStop() to run.
    gt.stop(); // The onStop() method should not be called, since the thread has not been started.
    assertFalse(gt.onStopMethodWasCalled());

    gt.start();
    assertTrue(gt.onStopMethodCalledCount() == 0);
    gt.stop();
    assertTrue(gt.onStopMethodCalledCount() == 1);
    gt.stop();
    assertTrue(gt.onStopMethodCalledCount() == 1); // No change, second call is ignored.
    gt.stop();
    assertTrue(gt.onStopMethodCalledCount() == 1); // No change, third call is ignored.
  }

  @Test
  public void testUncaughtExceptionMechanism() {
    UncaughtExceptionThread gt = new UncaughtExceptionThread();
    assertFalse(gt.hasThrownAnUncaughtException());
    assertFalse(gt.hasErrorMessage());
    gt.start();
    gt.waitUntilThreadStops();
    assertFalse(gt.isRunning());
    assertTrue(gt.hasThrownAnUncaughtException());
    assertTrue(gt.hasErrorMessage());
    //System.out.println(gt.getErrorMessage());
  }

  @Test
  public void testInterruptBlockingCall() {
    InterruptBlockingCall gt = new InterruptBlockingCall();
    gt.start();
    gt.waitUntilThreadInitializes();

    // Check that the thread is blocking.
    assertTrue(gt.isThreadBlocked());
    assertFalse(gt.wasThreadInterrupted());

    // Interrupt the thread.
    gt.stop();
    gt.waitUntilThreadStops();

    // Check that the thread is no longer blocking.
    assertFalse(gt.isThreadBlocked());
    assertTrue(gt.wasThreadInterrupted());

    // Check that thread shut down.
    assertFalse(gt.isRunning());
  }

  @Test
  public void testWaitForThreadToStop() {
    long sleepTimeMs = 100;
    long sleepTimeNs = sleepTimeMs * 1000000;
    WaitForThreadToStop gt = new WaitForThreadToStop(sleepTimeMs);

    long startTimeNs = System.nanoTime();
    gt.start();
    gt.waitUntilThreadStops();
    long elapsedTimeNs = System.nanoTime() - startTimeNs;
    assertTrue(elapsedTimeNs > sleepTimeNs);
  }

  @Test
  public void testInitializationThread() {
    InitializationThread gt = new InitializationThread();

    assertFalse(gt.isRunning());
    assertFalse(gt.isInitialized());

    gt.start();

    assertTrue(gt.isRunning());
    // assertFalse(gt.isInitialized()); // Causes race condition.

    gt.waitUntilThreadInitializes();

    assertTrue(gt.isRunning());
    assertTrue(gt.isInitialized());

    gt.setStartLatch();
    gt.waitUntilThreadStops();

    assertFalse(gt.isRunning());
    assertFalse(gt.isInitialized());
  }

  @Test
  public void testInitializableThread() {
    InitializableThread gt = new InitializableThread();

    assertFalse(gt.isRunning());
    assertFalse(gt.isInitialized());

    gt.start();

    assertTrue(gt.isRunning());
    assertFalse(gt.isInitialized());

    gt.setStartPreInitLatch();

    assertTrue(gt.isRunning());
    assertTrueWait(gt::isInitialized, 100000);

    gt.setStartPostInitLatch();
    gt.waitUntilThreadStops();

    assertFalse(gt.isRunning());
    assertFalse(gt.isInitialized());
  }

  @Test
  public void testInitializeNonconfiguredThread() {
    NonInitializableThread gt = new NonInitializableThread();
    gt.start();
    gt.waitUntilThreadStops();
    assertTrue(gt.hasErrorMessage());
    assertTrue(gt.hasThrownAnUncaughtException());
    assertTrue(gt.getUncaughtException() instanceof IllegalStateException);
  }
}
