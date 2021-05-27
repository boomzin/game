FROM mysql

RUN apt-get update \
    && apt-get install -y git \
    && git clone https://github.com/boomzin/game.git \
    && cp /game/init.sql /docker-entrypoint-initdb.d/ \
    && cp -f /game/my.cnf /etc/mysql/my.cnf \
    && rm -rf /var/lib/apt/lists/*

ENV MYSQL_DATABASE=dev
ENV MYSQL_ROOT_PASSWORD=root

EXPOSE 3306
