FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /app
COPY src src
COPY pom.xml .
RUN mvn clean package

#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /app/target/mqttKafkaBridge-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar
EXPOSE 5959
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]
