package hw1;

import java.util.concurrent.atomic.AtomicInteger;

public class PingPong {
	private static AtomicInteger semaphore = new AtomicInteger(0);

	private static Integer counterPing = 0;
	private static Integer counterPong = 0;
	private static Integer loopCounter = 5;

	public static void main(String[] args) {
		Thread ping = new Thread() {
			@Override
			public void run() {
				while (true) {
					synchronized (semaphore) {
						if (semaphore.incrementAndGet() == 1) {
							System.out.println("Ping!");
							counterPing++;
							semaphore.notifyAll();
						}
						if (counterPing == loopCounter) {
							break;
						}
					}
				}
			}
		};
		Thread pong = new Thread() {
			@Override
			public void run() {
				while (true) {
					synchronized (semaphore) {
						if (semaphore.decrementAndGet() == 0) {
							System.out.println("Pong!");
							counterPong++;
							semaphore.notifyAll();
						}
						if (counterPong == loopCounter) {
							break;
						}
					}
				}
			}
		};
		ping.start();
		pong.start();
		while (true) {
			//System.out.println(counterPing + " " + counterPong);
			if (counterPing == loopCounter && counterPong == loopCounter) {
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
				System.exit(0);
			}
		}
	}
}
