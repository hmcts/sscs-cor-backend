FROM openjdk:8-alpine

ENV APP sscs-cor-backend.jar
ENV APPLICATION_TOTAL_MEMORY 2048M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 100

COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=100s --timeout=100s --retries=10 CMD http_proxy="" wget -q http://localhost:8090/health || exit 1

EXPOSE 8090
