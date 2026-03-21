package com.greydev.notionbackup.cloudstorage.nextcloud;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;


class NextcloudClientTest {

	@Test
	public void upload_givenNonExistentFile_returnsFalse() {
		NextcloudClient client = new NextcloudClient("user@example.com", "password", "http://nextcloud.example.com/path/");
		File nonExistentFile = new File("this-file-does-not-exist.txt");

		boolean result = client.upload(nonExistentFile);

		assertFalse(result);
	}

	@Test
	public void upload_givenUrlWithoutTrailingSlash_andNonExistentFile_returnsFalse() {
		NextcloudClient client = new NextcloudClient("user@example.com", "password", "http://nextcloud.example.com/path/file.zip");
		File nonExistentFile = new File("this-file-does-not-exist.txt");

		boolean result = client.upload(nonExistentFile);

		assertFalse(result);
	}

}
