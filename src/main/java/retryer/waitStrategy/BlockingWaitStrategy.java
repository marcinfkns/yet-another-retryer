package retryer.waitStrategy;

import java.util.function.Consumer;

import retryer.RetryContext;

public class BlockingWaitStrategy<T> implements WaitStrategy<T> {

	final private Consumer<Long> waitingFunc;
	
	public BlockingWaitStrategy(Consumer<Long> waitingFunc) {
		this.waitingFunc = waitingFunc;
	}

	public BlockingWaitStrategy() {
		this.waitingFunc = (waitTime) -> {
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public void delay(RetryContext<T> ctx) {
		long waitTime = ctx.getNextDelay();
		long tstop = ctx.getTime() + waitTime;
		for (;;) {
			waitTime = tstop - ctx.getTime();
			try {
				this.waitingFunc.accept(waitTime);
				return;
			} catch (RuntimeException e) {
			}
		}
	}

}
