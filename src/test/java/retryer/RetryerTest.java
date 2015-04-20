package retryer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.policyselector.PolicySelector;
import retryer.policyselector.RuleSetPolicySelector;
import retryer.stopstrategy.AttemptsCountStopStrategy;
import retryer.stopstrategy.StopStrategy;
import retryer.waitStrategy.BlockingWaitStrategy;
import retryer.waitStrategy.WaitStrategy;
import retryer.waitTimeStrategy.NoWaitTimeStrategy;
import retryer.waitTimeStrategy.WaitTimeStrategy;

public class RetryerTest {

	@Mock
	RetryableAction<Object> service;

	@Mock
	ScheduledExecutorService executor;
	
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
			new RuleSetPolicySelector.Rule<>(policy1, ResultPredicates.exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<Object>();
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
			new RuleSetPolicySelector.Rule<>(policy1, ResultPredicates.exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<Object>();
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
			new RuleSetPolicySelector.Rule<>(policy1, ResultPredicates.resultRejected())
		));
		Retryer<Object> retryer = new Retryer<Object>();
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
				new RuleSetPolicySelector.Rule<>(policy1, ResultPredicates.resultRejected()),
				new RuleSetPolicySelector.Rule<>(policy2, ResultPredicates.exceptionAnyOf(ArithmeticException.class)),
				new RuleSetPolicySelector.Rule<>(policy3, ResultPredicates.exceptionAnyOf(NullPointerException.class))
		));
		Retryer<Object> retryer = new Retryer<Object>();
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

	@Test
	public void testAsyncRetryOnException() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(3);
		WaitTimeStrategy<Object> waitTimeStrategy = ctx -> ctx.getAttemptsCount()*1000L;
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		PolicySelector<Object> policySelector = new RuleSetPolicySelector<Object>(Arrays.asList(
			new RuleSetPolicySelector.Rule<>(policy1, ResultPredicates.exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<Object>();
		retryer.setPolicySelector(policySelector);

		final List<Long> delays = new ArrayList<>();
		Mockito.when(executor.schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS))).then(inv -> {
			Runnable action = (Runnable) inv.getArguments()[0];
			long delay = (long) inv.getArguments()[1];
			delays.add(delay);
			action.run();
			return null;
		});
		
		Mockito.when(service.invoke())
			.thenThrow(new RuntimeException("1"))
			.thenThrow(new ArithmeticException("2"))
			.thenReturn("OK");
		
		Future<Object> res = retryer.invokeAsync(service, executor);
		
		Assert.assertEquals(res.get(), "OK");
		Mockito.verify(service, Mockito.times(3)).invoke();
		Assert.assertEquals(delays, Arrays.asList(0L, 1000L, 2000L));
	}

	@Test(expectedExceptions={Exception.class}, expectedExceptionsMessageRegExp="2")
	public void testAsyncThrowUnexpectedException() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(3);
		WaitTimeStrategy<Object> waitTimeStrategy = ctx -> ctx.getAttemptsCount()*1000L;
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		PolicySelector<Object> policySelector = new RuleSetPolicySelector<Object>(Arrays.asList(
			new RuleSetPolicySelector.Rule<>(policy1, ResultPredicates.exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<Object>();
		retryer.setPolicySelector(policySelector);

		final List<Long> delays = new ArrayList<>();
		Mockito.when(executor.schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS))).then(inv -> {
			Runnable action = (Runnable) inv.getArguments()[0];
			long delay = (long) inv.getArguments()[1];
			delays.add(delay);
			action.run();
			return null;
		});
		
		Mockito.when(service.invoke())
			.thenThrow(new RuntimeException("1"))
			.thenThrow(new Exception("2"))
			.thenReturn("should never get in here!");

		Future<Object> res = retryer.invokeAsync(service, executor);

		Mockito.verify(service, Mockito.times(2)).invoke();
		Assert.assertEquals(delays, Arrays.asList(0L, 1000L));
		try {
			res.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	@Test
	public void testAsyncImmediateSuccess() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(3);
		WaitTimeStrategy<Object> waitTimeStrategy = ctx -> ctx.getAttemptsCount()*1000L;
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		PolicySelector<Object> policySelector = new RuleSetPolicySelector<Object>(Arrays.asList(
			new RuleSetPolicySelector.Rule<>(policy1, ResultPredicates.exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<Object>();
		retryer.setPolicySelector(policySelector);

		final List<Long> delays = new ArrayList<>();
		Mockito.when(executor.schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS))).then(inv -> {
			Runnable action = (Runnable) inv.getArguments()[0];
			long delay = (long) inv.getArguments()[1];
			delays.add(delay);
			action.run();
			return null;
		});
		
		Mockito.when(service.invoke())
			.thenReturn("OK");
		
		Future<Object> res = retryer.invokeAsync(service, executor);
		
		Assert.assertEquals(res.get(), "OK");
		Mockito.verify(service, Mockito.times(1)).invoke();
		Assert.assertEquals(delays, Arrays.asList(0L));
	}

}
