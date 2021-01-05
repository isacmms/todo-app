FROM openjdk:11

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/todo-app.jar

ENTRYPOINT ["java", "-jar", "todo-app.jar"]