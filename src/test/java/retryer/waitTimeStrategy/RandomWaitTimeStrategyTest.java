package retryer.waitTimeStrategy;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class RandomWaitTimeStrategyTest {

	@Mock
	RetryContext<Object> retryCtx;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void itReturnsWaitTimeWithinRange() {
		final long minDelayMillis = -10L;
		final long maxDelayMillis = 20L;
		RandomWaitTimeStrategy<Object> s = new RandomWaitTimeStrategy<>(minDelayMillis, maxDelayMillis);
		for (int i=0; i<1000_000; ++i) {
			long time = s.computeWaitTime(retryCtx);
			Assert.assertTrue(time >= minDelayMillis);
			Assert.assertTrue(time < maxDelayMillis);
		}
	}
}
