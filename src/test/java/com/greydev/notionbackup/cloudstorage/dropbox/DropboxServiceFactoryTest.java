package com.greydev.notionbackup.cloudstorage.dropbox;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.dropbox.core.v2.DbxClientV2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DropboxServiceFactoryTest {

	@Test
	public void create_givenBlankToken_returnsEmpty() {
		Optional<DbxClientV2> result = DropboxServiceFactory.create("   ");
		assertFalse(result.isPresent());
	}

	@Test
	public void create_givenEmptyToken_returnsEmpty() {
		Optional<DbxClientV2> result = DropboxServiceFactory.create("");
		assertFalse(result.isPresent());
	}

	@Test
	public void create_givenNullToken_returnsEmpty() {
		Optional<DbxClientV2> result = DropboxServiceFactory.create(null);
		assertFalse(result.isPresent());
	}

	@Test
	public void create_givenValidToken_returnsPresent() {
		Optional<DbxClientV2> result = DropboxServiceFactory.create("some-access-token");
		assertTrue(result.isPresent());
	}

}
