FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/expensetracker-0.0.1-SNAPSHOT.jar expensetracker-v1.0.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "expensetracker-v1.0.jar"]
