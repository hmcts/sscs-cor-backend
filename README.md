# 

[![Build Status](https://travis-ci.org/hmcts/.svg?branch=master)](https://travis-ci.org/hmcts/)

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8090` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8090/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-userId>
```

There is no need to remove postgres and java or similar core images.

## Debugging in Intellij configurations

VM options: -Dhttp.proxyHost=proxyout.reform.hmcts.net -Dhttp.proxyPort=8080  -Dhttps.proxyHost=proxyout.reform.hmcts.net -Dhttps.proxyPort=8080

Environment Variables: CREATE_CCD_ENDPOINT=true;COH_URL=http://coh-cor-aat.service.core-compute-aat.internal;IDAM_SSCS_SYSTEMUPDATE_USER=sscs-system-update@hmcts.net;IDAM_SSCS_SYSTEMUPDATE_PASSWORD=Bb********;IDAM_OAUTH2_CLIENT_ID=sscs;IDAM_OAUTH2_CLIENT_SECRET=3$*******;IDAM_S2S_AUTH=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal;IDAM_S2S_AUTH_TOTP_SECRET=44*********;S2S_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal;CORE_CASE_DATA_URL=http://ccd-data-store-api-aat.service.core-compute-aat.internal;IDAM_API_URL=http://idam-api.aat.platform.hmcts.net;IDAM_URL=http://idam-api.aat.platform.hmcts.net;IDAM_OAUTH2_REDIRECT_URL=https://evidence-sharing-preprod.sscs.reform.hmcts.net;PDF_API_URL=http://cmc-pdf-service-aat.service.core-compute-aat.internal;PDF_SERVICE_ACCESS_KEY=ZD********************;EMAIL_SERVER_HOST=mta.reform.hmcts.net;EMAIL_SERVER_PORT=25 


## Functional tests

Can be run in your ide or with

```bash
  ./gradlew functional
``` 

These tests will always run against COH on aat but you can set the url for sscs-backend-cor with the TEST_URL
environment variable.

If you are running this test locally you will need to set the USE_COH_PROXY environment variable to true.

If you are running this test locally and the sscs-backend-cor is remote i.e. in preview or AAT environment then you need
to set the USE_BACKEND_PROXY environment variable to true.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
