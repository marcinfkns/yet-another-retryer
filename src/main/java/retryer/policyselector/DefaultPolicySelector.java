package retryer.policyselector;

import retryer.Result;
import retryer.RetryPolicy;
import retryer.stopstrategy.StopStrategy;

public class DefaultPolicySelector<T> implements PolicySelector<T> {

	@Override
	public RetryPolicy<T> selectPolicy(Result<T> failure) {
		StopStrategy<T> stopStrategy = ctx -> true; // stop after 1st failure
		return new RetryPolicy<T>(stopStrategy, null, null);
	}

}
