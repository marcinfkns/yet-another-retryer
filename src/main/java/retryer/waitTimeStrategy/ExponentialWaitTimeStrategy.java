package retryer.waitTimeStrategy;

import retryer.RetryContext;

public class ExponentialWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	final long maxDelayMillis;

	long value;

	public ExponentialWaitTimeStrategy(long unitPeriodMillis, long maxDelayMillis) {
		this.value = unitPeriodMillis;
		this.maxDelayMillis = maxDelayMillis;
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		long res = value;
		if (res >= maxDelayMillis)
			return maxDelayMillis;
		value += value;
		return res;
	}

}
