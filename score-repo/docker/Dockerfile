FROM mariadb:11.8.9

LABEL maintainer="Hakju Oh <hakju.oh@nist.gov>"

ENV MYSQL_DATABASE=oagi
ENV MYSQL_USER=oagi
ENV MYSQL_PASSWORD=oagi
ENV MYSQL_ROOT_PASSWORD=oagi

RUN apt-get -y update
RUN apt-get -y upgrade

VOLUME /var/lib/mysql

ADD oagis.sql /docker-entrypoint-initdb.d/01-oagis.sql
# Runs as root before oagis.sql (files execute in name order, after MYSQL_USER is created); lets oagi manage every oagi_* database.
RUN printf '%s\n' 'GRANT ALL PRIVILEGES ON `oagi\_%`.* TO '"'"'oagi'"'"'@'"'"'%'"'"';' 'FLUSH PRIVILEGES;' \
    > /docker-entrypoint-initdb.d/00-grant-oagi.sql

USER mysql
EXPOSE 3306
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["mariadbd", "--plugin-load-add=type_mysql_json"]
