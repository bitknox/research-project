FROM bitnami/spark:3.3.1

USER root

RUN apt-get -y update --fix-missing && \
    apt-get -y install wget

ENV BEAST_VERSION 0.10.0-RC2

COPY ./beast/target/beast-${BEAST_VERSION}-bin.tar.gz .

RUN tar -f beast-${BEAST_VERSION}-bin.tar.gz -C $SPARK_HOME -xvz
COPY ./config/spark-defaults.conf /opt/bitnami/spark/conf/spark-defaults.conf

EXPOSE 8890