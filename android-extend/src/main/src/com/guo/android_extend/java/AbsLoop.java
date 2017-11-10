package com.guo.android_extend.java;

/**
 * Created by gqj3375 on 2015/12/17.
 */
public abstract class AbsLoop extends Thread {
	volatile Thread mBlinker = this;

	abstract public void setup();

	abstract public void loop();

	abstract public void over();

	@Override
	public void run() {
		Thread thisThread = Thread.currentThread();
		setup();
		while (mBlinker == thisThread) {
			loop();
		}
		over();
	}

	public void break_loop() {
		mBlinker = null;
	}

	public void shutdown() {
		break_loop();
		try {
			if (this != Thread.currentThread()) {
				synchronized (this) {
					this.notifyAll();
				}
				this.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
