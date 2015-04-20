package retryer.stopstrategy;

import retryer.RetryContext;

public class AttemptsCountStopStrategy<T> implements StopStrategy<T> {

	final int maxAttemptCount;

	public AttemptsCountStopStrategy(int maxAttemptCount) {
		this.maxAttemptCount = maxAttemptCount;
	}

	@Override
	public boolean mustStop(RetryContext<T> ctx) {
		return ctx.getAttemptsCount() >= maxAttemptCount;
	}

}
