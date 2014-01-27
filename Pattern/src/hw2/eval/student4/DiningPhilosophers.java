package hw2.eval.student4;

import java.util.Random;

//This program uses the Resource Hierarchy solution described in
//http://en.wikipedia.org/wiki/Dining_philosophers_problem#Resource_hierarchy_solution
//
//This program uses the Monitor Object patterns for the shared resource (Chopstick class).

public class DiningPhilosophers {
	public DiningPhilosophers() {
		final int NUM_PHILOSOPHERS = 5;
		final int NUM_CHOPSTICKS = 5;
		final int EAT_COUNT = 50; // How many times each philosopher must eat.
		
		final Chopstick[] chopsticks = new Chopstick[NUM_CHOPSTICKS];
		for (int i = 0; i < chopsticks.length; i++) {
			chopsticks[i] = new Chopstick();
		}
		
		final Philosopher[] philosophers = new Philosopher[NUM_PHILOSOPHERS];
		for (int i = 0; i < philosophers.length; i++) {
			philosophers[i] = new Philosopher(i, EAT_COUNT, chopsticks);
		}
		
		// Start the dinner!
		System.out.println("Dinner is starting!\n");
		
		for (final Philosopher p : philosophers) {
			p.start();
		}
		
		// Wait for all threads to finish.
		for (final Philosopher p : philosophers) {
			try {
				p.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Dinner is over!");
	}
	
	public static void main(String[] args) {
		new DiningPhilosophers();
	}
}

class Philosopher extends Thread {
	private int timesAte; // Records how many times this philosopher ate so far.
	private final int EAT_TARGET; // How many times to eat before stopping.
	private final int ID;
	private final Chopstick[] chopsticks;
	
	private static final Random random = new Random();

	public Philosopher(int id, int eatTarget, Chopstick[] chopsticks) {
		timesAte = 0;
		EAT_TARGET = eatTarget;
		ID = id;
		this.chopsticks = chopsticks;
	}

	private void eat() {
		// The philosopher will pick up the lower-numbered fork first (be it to his left, or right).
		final int right = ID;
		final int left = (ID + 1) % chopsticks.length;
		
/*		if (right < left) { // Pick up right chopstick first.
			chopsticks[right].pickUp(ID);
			System.out.println("Philosopher " + (ID + 1) + " picks up right chopstick.");
			chopsticks[left].pickUp(ID);
			System.out.println("Philosopher " + (ID + 1) + " picks up left chopstick.");
			
		} else { // Pick up left chopstick first.
*/			chopsticks[left].pickUp(ID);
			System.out.println("Philosopher " + (ID + 1) + " picks up left chopstick.");
			chopsticks[right].pickUp(ID);
			System.out.println("Philosopher " + (ID + 1) + " picks up right chopstick.");
//		}

		// Time to eat!
		++timesAte;
		System.out.println("Philosopher " + (ID + 1) + " eats.");
		
		// "Eat" for a while (to make it more realistic)...
/*		try {
			Thread.sleep(random.nextInt(500));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
*/
		// Finished eating! The order in which the chopsticks are put down does not matter.
		chopsticks[right].putDown(ID);
		System.out.println("Philosopher " + (ID + 1) + " puts down right chopstick.");
		chopsticks[left].putDown(ID);
		System.out.println("Philosopher " + (ID + 1) + " puts down left chopstick.");
	}
	
	@Override
	public void run() {
		while (timesAte < EAT_TARGET) {
			eat();
		}
	}
}

//This class utilizes the Monitor Object pattern. All of its methods are
//synchronized, and it uses the wait() and notifyAll() methods to manage
//threads waiting on this resource. 
class Chopstick {
	private boolean inUse = false;
	private int ownerId; // Who currently holds the chopstick.

	public synchronized void pickUp(int id) {
		// If the chopstick is being used, philosopher waits until it is available,
		// then grabs it.
		while (inUse) { // We need to use a while loop here (see http://stackoverflow.com/a/3186336).
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		inUse = true;
		ownerId = id;
	}
	
	// Release the chopstick (if it is acquired), and notify anyone waiting on it.
	public synchronized void putDown(int id) {
		if (inUse && ownerId == id) { // Only the philosopher who picked up the chopstick
			inUse = false;            // is allowed to put it back down.
			notify(); // There is at most a single thread waiting on this resource, no need to use notifyAll().
		}
	}
}
