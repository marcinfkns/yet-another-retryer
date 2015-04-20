package retryer.waitTimeStrategy;

import retryer.RetryContext;

public class FixedWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	final long delayMillis;

	public FixedWaitTimeStrategy(long delayMillis) {
		this.delayMillis = delayMillis;
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		return delayMillis;
	}

}
