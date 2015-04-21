package retryer.stopstrategy;

import retryer.RetryContext;

public class TimeoutStopStrategy<T> implements StopStrategy<T> {

	long totalTimeoutMillis;
	
	public TimeoutStopStrategy(long totalTimeoutMillis) {
		this.totalTimeoutMillis = totalTimeoutMillis;
	}

	@Override
	public boolean mustStop(RetryContext<T> ctx) {
		long now = ctx.getTime();
		//not only checks if it's too late now, but also if it'll be too late after 'next delay' time:
		return now + ctx.getNextDelay() - ctx.getFirstAttemptTime() > totalTimeoutMillis;
	}

}
