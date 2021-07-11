FROM openjdk:8
ARG SBT_VERSION="1.4.7"

RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && \
    apt-get install sbt


WORKDIR /mydir
COPY . .

ENV PORT=$PORT
ENV DB_NAME=$DB_NAME
EXPOSE $PORT

RUN apt-get install -y postgresql
RUN sbt compile

CMD sh wait-for-postgres.sh $DATABASE_URL sbt run