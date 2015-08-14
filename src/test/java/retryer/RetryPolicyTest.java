package retryer;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import retryer.stopstrategy.StopStrategy;
import retryer.stopstrategy.TimeoutStopStrategy;
import retryer.waitStrategy.BlockingWaitStrategy;
import retryer.waitStrategy.WaitStrategy;
import retryer.waitTimeStrategy.FixedWaitTimeStrategy;
import retryer.waitTimeStrategy.NoWaitTimeStrategy;
import retryer.waitTimeStrategy.WaitTimeStrategy;

public class RetryPolicyTest {

	@Test
	public void test3ArgsConstructor() throws Exception {
		StopStrategy<Object> stopStrategy = new TimeoutStopStrategy<Object>(1);
		WaitTimeStrategy<Object> waitTimeStrategy = new FixedWaitTimeStrategy<Object>(1);
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<Object>();
		RetryPolicy<Object> retryPolicy = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		Assert.assertEquals(retryPolicy.getStopStrategy(), stopStrategy);
		Assert.assertEquals(retryPolicy.getWaitTimeStrategy(), waitTimeStrategy);
		Assert.assertEquals(retryPolicy.getWaitStrategy(), waitStrategy);
	}
	
	@Test
	public void test2ArgsConstructor() throws Exception {
		StopStrategy<Object> stopStrategy = new TimeoutStopStrategy<Object>(1);
		WaitTimeStrategy<Object> waitTimeStrategy = new FixedWaitTimeStrategy<Object>(1);
		RetryPolicy<Object> retryPolicy = new RetryPolicy<>(stopStrategy, waitTimeStrategy);
		Assert.assertEquals(retryPolicy.getStopStrategy(), stopStrategy);
		Assert.assertEquals(retryPolicy.getWaitTimeStrategy(), waitTimeStrategy);
		Assert.assertEquals(retryPolicy.getWaitStrategy().getClass(), BlockingWaitStrategy.class);
	}
	
	@Test
	public void test1ArgConstructor() throws Exception {
		StopStrategy<Object> stopStrategy = new TimeoutStopStrategy<Object>(1);
		RetryPolicy<Object> retryPolicy = new RetryPolicy<>(stopStrategy);
		Assert.assertEquals(retryPolicy.getStopStrategy(), stopStrategy);
		Assert.assertEquals(retryPolicy.getWaitTimeStrategy().getClass(), NoWaitTimeStrategy.class);
		Assert.assertEquals(retryPolicy.getWaitStrategy().getClass(), BlockingWaitStrategy.class);
	}
	
	@Test
	public void testSwitchingPolicies() throws Exception {
		@SuppressWarnings("unchecked")
		StopStrategy<Object> stopStrategy = Mockito.mock(StopStrategy.class);
		@SuppressWarnings("unchecked")
		WaitTimeStrategy<Object> waitTimeStrategy = Mockito.mock(WaitTimeStrategy.class);
		@SuppressWarnings("unchecked")
		WaitStrategy<Object> waitStrategy = Mockito.mock(WaitStrategy.class);
		
		RetryPolicy<Object> retryPolicy = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		
		retryPolicy.switchOn();
		Mockito.verify(stopStrategy, Mockito.times(1)).switchOn();
		Mockito.verify(stopStrategy, Mockito.times(0)).switchOff();
		Mockito.verify(waitTimeStrategy, Mockito.times(1)).switchOn();
		Mockito.verify(waitTimeStrategy, Mockito.times(0)).switchOff();
		Mockito.verify(waitStrategy, Mockito.times(1)).switchOn();
		Mockito.verify(waitStrategy, Mockito.times(0)).switchOff();
		
		retryPolicy.switchOff();
		Mockito.verify(stopStrategy, Mockito.times(1)).switchOn();
		Mockito.verify(stopStrategy, Mockito.times(1)).switchOff();
		Mockito.verify(waitTimeStrategy, Mockito.times(1)).switchOn();
		Mockito.verify(waitTimeStrategy, Mockito.times(1)).switchOff();
		Mockito.verify(waitStrategy, Mockito.times(1)).switchOn();
		Mockito.verify(waitStrategy, Mockito.times(1)).switchOff();
	}
	
	@Test
	public void testSwitchingNullPolicies() throws Exception {
		StopStrategy<Object> stopStrategy = null;
		WaitTimeStrategy<Object> waitTimeStrategy = null;
		WaitStrategy<Object> waitStrategy = null;
		RetryPolicy<Object> retryPolicy = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		retryPolicy.switchOn();
		retryPolicy.switchOff();
		//no NullPointerException... phew!
	}
	
}
