package retryer;

import static retryer.ResultPredicates.exceptionAnyOf;
import static retryer.ResultPredicates.resultRejected;

import java.util.Arrays;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.policyselector.PolicySelector;
import retryer.policyselector.RuleSetPolicySelector;
import retryer.policyselector.RuleSetPolicySelector.Rule;
import retryer.stopstrategy.AttemptsCountStopStrategy;
import retryer.stopstrategy.StopStrategy;
import retryer.waitStrategy.BlockingWaitStrategy;
import retryer.waitStrategy.WaitStrategy;
import retryer.waitTimeStrategy.NoWaitTimeStrategy;
import retryer.waitTimeStrategy.WaitTimeStrategy;

public class RetryerTest {

	@Mock
	RetryableAction<Object> service;

	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testRetryOnException() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(3);
		WaitTimeStrategy<Object> waitTimeStrategy = new NoWaitTimeStrategy<>();
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		PolicySelector<Object> policySelector = new RuleSetPolicySelector<Object>(Arrays.asList(
			new Rule<>(policy1, exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<>();
		retryer.setPolicySelector(policySelector);
		
		Mockito.when(service.invoke())
			.thenThrow(new RuntimeException("1"))
			.thenThrow(new ArithmeticException("2"))
			.thenReturn("OK");
		
		Object res = retryer.invoke(service);
		
		Assert.assertEquals(res, "OK");
		Mockito.verify(service, Mockito.times(3)).invoke();
	}

	@Test(expectedExceptions={Exception.class}, expectedExceptionsMessageRegExp="2")
	public void testThrowUnexpectedException() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(3);
		WaitTimeStrategy<Object> waitTimeStrategy = new NoWaitTimeStrategy<>();
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		PolicySelector<Object> policySelector = new RuleSetPolicySelector<Object>(Arrays.asList(
			new Rule<>(policy1, exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<>();
		retryer.setPolicySelector(policySelector);
		
		Mockito.when(service.invoke())
			.thenThrow(new RuntimeException("1"))
			.thenThrow(new Exception("2"))
			.thenReturn("should never get in here!");
		
		try {
			retryer.invoke(service);
		} finally {
			Mockito.verify(service, Mockito.times(2)).invoke();
		}
	}

	@Test
	public void testRejectResult() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(3);
		WaitTimeStrategy<Object> waitTimeStrategy = new NoWaitTimeStrategy<>();
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		PolicySelector<Object> policySelector = new RuleSetPolicySelector<Object>(Arrays.asList(
			new Rule<>(policy1, resultRejected())
		));
		Retryer<Object> retryer = new Retryer<>();
		retryer.setPolicySelector(policySelector);
		retryer.setRejectingPredicate(ret -> !"OK".equals(ret));
		
		Mockito.when(service.invoke())
			.thenReturn("ERR1")
			.thenReturn("ERR2")
			.thenReturn("OK");
		
		Object res = retryer.invoke(service);
		
		Assert.assertEquals(res, "OK");
		Mockito.verify(service, Mockito.times(3)).invoke();
	}

	@Test
	public void testSwitchingPolicies() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(4);
		WaitTimeStrategy<Object> waitTimeStrategy = new NoWaitTimeStrategy<>();
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = Mockito.spy(new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy));
		RetryPolicy<Object> policy2 = Mockito.spy(new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy));
		RetryPolicy<Object> policy3 = Mockito.spy(new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy));
		PolicySelector<Object> policySelector = new RuleSetPolicySelector<Object>(Arrays.asList(
				new Rule<>(policy1, resultRejected()),
				new Rule<>(policy2, exceptionAnyOf(ArithmeticException.class)),
				new Rule<>(policy3, exceptionAnyOf(NullPointerException.class))
		));
		Retryer<Object> retryer = new Retryer<>();
		retryer.setPolicySelector(policySelector);
		retryer.setRejectingPredicate(ret -> !"OK".equals(ret));

		Mockito.when(service.invoke())
			.thenReturn("ERR1")
			.thenThrow(new ArithmeticException("2"))
			.thenThrow(new NullPointerException("3"))
			.thenReturn("OK");
		
		Object res = retryer.invoke(service);
		
		Assert.assertEquals(res, "OK");
		Mockito.verify(service, Mockito.times(4)).invoke();

		Mockito.verify(policy1, Mockito.times(1)).switchOn();
		Mockito.verify(policy1, Mockito.times(1)).switchOff();

		Mockito.verify(policy2, Mockito.times(1)).switchOn();
		Mockito.verify(policy2, Mockito.times(1)).switchOff();

		Mockito.verify(policy3, Mockito.times(1)).switchOn();
	}

}
