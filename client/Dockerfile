FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/latte-mixxiato.client-*.jar client.jar

ENTRYPOINT ["java", "-jar", "./client.jar"]
