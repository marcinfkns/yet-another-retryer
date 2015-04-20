package retryer.waitTimeStrategy;

import retryer.RetryContext;
import retryer.Switch;

@FunctionalInterface
public interface WaitTimeStrategy<T> extends Switch {
	public long computeWaitTime(RetryContext<T> ctx);
}