package retryer.waitTimeStrategy;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class FixedWaitTimeStrategyTest {

	@Mock
	RetryContext<Object> retryCtx;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void itReturnsFixedTime() {
		final long delayMillis = 123;
		FixedWaitTimeStrategy<Object> strategy = new FixedWaitTimeStrategy<>(delayMillis);
		for (int i=0; i<1000; ++i) {
			Assert.assertEquals(strategy.computeWaitTime(retryCtx), delayMillis);
		}
	}
}
