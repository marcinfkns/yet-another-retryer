package retryer.policyselector;

import retryer.Result;
import retryer.RetryPolicy;

public class SinglePolicySelector<T> implements PolicySelector<T> {

	RetryPolicy<T> policy;

	public SinglePolicySelector(RetryPolicy<T> policy) {
		this.policy = policy;
	}

	@Override
	public RetryPolicy<T> selectPolicy(Result<T> failure) {
		return policy;
	}

}
