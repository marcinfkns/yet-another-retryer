package retryer.waitTimeStrategy;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class SumWaitTimeStrategyTest {

	SumWaitTimeStrategy<Object> sum;

	@Mock
	RetryContext<Object> retryCtx;
	
	@Mock
	WaitTimeStrategy<Object> s1, s2;
	
	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		sum = new SumWaitTimeStrategy<>(s1,s2);
	}

	@Test
	public void itSumsWaitTimes() {
		Mockito.when(s1.computeWaitTime(Mockito.any())).thenReturn(1L);
		Mockito.when(s2.computeWaitTime(Mockito.any())).thenReturn(2L);
		long res = sum.computeWaitTime(retryCtx);
		Assert.assertEquals(res, 3L);
	}

	@Test
	public void itSumsNegativeWaitTimes() {
		Mockito.when(s1.computeWaitTime(Mockito.any())).thenReturn(-1L);
		Mockito.when(s2.computeWaitTime(Mockito.any())).thenReturn(2L);
		long res = sum.computeWaitTime(retryCtx);
		Assert.assertEquals(res, 1L);
	}

}
