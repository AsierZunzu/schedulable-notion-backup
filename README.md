# Schedulable notion backups

![example workflow name](https://github.com/AsierZunzu/schedulable-notion-backup/actions/workflows/docker-publish.yml/badge.svg?branch=main)

This project extends the [jckleiner/notion-backup](https://github.com/jckleiner/notion-backup) project by patching a couple of bugs and incorporating a scheduler to the Docker image so that the backups can be scheduled. This is achieved using a _cron_ which scheduling is based on an `.env` value.
An updated `.env.dist` file is provided with the original project's variables (see [README -> Set Credentials](https://github.com/jckleiner/notion-backup?tab=readme-ov-file#set-credentials)) plus a couple of custom more. A `compose.yaml` is included too, to ease up the configuration of the container.

## Configure the service
Follow the instructions at [README -> Set Credentials](https://github.com/jckleiner/notion-backup?tab=readme-ov-file#set-credentials) and add:
* `BACKUP_PATH` to set where do you want your backup files to be stored
* `SCHEDULING_CONFIG` to set the scheduling interval. Check out [this link](https://en.wikipedia.org/wiki/Cron) for more information on the format

## Run the service
Just start the service with docker compose and you are good to go:
```
docker compose up -d
```