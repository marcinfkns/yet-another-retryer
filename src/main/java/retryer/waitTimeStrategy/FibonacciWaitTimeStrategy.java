package retryer.waitTimeStrategy;

import retryer.RetryContext;

public class FibonacciWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	final long maxDelayMillis;

	long prev = 0, current;

	public FibonacciWaitTimeStrategy(long unitPeriodMillis, long maxDelayMillis) {
		this.current = unitPeriodMillis;
		this.maxDelayMillis = maxDelayMillis;
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		long res = current;
		if (res >= maxDelayMillis)
			return maxDelayMillis;
		long next = prev + current;
		prev = current;
		current = next;
		return res;
	}

}
