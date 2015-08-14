package retryer.waitTimeStrategy;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class IncrementalWaitTimeStrategyTest {

	@Mock
	RetryContext<Object> retryCtx;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testComputeWaitTime() {
		final long initialDelayMillis = 1;
		final long incrementMillis = 2;
		final long maxDelayMillis = 11;
		IncrementalWaitTimeStrategy<Object> s = new IncrementalWaitTimeStrategy<>(initialDelayMillis, incrementMillis, maxDelayMillis);
		Assert.assertEquals(s.computeWaitTime(retryCtx), initialDelayMillis+incrementMillis*0);
		Assert.assertEquals(s.computeWaitTime(retryCtx), initialDelayMillis+incrementMillis*1);
		Assert.assertEquals(s.computeWaitTime(retryCtx), initialDelayMillis+incrementMillis*2);
		Assert.assertEquals(s.computeWaitTime(retryCtx), initialDelayMillis+incrementMillis*3);
		Assert.assertEquals(s.computeWaitTime(retryCtx), initialDelayMillis+incrementMillis*4);
		Assert.assertEquals(s.computeWaitTime(retryCtx), initialDelayMillis+incrementMillis*5);
		//after reaching max value always return the same result:
		for (int i=0; i<1000; ++i)
			Assert.assertEquals(s.computeWaitTime(retryCtx), initialDelayMillis+incrementMillis*5);
	}
}
