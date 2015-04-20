package retryer.waitTimeStrategy;

import retryer.RetryContext;

public class ExponentialWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	long multiplier;
	long maxDelayMillis;

	long value = 1;

	public ExponentialWaitTimeStrategy(long multiplier, long maxDelayMillis) {
		this.multiplier = multiplier;
		this.maxDelayMillis = maxDelayMillis;
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		long res = value * multiplier;
		if (res >= maxDelayMillis)
			return maxDelayMillis;
		value *= 2;
		return res;
	}

}
