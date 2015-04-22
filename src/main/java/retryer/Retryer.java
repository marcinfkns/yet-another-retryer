package retryer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import retryer.policyselector.DefaultPolicySelector;
import retryer.policyselector.PolicySelector;
import retryer.policyselector.SinglePolicySelector;
import retryer.stopstrategy.StopStrategy;
import retryer.waitStrategy.WaitStrategy;
import retryer.waitTimeStrategy.WaitTimeStrategy;

public class Retryer<T> implements RetryContext<T> {

	Clock clock = System::currentTimeMillis;
	PolicySelector<T> policySelector = new DefaultPolicySelector<T>();
	Predicate<T> rejectingPredicate;
	RetryableAction<T> action;

	boolean finished = false;
	int attemptsCount = 0;
	long firstAttemptTime = -1L;
	long lastAttemptTime = -1L;
	long lastDelay = 0L;
	long nextDelay = 0L;
	RetryPolicy<T> lastRetryPolicy;
	Result<T> lastResult = null;

	public Retryer() {
	}

	public void setClock(Clock clock) {
		this.clock = clock;
	}

	public void setPolicySelector(PolicySelector<T> policySelector) {
		this.policySelector = policySelector;
	}

	/** shortcut for specifying single policy */
	public void setPolicy(StopStrategy<T> stopStrategy, WaitTimeStrategy<T> waitTimeStrategy, WaitStrategy<T> waitStrategy) {
		this.policySelector = new SinglePolicySelector<>(new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy));
	}

	public void setRejectingPredicate(Predicate<T> rejectingPredicate) {
		this.rejectingPredicate = rejectingPredicate;
	}

	public T invoke(RetryableAction<T> action) throws Throwable {
		this.action = action;
		for(;;) {
			Result<T> res = singleAttempt();
			if (finished)
				return res.execute();
			else {
				lastRetryPolicy.getWaitStrategy().delay(this);
			}
		}
	}

	public Future<T> invokeAsync(RetryableAction<T> action, final ScheduledExecutorService executor) throws Throwable {
		this.action = action;
		final CompletableFuture<T> promise = new CompletableFuture<>();
		singleAttemptAsync(executor, promise);
		return promise;
	}

	private void singleAttemptAsync(final ScheduledExecutorService executor, final CompletableFuture<T> promise) {
		try {
			executor.schedule( () -> {
				Result<T> res = singleAttempt();
				if (finished) {
					if (res.exception != null)
						promise.completeExceptionally(res.exception);
					else
						promise.complete(res.result);
				}
				else
					singleAttemptAsync(executor, promise);
			}, nextDelay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException ex) {
			//executor rejected our task
			promise.completeExceptionally(ex);
		}
	}
	
	private Result<T> singleAttempt() {
		
		lastDelay = nextDelay;
		nextDelay = 0L;
		
		//if the next attempt was delayed too much (for different reasons), perhaps timeout based strategies might refuse to continue:
		if (lastRetryPolicy!=null && lastRetryPolicy.getStopStrategy().mustStop(this)) {
			finished = true;
			return lastResult;
		}

		prepareForNextAttempt();

		try {
			T ret = action.invoke();
			if (rejectingPredicate!=null && rejectingPredicate.test(ret))
				lastResult = Result.failure(ret);
			else {
				finished = true;
				return Result.success(ret);
			}
		} catch (Throwable e) {
			lastResult = Result.failure(e);	
		}
		
		RetryPolicy<T> policy = policySelector.selectPolicy(lastResult);
		if (policy==null)
			policy = new DefaultPolicySelector<T>().selectPolicy(lastResult);
		
		if (policy != lastRetryPolicy) {
			if (lastRetryPolicy != null)
				lastRetryPolicy.switchOff();
			policy.switchOn();
			lastRetryPolicy = policy;
		}
		
		if (policy.getStopStrategy().mustStop(this)) {
			finished = true;
			return lastResult;
		}
		
		final long waitTime = policy.getWaitTimeStrategy().computeWaitTime(this);
		nextDelay = waitTime >= 0L ? waitTime : 0L;

		//check if any timeout based stop strategies will accept next attempt (after 'nextDelay' time)
		if (policy.getStopStrategy().mustStop(this)) {
			finished = true;
			return lastResult;
		}

		return lastResult;
	}

	private void prepareForNextAttempt() {
		long tm = clock.get();
		lastAttemptTime = tm;
		if (firstAttemptTime < 0)
			firstAttemptTime = tm;
		attemptsCount += 1;
	}

	@Override
	public int getAttemptsCount() {
		return attemptsCount;
	}

	@Override
	public long getFirstAttemptTime() {
		return firstAttemptTime;
	}

	@Override
	public long getNextDelay() {
		return nextDelay;
	}

	@Override
	public long getLastDelay() {
		return lastDelay;
	}

	@Override
	public long getTime() {
		return clock.get();
	}

}