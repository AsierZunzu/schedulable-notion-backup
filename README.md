# Schedulable notion backups

![example workflow name](https://github.com/AsierZunzu/schedulable-notion-backup/actions/workflows/docker-publish.yml/badge.svg?branch=main)
![Lint](https://github.com/AsierZunzu/schedulable-notion-backup/actions/workflows/lint.yml/badge.svg)

This project extends the [jckleiner/notion-backup](https://github.com/jckleiner/notion-backup) project by patching a couple of bugs and incorporating a scheduler to the Docker image so that the backups can be scheduled. This is achieved using a _cron_ which scheduling is based on an `.env` value.
An updated `.env.dist` file is provided with the original project's variables (see [README -> Set Credentials](https://github.com/jckleiner/notion-backup?tab=readme-ov-file#set-credentials)) plus a couple of custom more. A `compose.yaml` is included too, to ease up the configuration of the container.

## Configure the service
Follow the instructions at [README -> Set Credentials](https://github.com/jckleiner/notion-backup?tab=readme-ov-file#set-credentials) and add:
* `BACKUP_PATH` to set where do you want your backup files to be stored
* `SCHEDULING_CONFIG` to set the scheduling interval. Check out [this link](https://en.wikipedia.org/wiki/Cron) for more information on the format

## Backup retention

After each backup is created, a retention policy is applied to the local backup directory to automatically delete old files. Each rule operates on a **non-overlapping time window** and preserves the **oldest** available backups within that window, maximising temporal coverage.

| Environment variable | Default | Description |
|---|---|---|
| `RETENTION_KEEP_LAST` | `5` | Always keep the N most recent backups, regardless of age |
| `RETENTION_KEEP_LAST_DAY` | `1` | Keep the N oldest backups from the last 24 hours |
| `RETENTION_KEEP_LAST_WEEK` | `3` | Keep the N oldest backups from the 1–7 day range |
| `RETENTION_KEEP_LAST_MONTH` | `8` | Keep the N oldest backups from the 7–30 day range |
| `RETENTION_KEEP_LAST_YEAR` | `12` | Keep the N oldest backups from the 30–365 day range |

A file is deleted only if it is not selected by any rule. Set a variable to `0` to disable that rule.

Example outcome with daily backups after one year (default values):
- **Most recent 5** — unconditional, always available
- **~23 h ago** — oldest from the last day window
- **~4, 5, 6 days ago** — oldest 3 from the last week window
- **~22–29 days ago** — oldest 8 from the last month window
- **~353–364 days ago** — oldest 12 from the last year window

## Code style

This project uses [Checkstyle](https://checkstyle.org/) to enforce a consistent code style. The rules are defined in [`checkstyle.xml`](checkstyle.xml) and cover:

- No wildcard or unused imports
- Standard Java naming conventions (types, constants, methods, variables)
- All control structures must use braces
- No empty `catch` blocks (unless the variable is named `expected` or `ignore`)
- One statement per line
- Miscellaneous best-practice rules (`StringLiteralEquality`, `SimplifyBooleanReturn`, `FallThrough`, …)

Checkstyle runs automatically on every push and pull request via the **Lint** GitHub Actions workflow. It also runs locally as part of the Maven build during the `validate` phase, so any violation will fail `mvn install` too:

```bash
mvn checkstyle:check
```

## Run the service
Just start the service with docker compose and you are good to go:
```
docker compose up -d
```