package com.greydev.notionbackup.model;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ResultsTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void deserialize_givenJsonWithResultsList_setsResults() throws Exception {
		String json = "{\"results\":[{\"state\":\"success\"},{\"state\":\"failure\"}]}";

		Results results = objectMapper.readValue(json, Results.class);

		assertNotNull(results.getResults());
		assertEquals(2, results.getResults().size());
		assertEquals("success", results.getResults().get(0).getState());
		assertEquals("failure", results.getResults().get(1).getState());
	}

	@Test
	public void deserialize_givenJsonWithEmptyResultsList_setsEmptyList() throws Exception {
		String json = "{\"results\":[]}";

		Results results = objectMapper.readValue(json, Results.class);

		assertNotNull(results.getResults());
		assertTrue(results.getResults().isEmpty());
	}

	@Test
	public void deserialize_givenEmptyJson_setsNullResults() throws Exception {
		String json = "{}";

		Results results = objectMapper.readValue(json, Results.class);

		assertNull(results.getResults());
	}

	@Test
	public void deserialize_givenJsonWithUnknownField_ignoresIt() throws Exception {
		String json = "{\"results\":[],\"unknownField\":\"ignored\"}";

		Results results = objectMapper.readValue(json, Results.class);

		assertNotNull(results.getResults());
		assertTrue(results.getResults().isEmpty());
	}

	@Test
	public void deserialize_givenResultWithNestedStatus_setsStatusCorrectly() throws Exception {
		String json = "{\"results\":[{\"state\":\"success\",\"status\":{\"type\":\"complete\",\"pagesExported\":5,\"exportURL\":\"https://example.com/export.zip\"}}]}";

		Results results = objectMapper.readValue(json, Results.class);

		assertNotNull(results.getResults());
		assertEquals(1, results.getResults().size());
		Result result = results.getResults().get(0);
		assertEquals("success", result.getState());
		assertNotNull(result.getStatus());
		assertEquals("complete", result.getStatus().getType());
		assertEquals(5, result.getStatus().getPagesExported());
		assertEquals("https://example.com/export.zip", result.getStatus().getExportUrl());
	}

	@Test
	public void setters_and_getters_work_correctly() {
		Result r1 = new Result();
		r1.setState("success");
		Result r2 = new Result();
		r2.setState("failure");

		Results results = new Results();
		results.setResults(List.of(r1, r2));

		assertEquals(2, results.getResults().size());
		assertEquals("success", results.getResults().get(0).getState());
		assertEquals("failure", results.getResults().get(1).getState());
	}

}
