FROM maven:3-jdk-8

RUN apt-get update && apt-get install -y build-essential

ENV LOG_LEVEL INFO
ENV LOG_FILE /logs/jabot-app.log

ADD . /jabot
WORKDIR /jabot

RUN make install

EXPOSE 4000
VOLUME ["/logs"]

CMD ["make", "run"]
