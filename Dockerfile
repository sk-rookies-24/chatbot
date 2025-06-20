FROM openjdk:11-jdk-slim

COPY build/libs/chatbot-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]