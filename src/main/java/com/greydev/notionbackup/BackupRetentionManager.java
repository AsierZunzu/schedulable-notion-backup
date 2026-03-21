package com.greydev.notionbackup;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BackupRetentionManager {

	static final String KEY_RETENTION_KEEP_LAST = "RETENTION_KEEP_LAST";
	static final String KEY_RETENTION_KEEP_LAST_DAY = "RETENTION_KEEP_LAST_DAY";
	static final String KEY_RETENTION_KEEP_LAST_WEEK = "RETENTION_KEEP_LAST_WEEK";
	static final String KEY_RETENTION_KEEP_LAST_MONTH = "RETENTION_KEEP_LAST_MONTH";
	static final String KEY_RETENTION_KEEP_LAST_YEAR = "RETENTION_KEEP_LAST_YEAR";

	private static final int DEFAULT_KEEP_LAST = 5;
	private static final int DEFAULT_KEEP_LAST_DAY = 1;
	private static final int DEFAULT_KEEP_LAST_WEEK = 3;
	private static final int DEFAULT_KEEP_LAST_MONTH = 8;
	private static final int DEFAULT_KEEP_LAST_YEAR = 12;

	private final int keepLast;
	private final int keepLastDay;
	private final int keepLastWeek;
	private final int keepLastMonth;
	private final int keepLastYear;
	private final String downloadsDirectoryPath;

	public BackupRetentionManager(Dotenv dotenv, String downloadsDirectoryPath) {
		this.downloadsDirectoryPath = downloadsDirectoryPath;
		this.keepLast = parseEnvInt(dotenv, KEY_RETENTION_KEEP_LAST, DEFAULT_KEEP_LAST);
		this.keepLastDay = parseEnvInt(dotenv, KEY_RETENTION_KEEP_LAST_DAY, DEFAULT_KEEP_LAST_DAY);
		this.keepLastWeek = parseEnvInt(dotenv, KEY_RETENTION_KEEP_LAST_WEEK, DEFAULT_KEEP_LAST_WEEK);
		this.keepLastMonth = parseEnvInt(dotenv, KEY_RETENTION_KEEP_LAST_MONTH, DEFAULT_KEEP_LAST_MONTH);
		this.keepLastYear = parseEnvInt(dotenv, KEY_RETENTION_KEEP_LAST_YEAR, DEFAULT_KEEP_LAST_YEAR);

		log.info("Retention policy: keepLast={}, keepLastDay={}, keepLastWeek={}, keepLastMonth={}, keepLastYear={}",
				keepLast, keepLastDay, keepLastWeek, keepLastMonth, keepLastYear);
	}

	public void applyRetentionPolicy() {
		File downloadsDir = new File(downloadsDirectoryPath);
		if (!downloadsDir.isDirectory()) {
			log.warn("Downloads directory does not exist or is not a directory: {}", downloadsDirectoryPath);
			return;
		}

		File[] backupFiles = downloadsDir.listFiles(
				(dir, name) -> name.startsWith("notion-export") && name.endsWith(".zip"));

		if (backupFiles == null || backupFiles.length == 0) {
			log.info("No backup files found in {}. Nothing to clean up.", downloadsDirectoryPath);
			return;
		}

		// Sort newest first; oldest-first iteration is done by walking the list in reverse
		List<File> sortedFiles = Arrays.stream(backupFiles)
				.sorted(Comparator.comparingLong(File::lastModified).reversed())
				.collect(Collectors.toList());

		log.info("Applying retention policy to {} backup file(s) in {}", sortedFiles.size(), downloadsDirectoryPath);

		Instant now = Instant.now();
		Instant cutDay   = now.minus(1,   ChronoUnit.DAYS);
		Instant cutWeek  = now.minus(7,   ChronoUnit.DAYS);
		Instant cutMonth = now.minus(30,  ChronoUnit.DAYS);
		Instant cutYear  = now.minus(365, ChronoUnit.DAYS);

		Set<File> filesToKeep = new HashSet<>();

		// Unconditional: the N most recent backups regardless of age
		markNewestToKeep(filesToKeep, sortedFiles, keepLast, "last " + keepLast);

		// Time-windowed rules use exclusive, non-overlapping windows so each rule
		// contributes distinct files. Within each window the OLDEST files are chosen
		// to maximise temporal coverage (reach as far back in the window as possible).
		markOldestToKeep(filesToKeep, sortedFiles, keepLastDay,   cutDay,   now,      "last day   (now-1d  → now)");
		markOldestToKeep(filesToKeep, sortedFiles, keepLastWeek,  cutWeek,  cutDay,   "last week  (now-7d  → now-1d)");
		markOldestToKeep(filesToKeep, sortedFiles, keepLastMonth, cutMonth, cutWeek,  "last month (now-30d → now-7d)");
		markOldestToKeep(filesToKeep, sortedFiles, keepLastYear,  cutYear,  cutMonth, "last year  (now-365d→ now-30d)");

		int deleted = 0;
		for (File file : sortedFiles) {
			if (!filesToKeep.contains(file)) {
				log.info("Deleting old backup: {}", file.getName());
				if (file.delete()) {
					deleted++;
				} else {
					log.warn("Failed to delete backup file: {}", file.getName());
				}
			}
		}

		log.info("Retention policy applied: kept {}, deleted {} backup file(s)", filesToKeep.size(), deleted);
	}

	/** Keeps the N most recent files from the full list (no time bounds). */
	private void markNewestToKeep(Set<File> filesToKeep, List<File> sortedFiles, int count, String label) {
		if (count <= 0) {
			return;
		}
		int marked = 0;
		for (File file : sortedFiles) {
			if (marked >= count) {
				break;
			}
			filesToKeep.add(file);
			marked++;
		}
		log.debug("Retention rule '{}': marked {} file(s) to keep", label, marked);
	}

	/**
	 * Keeps the N oldest files that fall strictly inside (from, to].
	 * Using the oldest files in each exclusive window maximises temporal spread:
	 * e.g. for "last week" we preserve a backup from ~7 days ago rather than ~2 days ago.
	 */
	private void markOldestToKeep(Set<File> filesToKeep, List<File> sortedFiles, int count,
			Instant from, Instant to, String label) {
		if (count <= 0) {
			return;
		}
		int marked = 0;
		// sortedFiles is newest-first, so iterate in reverse to get oldest-first
		for (int i = sortedFiles.size() - 1; i >= 0 && marked < count; i--) {
			File file = sortedFiles.get(i);
			Instant fileTime = Instant.ofEpochMilli(file.lastModified());
			// Window is exclusive on the lower bound, inclusive on the upper bound: (from, to]
			if (!fileTime.isAfter(from) || fileTime.isAfter(to)) {
				continue;
			}
			filesToKeep.add(file);
			marked++;
		}
		log.debug("Retention rule '{}': marked {} file(s) to keep", label, marked);
	}

	private int parseEnvInt(Dotenv dotenv, String key, int defaultValue) {
		String value = dotenv.get(key);
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		try {
			int parsed = Integer.parseInt(value.trim());
			if (parsed < 0) {
				log.warn("Negative value for {}: {}. Using default: {}", key, parsed, defaultValue);
				return defaultValue;
			}
			return parsed;
		} catch (NumberFormatException e) {
			log.warn("Invalid value for {}: '{}'. Using default: {}", key, value, defaultValue);
			return defaultValue;
		}
	}
}
