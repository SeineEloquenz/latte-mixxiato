FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/latte-mixxiato.coordinator-*.jar coordinator.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "./coordinator.jar"]
