package retryer.waitTimeStrategy;

import java.util.Arrays;
import java.util.List;

import retryer.RetryContext;

public class SumWaitTimeStrategy<T> implements WaitTimeStrategy<T> {

	List<WaitTimeStrategy<T>> strategies;

	public SumWaitTimeStrategy(List<WaitTimeStrategy<T>> strategies) {
		this.strategies = strategies;
	}

	@SafeVarargs
	public SumWaitTimeStrategy(WaitTimeStrategy<T>... strategies) {
		this.strategies = Arrays.asList(strategies);
	}

	@Override
	public long computeWaitTime(RetryContext<T> ctx) {
		long delay = 0;
		for (WaitTimeStrategy<T> s : strategies)
			delay += s.computeWaitTime(ctx);
		return delay;
	}

}
