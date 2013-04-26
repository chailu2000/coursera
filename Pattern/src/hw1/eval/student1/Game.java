package hw1.eval.student1;


	class Player extends Thread {
		
		String name;
		Object ball;
		int counter;
		static int POINTS = 3;
		
		public Player(String player, Object commonBall){
			counter = 0; 
			name=player;
			ball = commonBall;
		}
		
		@Override
		public void run() {
			while (counter < POINTS){
				counter++;
				System.out.println(name); 
				synchronized (ball) {
					ball.notify();	
				}
				try {
					synchronized (ball) {
						ball.wait();		
					}
					//Thread.currentThread();
					//Thread.sleep(1000); // just in order to process slower				
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} // end of while
			/* Send notification, since second thread is still waiting */
			synchronized (ball) {
				ball.notify();	
			}
		}
	}

public class Game {

	public static void main(String[] args) {
		System.out.println("Ready… Set… Go!");
		
		Object ball = new Object();
		Player tPing = new Player("Ping!", ball);
		Player tPong = new Player("Pong!", ball);
		tPing.start();
		tPong.start();
		
		try {
			tPing.join();
			tPong.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done!");
	}
}
