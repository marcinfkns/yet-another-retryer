package retryer.waitTimeStrategy;

import java.util.Random;

import retryer.RetryContext;

public class RandomWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	long minDelayMillis;
	long maxDelayMillis;

	final Random random = new Random();

	public RandomWaitTimeStrategy(long minDelayMillis, long maxDelayMillis) {
		this.minDelayMillis = minDelayMillis;
		this.maxDelayMillis = maxDelayMillis;
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		return minDelayMillis + (Math.abs(random.nextLong()) % (maxDelayMillis - minDelayMillis));
	}

}
