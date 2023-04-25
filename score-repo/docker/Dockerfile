FROM --platform=linux/amd64 mysql:5.7.41

LABEL maintainer="Hakju Oh <hakju.oh@nist.gov>"

ENV MYSQL_DATABASE=oagi
ENV MYSQL_USER=oagi
ENV MYSQL_PASSWORD=oagi
ENV MYSQL_ROOT_PASSWORD=oagi

RUN yum -y update
RUN yum -y upgrade

VOLUME /var/lib/mysql

ADD oagis.sql /docker-entrypoint-initdb.d/oagis.sql

USER mysql
EXPOSE 3306
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["mysqld"]
