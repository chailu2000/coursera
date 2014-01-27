package hw1;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Pattern - Week4 Programming Assignment 1
 * Print Ping! Pong! alternatively by two sync'd threads in no particular order
 * Only print up to predefined LOOP_COUNTER===5 times each
 * 
 * @author Lu Chai
 * 
 */
public class PingPongNoOrder {
	private static AtomicReference<String> mutex = new AtomicReference<String>(
			"INIT");

	private static Integer counterPing = 0;
	private static Integer counterPong = 0;
	private static Integer LOOP_COUNTER = 5;

	public static void main(String[] args) {
		// Ping thread
		Thread ping = new Thread() {
			@Override
			public void run() {
				while (true) {
					synchronized (mutex) {
						if (mutex.compareAndSet("INIT", "PING")
								|| mutex.compareAndSet("PONG", "PING")) {
							System.out.println("Ping!");
							counterPing++;
							mutex.notifyAll();
						}
						if (counterPing == LOOP_COUNTER) {
							break;
						}
					}
				}
			}
		};
		
		// Pong thread
		Thread pong = new Thread() {
			@Override
			public void run() {
				while (true) {
					synchronized (mutex) {
						if (mutex.compareAndSet("INIT", "PONG")
								|| mutex.compareAndSet("PING", "PONG")) {
							System.out.println("Pong!");
							counterPong++;
							mutex.notifyAll();
						}
						if (counterPong == LOOP_COUNTER) {
							break;
						}
					}
				}
			}
		};

		// Main thread
		System.out.println("Ready... Set... Go!\n");
		
		// Start ping and pong
		ping.start();
		pong.start();

		// wait for ping and pong to terminate
		try {
			ping.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			pong.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done!");
	}

}
