package edgenodes.model;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MyLock implements Lock {
	private int holdlock;
	private long threadid;

	@Override
	public synchronized void lock () {
		if (holdlock == 0) {
			System.out.println("Thread " + Thread.currentThread().getId() + ", acquisisco il lock di elezione. ");
			holdlock++;
			threadid = Thread.currentThread().getId();
		} else if (holdlock > 0 && threadid == Thread.currentThread().getId()) {
			System.out.println("Thread " + Thread.currentThread().getId() + ", acquisisco il lock di elezione. ");
			holdlock++;
		} else {
			try {
				System.out.println("Thread " + Thread.currentThread().getId() + " va in wait di elezione. ");
				wait();
				holdlock++;
				threadid = Thread.currentThread().getId();
			} catch (InterruptedException e) {
				System.out.println("Errore :- si è verificato un problema durante l'acquisizione del lock");
			}
		}
	}

	@Override
	public void lockInterruptibly () throws InterruptedException {

	}

	@Override
	public synchronized boolean tryLock () {
		return (this.holdlock <= 0 || (holdlock > 0 && threadid == Thread.currentThread().getId()));
	}

	@Override
	public boolean tryLock (long time, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public synchronized void unlock () {
		if (holdlock > 0) {
			holdlock--;
		}
		if (holdlock == 0) {
			System.out.println("Thread " + Thread.currentThread().getId() + " rilascio lock elezione.");
			notify();
		}
	}

	@Override
	public Condition newCondition () {
		return null;
	}
}
