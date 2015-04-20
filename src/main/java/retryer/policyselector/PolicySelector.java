package retryer.policyselector;

import retryer.Result;
import retryer.RetryPolicy;

@FunctionalInterface
public interface PolicySelector<T> {
	RetryPolicy<T> selectPolicy(Result<T> failure);
}