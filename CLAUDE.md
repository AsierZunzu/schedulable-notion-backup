# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Dockerized Java application that exports Notion workspaces on a cron schedule and uploads them to cloud storage (Google Drive, Dropbox, Nextcloud, pCloud). It is an enhanced fork of [jckleiner/notion-backup](https://github.com/jckleiner/notion-backup).

## Commands

### Build & Run

```bash
# Build the JAR locally
mvn clean install

# Run locally (requires .env file)
java -jar target/notion-backup-*.jar

# Build and run with Docker (local build)
docker compose up -d

# Run with published image
docker compose -f compose.yaml up -d
```

### Tests

```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=DropboxClientTest

# Run a single test method
mvn test -Dtest=DropboxClientTest#uploadSuccess
```

### Docker

```bash
# Build the Docker image
docker build -t notion-backup .

# Rebuild without cache
docker build --no-cache -t notion-backup .
```

## Architecture

### Execution Flow

1. **Container startup** (`entrypoint.sh`): Dumps env vars to `/etc/environment` for cron access, validates `SCHEDULING_CONFIG` cron syntax, writes cron job to `/etc/cron.d/notion-backup`, starts cron daemon.
2. **Cron fires** → runs `java -jar /notion-backup.jar`
3. **`NotionBackup.java`** (main class): Triggers Notion export, downloads the ZIP, then uploads in parallel via `CompletableFuture` to all configured cloud providers. Exits with code `1` if any upload fails.
4. **`NotionClient.java`**: POSTs to Notion API to enqueue export task, polls up to 500 times (5s intervals) for the download URL, then downloads the ZIP.

### Cloud Storage

Each provider implements `CloudStorageClient` (single `upload(File)` method). Providers are skipped silently if their required env vars are absent.

- `GoogleDriveClient` — Uses service account JSON (inline or file path)
- `DropboxClient` — Supports both access token and OAuth refresh token flow
- `NextcloudClient` — WebDAV protocol with email/password auth
- `PCloudClient` — pCloud SDK with folder ID targeting

Service factories (`GoogleDriveServiceFactory`, `DropboxServiceFactory`, `PCloudApiClientFactory`) handle SDK client initialization separately from upload logic.

### Key Configuration (`.env`)

| Variable | Purpose |
|---|---|
| `SCHEDULING_CONFIG` | Cron expression (e.g. `0 2 * * *`) |
| `TZ` | Container timezone |
| `NOTION_SPACE_ID` / `NOTION_TOKEN_V2` | Notion auth |
| `NOTION_EXPORT_TYPE` | `markdown` or `html` |
| `DOWNLOADS_DIRECTORY_PATH` | Path inside container for ZIP files |

Copy `.env.dist` to `.env` to get started.

### Multi-stage Docker Build

- **Build stage**: `maven:3.8.6-openjdk-11-slim` compiles the fat JAR
- **Runtime stage**: `openjdk:11` + `cron` package; JAR copied from build stage

### CI/CD

`.github/workflows/docker-publish.yml` publishes to `ghcr.io/asierzunzu/schedulable-notion-backup` on pushes to `main` and version tags (`v*.*.*`). Uses cosign for image signing and multi-platform builds.
