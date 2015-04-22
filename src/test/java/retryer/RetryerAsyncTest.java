package retryer;

import static retryer.ResultPredicates.exceptionAnyOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
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
import retryer.policyselector.RuleSetPolicySelector.Rule;
import retryer.stopstrategy.AttemptsCountStopStrategy;
import retryer.stopstrategy.StopStrategy;
import retryer.waitStrategy.BlockingWaitStrategy;
import retryer.waitStrategy.WaitStrategy;
import retryer.waitTimeStrategy.WaitTimeStrategy;

public class RetryerAsyncTest {

	@Mock
	RetryableAction<Object> service;

	@Mock
	ScheduledExecutorService executor;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testAsyncRetryOnException() throws Throwable {
		StopStrategy<Object> stopStrategy = new AttemptsCountStopStrategy<>(3);
		WaitTimeStrategy<Object> waitTimeStrategy = ctx -> ctx.getAttemptsCount()*1000L;
		WaitStrategy<Object> waitStrategy = new BlockingWaitStrategy<>();
		RetryPolicy<Object> policy1 = new RetryPolicy<>(stopStrategy, waitTimeStrategy, waitStrategy);
		PolicySelector<Object> policySelector = new RuleSetPolicySelector<Object>(Arrays.asList(
			new Rule<>(policy1, exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<>();
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
			new Rule<>(policy1, exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<>();
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
			new Rule<>(policy1, exceptionAnyOf(RuntimeException.class))
		));
		Retryer<Object> retryer = new Retryer<>();
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

	// when executor rejects our call/retry attempt:
	@Test(expectedExceptions={RejectedExecutionException.class}, expectedExceptionsMessageRegExp="task rejected")
	public void testAsyncRejectedByExecutor() throws Throwable {
		Retryer<Object> retryer = new Retryer<>(); //no policy configured -> will use the default one

		Mockito.when(executor.schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS))).then(inv -> {
			throw new RejectedExecutionException("task rejected");
		});
		
		Future<Object> res = retryer.invokeAsync(service, executor);
		
		Mockito.verify(service, Mockito.times(0)).invoke();

		try {
			res.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

}
