package retryer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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
	RetryPolicy<T> lastRetryPolicy;

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
		}, lastDelay, TimeUnit.MILLISECONDS);
	}
	
	private Result<T> singleAttempt() {
		nextAttempt();
		Result<T> res = null;
		try {
			T ret = action.invoke();
			if (rejectingPredicate!=null && rejectingPredicate.test(ret))
				res = Result.failure(ret);
			else {
				finished = true;
				return Result.success(ret);
			}
		} catch (Throwable e) {
			res = Result.failure(e);	
		}
		
		RetryPolicy<T> policy = policySelector.selectPolicy(res);
		if (policy==null)
			policy = new DefaultPolicySelector<T>().selectPolicy(res);
		
		if (policy != lastRetryPolicy) {
			if (lastRetryPolicy != null)
				lastRetryPolicy.switchOff();
			policy.switchOn();
			lastRetryPolicy = policy;
		}
		
		if (policy.getStopStrategy().mustStop(this)) {
			finished = true;
			return res;
		}
		
		long waitTime = policy.getWaitTimeStrategy().computeWaitTime(this);
		lastDelay = waitTime >= 0L ? waitTime : 0L;
		
		return res;
	}

	private void nextAttempt() {
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
	public long getLastDelay() {
		return lastDelay;
	}

	@Override
	public Clock getClock() {
		return clock;
	}

}