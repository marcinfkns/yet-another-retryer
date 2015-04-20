# yet-another-retryer
A retry-on-failure library for Java

## Features
- you can specify different retry policy for each kind of failure (inspired by [Spring Batch](http://docs.spring.io/spring-batch/2.1.x/reference/html-single/index.html#retryPolicies))
- many built-in policies (eg. expotential/fibonacci backoff etc. - inspired by [guava-retrying](https://github.com/rholder/guava-retrying))
- asynchronous mode (inspiration from [async-retry](https://github.com/nurkiewicz/async-retry))
- not only exceptions are considered failures: you can specify a predicate classifying the return value as a success of failure

# Retry policies

A policy consists of:
- stop strategy: when to give up retrying
- backoff strategy: determines delays between attempts
- block strategy: implements the actual waiting (only relevant for synchronous mode)

## Stop strategies
- AttemptsCountStopStrategy: give up after specified number of attempts
- TimeoutStopStrategy: give up after specified time since first attempt
- stop strategies can be combined together using AND/OR semantics (see StopStrategy.or() and StopStrategy.and())

## Backoff strategies
- NoWaitTimeStrategy: no delay at all
- FixedWaitTimeStrategy: fixed delay
- IncrementalWaitTimeStrategy: every next delay is longer by fixed amount of time
- RandomWaitTimeStrategy: random delay between min and max
- ExponentialWaitTimeStrategy: each time wait two times longer than previously
- FibonacciWaitTimeStrategy: delay times computed based on Fibonacci sequence
- SumWaitTimeStrategy: computes delay time as a sum of values returned by individual strategies, eg. exponential backoff with random jitter

## Block strategies
- BlockingWaitStrategy: Thread.sleep

# Examples

## Basic example
Assume you want to call the following service:

```java
String doStuff() throws Exception1, Exception2;
```

Here's a Retryer for that:

```java
Retryer<String> retryer = new Retryer<>();
retryer.setPolicy(
	new AttemptsCountStopStrategy<>(3), 
	new FixedWaitTimeStrategy<>(1000L), 
	new BlockingWaitStrategy<>());
try {
	String result = retryer.invoke( () -> service.doStuff() );
} catch (Exception1 e) {
	// after 3 unsuccessfull attempts this exception was thrown
} catch (Exception2 e) { 
	// after 3 unsuccessfull attempts this exception was thrown
}
```

## Retrying with multiple policies
```java
Retryer<String> retryer = new Retryer<>();

RetryPolicy<String> policy1 = new RetryPolicy<>(
	new AttemptsCountStopStrategy<>(3), 
	new FixedWaitTimeStrategy<>(1000L));

RetryPolicy<String> policy2 = new RetryPolicy<>(
	new TimeoutStopStrategy<>(30_000L), 
	new ExponentialWaitTimeStrategy<>(1000L, 10_000L));

retryer.setPolicySelector(new RuleSetPolicySelector<String>(Arrays.asList(
	new Rule<>(policy1, exceptionAnyOf(Exception1.class)),
	new Rule<>(policy2, exceptionAnyOf(Exception2.class))
)));

try {
	String result = retryer.invoke( () -> service.doStuff() );
} catch (Exception1 e) {
	// after 3 unsuccessfull attempts this exception was thrown
} catch (Exception2 e) { 
	// after 3 unsuccessfull attempts this exception was thrown
}
```

## Result as a failure
```java
Retryer<String> retryer = new Retryer<>();

RetryPolicy<String> policy1 = new RetryPolicy<>( ... );
RetryPolicy<String> policy2 = new RetryPolicy<>( ... );

retryer.setRejectingPredicate(ret -> !"OK".equals(ret));

retryer.setPolicySelector(new RuleSetPolicySelector<String>(Arrays.asList(
	new Rule<>(policy1, resultRejected()),
	new Rule<>(policy2, exceptionAnyOf(Exception.class))
)));

try {
	String result = retryer.invoke( () -> service.doStuff() );
} catch (Exception e) { ... }
```

## Asynchronous mode

```java
Retryer<String> retryer = new Retryer<>();
retryer.setPolicy(
	new AttemptsCountStopStrategy<>(3), 
	new FixedWaitTimeStrategy<>(1000L), 
	new BlockingWaitStrategy<>());

// Executor service for running retries asynchronously:
ScheduledExecutorService executor = ... ;

String res;
try {
	Future<String> f = retryer.invokeAsync(
		() -> service.doStuff(), 
		executor);
	res = f.get();
} catch (Exception1 e) {
	// after 3 unsuccessfull attempts this exception was thrown
} catch (Exception2 e) { 
	// after 3 unsuccessfull attempts this exception was thrown
}
```
