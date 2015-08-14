package retryer.stopstrategy;

import java.util.Arrays;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import retryer.RetryContext;

public class CompositeStopStrategyTest {

	CompositeStopStrategy<Object> compositeStrategy;

	@Mock
	StopStrategy<Object> s1, s2;
	
	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		compositeStrategy = new CompositeStopStrategy<Object>(Arrays.asList(s1, s2)) {
			@Override
			public boolean mustStop(RetryContext<Object> ctx) {
				return false; //this will not be invoked here anyway
			}
		};
	}

	@Test
	public void switchOff() {
		compositeStrategy.switchOff();
		Mockito.verify(s1, Mockito.times(1)).switchOff();
		Mockito.verify(s2, Mockito.times(1)).switchOff();
		Mockito.verify(s1, Mockito.times(0)).switchOn();
		Mockito.verify(s2, Mockito.times(0)).switchOn();
	}

	@Test
	public void switchOn() {
		compositeStrategy.switchOn();
		Mockito.verify(s1, Mockito.times(1)).switchOn();
		Mockito.verify(s2, Mockito.times(1)).switchOn();
		Mockito.verify(s1, Mockito.times(0)).switchOff();
		Mockito.verify(s2, Mockito.times(0)).switchOff();
	}
}
