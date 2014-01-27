package hw2.eval.student1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue; 
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demo implementation for Dining Philosofers problem.
 * Coursera, POSA, [W5] Programming Assignment #2 
 * 
 * @author taky
 */
public class DiningPhilosophersProblem {

	private static class DiningPhilosoph implements Runnable {

		private final int id;
		private final Table table;

		public DiningPhilosoph(int id,  Table table) {
			this.id = id;
			this.table = table;
		}

		/** 
		 * Start eat food here.
		 */
		@Override
		public void run() {
			try {
				while (!table.isEmpty()) {
					Dish dish = table.getDish(id);
					if(dish != null){
						eatDish(dish);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void eatDish(Dish dish) throws InterruptedException {
			//Thread.sleep(dish.CALORIES);			
		}
	}
	
	private static class Dish {
		
		public final long CALORIES;
		
		public Dish(){
			this(Math.round(Math.random()*10000));
		}
		
		public Dish(long calories){
			this.CALORIES = calories;
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return "Dish [CALORIES=" + CALORIES + "]";
		}
		
	}

	private static class Chopstick {
		
		private Lock lock = new ReentrantLock();

		public boolean tryTake() {
			return lock.tryLock();
		}

		public void putChopstick() {
			lock.unlock();
		}

	}
	
	private static class Table {
		
		private final List<Chopstick> chopsticks = new ArrayList<>();
		private final Queue<Dish> dishes = new LinkedList<>();
		
		public Table(int personsCount, int dishesCount){
			for(int i = 0; i < dishesCount; i++){
				dishes.add(new Dish());
			}
			for(int i = 0; i < dishesCount; i++){
				chopsticks.add(new Chopstick());
			}
		}

		public Dish getDish(int clientId) {
			Chopstick leftChopstick = findLeftChopstick(clientId);
			if (leftChopstick.tryTake()) {
				say(String.format("Philosopher %s picks up left chopstick.", clientId));
				try {
					Chopstick rightChopstick = findRightChopstick(clientId);
					if (rightChopstick.tryTake()) {
						say(String.format("Philosopher %s picks up right chopstick.", clientId));
						try {
							Dish dish = dishes.poll();
							if(dish != null){
								say(String.format("Philosopher %s eats - %s.", clientId, dish));
								return dish;
							} else {
								say(String.format("Philosofer %s find empty table. Go home.", clientId));
							}
						} finally {
							say(String.format("Philosopher %s puts down right chopstick.", clientId));
							rightChopstick.putChopstick();
						}
					}
				} finally {
					say(String.format("Philosopher %s puts down left chopstick.", clientId));
					leftChopstick.putChopstick();
				}
			}
			return null;
		}

		private Chopstick findRightChopstick(int clientId) {
			return chopsticks.get(clientId);
		}

		private Chopstick findLeftChopstick(int clientId) {
			int chopStickIndex = clientId == 0 ? chopsticks.size() - 1
					: clientId - 1;
			return chopsticks.get(chopStickIndex);
		}

		public boolean isEmpty() {
			return dishes.isEmpty();
		}
	}

	public static class Dining {

		private final Table table;
		
		private final Thread[] chairs;

		public Dining(int personsCount, int dishesCount) {
			this.table = new Table(personsCount, dishesCount);
			this.chairs = new Thread[personsCount];
		}

		/**
		 * Create philosofers. Put chopstiks on the table. Bind philosofers and chopstick.
		 */
		public void sitDown() {
			for(int i = 0; i < chairs.length; i++){
				DiningPhilosoph diningPhilosoph = new DiningPhilosoph(i, table);
				chairs[i] = new Thread(diningPhilosoph);
			}
		}

		public void eat() throws InterruptedException {
			say("Dinner is starting!");
			for(int i = 0; i < chairs.length; i++){
				chairs[i].start();
			}
			for (Thread chair : chairs) {
				  chair.join();
			}
			say("Dinner is over!");
		}
	}
	
	public static void say(String words) {
		System.out.println(words);
	}

	public static void main(String[] args) throws InterruptedException {
		int PERSONS_COUNT = 5;
		int DISHES_COUNT = 50;
		Dining dining = new Dining(PERSONS_COUNT, DISHES_COUNT);
		dining.sitDown();
		dining.eat();
	}

}
