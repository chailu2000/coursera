package hw2;

import java.util.Random;

/**
 * Dining philosopher problem using the following sitting order
 * Chopstick 1 is to the left of Philosopher 1
 * Chopstick 2 is to the left of Philosopher 2 and so on...
 * p1-c1-p2-c2-p3-c3-p4-c4-p5-c5-p1
 * 
 * @author ubuntu
 * 
 */
public interface DiningPhilosopher {
	public void pickupLeft() throws Exception;

	public void pickupRight() throws Exception;

	public void putsdownLeft();

	public void putsdownRight();

	public void eat();

	public boolean isLeftAvailable();

	public boolean isRightAvailable();
}

class DiningPhilosopherImpl implements Runnable, DiningPhilosopher {
	static final int NUM_PHILOSOPHER = 5;
	private static final int WAIT_TIMEOUT = 1000;

	private int EAT_TIMES = 5;

	private int id;

	private Random random = null;

	private Chopsticks left = null;
	private Chopsticks right = null;

	private DiningPhilosopherImpl() {
	}

	public DiningPhilosopherImpl(int id) throws Exception {
		this.id = id;
		if (id < 0)
			throw new Exception("Philosopher Id cannot be less than zero.");
		left = Chopsticks.getInstance(getChopstickId(id, true));
		right = Chopsticks.getInstance(getChopstickId(id, false));
		random = new Random();
		random.setSeed(System.nanoTime());
	}

	/**
	 * Use random left/right order to pickup or putdown
	 * Use sleep up to a second as the 'backoff' strategy
	 */
	@Override
	public void run() {
		while (EAT_TIMES > 0) { 
			// randomize left/right order to pickup and putdown
			int rand = getRandom(1, 2);
			try {
				if (rand % 2 == 0) {
					pickupLeft();
					sleep();
					pickupRight();
				} else {
					pickupRight();
					sleep();
					pickupLeft();
				}
			} catch (Exception e) {
				sleep();
				continue;
			}
			
			eat();
			EAT_TIMES--;

			int rand2 = getRandom(1, 2);
			if (rand2 % 2 == 0) {
				putsdownLeft();
				putsdownRight();
			} else {
				putsdownRight();
				putsdownLeft();
			}
		}
	}
	
	@Override
	public synchronized void pickupLeft() throws Exception {
		synchronized (left) {
			try {
				while (!isLeftAvailable_i()) {
					left.wait(WAIT_TIMEOUT);
					if (!isLeftAvailable_i()) {
						// if timed out
						// relinquish hold on the right chopstick if that's the
						// case
						if (isRightAvailable_i()) {
							putsdownRight_i();
						}
						throw new Exception("Philosopher " + id
								+ " Timed out waiting for left chopstick "
								+ getChopstickId(id, true));
					}
				}
				pickupLeft_i();
			} catch (Exception e) {
				throw new Exception(e);
			}
		}
	}

	@Override
	public synchronized void pickupRight() throws Exception {
		synchronized (right) {
			try {
				while (!isRightAvailable_i()) {
					right.wait(WAIT_TIMEOUT);
					if (!isRightAvailable_i()) {
						// if timed out
						// relinquish hold on the left chopstick if that's the
						// case
						if (isLeftAvailable_i()) {
							putsdownLeft_i();
						}
						throw new Exception("Philosopher " + id
								+ " Timed out waiting for right chopstick "
								+ getChopstickId(id, false));
					}
				}
				pickupRight_i();
			} catch (Exception e) {
				throw new Exception(e);
			}
		}
	}

	@Override
	public synchronized void eat() {
		if (isLeftAvailable_i() && isRightAvailable_i()) {
			// eat
			eat_i();
		}
	}

	@Override
	public synchronized void putsdownLeft() {
		synchronized (left) {
			putsdownLeft_i();
			left.notifyAll();
		}
	}

	@Override
	public synchronized void putsdownRight() {
		synchronized (right) {
			putsdownRight_i();
			right.notifyAll();
		}
	}

	@Override
	public synchronized boolean isLeftAvailable() {
		return isLeftAvailable_i();
	}

	@Override
	public synchronized boolean isRightAvailable() {
		return isRightAvailable_i();
	}

	private void pickupLeft_i() {
		left.setOwnerId(id);
		System.out.println("Philosopher " + id + " picks up left chopstick.");
	}

	private void pickupRight_i() {
		right.setOwnerId(id);
		System.out.println("Philosopher " + id + " picks up right chopstick.");
	}

	private void eat_i() {
		System.out.println("Philosopher " + id + " eats.");
	}

	private void putsdownLeft_i() {
		left.setOwnerId(Chopsticks.INIT_OWNER_ID);
		System.out.println("Philosopher " + id + " puts down left chopstick.");
	}

	private void putsdownRight_i() {
		right.setOwnerId(Chopsticks.INIT_OWNER_ID);
		System.out.println("Philosopher " + id + " puts down right chopstick.");
	}

	private boolean isLeftAvailable_i() {
		return left.getOwnerId() == id
				|| left.getOwnerId() == Chopsticks.INIT_OWNER_ID;
	}

	private boolean isRightAvailable_i() {
		return right.getOwnerId() == id
				|| right.getOwnerId() == Chopsticks.INIT_OWNER_ID;
	}

	private int getRandom() {
		return getRandom(1,1000); // use this for sleeping
	}

	private int getRandom(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

	private void sleep() {
		try {
			Thread.currentThread().sleep(getRandom());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * get Chopstick id based on Philosopher id
	 */
	private int getChopstickId(int id, boolean isLeft) {
		if (isLeft)
			return id;
		else
			return (id - 1) == 0 ? NUM_PHILOSOPHER : (id - 1);
	}

}

class Chopsticks {
	static final int INIT_OWNER_ID = -1;

	private static Chopsticks[] chopsticks = new Chopsticks[DiningPhilosopherImpl.NUM_PHILOSOPHER];
	static {
		for (int i=0; i<DiningPhilosopherImpl.NUM_PHILOSOPHER; i++) {
			chopsticks[i] = new Chopsticks(i+1);
		}
	}
	private int id;
	private int ownerId = INIT_OWNER_ID;

	private Chopsticks() {
	}

	private Chopsticks(int id) {
		this.id = id;
	}

	public static Chopsticks getInstance(int id) throws Exception {
		return chopsticks[id-1];
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

}

class Dinning {
	public static void main(String[] args) throws Exception {
		Thread[] philosophers = new Thread[DiningPhilosopherImpl.NUM_PHILOSOPHER];
		
		for (int i=0; i<DiningPhilosopherImpl.NUM_PHILOSOPHER;i++) {
			philosophers[i] = new Thread(new DiningPhilosopherImpl(i+1));
		}

		System.out.println("Dinner is starting!\n");

		for (int i=0; i<DiningPhilosopherImpl.NUM_PHILOSOPHER;i++) {
			philosophers[i].start();
		}

		for (int i=0; i<DiningPhilosopherImpl.NUM_PHILOSOPHER;i++) {
			philosophers[i].join();
		}

		System.out.println("\nDinner is over!");
	}
}
