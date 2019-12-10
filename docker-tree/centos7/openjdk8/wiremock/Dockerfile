# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


ENV WIREMOCK_VERSION 2.23.2
ENV GOSU_VERSION 1.11

ARG GOSU_URL=https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-amd64
ARG WIREMOCK_JAR_URL=https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-jre8-standalone/$WIREMOCK_VERSION/wiremock-jre8-standalone-$WIREMOCK_VERSION.jar

RUN setproxy && \
    curl -L ${GOSU_URL} -o /usr/local/bin/gosu && \
    unsetproxy && \
    chmod +x /usr/local/bin/gosu && \
    gosu nobody true

RUN mkdir -p /var/wiremock/lib/ && \
    setproxy && \
    curl -L ${WIREMOCK_JAR_URL} -o wiremock-standalone.jar && \
    unsetproxy && \
    mv wiremock-standalone.jar /var/wiremock/lib

WORKDIR /home/wiremock

COPY src/docker-entrypoint.sh /

EXPOSE 8080 8443

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["java", "-cp", "/var/wiremock/lib/*:/var/wiremock/extensions/*","com.github.tomakehurst.wiremock.standalone.WireMockServerRunner"]
