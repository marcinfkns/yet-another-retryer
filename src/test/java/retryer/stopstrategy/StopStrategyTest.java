package retryer.stopstrategy;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class StopStrategyTest {

	@Mock
	StopStrategy<Object> s1, s2;
	@Mock
	RetryContext<Object> retryCtx;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void andReturnsFalseWhenNotAllPoliciesReturnTrue() {
		Mockito.when(s1.mustStop(Mockito.any())).thenReturn(false);
		Mockito.when(s2.mustStop(Mockito.any())).thenReturn(true);
		boolean mustStop = StopStrategy.and(s1, s2).mustStop(retryCtx);
		Assert.assertFalse(mustStop);
	}

	@Test
	public void andReturnsTrueWhenAllPoliciesReturnTrue() {
		Mockito.when(s1.mustStop(Mockito.any())).thenReturn(true);
		Mockito.when(s2.mustStop(Mockito.any())).thenReturn(true);
		boolean mustStop = StopStrategy.and(s1, s2).mustStop(retryCtx);
		Assert.assertTrue(mustStop);
	}

	@Test
	public void orReturnsFalseWhenAllPoliciesReturnFalse() {
		Mockito.when(s1.mustStop(Mockito.any())).thenReturn(false);
		Mockito.when(s2.mustStop(Mockito.any())).thenReturn(false);
		boolean mustStop = StopStrategy.or(s1, s2).mustStop(retryCtx);
		Assert.assertFalse(mustStop);
	}
	
	@Test
	public void orReturnsTrueWhenAtLeastOnePolicyReturnsTrue() {
		Mockito.when(s1.mustStop(Mockito.any())).thenReturn(true);
		Mockito.when(s2.mustStop(Mockito.any())).thenReturn(false);
		boolean mustStop = StopStrategy.or(s1, s2).mustStop(retryCtx);
		Assert.assertTrue(mustStop);
	}
	
}
