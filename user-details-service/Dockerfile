FROM openjdk:17-jdk-slim

ARG JAR_FILE=target/user-details-service-0.0.1-SNAPSHOT.jar

WORKDIR /app

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]