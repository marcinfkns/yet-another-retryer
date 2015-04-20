package retryer.waitTimeStrategy;

import retryer.RetryContext;

public class IncrementalWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	long initialDelayMillis;
	long increment;
	long maxDelayMillis;

	long value;

	public IncrementalWaitTimeStrategy(long initialDelayMillis, long increment, long maxDelayMillis) {
		this.initialDelayMillis = initialDelayMillis;
		this.increment = increment;
		this.maxDelayMillis = maxDelayMillis;
		this.value = initialDelayMillis;
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		long res = value;
		if (res >= maxDelayMillis)
			return maxDelayMillis;
		value += increment;
		return res;
	}

}
