package hw2;

import java.util.Random;

public class DPMonitor {
	static final int NUM_PHILOSOPHER = 5;
	static final int EAT_TIMES = 500;

	public static void main(String[] args) {
		Thread[] philosophers = new Thread[NUM_PHILOSOPHER];

		for (int i = 0; i < NUM_PHILOSOPHER; i++) {
			philosophers[i] = new Thread(new DinningPhilospher(i + 1));
		}

		System.out.println("Dinner is starting!\n");

		for (int i = 0; i < NUM_PHILOSOPHER; i++) {
			philosophers[i].start();
		}

		for (int i = 0; i < NUM_PHILOSOPHER; i++) {
			try {
				philosophers[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("\nDinner is over!");
	}

}

class DinningPhilospher implements Runnable {
	private int id;
	private Chopstick leftChopstick;
	private Chopstick rightChopstick;
	private Random random = null;

	private DinningPhilospher() {
	}

	DinningPhilospher(int id) {
		this.id = id;
		leftChopstick = ChopstickImpl.getInstance(getChopstickId(id, true));
		rightChopstick = ChopstickImpl.getInstance(getChopstickId(id, false));
		random = new Random();
		random.setSeed(System.nanoTime());
	}

	/**
	 * Use random left/right order to pickup or putdown Use sleep up to a second
	 * as the 'backoff' strategy
	 */
	@Override
	public void run() {
		int cnt = 0;
		while (cnt < DPMonitor.EAT_TIMES) {

			int rand = getRandom(1, 2);
			if (rand % 2 == 0) {
				leftChopstick.pickup(id, rightChopstick);
				rightChopstick.pickup(id, leftChopstick);
			} else {
				rightChopstick.pickup(id, leftChopstick);
				leftChopstick.pickup(id, rightChopstick);
			}

			if (!leftChopstick.isTaken(id) && !rightChopstick.isTaken(id)) {
				eat();
				cnt++;
			} else {
				// back off and sleep up to a sec
				sleep();
				continue;
			}
			rand = getRandom(1, 2);
			if (rand % 2 == 0) {
				leftChopstick.putdown(id);
				rightChopstick.putdown(id);
			} else {
				rightChopstick.putdown(id);
				leftChopstick.putdown(id);
			}
		}
	}

	private void eat() {
		System.out.println("Philosopher " + id + " eats.");
	}

	/*
	 * Helper method to get Chopstick id based on Philosopher id
	 */
	private int getChopstickId(int id, boolean isLeft) {
		if (isLeft)
			return id;
		else
			return (id - 1) == 0 ? DPMonitor.NUM_PHILOSOPHER : (id - 1);
	}

	// Helper method to get a integer random
	private int getRandom(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

	// Helper method to sleep for up to a second
	private void sleep() {
		try {
			Thread.currentThread().sleep(getRandom(1, 1000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

// Our Monitor Object Interface
interface Chopstick {
	boolean isTaken(int pId);

	void pickup(int pId, Chopstick theOther);

	void putdown(int pId);

	int getOwnerId(); // readonly, not synched
}

// Our Monitor Object
class ChopstickImpl implements Chopstick {
	private static final int INIT_OWNER_ID = -1;
	private static final long TIMEOUT = 1000;
	// Initialize Chopsticks
	private static Chopstick[] chopsticks = new ChopstickImpl[DPMonitor.NUM_PHILOSOPHER];
	static {
		for (int i = 0; i < DPMonitor.NUM_PHILOSOPHER; i++) {
			chopsticks[i] = new ChopstickImpl(i + 1);
		}
	}

	static Chopstick getInstance(int id) {
		return chopsticks[id - 1];
	}

	private int id;
	private int ownerId;

	private ChopstickImpl() {
	}

	ChopstickImpl(int id) {
		this.id = id;
		this.ownerId = INIT_OWNER_ID;
	}

	@Override
	public synchronized boolean isTaken(int pId) {
		return isTaken_i(pId);
	}

	@Override
	public synchronized void pickup(int pId, Chopstick theOther) {
		while (isTaken_i(pId)) {
			try {
				wait(TIMEOUT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (isTaken_i(pId)) {
				// TIMOUT occurred, release the other if that's the case
				if (theOther.getOwnerId() == pId) {
					synchronized (theOther) {
						theOther.putdown(pId);
						theOther.notifyAll();
					}
				}
			}
		}
		if (!isTaken_i(pId)) {
			ownerId = pId;
			pickup_i(pId);
		}
	}

	@Override
	public synchronized void putdown(int pId) {
		ownerId = INIT_OWNER_ID;
		putdown_i(pId);
		notifyAll();
	}

	/**
	 * return owner id,
	 */
	@Override
	public int getOwnerId() {
		return ownerId;
	}

	// Implementation method
	private boolean isTaken_i(int pId) {
		return ownerId != INIT_OWNER_ID && ownerId != pId;
	}

	private void pickup_i(int pId) {
		System.out.println("Philosopher " + pId + " picks up "
				+ getSide(id, pId) + " chopstick.");
	}

	private void putdown_i(int pId) {
		System.out.println("Philosopher " + pId + " puts down "
				+ getSide(id, pId) + " chopstick.");
	}

	// Helper method
	private String getSide(int cId, int pId) {
		if (cId == pId)
			return "left";
		else
			return "right";
	}

}
