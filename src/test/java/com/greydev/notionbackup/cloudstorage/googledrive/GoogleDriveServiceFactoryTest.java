package com.greydev.notionbackup.cloudstorage.googledrive;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.api.services.drive.Drive;

import static org.junit.jupiter.api.Assertions.assertFalse;


class GoogleDriveServiceFactoryTest {

	@Test
	public void create_givenInvalidJson_returnsEmpty() {
		Optional<Drive> result = GoogleDriveServiceFactory.create("not-valid-json");
		assertFalse(result.isPresent());
	}

	@Test
	public void create_givenEmptyJsonObject_returnsEmpty() {
		Optional<Drive> result = GoogleDriveServiceFactory.create("{}");
		assertFalse(result.isPresent());
	}

}
