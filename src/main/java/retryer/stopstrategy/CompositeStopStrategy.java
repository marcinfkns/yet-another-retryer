package retryer.stopstrategy;

import java.util.List;

import retryer.RetryContext;

/** concrete instances provided by StopStrategy.or/and methods */
public abstract class CompositeStopStrategy<T> implements StopStrategy<T> {

	List<StopStrategy<T>> strategies;

	public CompositeStopStrategy(List<StopStrategy<T>> strategies) {
		this.strategies = strategies;
	}

	@Override
	public void switchOn() {
		strategies.forEach(s -> s.switchOn());
	}

	@Override
	public void switchOff() {
		strategies.forEach(s -> s.switchOff());
	}

}
