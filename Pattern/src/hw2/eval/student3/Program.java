package hw2.eval.student3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Pattern Oriented Architecture for Concurrent and Networked Software
 * Assignment Week 5: Dining Philosopher
 */
public class Program implements Runnable {
    public static void main(String[] args) {
        Program program = new Program();
        program.run();
    }

    final static private int PHILOSOPHER_COUNT = 5;
    final static private int MEAL_COUNT = 5;

    final static class Chopstick {
        private Lock inUse;

        public Chopstick() {
            inUse = new ReentrantLock();
        }

        /**
         * Pick up the chopstick. If the chopstick is already in use,
         * then the requester will wait until the chopstick becomes available.
         * Once you are done using the chopstick, call put down to make it available for others
         * @see #putDown()
         */
        public void pickUp() {
            inUse.lock();
        }

        /**
         * Put down the chopstick if it is not used any longer. This makes it
         * available for others to use.
         * @see #pickUp()
         */
        public void putDown() {
            inUse.unlock();
        }
    }

    final static class Philosopher implements Runnable {
        private final int id;
        private int mealCount;
        private Chopstick leftChopstick, rightChopstick;

        public Philosopher(int id, Chopstick leftChopstick, Chopstick rightChopstick) {
            if (id <= 0)
                throw new IllegalArgumentException("The philosopher id must be greater than 0");

            this.id = id;

            mealCount = 0;

            this.leftChopstick = leftChopstick;
            this.rightChopstick = rightChopstick;
        }

        /**
         * @return the numeric identifier that identifies this philosopher. The first philosopher
         * starts with id 1.
         */
        public int getId() {
            return id;
        }

        /**
         * Start philosophizing
         */
        public void run() {
            // Keep on philosophizing respectively eating until dinner is over
            while (!dinnerOver()) {
                getChopsticks();
                eat();
                doneEating();
            }
        }

        /**
         * Attempt to eat: Get first your left and then your right chopstick.
         */
        private void getChopsticks() {
            leftChopstick.pickUp();
            System.out.println("Philosopher " + getId() + " picks up left chopstick");

            rightChopstick.pickUp();
            System.out.println("Philosopher " + getId() + " picks up right chopstick");
        }

        /**
         * Eat. Increase the current meal count by one.
         */
        private void eat() {
            System.out.println("Philosopher " + getId() + " eats");
            mealCount++;
        }

        /**
         * Done eating: Put down first the right chopstick, then the left chopstick.
         */
        private void doneEating() {
            rightChopstick.putDown();
            System.out.println("Philosopher " + getId() + " puts down right chopstick");

            leftChopstick.putDown();
            System.out.println("Philosopher " + getId() + " puts down left chopstick");
        }

        /**
         * @return dinner is over for this philosopher if the number of meals have been eaten.
         */
        private boolean dinnerOver() {
            return mealCount >= MEAL_COUNT;
        }
    }

    final private Chopstick[] chopsticks;
    final private Philosopher[] philosophers;

    public Program() {
        // Create chopsticks based on the same number of philosopher.
        chopsticks = new Chopstick[PHILOSOPHER_COUNT];
        for (int i = 0; i < PHILOSOPHER_COUNT; i++) {
            chopsticks[i] = new Chopstick();
        }

        // Create the number of philosopher. Each philosopher gets an identifier > 0.
        // Each philosopher gets a left and right chopstick. A philosopher always picks up the
        // chopstick in the same order and puts them down in the reverse order: first left, then right.
        // The global locking order is enforced in that the left chopstick has always the lower chopstick order number.
        // The global pick-up order thus becomes: Get first the lower ordered chopstick, then the higher ordered one.
        // This ensures that chopsticks are not locked out of order and thus creating a deadlock.
        // A possible deadlock if this is not enforced:
        //  - P1: Get chopstick 0 - this is P1's left chopstick
        //  - P5: Get chopstick 4 - this is P4's left chopstick
        //  - P1: Try to get chopstick 4 - this is P1's left chopstick held by P5
        //  - P5: Try to get chopstick 0 - this is P5's right chopstick hed by P1
        philosophers = new Philosopher[PHILOSOPHER_COUNT];
        for (int i = 0; i < PHILOSOPHER_COUNT - 1; i++) {
            // Get chopsticks left - right: The right chopstick is always greater than the left
            philosophers[i] = new Philosopher(i + 1, chopsticks[i], chopsticks[i + 1]);
        }
        // For the final philosopher, the left chopstick is the first chopstick 0, the right the last one.
        // This break of symmetry ensures that the left chopstick is always the lower numbered one
        philosophers[PHILOSOPHER_COUNT - 1] = new Philosopher(PHILOSOPHER_COUNT,
                chopsticks[0], chopsticks[PHILOSOPHER_COUNT - 1]);
    }

    public void run() {
        System.out.println("Dinner is starting!");
        System.out.println();

        // Create the philosopher thread and start
        Thread[] philosopherThreads = new Thread[PHILOSOPHER_COUNT];
        for (int i = 0; i < PHILOSOPHER_COUNT; i++) {
            philosopherThreads[i] = new Thread(philosophers[i]);
            philosopherThreads[i].start();
        }

        // Wait for all philosopher to finish eating
        for (Thread philosopherThread: philosopherThreads) {
            try {
                philosopherThread.join();
            } catch (InterruptedException e) {
                // Ignore interrupted thread
            }
        }

        System.out.println();
        System.out.println("Dinner is over!");
    }
}