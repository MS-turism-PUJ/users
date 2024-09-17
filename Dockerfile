FROM openjdk:17-jdk-alpine
COPY target/users-*.jar java-app.jar
ENTRYPOINT [ "java", "-jar", "java-app.jar"]
