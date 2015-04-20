package retryer;

public interface RetryContext<T> {

	public int getAttemptsCount();

	public long getFirstAttemptTime();

	public long getLastDelay();

	public Clock getClock();

}