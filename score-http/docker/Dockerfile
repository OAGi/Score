# Spring Boot 4 requires a Servlet 6.1 container (#1750). Tomcat 10.1 is Servlet 6.0;
# Tomcat 11.0.x is Servlet 6.1 (Jakarta EE 11) and matches the embedded tomcat-embed 11.0.x
# pulled by the Boot 4.1 BOM.
FROM tomcat:11.0.27-jdk21-temurin

LABEL maintainer="Hakju Oh <hakju.oh@nist.gov>"

ARG MARIADB_CLIENT_VERSION

ENV DB_HOST=localhost \
    DB_PORT=3306 \
    DB_DATABASE=oagi \
    DB_USERNAME=oagi \
    REDIS_HOST=localhost \
    REDIS_PORT=6379

RUN apt-get -y update
RUN apt-get -y upgrade

# The Ubuntu 26.04 base ships /usr/bin/pebble (a service manager, not owned by any dpkg package)
# built with a Go stdlib that Trivy flags (HIGH). This image starts Tomcat via catalina.sh and
# never runs pebble, so drop it. Re-check when the base image changes.
RUN rm -f /usr/bin/pebble

COPY mariadb-java-client-${MARIADB_CLIENT_VERSION}.jar /usr/local/tomcat/lib/mariadb-java-client-${MARIADB_CLIENT_VERSION}.jar

# The WAR runs on this Tomcat, so Spring Boot's server.compression.* is not applied; compress
# responses (JSON, HTML, CSS, JS by Tomcat's default compressibleMimeType) on the Connector.
RUN sed -i 's|<Connector port="8080" protocol="HTTP/1.1"|& compression="on" compressionMinSize="1024"|' /usr/local/tomcat/conf/server.xml && \
    grep -q 'compression="on"' /usr/local/tomcat/conf/server.xml

RUN rm -rf /usr/local/tomcat/webapps/* && \
    chgrp -R 0 /usr/local/tomcat/ && \
    chmod -R g=u /usr/local/tomcat/

USER 1001
COPY score-http.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]
