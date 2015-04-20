package retryer.stopstrategy;

import java.util.List;

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
