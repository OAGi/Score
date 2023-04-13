FROM tomcat:10.1.7-jdk17-temurin

LABEL maintainer="Hakju Oh <hakju.oh@nist.gov>"

ENV DB_HOST localhost
ENV DB_PORT 3306
ENV DB_DATABASE oagi
ENV DB_USERNAME oagi
ENV DB_PASSWORD oagi
ENV REDIS_HOST localhost
ENV REDIS_PORT 6379

RUN apt-get -y update
RUN apt-get -y upgrade

COPY mysql-connector-j-8.0.32.jar /usr/local/tomcat/lib/mysql-connector-j-8.0.32.jar

RUN rm -rf /usr/local/tomcat/webapps/* && \
    chgrp -R 0 /usr/local/tomcat/ && \
    chmod -R g=u /usr/local/tomcat/

USER 1001
COPY score-http-3.0.2.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]
