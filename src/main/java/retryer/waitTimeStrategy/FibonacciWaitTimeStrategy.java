package retryer.waitTimeStrategy;

import retryer.RetryContext;

public class FibonacciWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	long multiplier;
	long maxDelayMillis;

	long prev = 0, current = 1;

	public FibonacciWaitTimeStrategy(long multiplier, long maxDelayMillis) {
		this.multiplier = multiplier;
		this.maxDelayMillis = maxDelayMillis;
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		long res = current * multiplier;
		if (res >= maxDelayMillis)
			return maxDelayMillis;
		long next = prev + current;
		prev = current;
		current = next;
		return res;
	}

}
