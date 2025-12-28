FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/kafka-deterministic-decisions-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]