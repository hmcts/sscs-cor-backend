ARG APP_INSIGHTS_AGENT_VERSION=2.3.1
FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.0

ENV APP sscs-cor-backend.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 56

COPY lib/applicationinsights-agent-2.3.1.jar /opt/app/
COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=100s --timeout=100s --retries=10 CMD http_proxy="" wget -q http://localhost:8090/health || exit 1

EXPOSE 8090

CMD ["sscs-cor-backend.jar"]
