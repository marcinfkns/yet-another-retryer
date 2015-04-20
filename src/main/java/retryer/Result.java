package retryer;

public class Result<T> {
	final T result;
	final Throwable exception; // null -> the result of the last attempt was rejected by predicate function
	final boolean success;

	private Result(Throwable e) {
		this.exception = e;
		this.success = false;
		this.result = null;
	}

	private Result(T result, boolean success) {
		this.result = result;
		this.success = success;
		this.exception = null;
	}

	public T execute() throws Throwable {
		if (exception != null)
			throw exception;
		else
			return result;
	}

	public static <T> Result<T> success(T result) {
		return new Result<>(result, true);
	}

	public static <T> Result<T> failure(T result) {
		return new Result<>(result, false);
	}

	public static <T> Result<T> failure(Throwable e) {
		return new Result<>(e);
	}

}