package retryer.waitTimeStrategy;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class FibonacciWaitTimeStrategyTest {

	@Mock
	RetryContext<Object> retryCtx;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testComputeWaitTime() {
		final long unitPeriodMillis = 10;
		final long maxDelayMillis = 130;
		FibonacciWaitTimeStrategy<Object> s = new FibonacciWaitTimeStrategy<>(unitPeriodMillis, maxDelayMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 1 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 1 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 2 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 3 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 5 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 8 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 13 * unitPeriodMillis);
		//after reaching max value always return the same result:
		for (int i=0; i<1000; ++i)
			Assert.assertEquals(s.computeWaitTime(retryCtx), 13 * unitPeriodMillis);
	}
}
