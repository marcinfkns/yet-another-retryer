package retryer.policyselector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import retryer.Result;
import retryer.RetryPolicy;

public class RuleSetPolicySelector<T> implements PolicySelector<T> {

	public static class Rule<T> {
		Set<Predicate<Result<T>>> failures;
		RetryPolicy<T> policy;

		@SafeVarargs
		public Rule(RetryPolicy<T> policy, Predicate<Result<T>>... failures) {
			this.policy = policy;
			this.failures = new HashSet<>(Arrays.asList(failures));
		}
	}

	List<Rule<T>> rules;

	public RuleSetPolicySelector(List<Rule<T>> rules) {
		this.rules = rules;
	}

	public RetryPolicy<T> selectPolicy(Result<T> failure) {
		for (Rule<T> rule : rules) {
			if (rule.failures.stream().anyMatch(f -> f.test(failure)))
				return rule.policy;
		}
		return null;
	}
}