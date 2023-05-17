FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/mix-coordinator-*.jar mix-coordinator.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "./mix-coordinator.jar"]
