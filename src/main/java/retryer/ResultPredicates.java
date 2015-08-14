package retryer;

import java.util.Arrays;
import java.util.function.Predicate;

public interface ResultPredicates {

	public static <T> Predicate<Result<T>> resultRejected() {
		return f -> !f.success && f.exception==null;
	}

	@SafeVarargs
	public static <T> Predicate<Result<T>> exceptionAnyOf(final Class<? extends Throwable>... exceptions) {
		return f -> f.exception!=null && Arrays.asList(exceptions).stream().anyMatch(exClaz -> exClaz.isAssignableFrom(f.exception.getClass()));
	}
	
}