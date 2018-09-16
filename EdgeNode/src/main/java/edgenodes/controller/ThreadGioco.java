package edgenodes.controller;

import edgenodes.model.ElectionMutex;

public class ThreadGioco extends Thread {

	public ThreadGioco () {

	}

	@Override
	public void run () {
		ElectionMutex.getInstance().enter();
		System.out.println("Thread " + Thread.currentThread().getId() + " inizio");
		try {
			Thread.sleep(40000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Thread " + Thread.currentThread().getId() + " fine");
		ElectionMutex.getInstance().exit();
	}

}
