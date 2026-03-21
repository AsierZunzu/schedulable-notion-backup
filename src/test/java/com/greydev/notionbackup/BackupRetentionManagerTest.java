package com.greydev.notionbackup;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupRetentionManagerTest {

	@TempDir
	Path tempDir;

	@Mock
	private Dotenv dotenv;

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private BackupRetentionManager buildManager(int keepLast, int keepLastDay, int keepLastWeek,
			int keepLastMonth, int keepLastYear) {
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST)).thenReturn(String.valueOf(keepLast));
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_DAY)).thenReturn(String.valueOf(keepLastDay));
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_WEEK)).thenReturn(String.valueOf(keepLastWeek));
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_MONTH)).thenReturn(String.valueOf(keepLastMonth));
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_YEAR)).thenReturn(String.valueOf(keepLastYear));
		return new BackupRetentionManager(dotenv, tempDir.toString());
	}

	private File createBackup(String name, Instant timestamp) throws IOException {
		Path filePath = tempDir.resolve(name);
		Files.createFile(filePath);
		filePath.toFile().setLastModified(timestamp.toEpochMilli());
		return filePath.toFile();
	}

	private File createFile(String name) throws IOException {
		Path filePath = tempDir.resolve(name);
		Files.createFile(filePath);
		return filePath.toFile();
	}

	// -------------------------------------------------------------------------
	// Edge cases
	// -------------------------------------------------------------------------

	@Test
	void applyRetentionPolicy_emptyDirectory_doesNotThrow() {
		BackupRetentionManager manager = buildManager(5, 1, 3, 8, 12);
		manager.applyRetentionPolicy();
	}

	@Test
	void applyRetentionPolicy_nonExistentDirectory_doesNotThrow() {
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST)).thenReturn("5");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_DAY)).thenReturn("1");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_WEEK)).thenReturn("3");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_MONTH)).thenReturn("8");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_YEAR)).thenReturn("12");
		BackupRetentionManager manager = new BackupRetentionManager(dotenv, "/path/does/not/exist");
		manager.applyRetentionPolicy();
	}

	@Test
	void applyRetentionPolicy_nonBackupFilesAreNeverDeleted() throws IOException {
		createFile("logfile.log");
		createFile("notes.txt");
		BackupRetentionManager manager = buildManager(0, 0, 0, 0, 0);

		manager.applyRetentionPolicy();

		assertThat(tempDir.resolve("logfile.log").toFile()).exists();
		assertThat(tempDir.resolve("notes.txt").toFile()).exists();
	}

	@Test
	void applyRetentionPolicy_allRulesDisabled_deletesAllBackups() throws IOException {
		Instant now = Instant.now();
		File f1h = createBackup("notion-export-1h.zip", now.minus(1, ChronoUnit.HOURS));
		File f5d = createBackup("notion-export-5d.zip", now.minus(5, ChronoUnit.DAYS));
		BackupRetentionManager manager = buildManager(0, 0, 0, 0, 0);

		manager.applyRetentionPolicy();

		assertThat(f1h).doesNotExist();
		assertThat(f5d).doesNotExist();
	}

	// -------------------------------------------------------------------------
	// keepLast rule
	// -------------------------------------------------------------------------

	@Test
	void applyRetentionPolicy_keepLast_keepsNewestN_deletesOlderOnes() throws IOException {
		Instant now = Instant.now();
		File f1h = createBackup("notion-export-1h.zip", now.minus(1, ChronoUnit.HOURS));
		File f2h = createBackup("notion-export-2h.zip", now.minus(2, ChronoUnit.HOURS));
		File f3h = createBackup("notion-export-3h.zip", now.minus(3, ChronoUnit.HOURS));
		File f4h = createBackup("notion-export-4h.zip", now.minus(4, ChronoUnit.HOURS));
		File f5h = createBackup("notion-export-5h.zip", now.minus(5, ChronoUnit.HOURS));
		BackupRetentionManager manager = buildManager(3, 0, 0, 0, 0);

		manager.applyRetentionPolicy();

		assertThat(f1h).exists();
		assertThat(f2h).exists();
		assertThat(f3h).exists();
		assertThat(f4h).doesNotExist();
		assertThat(f5h).doesNotExist();
	}

	@Test
	void applyRetentionPolicy_keepLast_savesFilesOlderThanAllTimeWindows() throws IOException {
		// keepLast has no time bounds — even a very old file is kept if it is among the N most recent
		Instant now = Instant.now();
		File f400d = createBackup("notion-export-400d.zip", now.minus(400, ChronoUnit.DAYS));
		BackupRetentionManager manager = buildManager(1, 0, 0, 0, 0);

		manager.applyRetentionPolicy();

		assertThat(f400d).exists();
	}

	// -------------------------------------------------------------------------
	// keepLastDay rule — exclusive window: (now-1d, now]
	// -------------------------------------------------------------------------

	@Test
	void applyRetentionPolicy_keepLastDay_keepsOldestNFromDayWindow() throws IOException {
		// Three files all within 24 h; oldest two should survive with keepLastDay=2
		Instant now = Instant.now();
		File f2h = createBackup("notion-export-2h.zip", now.minus(2, ChronoUnit.HOURS));
		File f8h = createBackup("notion-export-8h.zip", now.minus(8, ChronoUnit.HOURS));
		File f20h = createBackup("notion-export-20h.zip", now.minus(20, ChronoUnit.HOURS));
		BackupRetentionManager manager = buildManager(0, 2, 0, 0, 0);

		manager.applyRetentionPolicy();

		assertThat(f8h).exists();
		assertThat(f20h).exists();
		assertThat(f2h).doesNotExist();
	}

	// -------------------------------------------------------------------------
	// keepLastWeek rule — exclusive window: (now-7d, now-1d]
	// -------------------------------------------------------------------------

	@Test
	void applyRetentionPolicy_keepLastWeek_usesExclusiveWindow_doesNotOverlapWithDay() throws IOException {
		// File in day window must NOT count towards the week quota
		Instant now = Instant.now();
		File f12h = createBackup("notion-export-12h.zip", now.minus(12, ChronoUnit.HOURS)); // day window
		File f2d = createBackup("notion-export-2d.zip", now.minus(2, ChronoUnit.DAYS));     // week exclusive
		File f5d = createBackup("notion-export-5d.zip", now.minus(5, ChronoUnit.DAYS));     // week exclusive
		BackupRetentionManager manager = buildManager(0, 0, 1, 0, 0);

		manager.applyRetentionPolicy();

		// keepLastWeek=1 picks the OLDEST from the exclusive week window
		assertThat(f5d).exists();
		assertThat(f12h).doesNotExist(); // day window, not covered by any rule
		assertThat(f2d).doesNotExist();  // not the oldest in the week window
	}

	@Test
	void applyRetentionPolicy_keepLastWeek_keepsOldestN() throws IOException {
		Instant now = Instant.now();
		File f2d = createBackup("notion-export-2d.zip", now.minus(2, ChronoUnit.DAYS));
		File f4d = createBackup("notion-export-4d.zip", now.minus(4, ChronoUnit.DAYS));
		File f6d = createBackup("notion-export-6d.zip", now.minus(6, ChronoUnit.DAYS));
		BackupRetentionManager manager = buildManager(0, 0, 2, 0, 0);

		manager.applyRetentionPolicy();

		assertThat(f4d).exists();
		assertThat(f6d).exists();
		assertThat(f2d).doesNotExist();
	}

	// -------------------------------------------------------------------------
	// keepLastMonth rule — exclusive window: (now-30d, now-7d]
	// -------------------------------------------------------------------------

	@Test
	void applyRetentionPolicy_keepLastMonth_usesExclusiveWindow_doesNotOverlapWithWeek() throws IOException {
		Instant now = Instant.now();
		File f3d = createBackup("notion-export-3d.zip", now.minus(3, ChronoUnit.DAYS));   // week window
		File f10d = createBackup("notion-export-10d.zip", now.minus(10, ChronoUnit.DAYS)); // month exclusive
		File f25d = createBackup("notion-export-25d.zip", now.minus(25, ChronoUnit.DAYS)); // month exclusive
		BackupRetentionManager manager = buildManager(0, 0, 0, 1, 0);

		manager.applyRetentionPolicy();

		assertThat(f25d).exists();
		assertThat(f3d).doesNotExist();  // week window, not covered
		assertThat(f10d).doesNotExist(); // not the oldest in the month window
	}

	// -------------------------------------------------------------------------
	// keepLastYear rule — exclusive window: (now-365d, now-30d]
	// -------------------------------------------------------------------------

	@Test
	void applyRetentionPolicy_keepLastYear_usesExclusiveWindow_doesNotOverlapWithMonth() throws IOException {
		Instant now = Instant.now();
		File f15d = createBackup("notion-export-15d.zip", now.minus(15, ChronoUnit.DAYS));   // month window
		File f45d = createBackup("notion-export-45d.zip", now.minus(45, ChronoUnit.DAYS));   // year exclusive
		File f200d = createBackup("notion-export-200d.zip", now.minus(200, ChronoUnit.DAYS)); // year exclusive
		BackupRetentionManager manager = buildManager(0, 0, 0, 0, 1);

		manager.applyRetentionPolicy();

		assertThat(f200d).exists();
		assertThat(f15d).doesNotExist();  // month window, not covered
		assertThat(f45d).doesNotExist();  // not the oldest in the year window
	}

	@Test
	void applyRetentionPolicy_filesOlderThanOneYear_deletedWhenNotCoveredByKeepLast() throws IOException {
		Instant now = Instant.now();
		File f400d = createBackup("notion-export-400d.zip", now.minus(400, ChronoUnit.DAYS));
		BackupRetentionManager manager = buildManager(0, 1, 1, 1, 1);

		manager.applyRetentionPolicy();

		assertThat(f400d).doesNotExist();
	}

	// -------------------------------------------------------------------------
	// Config parsing
	// -------------------------------------------------------------------------

	@Test
	void applyRetentionPolicy_invalidEnvVar_fallsBackToDefaultKeepLast() throws IOException {
		// Create 6 files; invalid env var for keepLast should fall back to default (5)
		Instant now = Instant.now();
		File f1h = createBackup("notion-export-1h.zip", now.minus(1, ChronoUnit.HOURS));
		File f2h = createBackup("notion-export-2h.zip", now.minus(2, ChronoUnit.HOURS));
		File f3h = createBackup("notion-export-3h.zip", now.minus(3, ChronoUnit.HOURS));
		File f4h = createBackup("notion-export-4h.zip", now.minus(4, ChronoUnit.HOURS));
		File f5h = createBackup("notion-export-5h.zip", now.minus(5, ChronoUnit.HOURS));
		File f6h = createBackup("notion-export-6h.zip", now.minus(6, ChronoUnit.HOURS));

		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST)).thenReturn("not-a-number");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_DAY)).thenReturn("0");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_WEEK)).thenReturn("0");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_MONTH)).thenReturn("0");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_YEAR)).thenReturn("0");
		BackupRetentionManager manager = new BackupRetentionManager(dotenv, tempDir.toString());

		manager.applyRetentionPolicy();

		// Default keepLast=5 → 5 most recent kept, 6th deleted
		assertThat(f1h).exists();
		assertThat(f2h).exists();
		assertThat(f3h).exists();
		assertThat(f4h).exists();
		assertThat(f5h).exists();
		assertThat(f6h).doesNotExist();
	}

	@Test
	void applyRetentionPolicy_negativeEnvVar_fallsBackToDefaultKeepLast() throws IOException {
		Instant now = Instant.now();
		File f1h = createBackup("notion-export-1h.zip", now.minus(1, ChronoUnit.HOURS));
		File f2h = createBackup("notion-export-2h.zip", now.minus(2, ChronoUnit.HOURS));
		File f3h = createBackup("notion-export-3h.zip", now.minus(3, ChronoUnit.HOURS));
		File f4h = createBackup("notion-export-4h.zip", now.minus(4, ChronoUnit.HOURS));
		File f5h = createBackup("notion-export-5h.zip", now.minus(5, ChronoUnit.HOURS));
		File f6h = createBackup("notion-export-6h.zip", now.minus(6, ChronoUnit.HOURS));

		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST)).thenReturn("-3");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_DAY)).thenReturn("0");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_WEEK)).thenReturn("0");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_MONTH)).thenReturn("0");
		when(dotenv.get(BackupRetentionManager.KEY_RETENTION_KEEP_LAST_YEAR)).thenReturn("0");
		BackupRetentionManager manager = new BackupRetentionManager(dotenv, tempDir.toString());

		manager.applyRetentionPolicy();

		assertThat(f1h).exists();
		assertThat(f2h).exists();
		assertThat(f3h).exists();
		assertThat(f4h).exists();
		assertThat(f5h).exists();
		assertThat(f6h).doesNotExist();
	}

	// -------------------------------------------------------------------------
	// Integration: all rules combined
	// -------------------------------------------------------------------------

	@Test
	void applyRetentionPolicy_allRules_eachWindowContributesDistinctFiles() throws IOException {
		Instant now = Instant.now();
		File fDay = createBackup("notion-export-6h.zip", now.minus(6, ChronoUnit.HOURS));
		File fWeek = createBackup("notion-export-3d.zip", now.minus(3, ChronoUnit.DAYS));
		File fMonth = createBackup("notion-export-15d.zip", now.minus(15, ChronoUnit.DAYS));
		File fYear = createBackup("notion-export-60d.zip", now.minus(60, ChronoUnit.DAYS));
		File fOutside = createBackup("notion-export-400d.zip", now.minus(400, ChronoUnit.DAYS));
		BackupRetentionManager manager = buildManager(1, 1, 1, 1, 1);

		manager.applyRetentionPolicy();

		assertThat(fDay).exists();
		assertThat(fWeek).exists();
		assertThat(fMonth).exists();
		assertThat(fYear).exists();
		assertThat(fOutside).doesNotExist();
	}
}
