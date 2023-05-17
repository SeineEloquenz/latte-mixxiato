FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/mix-dead-drop-*.jar mix-dead-drop.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "./mix-dead-drop.jar", "8888"]
