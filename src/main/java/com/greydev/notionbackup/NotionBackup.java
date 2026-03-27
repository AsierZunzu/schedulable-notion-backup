package com.greydev.notionbackup;

import java.io.File;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NotionBackup {

	private static final Dotenv DOTENV;

	static {
		DOTENV = initDotenv();
	}

	public static void main(String[] args) {
		log.info("---------------- Starting Notion Backup ----------------");

		NotionClient notionClient = new NotionClient(DOTENV);

		final File exportedFile = notionClient.export()
				.orElseThrow(() -> new IllegalStateException("Could not export notion file"));

		new BackupRetentionManager(DOTENV, notionClient.getDownloadsDirectoryPath()).applyRetentionPolicy();

		log.info("Backup completed successfully: {}", exportedFile.getName());
	}

	private static Dotenv initDotenv() {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.ignoreIfMalformed()
				.load();
		if (dotenv == null) {
			throw new IllegalStateException("Could not load dotenv!");
		}
		return dotenv;
	}
}
