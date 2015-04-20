package retryer.waitStrategy;

import retryer.RetryContext;

public class BlockingWaitStrategy<T> implements WaitStrategy<T> {

	@Override
	public void delay(RetryContext<T> ctx) {
		long waitTime = ctx.getLastDelay();
		long tstop = ctx.getClock().get() + waitTime;
		for (;;) {
			waitTime = tstop - ctx.getClock().get();
			try {
				Thread.sleep(waitTime);
				return;
			} catch (InterruptedException e) {
			}
		}
	}

}
