FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.4

ENV APP sscs-cor-backend.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 56

COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=100s --timeout=100s --retries=10 CMD http_proxy="" wget -q http://localhost:8090/health || exit 1

EXPOSE 8090
