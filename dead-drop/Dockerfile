FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/latte-mixxiato.dead-drop-*.jar dead-drop.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "./dead-drop.jar"]
