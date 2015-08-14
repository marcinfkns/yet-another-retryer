package retryer;

import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ResultPredicatesTest {

	@Test
	public void resultRejectedReturnsFalseForSuccess() throws Exception {
		Predicate<Result<Object>> isRejected = ResultPredicates.resultRejected();
		Result<Object> result = Result.success("OK");
		Assert.assertFalse(isRejected.test(result));
	}
	
	@Test
	public void resultRejectedReturnsFalseForFailure() throws Exception {
		Predicate<Result<Object>> isRejected = ResultPredicates.resultRejected();
		Result<Object> result = Result.failure(new Exception("FAIL"));
		Assert.assertFalse(isRejected.test(result));
	}
	
	@Test
	public void resultRejectedReturnsTrueForResultRejectedByUserProvidedPredicate() throws Exception {
		Predicate<Result<Object>> isRejected = ResultPredicates.resultRejected();
		Result<Object> result = Result.failure("result not acceptable by the user");
		Assert.assertTrue(isRejected.test(result));
	}
	
	@Test
	public void exceptionAnyOfReturnsFalseForSuccessfullResult() throws Exception {
		Predicate<Result<Object>> isAnyExceptionOf = ResultPredicates.exceptionAnyOf(Exception.class);
		Result<Object> result = Result.success("OK");
		Assert.assertFalse(isAnyExceptionOf.test(result));
	}
	
	@Test
	public void exceptionAnyOfReturnsFalseForResultRejectedByUser() throws Exception {
		Predicate<Result<Object>> isAnyExceptionOf = ResultPredicates.exceptionAnyOf(Exception.class);
		Result<Object> result = Result.failure("result not acceptable by the user");
		Assert.assertFalse(isAnyExceptionOf.test(result));
	}
	
	@Test
	public void exceptionAnyOfReturnsTrueForFailure() throws Exception {
		Predicate<Result<Object>> isAnyExceptionOf = ResultPredicates.exceptionAnyOf(Exception.class);
		Result<Object> result = Result.failure(new Exception("FAIL"));
		Assert.assertTrue(isAnyExceptionOf.test(result));
	}
	
	@Test
	public void exceptionAnyOfReturnsFalseForFailureWithUnmatchedException() throws Exception {
		Predicate<Result<Object>> isAnyExceptionOf = ResultPredicates.exceptionAnyOf(RuntimeException.class);
		Result<Object> result = Result.failure(new Exception("FAIL"));
		Assert.assertFalse(isAnyExceptionOf.test(result));
	}
	
}
