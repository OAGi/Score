FROM --platform=linux/amd64 nginxinc/nginx-unprivileged:1.27.5-alpine

LABEL maintainer="Hakju Oh <hakju.oh@nist.gov>"

USER 0
RUN apk update
RUN apk upgrade

RUN ["rm", "-f", "/etc/nginx/conf.d/default.conf"]
RUN ["rm", "-rf", "/usr/share/nginx/html"]

COPY score-web.conf.template /etc/nginx/conf.d/score-web.conf

ADD score-web/browser /usr/share/nginx/score-web
ADD docs /usr/share/nginx/docs

COPY score-web-entrypoint.sh /docker-entrypoint.d/score-web-entrypoint.sh

USER 1001
EXPOSE 4200

CMD ["nginx", "-g", "daemon off;"]
