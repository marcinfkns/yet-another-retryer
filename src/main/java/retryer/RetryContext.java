package retryer;

public interface RetryContext<T> {

	/** @return current time from Retryer's clock */
	public long getTime();

	public int getAttemptsCount();

	public long getFirstAttemptTime();

	/** @return the delay before last attempt */
	public long getLastDelay();
	
	/** @return the delay to wait before next attempt (if already calculated by WaitTimeStratey, otherwise 0) */
	public long getNextDelay();

}