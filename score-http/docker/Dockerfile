FROM tomcat:9.0.73-jdk17-corretto

LABEL maintainer="Hakju Oh <hakju.oh@nist.gov>"

ENV DB_HOST localhost
ENV DB_PORT 3306
ENV DB_DATABASE oagi
ENV DB_USERNAME oagi
ENV DB_PASSWORD oagi
ENV REDIS_HOST localhost
ENV REDIS_PORT 6379

RUN yum -y update

COPY mysql-connector-j-8.0.32.jar /usr/local/tomcat/lib/mysql-connector-j-8.0.32.jar

RUN rm -rf /usr/local/tomcat/webapps/* && \
    chgrp -R 0 /usr/local/tomcat/ && \
    chmod -R g=u /usr/local/tomcat/

USER 1001
COPY score-http-3.0.0.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]
