FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.4

COPY build/bootScripts/ /opt/app/bin/

COPY build/libs/sscs-cor-backend.jar /opt/app/lib/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8090/health

EXPOSE 8090

ENTRYPOINT ["/opt/app/bin/"]
