package retryer.waitTimeStrategy;

import retryer.RetryContext;

public class NoWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		return 0L;
	}

}
