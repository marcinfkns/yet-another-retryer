package retryer.stopstrategy;

import java.util.Arrays;

import retryer.RetryContext;
import retryer.Switch;

@FunctionalInterface
public interface StopStrategy<T> extends Switch {

	public boolean mustStop(RetryContext<T> ctx);

	@SafeVarargs
	public static <T> StopStrategy<T> or(StopStrategy<T>... strategies) {
		return new CompositeStopStrategy<T>(Arrays.asList(strategies)) {
			@Override
			public boolean mustStop(final RetryContext<T> ctx) {
				return this.strategies.stream().anyMatch(s -> s.mustStop(ctx));
			}
		};
	}

	@SafeVarargs
	public static <T> StopStrategy<T> and(StopStrategy<T>... strategies) {
		return new CompositeStopStrategy<T>(Arrays.asList(strategies)) {
			@Override
			public boolean mustStop(final RetryContext<T> ctx) {
				return this.strategies.stream().allMatch(s -> s.mustStop(ctx));
			}
		};
	}

}