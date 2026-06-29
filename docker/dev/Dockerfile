# Code-workspace image for an isolated dev lane.
# JDK 21 (matches the score-http Tomcat runtime) + Node 22 (matches score-web engines)
# + git + a mariadb client. The repo is cloned INTO a volume at runtime by entrypoint.sh,
# so the image itself carries no source — only the toolchain.
FROM eclipse-temurin:21-jdk

ENV DEBIAN_FRONTEND=noninteractive \
    NG_CLI_ANALYTICS=false

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      ca-certificates curl git procps mariadb-client \
 && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
 && apt-get install -y --no-install-recommends nodejs \
 && npm install -g npm@latest \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

WORKDIR /workspace
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["sleep", "infinity"]
