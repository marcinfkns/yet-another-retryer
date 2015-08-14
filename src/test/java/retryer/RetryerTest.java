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
import retryer.policyselector.SinglePolicySelector;
import retryer.stopstrategy.AttemptsCountStopStrategy;
import retryer.stopstrategy.StopStrategy;
import retryer.stopstrategy.TimeoutStopStrategy;
import retryer.waitStrategy.BlockingWaitStrategy;
import retryer.waitStrategy.WaitStrategy;
import retryer.waitTimeStrategy.FixedWaitTimeStrategy;
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
		Assert.assertEquals(retryer.getAttemptsCount(), 3);
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
			Assert.assertEquals(retryer.getAttemptsCount(), 2);
		}
	}

	@Test
	public void testNegativeWaitTimes() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(3);
		WaitTimeStrategy<Object> waitTimeStrategy = ctx -> -1L; //deliberately returns negative back off time
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		PolicySelector<Object> policySelector = new SinglePolicySelector<>(policy1);
		Retryer<Object> retryer = new Retryer<>();
		retryer.setPolicySelector(policySelector);
		retryer.setClock(() -> 0L);
		
		Mockito.when(service.invoke())
			.thenThrow(new RuntimeException("ERR"))
			.thenReturn("OK");
		
		try {
			retryer.invoke(service);
		} finally {
			System.out.printf("testNegativeWaitTimes: %s %s\n", retryer.getLastDelay(), retryer.getAttemptsCount());
			Assert.assertEquals(retryer.getLastDelay(), 0); //if back off time < 0 it should assume 0
			Assert.assertEquals(retryer.getAttemptsCount(), 2);
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
		Assert.assertEquals(retryer.getAttemptsCount(), 3);
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
		Assert.assertEquals(retryer.getAttemptsCount(), 4);

		Mockito.verify(policy1, Mockito.times(1)).switchOn();
		Mockito.verify(policy1, Mockito.times(1)).switchOff();

		Mockito.verify(policy2, Mockito.times(1)).switchOn();
		Mockito.verify(policy2, Mockito.times(1)).switchOff();

		Mockito.verify(policy3, Mockito.times(1)).switchOn();
	}
	
	//when waiting until next attempt would have exceeded the total timeout
	@Test(expectedExceptions={RuntimeException.class}, expectedExceptionsMessageRegExp="1")
	public void testTimeoutBeforeBackoff() throws Throwable {
		Retryer<Object> retryer = new Retryer<>();
		StopStrategy<Object> stopStrategy = new TimeoutStopStrategy<>(1000);
		WaitTimeStrategy<Object> waitTimeStrategy = new FixedWaitTimeStrategy<>(2000); //wait time > total timeout(!)
		@SuppressWarnings("unchecked")
		WaitStrategy<Object> waitStrategy = Mockito.mock(WaitStrategy.class); 
		retryer.setPolicy(stopStrategy, waitTimeStrategy, waitStrategy);

		Mockito.when(service.invoke())
			.thenThrow(new RuntimeException("1"));

		try {
			retryer.invoke(service);
		} finally {
			Assert.assertEquals(retryer.getLastDelay(), 0L);
			Mockito.verify(service, Mockito.times(1)).invoke(); //shouldn't try next attempt, because it would exceed total timeout
			Assert.assertEquals(retryer.getAttemptsCount(), 1);
			Mockito.verify(waitStrategy, Mockito.never()).delay(Mockito.any()); //no backoff expected
		}
	}

	//when a retry attempt is delayed more than specified by WaitTime strategy and would occur after total timeout:
	@Test(expectedExceptions={RuntimeException.class}, expectedExceptionsMessageRegExp="1")
	public void testTimeoutOnDelayedAttempt() throws Throwable {
		Retryer<Object> retryer = new Retryer<>();
		StopStrategy<Object> stopStrategy = new TimeoutStopStrategy<>(1000);
		WaitTimeStrategy<Object> waitTimeStrategy = new FixedWaitTimeStrategy<>(1);
		@SuppressWarnings("unchecked")
		WaitStrategy<Object> waitStrategy = Mockito.mock(WaitStrategy.class); 
		retryer.setPolicy(stopStrategy, waitTimeStrategy, waitStrategy);

		Mockito.when(service.invoke())
			.thenThrow(new RuntimeException("1"));

		retryer.setClock(()->0L); //initial clock value: 0
		
		Mockito.doAnswer(invk -> {
			retryer.setClock(()->1001L); //1ms after total timeout!
			return null;
		}).when(waitStrategy).delay(Mockito.any());
		
		try {
			retryer.invoke(service);
		} finally {
			Assert.assertEquals(retryer.getLastDelay(), 1L);
			Mockito.verify(service, Mockito.times(1)).invoke(); //shouldn't try next attempt, because it would exceed total timeout
			Assert.assertEquals(retryer.getAttemptsCount(), 1);
			Mockito.verify(waitStrategy, Mockito.times(1)).delay(Mockito.any());
		}
	}

}
