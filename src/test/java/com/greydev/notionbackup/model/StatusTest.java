package com.greydev.notionbackup.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class StatusTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void deserialize_givenFullJson_setsAllFields() throws Exception {
		String json = "{\"type\":\"complete\",\"pagesExported\":42,\"exportURL\":\"https://example.com/export.zip\"}";

		Status status = objectMapper.readValue(json, Status.class);

		assertEquals("complete", status.getType());
		assertEquals(42, status.getPagesExported());
		assertEquals("https://example.com/export.zip", status.getExportUrl());
	}

	@Test
	public void deserialize_givenJsonWithExportURLProperty_mapsToExportUrlField() throws Exception {
		String json = "{\"exportURL\":\"https://example.com/file.zip\"}";

		Status status = objectMapper.readValue(json, Status.class);

		assertEquals("https://example.com/file.zip", status.getExportUrl());
	}

	@Test
	public void deserialize_givenEmptyJson_setsNullFields() throws Exception {
		String json = "{}";

		Status status = objectMapper.readValue(json, Status.class);

		assertNull(status.getType());
		assertNull(status.getPagesExported());
		assertNull(status.getExportUrl());
	}

	@Test
	public void deserialize_givenJsonWithUnknownField_ignoresIt() throws Exception {
		String json = "{\"type\":\"complete\",\"unknownField\":\"ignored\"}";

		Status status = objectMapper.readValue(json, Status.class);

		assertEquals("complete", status.getType());
		assertNull(status.getPagesExported());
		assertNull(status.getExportUrl());
	}

	@Test
	public void setters_and_getters_work_correctly() {
		Status status = new Status();
		status.setType("in_progress");
		status.setPagesExported(10);
		status.setExportUrl("https://example.com/file.zip");

		assertEquals("in_progress", status.getType());
		assertEquals(10, status.getPagesExported());
		assertEquals("https://example.com/file.zip", status.getExportUrl());
	}

}
