package retryer;

@FunctionalInterface
public interface RetryableAction<T> {
	public T invoke() throws Throwable;
}