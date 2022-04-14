### Build Stage ###
FROM maven:3-openjdk-17-slim AS build
WORKDIR /opt/lorva/
ADD . .
RUN cp -f src/main/resources/application.properties.sample src/main/resources/application.properties
RUN mvn package -s settings.xml

### Run Stage ###
FROM openjdk:17-slim
WORKDIR /opt/lorva/
COPY --from=build /opt/lorva/target/lorva.jar .

EXPOSE 8080
ENTRYPOINT ["java","-jar","lorva.jar"]