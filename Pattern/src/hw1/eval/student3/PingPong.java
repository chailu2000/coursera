package hw1.eval.student3;

/*
 * PingPong - Program creates two threads to play pingpong
 * -------------------------------------------------------
 *
 * General Implemenation Details:
 *
 *     PingPongBall class has two synchronized methods, ping and pong. 
 *     These ping and pong methods utilize wait() and notify() to coordinate
 *     turns.
 *     
 *     Player class is a thread and implements the player of the games.
 *
 *     Pong is derived from Player and defines Player's hitTheBall method
 *     calling ball.pong (); 
 *
 *     Ping is derived from Player and defines Player's hitTheBall method
 *     calling ball.ping (); 
 *
 */
public class PingPong {

    public static void main(String[] args) 
    {

        // Create synchronized "ball" object
        PingPongBall ball = new PingPongBall();

        // Number of hits
        int numHits = 10;

        // Create threads
        Ping ping = new Ping(ball, numHits);
        Pong pong = new Pong(ball, numHits);

        System.out.println ("Ready... Set... Go!\n");

        try {
          
            // Start threads
            ping.start();
            pong.start(); 

            // Wait for threads to complete
            ping.join ();
            pong.join ();
          
            System.out.println ("Done!");

        } 
        catch (InterruptedException e) { 
            System.err.println (e.getMessage());
        }
        
    } // main
}

/*
 * Class PingPongBall
 * ------------------
 *
 * Methods ping and pong are synchronized and use wait() and notify()
 * to coordinate turns.
 *
 */
class PingPongBall {

    private boolean pongsTurn = false;

    private boolean notPongsTurn ()
    {
        return (pongsTurn == false);
    }

    private boolean notPingsTurn ()
    {
        return (pongsTurn == true);
    }

    public synchronized void pong() 
    {
        while (notPongsTurn()) { // Wait for pong's turn
            try {
                wait(); // Wait for ping
            }
            catch (InterruptedException e) {
                System.err.println (e.getMessage());
            }
        }

        System.out.println("Pong!"); 

        pongsTurn = false;

        notify(); // Notify ping
    } // pong

    public synchronized void ping() 
    {
        while (notPingsTurn()) { // Wait for ping's turn
            try {
                wait(); // Wait for pong
            }
            catch (InterruptedException e) { 
                System.err.println (e.getMessage());
            } 
        }

        System.out.println("Ping!"); 

        pongsTurn = true;

        notify(); // Notify pong

    } // ping
}

/*
 * Class Player
 * ------------
 * Abstract implementation of Player thread.
 *
 * Constructor:  
 *    Player (ball, hits)
 *    where..
 *      ball - PingPongBall
 *      hits - The number of times to hit the ball 
 *
 */
abstract class Player extends Thread {

    public PingPongBall ball;

    private int numberOfHits;

    public Player (PingPongBall ball, int hits) 
    {
        this.ball    = ball;
        numberOfHits = hits;
    }

    abstract void hitTheBall (); // To be defined by concrete Players

    public void run() 
    {
        for (int hit = 0; hit < numberOfHits; hit++) {
            hitTheBall ();
        }
    }
}

/*
 * Class Pong
 * ----------
 * Concrete implementation of Player thread.
 *
 * Defines hitTheBall method using ball.pong ()
 *
 */
class Pong extends Player 
{
    public Pong (PingPongBall ball, int hits)
    {
        super (ball, hits);
    }

    public void hitTheBall () 
    {
        ball.pong ();
    }
}

/*
 * Class Ping
 * ----------
 * Concrete implementation of Player thread.
 *
 * Defines hitTheBall method using ball.ping ()
 *
 */
class Ping extends Player
{
    public Ping (PingPongBall ball, int hits)
    {
        super (ball, hits);
    }

    public void hitTheBall ()
    {
        ball.ping ();
    }
}
