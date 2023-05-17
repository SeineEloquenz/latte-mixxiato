FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/mix-client-*.jar mix-client.jar

ENTRYPOINT ["java", "-jar", "./mix-client.jar"]
