FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/latte-mixxiato.gateway-*.jar gateway.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "./gateway.jar"]
