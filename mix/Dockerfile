FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
WORKDIR /opt/app

COPY ./target/latte-mixxiato.mix-*.jar mix.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "./mix.jar", "8888"]
