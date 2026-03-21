package com.greydev.notionbackup.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ResultTest {

	@Test
	public void isSuccess_givenSuccessState_returnsTrue() {
		Result result = new Result();
		result.setState("success");
		assertTrue(result.isSuccess());
	}

	@Test
	public void isSuccess_givenSuccessStateUppercase_returnsTrue() {
		Result result = new Result();
		result.setState("SUCCESS");
		assertTrue(result.isSuccess());
	}

	@Test
	public void isSuccess_givenMixedCaseSuccessState_returnsTrue() {
		Result result = new Result();
		result.setState("Success");
		assertTrue(result.isSuccess());
	}

	@Test
	public void isSuccess_givenFailureState_returnsFalse() {
		Result result = new Result();
		result.setState("failure");
		assertFalse(result.isSuccess());
	}

	@Test
	public void isSuccess_givenArbitraryState_returnsFalse() {
		Result result = new Result();
		result.setState("in_progress");
		assertFalse(result.isSuccess());
	}

	@Test
	public void isSuccess_givenNullState_returnsFalse() {
		Result result = new Result();
		assertFalse(result.isSuccess());
	}

	@Test
	public void isFailure_givenFailureState_returnsTrue() {
		Result result = new Result();
		result.setState("failure");
		assertTrue(result.isFailure());
	}

	@Test
	public void isFailure_givenFailureStateUppercase_returnsTrue() {
		Result result = new Result();
		result.setState("FAILURE");
		assertTrue(result.isFailure());
	}

	@Test
	public void isFailure_givenMixedCaseFailureState_returnsTrue() {
		Result result = new Result();
		result.setState("Failure");
		assertTrue(result.isFailure());
	}

	@Test
	public void isFailure_givenSuccessState_returnsFalse() {
		Result result = new Result();
		result.setState("success");
		assertFalse(result.isFailure());
	}

	@Test
	public void isFailure_givenArbitraryState_returnsFalse() {
		Result result = new Result();
		result.setState("in_progress");
		assertFalse(result.isFailure());
	}

	@Test
	public void isFailure_givenNullState_returnsFalse() {
		Result result = new Result();
		assertFalse(result.isFailure());
	}

}
