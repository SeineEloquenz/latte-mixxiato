FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/mix-node-*.jar mix-node.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "./mix-node.jar", "8888"]
