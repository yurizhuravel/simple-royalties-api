FROM eclipse-temurin:21-jre-jammy AS base

# Install Java and set the JAVA_HOME variable /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk

COPY --from=eclipse-temurin:21 $JAVA_HOME $JAVA_HOME

ENV PATH="${JAVA_HOME}/bin:${PATH}"

ENV SBT_VERSION=1.11.2

# Install curl
RUN \
  apt-get update && \
  apt-get -y install curl 

# Install scala and sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get -y install sbt

EXPOSE 8080

# Production
FROM base AS production

WORKDIR /app

COPY . .

ENTRYPOINT [ "sbt", "run" ]
