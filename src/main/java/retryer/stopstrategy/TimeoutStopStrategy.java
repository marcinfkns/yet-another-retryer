package retryer.stopstrategy;

import retryer.RetryContext;

public class TimeoutStopStrategy<T> implements StopStrategy<T> {

	long totalTimeoutMillis;
	
	public TimeoutStopStrategy(long totalTimeoutMillis) {
		this.totalTimeoutMillis = totalTimeoutMillis;
	}

	@Override
	public boolean mustStop(RetryContext<T> ctx) {
		return ctx.getClock().get() - ctx.getFirstAttemptTime() > totalTimeoutMillis;
	}

}
