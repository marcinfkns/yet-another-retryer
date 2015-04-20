package retryer;

import retryer.stopstrategy.StopStrategy;
import retryer.waitStrategy.BlockingWaitStrategy;
import retryer.waitStrategy.WaitStrategy;
import retryer.waitTimeStrategy.NoWaitTimeStrategy;
import retryer.waitTimeStrategy.WaitTimeStrategy;

public class RetryPolicy<T> implements Switch {

	StopStrategy<T> stopStrategy;
	WaitTimeStrategy<T> waitTimeStrategy;
	WaitStrategy<T> waitStrategy;
	
	public RetryPolicy(StopStrategy<T> stopStrategy, WaitTimeStrategy<T> waitTimeStrategy, WaitStrategy<T> waitStrategy) {
		this.stopStrategy = stopStrategy;
		this.waitTimeStrategy = waitTimeStrategy;
		this.waitStrategy = waitStrategy;
	}

	public RetryPolicy(StopStrategy<T> stopStrategy, WaitTimeStrategy<T> waitTimeStrategy) {
		this(stopStrategy, waitTimeStrategy, new BlockingWaitStrategy<T>());
	}

	public RetryPolicy(StopStrategy<T> stopStrategy) {
		this(stopStrategy, new NoWaitTimeStrategy<T>(), new BlockingWaitStrategy<T>());
	}

	public void switchOn() {
		if (stopStrategy!=null) stopStrategy.switchOn();
		if (waitTimeStrategy!=null) waitTimeStrategy.switchOn();
		if (waitStrategy!=null) waitStrategy.switchOn();
	}
	
	public void switchOff() {
		if (stopStrategy!=null) stopStrategy.switchOff();
		if (waitTimeStrategy!=null) waitTimeStrategy.switchOff();
		if (waitStrategy!=null) waitStrategy.switchOff();
	}

	public StopStrategy<T> getStopStrategy() {
		return stopStrategy;
	}

	public WaitTimeStrategy<T> getWaitTimeStrategy() {
		return waitTimeStrategy;
	}

	public WaitStrategy<T> getWaitStrategy() {
		return waitStrategy;
	}

}
