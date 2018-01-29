FROM sath89/oracle-xe-11g

RUN mkdir /docker-entrypoint-initdb.d
ADD oagi.dmp /docker-entrypoint-initdb.d
RUN chmod 644 /docker-entrypoint-initdb.d/oagi.dmp

ADD entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]
CMD [""]
