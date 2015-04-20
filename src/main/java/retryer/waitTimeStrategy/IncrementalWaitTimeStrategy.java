package retryer.waitTimeStrategy;

import retryer.RetryContext;

public class IncrementalWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	final long incrementMillis;
	final long maxDelayMillis;

	long value;

	public IncrementalWaitTimeStrategy(long initialDelayMillis, long incrementMillis, long maxDelayMillis) {
		this.value = initialDelayMillis;
		this.incrementMillis = incrementMillis;
		this.maxDelayMillis = maxDelayMillis;
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		long res = value;
		if (res >= maxDelayMillis)
			return maxDelayMillis;
		value += incrementMillis;
		return res;
	}

}
