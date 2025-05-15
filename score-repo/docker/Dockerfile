FROM --platform=linux/amd64 mariadb:11.0.6

LABEL maintainer="Hakju Oh <hakju.oh@nist.gov>"

ENV MYSQL_DATABASE=oagi
ENV MYSQL_USER=oagi
ENV MYSQL_PASSWORD=oagi
ENV MYSQL_ROOT_PASSWORD=oagi

RUN apt-get -y update
RUN apt-get -y upgrade

VOLUME /var/lib/mysql

ADD oagis.sql /docker-entrypoint-initdb.d/oagis.sql

USER mysql
EXPOSE 3306
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["mariadbd", "--plugin-load-add=type_mysql_json"]
