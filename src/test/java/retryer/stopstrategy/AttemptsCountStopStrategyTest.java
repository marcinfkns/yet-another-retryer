package retryer.stopstrategy;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class AttemptsCountStopStrategyTest {

	private static final int MAX_ATTEMPT_COUNT = 2;

	private AttemptsCountStopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(MAX_ATTEMPT_COUNT);

	@Mock
	private RetryContext<Object> ctx;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void itMustNotStopAfterZeroAttempts() {
		Mockito.when(ctx.getAttemptsCount()).thenReturn(0);
		Assert.assertFalse(stopStrategy.mustStop(ctx));
	}

	@Test
	public void itMustNotStopAfterLimitMinusOneAttempts() {
		Mockito.when(ctx.getAttemptsCount()).thenReturn(MAX_ATTEMPT_COUNT - 1);
		Assert.assertFalse(stopStrategy.mustStop(ctx));
	}
	
	@Test
	public void itMustNotStopAfterLimitAttempts() {
		Mockito.when(ctx.getAttemptsCount()).thenReturn(MAX_ATTEMPT_COUNT);
		Assert.assertTrue(stopStrategy.mustStop(ctx));
	}
	
	@Test
	public void itMustNotStopAfterMoreThanLimitAttempts() {
		Mockito.when(ctx.getAttemptsCount()).thenReturn(MAX_ATTEMPT_COUNT  + 1);
		Assert.assertTrue(stopStrategy.mustStop(ctx));
	}
	
}
