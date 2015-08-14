package retryer.stopstrategy;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class TimeoutStopStrategyTest {

	@Mock
	RetryContext<Object> retryCtx;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void itDoesntStopImmediatelyForZeroTimeout() {
		TimeoutStopStrategy<Object> s = new TimeoutStopStrategy<>(0);
		Assert.assertFalse(s.mustStop(retryCtx));
	}

	@Test
	public void itStopsWhenTimeoutAlreadyExceeded() {
		TimeoutStopStrategy<Object> s = new TimeoutStopStrategy<>(10);
		Mockito.when(retryCtx.getTime()).thenReturn(11L);
		Mockito.when(retryCtx.getFirstAttemptTime()).thenReturn(0L);
		Mockito.when(retryCtx.getNextDelay()).thenReturn(0L);
		Assert.assertTrue(s.mustStop(retryCtx));
	}

	@Test
	public void itStopsWhenTimeoutWouldBeExceeded() {
		TimeoutStopStrategy<Object> s = new TimeoutStopStrategy<>(10);
		Mockito.when(retryCtx.getTime()).thenReturn(0L);
		Mockito.when(retryCtx.getFirstAttemptTime()).thenReturn(0L);
		Mockito.when(retryCtx.getNextDelay()).thenReturn(11L);
		Assert.assertTrue(s.mustStop(retryCtx));
	}

	@Test
	public void itDoesntStopBeforeTimeout() {
		TimeoutStopStrategy<Object> s = new TimeoutStopStrategy<>(10);
		Mockito.when(retryCtx.getTime()).thenReturn(10L);
		Mockito.when(retryCtx.getFirstAttemptTime()).thenReturn(0L);
		Mockito.when(retryCtx.getNextDelay()).thenReturn(0L);
		Assert.assertFalse(s.mustStop(retryCtx));
	}

}
