package retryer.waitTimeStrategy;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class ExponentialWaitTimeStrategyTest {

	@Mock
	RetryContext<Object> retryCtx;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testComputeWaitTime() {
		final long unitPeriodMillis = 10;
		final long maxDelayMillis = 640;
		ExponentialWaitTimeStrategy<Object> s = new ExponentialWaitTimeStrategy<>(unitPeriodMillis, maxDelayMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 1 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 2 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 4 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 8 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 16 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 32 * unitPeriodMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), 64 * unitPeriodMillis);
		//after reaching max value always return the same result:
		for (int i=0; i<1000; ++i)
			Assert.assertEquals(s.computeWaitTime(retryCtx), 64 * unitPeriodMillis);
	}
}
