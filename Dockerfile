FROM maven:3.8.6-openjdk-11-slim AS build

COPY ./pom.xml /usr/src/mymaven/pom.xml
COPY ./src /usr/src/mymaven/src
WORKDIR /usr/src/mymaven

RUN mvn clean install

FROM openjdk:11

WORKDIR /

RUN mkdir /downloads
RUN chmod 755 /downloads

COPY --from=build /usr/src/mymaven/target/notion-backup-1.0-SNAPSHOT.jar /notion-backup.jar

RUN apt-get update && apt-get install -y cron --no-install-recommends && rm -rf /var/cache/apt/archives /var/lib/apt/lists/*.
RUN touch /etc/cron.d/notion-backup \
   chmod 0644 /etc/cron.d/notion-backup \
   crontab /etc/cron.d/notion-backup
RUN touch /var/log/cron.log

COPY ./entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]


### Build/Run

#   docker run -it --rm --name my-maven-project -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3.8.6-openjdk-11-slim mvn clean install && docker build --platform linux/amd64 --build-arg PATH_TO_JAR=./target/notion-backup-1.0-SNAPSHOT.jar .
