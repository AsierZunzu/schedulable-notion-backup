package com.greydev.notionbackup;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class NotionClientTest {

	@Mock
	private Dotenv dotenv;

	private void setupRequiredEnvVars() {
		when(dotenv.get("NOTION_SPACE_ID")).thenReturn("test-space-id");
		when(dotenv.get("NOTION_TOKEN_V2")).thenReturn("test-token-v2");
	}

	@Test
	public void constructor_givenNullDownloadsPath_usesDefaultPath() {
		setupRequiredEnvVars();
		// dotenv.get("DOWNLOADS_DIRECTORY_PATH") returns null (Mockito default)

		NotionClient client = new NotionClient(dotenv);

		assertEquals("/downloads", client.getDownloadsDirectoryPath());
	}

	@Test
	public void constructor_givenBlankDownloadsPath_usesDefaultPath() {
		setupRequiredEnvVars();
		when(dotenv.get("DOWNLOADS_DIRECTORY_PATH")).thenReturn("   ");

		NotionClient client = new NotionClient(dotenv);

		assertEquals("/downloads", client.getDownloadsDirectoryPath());
	}

	@Test
	public void constructor_givenCustomDownloadsPath_usesCustomPath() {
		setupRequiredEnvVars();
		when(dotenv.get("DOWNLOADS_DIRECTORY_PATH")).thenReturn("/custom/backup/path");

		NotionClient client = new NotionClient(dotenv);

		assertEquals("/custom/backup/path", client.getDownloadsDirectoryPath());
	}

}

