package retryer.waitStrategy;

import retryer.RetryContext;

public class BlockingWaitStrategy<T> implements WaitStrategy<T> {

	@Override
	public void delay(RetryContext<T> ctx) {
		long waitTime = ctx.getNextDelay();
		long tstop = ctx.getTime() + waitTime;
		for (;;) {
			waitTime = tstop - ctx.getTime();
			try {
				Thread.sleep(waitTime);
				return;
			} catch (InterruptedException e) {
			}
		}
	}

}
