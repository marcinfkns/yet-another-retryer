package retryer;

import java.util.function.Supplier;

@FunctionalInterface
public interface Clock extends Supplier<Long> {

}