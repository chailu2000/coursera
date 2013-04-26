package hw1.eval.student2;

import java.util.concurrent.locks.*;

/**
 * Starts two Threads, each with an initial state, Ping or Pong.
 * Synchronizes on Main thread's PingPongState object and printnumber. 
 *
 */
public class ThreadedPingPong 
{
    private static enum PingPongState { Ping, Pong };
    private static PingPongState state = PingPongState.Ping;
    private static int printNumber = 5;
    
    public static void main(String[] args) throws Exception
    {
        System.out.println("Ready... Set... Go!\n");
        Thread pingThread = new Thread(new PingPongRunnable(PingPongState.Ping), "ping");
        Thread pongThread = new Thread(new PingPongRunnable(PingPongState.Pong), "pong");
        pingThread.start();
        pongThread.start();
        pingThread.join();
        pongThread.join();
        System.out.println("Done!");
    }
    
    public static void toggleState() 
    {
        state = (state == PingPongState.Ping) ? PingPongState.Pong : PingPongState.Ping;
    }
    
    public static PingPongState getState()
    {
        return state;
    }

    /**
     * Each Runnable will print its state when it matches the Main thread.
     * Otherwise it will wait its turn.
     *
     */
    public static class PingPongRunnable implements Runnable
    {
        public PingPongRunnable(PingPongState state)
        {
            myState = state;
        }
        
        @Override
        public void run()
        {
            printLock.lock();
            try 
            {
                while (printNumber > 0)
                {
                    while (myState != getState()) 
                    {
                        //Thread.sleep(500);
                        myTurnToPrint.await();
                    }
                    
                    System.out.println(myState.toString() + "!");
                    toggleState();
                    printNumber--;
                    myTurnToPrint.signalAll();
                }
            }
            catch (InterruptedException e) {} 
            finally
            {
                printLock.unlock();
            }
        }
        
        private PingPongState myState;
        private static final Lock printLock = new ReentrantLock();
        private static final Condition myTurnToPrint = printLock.newCondition();
    };
}