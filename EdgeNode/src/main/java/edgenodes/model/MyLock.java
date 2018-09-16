package edgenodes.model;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ElectionMyLock implements Lock {
	private int holdlock;
	private long threadid;

	@Override

	public synchronized void lock () {
		if (holdlock == 0) {
			holdlock++;
			threadid = Thread.currentThread().getId();
		} else if (holdlock > 0 && threadid == Thread.currentThread().getId()) {
			holdlock++;
		} else {
			try {
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
	public boolean tryLock () {
		return false;
	}

	@Override
	public boolean tryLock (long time, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public synchronized void unlock () {
		if(holdlock>0){
			holdlock--;
		}
		if(holdlock==0){
			notify();
		}
	}

	@Override
	public Condition newCondition () {
		return null;
	}
}
