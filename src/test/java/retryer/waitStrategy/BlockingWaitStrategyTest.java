package retryer.waitStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.management.RuntimeErrorException;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class BlockingWaitStrategyTest {

	@Mock
	RetryContext<Object> retryCtx;
	
	@Mock
	Consumer<Long> waitingFunc;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testUninterruptedDelay() {
		BlockingWaitStrategy<Object> s = new BlockingWaitStrategy<>(waitingFunc);
		Mockito.when(retryCtx.getNextDelay()).thenReturn(1234L);
		s.delay(retryCtx);
		Mockito.verify(waitingFunc, Mockito.times(1)).accept(1234L);
	}

	@Test
	public void testInterruptedDelay() {
		BlockingWaitStrategy<Object> s = new BlockingWaitStrategy<>(waitingFunc);
		Mockito.when(retryCtx.getNextDelay()).thenReturn(1234L);
		Mockito
			.doThrow(new RuntimeException())
			.doThrow(new RuntimeException())
			.doNothing()
			.when(waitingFunc).accept(1234L);
		s.delay(retryCtx);
		Mockito.verify(waitingFunc, Mockito.times(3)).accept(1234L);
	}

	@Test
	public void testInterruptedPartialDelays() {
		BlockingWaitStrategy<Object> s = new BlockingWaitStrategy<>(waitingFunc);
		Mockito.when(retryCtx.getNextDelay()).thenReturn(1234L);
		final AtomicLong clock = new AtomicLong(0L);
		Mockito.when(retryCtx.getTime()).thenAnswer(invk -> clock.get());
		final Collection<Long> waitTimes = new ArrayList<>();
		Mockito
			.doAnswer(invk -> {
				waitTimes.add((long)invk.getArguments()[0]);
				clock.set(1000);
				throw new RuntimeException();
			})
			.doAnswer(invk -> {
				waitTimes.add((long)invk.getArguments()[0]);
				clock.set(1100);
				throw new RuntimeException();
			})
			.doAnswer(invk -> {
				waitTimes.add((long)invk.getArguments()[0]);
				return null;
			})
			.when(waitingFunc).accept(Mockito.anyLong());
		s.delay(retryCtx);
		Mockito.verify(waitingFunc, Mockito.times(3)).accept(Mockito.anyLong());
		Assert.assertEquals(waitTimes, Arrays.asList(1234L, 234L, 134L));
	}

}
