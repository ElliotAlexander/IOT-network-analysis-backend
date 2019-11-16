FROM zenika/alpine-maven

WORKDIR "/app"
RUN apk update && apk add libpcap-dev
ENTRYPOINT mvn clean package
