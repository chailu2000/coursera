package hw1.eval.student4;

/**
 * A simple program to print ping and pong alternating to the console using two threads.
 * <p/>
 * Example output:
 * <p/>
 * Ready... Set... Go!
 * <p/>
 * Ping!
 * Pong!
 * Ping!
 * Pong!
 * Ping!
 * Pong!
 * Done!
 */
public class Program {

  public static final int STARTUP_MESSAGE_SLEEP_MS = 500;

  public static void main(String[] args) {
    displayStartupMessage();
    displayPingPongMessage();
    displayEndMessage();
  }

  private static void displayStartupMessage() {
    System.out.print("Ready... ");
    sleepIgnoringExceptions(STARTUP_MESSAGE_SLEEP_MS);
    System.out.print("Set... ");
    sleepIgnoringExceptions(STARTUP_MESSAGE_SLEEP_MS);
    System.out.println("Go!\n");
  }

  private static void displayPingPongMessage() {
    new PingPongWrapperFacade().run();
  }

  private static void sleepIgnoringExceptions(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      System.err.println("Could not sleep for the desired amount of time.");
    }
  }

  private static void displayEndMessage() {
    System.out.println("Done!");
  }
}

/**
 * Facade that allows to print alternating ping and pong in the console in a multi threaded way.
 * <p/>
 * Just call the public "run" method, it will return synchronously when ping and pong has been printed three times.
 */
class PingPongWrapperFacade {

  public static final int MESSAGE_PRINT_AMOUNT = 3;

  private PingPongSynchronizingMonitor pingPongSynchronizingMonitor;

  private Thread pingThread;
  private Thread pongThread;

  public PingPongWrapperFacade() {
    pingPongSynchronizingMonitor = new PingPongSynchronizingMonitor();
    pingThread = new RepeatingCommandExecutor(new PingMessagePrinterCommand(pingPongSynchronizingMonitor), MESSAGE_PRINT_AMOUNT);
    pongThread = new RepeatingCommandExecutor(new PongMessagePrinterCommand(pingPongSynchronizingMonitor), MESSAGE_PRINT_AMOUNT);
  }

  public void run() {
    startThreads();
    waitForThreadsToEnd();
  }

  private void startThreads() {
    pingThread.start();
    pongThread.start();
  }

  private void waitForThreadsToEnd() {
    waitForThreadIgnoringExceptions(pingThread);
    waitForThreadIgnoringExceptions(pongThread);
  }

  private void waitForThreadIgnoringExceptions(Thread thread) {
    try {
      thread.join();
    } catch (InterruptedException e) {
      System.err.println("Could not wait for the pingThread to shut down.");
    }
  }
}

/**
 * Ping Pong monitor for use in a multi threaded context. Two concurrent threads may use ping and pong while the monitor
 * assures that ping and pong is printed alternating. stdout is synchronized.
 */
class PingPongSynchronizingMonitor {

  public static final int DEADLOCK_WAIT_TIMEOUT_MS = 10000;

  public enum PingPongStatus {
    PING,
    PONG
  }

  ;

  private PingPongStatus currentState = PingPongStatus.PING;

  public synchronized void sayPing() throws InterruptedException {
    say(PingPongStatus.PING, "Ping!");
  }

  public synchronized void sayPong() throws InterruptedException {
    say(PingPongStatus.PONG, "Pong!");
  }

  private void say(PingPongStatus expectedStatus, String message) throws InterruptedException {
    while (currentState != expectedStatus) {
      this.wait(DEADLOCK_WAIT_TIMEOUT_MS);
    }

    doSay(message);
    setNextState();
    notifyAll();
  }

  private void setNextState() {
    if (currentState == PingPongStatus.PING) {
      currentState = PingPongStatus.PONG;
    } else {
      currentState = PingPongStatus.PING;
    }
  }

  private void doSay(String message) {
    System.out.println(message);
  }
}

/**
 * A simple, eventually terminating command that can be executed in a threaded context. Upon interruption it should
 * propagate the exception upwards.
 */
interface Command {
  void execute() throws InterruptedException;
}

/**
 * Concrete command implementation to print ping to the command line, using the ping pong synchronization monitor.
 */
class PingMessagePrinterCommand implements Command {
  private PingPongSynchronizingMonitor pingPongSynchronizingMonitor;

  PingMessagePrinterCommand(PingPongSynchronizingMonitor pingPongSynchronizingMonitor) {
    this.pingPongSynchronizingMonitor = pingPongSynchronizingMonitor;
  }

  @Override
  public void execute() throws InterruptedException {
    pingPongSynchronizingMonitor.sayPing();
  }
}

/**
 * Concrete command implementation to print pong to the command line, using the ping pong synchronization monitor.
 */
class PongMessagePrinterCommand implements Command {
  private PingPongSynchronizingMonitor pingPongSynchronizingMonitor;

  PongMessagePrinterCommand(PingPongSynchronizingMonitor pingPongSynchronizingMonitor) {
    this.pingPongSynchronizingMonitor = pingPongSynchronizingMonitor;
  }

  @Override
  public void execute() throws InterruptedException {
    pingPongSynchronizingMonitor.sayPong();
  }
}

/**
 * Executes a given command for numberOfExecutions times in a new thread or until the thread is interrupted.
 */
class RepeatingCommandExecutor extends Thread {
  private final int numberOfExecutions;

  private final Command command;

  RepeatingCommandExecutor(Command command, int numberOfExecutions) {
    assert numberOfExecutions > 0;
    this.command = command;
    this.numberOfExecutions = numberOfExecutions;
  }

  @Override
  public void run() {
    try {
      int executionsCounter = 0;
      while (executionsCounter < numberOfExecutions && !Thread.interrupted()) {
        command.execute();
        ++executionsCounter;
      }
    } catch (InterruptedException e) {
      // Terminate quietly
      return;
    }
  }

}

