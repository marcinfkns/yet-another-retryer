package retryer.waitTimeStrategy;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class NoWaitTimeStrategyTest {

	@Mock
	RetryContext<Object> retryCtx;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void itReturnsZero() {
		NoWaitTimeStrategy<Object> strategy = new NoWaitTimeStrategy<>();
		for (int i=0; i<1000; ++i) {
			Assert.assertEquals(strategy.computeWaitTime(retryCtx), 0L);
		}
	}
}
