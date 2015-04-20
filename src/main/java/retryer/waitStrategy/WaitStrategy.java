package retryer.waitStrategy;

import retryer.RetryContext;
import retryer.Switch;

@FunctionalInterface
public interface WaitStrategy<T> extends Switch {
	public void delay(RetryContext<T> ctx);
}