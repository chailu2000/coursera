package hw2.eval.student2;

import java.util.Random;

public class DiningPhilosophers {

    final static int NUM_PHILOSOPHERS = 5;
    final static int NUM_MEALS = 50;

    static class Chopstick {
        private boolean used;
        public boolean isUsed(){ return used; }
        public void setUsed(boolean used){ this.used = used;}
    }

    static class Philosopher extends Thread {

        private static final int TIME_TO_THINKING = 1000;
        private static final int TIME_TO_EAT= 800;

        private Random random = new Random();

        private Chopstick left;
        private Chopstick right;
        private int _state;
        private String id;

        public Philosopher(String id, Chopstick left, Chopstick right) {
            this.left = left;
            this.right = right;
            this.id = id;
        }

        private void think() throws InterruptedException {
            this.setState(1);  //think
            //Thread.sleep(random.nextInt(TIME_TO_THINKING));
        }

        private void setState(int state){ this._state = state; }

        private void eat() throws InterruptedException {
            synchronized(left){
                while( left.isUsed() || right.isUsed()) {
                    try{
                        this.setState( 2 );  //wait
                        left.wait();
                    }
                    catch (InterruptedException e){ }
                }

                synchronized( right ) {
                    try{
                        //Thread.sleep(1);
                        left.setUsed(true);
                        System.out.println( id + " picks up left chopstick.");
                        right.setUsed(true);
                        System.out.println( id + " picks up right chopstick.");
                        this.setState( 0 );
                        System.out.println( id + " eats.");

                        //Thread.sleep(random.nextInt(TIME_TO_EAT));
                    }
                    finally {
                        left.setUsed(false);
                        System.out.println( id + " puts down left chopstick.");
                        right.setUsed(false);
                        System.out.println( id + " puts down right chopstick.");
                        left.notify();
                        right.notify();
                    }
                }
            }

            think();
        }

        public void run(){
            for(int i =0; i< NUM_MEALS ;i++){
                try { eat();}
                catch (InterruptedException e) { e.printStackTrace(); }
            }
        }

    }

    public static void main(String[] args){

        Chopstick[] chopsticks = new Chopstick[NUM_PHILOSOPHERS];
        Thread []   philosophers = new Thread[NUM_PHILOSOPHERS];

        /*Initialise*/
        for(int i = 0;i < NUM_PHILOSOPHERS;i++) { chopsticks[i]= new Chopstick(); }

        for(int i = 0;i < NUM_PHILOSOPHERS;i++) {
            philosophers[i] = new Thread(new Philosopher("Philosopher " + (i+1),chopsticks[(i==0? NUM_PHILOSOPHERS - 1 : i-1)],chopsticks[i]));
        }

        // start
        for (int i =0; i < NUM_PHILOSOPHERS; i++) { philosophers[i].start(); }

    }
}

