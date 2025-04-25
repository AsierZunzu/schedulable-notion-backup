FROM ghcr.io/jckleiner/notion-backup

RUN apt-get update && apt-get install -y cron && rm -rf /var/cache/apt/archives /var/lib/apt/lists/*.
RUN touch /etc/cron.d/simple-cron
RUN chmod 0644 /etc/cron.d/simple-cron
RUN crontab /etc/cron.d/simple-cron
RUN touch /var/log/cron.log

COPY ./entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]